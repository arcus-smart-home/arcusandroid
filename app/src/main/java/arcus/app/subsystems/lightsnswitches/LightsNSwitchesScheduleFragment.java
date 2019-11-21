/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.app.subsystems.lightsnswitches;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.model.CardItemView;
import arcus.cornea.SessionController;
import arcus.cornea.subsystem.ScheduleGenericStateModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.LeftScheduleTextCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.GenericMultiPopup;
import arcus.app.common.schedule.GenericMaterialListView;
import arcus.app.subsystems.lightsnswitches.controllers.ScheduleLightsListController;
import arcus.app.subsystems.lightsnswitches.controllers.ScheduleLightsStateController;
import arcus.app.common.schedule.RecyclerItemGenericListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class LightsNSwitchesScheduleFragment extends BaseFragment implements
      ScheduleLightsListController.Callback, AbstractCardController.Callback, GenericMultiPopup.OnButtonClickedListener, IShowedFragment {

    private GenericMaterialListView materialWaterListView;
    private ListenerRegistration mListener;
    private ScheduleLightsListController mController;

    private List<ScheduleGenericStateModel> mDevices;
    private View devicesRegion;

    private View noDeviceContainer;
    private TextView noDeviceTitle;
    private TextView noDeviceDescription;
    private View shopBtn;
    private LightsNSwitchesParentFragment parentFragment;


    //todo G: you may need to add a subsystem here as param
    @NonNull
    public static LightsNSwitchesScheduleFragment newInstance() {
        return new LightsNSwitchesScheduleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        materialWaterListView = (GenericMaterialListView) view.findViewById(R.id.material_listview);
        devicesRegion = view.findViewById(R.id.devices_region);
        noDeviceContainer = view.findViewById(R.id.climate_schedule_no_device_container);
        noDeviceTitle = (TextView) view.findViewById(R.id.climate_schedule_no_device_title);
        noDeviceDescription = (TextView) view.findViewById(R.id.climate_schedule_no_device_des);
        shopBtn = view.findViewById(R.id.climate_schedule_no_device_shop_btn);
        parentFragment = (LightsNSwitchesParentFragment) BackstackManager.getInstance().getFragmentOnStack(LightsNSwitchesParentFragment.class);

        materialWaterListView.addOnItemTouchListener(new RecyclerItemGenericListener.OnItemClickListener() {

            @Override
            public void onItemClick(CardItemView cardItemView, int position, MotionEvent event) {

                ImageView radButton = (ImageView) cardItemView.findViewById(R.id.rad_button);
                ScheduleGenericStateModel mModel = mDevices.get(position);
                if (event.getX() < radButton.getRight()) {
                    ScheduleGenericStateModel model = mDevices.get(position);

                    //todo G: you will need to make this switch across various systems.
                    //There is a schedule
                    if (mModel.isSchedOn()) {
                        if (mModel.isChecked()) {
                            ScheduleLightsStateController.instance().setScheduleEnabled(model, false);
                            model.setChecked(false);
                        } else {
                            ScheduleLightsStateController.instance().setScheduleEnabled(model, true);
                            model.setChecked(true);
                        }
                        updateCard(null);
                    } else {

                        AlertPopup popup = AlertPopup.newInstance(getActivity().getString(R.string.water_schedule_no_events),
                                getActivity().getString(R.string.water_schedule_no_events_sub), null, null, new AlertPopup.AlertButtonCallback() {
                                    @Override
                                    public boolean topAlertButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public boolean bottomAlertButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public boolean errorButtonClicked() {
                                        return false;
                                    }

                                    @Override
                                    public void close() {
                                        BackstackManager.getInstance().navigateBack();
                                        getActivity().invalidateOptionsMenu();
                                    }
                                });
                        popup.setCloseButtonVisible(true);
                        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);

                    }
                    return;
                }

                BackstackManager.getInstance().navigateToFragment(LightsNSwitchesWeeklyScheduleFragment.newInstance("DRIV:dev:"+mModel.getDeviceId(), mModel.getName()), true);

            }

            @Override
            public void onItemLongClick(CardItemView cardItemView, int i) {
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mController == null) {
            mController = ScheduleLightsListController.instance();
        }
        if(SessionController.instance().getActivePlace() != null && SessionController.instance() != null) {
            mListener = mController.selectAll(SessionController.instance().getActivePlace(), this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mListener != null) {
            mListener.remove();
        }
    }


    //this takes any types that you don't want.
    private List<ScheduleGenericStateModel> filterUnwantedCards(List<ScheduleGenericStateModel> list, String... unwantedTypes) {
       //accumulator
        List<ScheduleGenericStateModel> retList = new ArrayList<>();
        outer: for (ScheduleGenericStateModel item : list) {
            for (String uwt : unwantedTypes) {
                if(item.getType().equalsIgnoreCase(uwt)) {
                    continue outer;
                }
            }
            retList.add(item);
        }
        return retList;
    }

    @Override
    public void updateCard(Card c) {
        materialWaterListView.clear();

        for (ScheduleGenericStateModel model : mDevices) {

            LeftScheduleTextCard card = new LeftScheduleTextCard(getActivity());
            card.showChevron();
            card.showDivider();
            card.setTitle(model.getName());
            card.setDeviceID(model.getDeviceId());
            card.setRadioChecked(model.isChecked());
            card.setSchedIconShown(model.isSchedOn());
            materialWaterListView.add(card);
        }
    }


    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_generic_schedule;
    }



    @Override
    public void onButtonClicked(String buttonValue, ScheduleGenericStateModel model) {


    }

   //these two methods satisfy the ScheduleWaterListController.Callback
    @Override
    public void showNoSchedulableDevices() {
        logger.debug("No Schedulable devices");
        noDeviceContainer.setVisibility(View.VISIBLE);
        devicesRegion.setVisibility(View.GONE);
        shopBtn.setVisibility(View.VISIBLE);

        //todo G: these will need to be set in each sub card
        noDeviceTitle.setText(getString(R.string.climate_thermostat_no_device_title));
        noDeviceDescription.setText(getString(R.string.water_schedule_no_device_des));

        shopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo: go to website?
            }
        });
    }

    @Override
    public void showSchedules(List<ScheduleGenericStateModel> models) {
        //  setController();
        logger.debug("Got list of device: {}", models);
        noDeviceContainer.setVisibility(View.GONE);
        devicesRegion.setVisibility(View.VISIBLE);

        mDevices = new ArrayList<>();
        mDevices.addAll(models);
        //no need to filter devices for lights and switches.
        // mDevices = filterUnwantedCards(mDevices, DeviceControlType.WATER_SOFTENER.toString());

        Collections.sort(mDevices);
        updateCard(null);
    }

    @Override public void onShowedFragment() {
        if (parentFragment != null) {
            parentFragment.setEditMenuVisible(false);
            parentFragment.setEditModeChangeListener(null);
        }
    }
}
