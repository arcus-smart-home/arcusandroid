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
package arcus.app.subsystems.climate;

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
import arcus.cornea.subsystem.climate.ScheduleListController;
import arcus.cornea.subsystem.climate.ScheduleStateController;
import arcus.cornea.subsystem.climate.model.DeviceControlType;
import arcus.cornea.subsystem.climate.model.ScheduleStateModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.LeftScheduleTextCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.SchedulingError;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.schedule.GenericMaterialListView;
import arcus.app.common.schedule.RecyclerItemGenericListener;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.subsystems.climate.schedule.FanWeeklyScheduleFragment;
import arcus.app.subsystems.climate.schedule.SpaceHeaterWeeklyScheduleFragment;
import arcus.app.subsystems.climate.schedule.VentWeeklyScheduleFragment;

import java.util.ArrayList;
import java.util.List;


public class ScheduleFragment extends BaseFragment implements ScheduleListController.Callback, AbstractCardController.Callback {

    private GenericMaterialListView materialListView;
    private ListenerRegistration mListener;
    private ScheduleListController mController;

    private List<ScheduleStateModel> mDevices;
    private View devicesRegion;

    private View noDeviceContainer;
    private TextView noDeviceTitle;
    private TextView noDeviceDescription;
    private View shopBtn;

    @NonNull
    public static ScheduleFragment newInstance() {
        return new ScheduleFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        materialListView = (GenericMaterialListView) view.findViewById(R.id.material_listview);
        devicesRegion = view.findViewById(R.id.devices_region);
        noDeviceContainer = view.findViewById(R.id.climate_schedule_no_device_container);
        noDeviceTitle = (TextView) view.findViewById(R.id.climate_schedule_no_device_title);
        noDeviceDescription = (TextView) view.findViewById(R.id.climate_schedule_no_device_des);
        shopBtn = view.findViewById(R.id.climate_schedule_no_device_shop_btn);

        materialListView.addOnItemTouchListener(new RecyclerItemGenericListener.OnItemClickListener() {
            @Override
            public void onItemClick(CardItemView cardItemView, int position, MotionEvent event) {
                ScheduleStateModel model = mDevices.get(position);

                if (model.getType() == DeviceControlType.NESTTHERMOSTAT) {
                    showNoScheduling(model.getType());
                    return;
                }


                ImageView radButton = (ImageView) cardItemView.findViewById(R.id.rad_button);
                if (event.getX() < radButton.getRight()) {
                    //There is a schedule
                    if (model.isSchedOn()) {
                        if (model.isChecked()) {
                            ScheduleStateController.instance().setScheduleEnabled(model, false);
                            model.setChecked(false);
                        } else {
                            ScheduleStateController.instance().setScheduleEnabled(model, true);
                            model.setChecked(true);
                        }
                        updateCard(null);
                    } else {
                        // Says Water_Schedule_No_events but the text is generic..
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
                        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                    }
                    return;
                }

                if (model.getType() == DeviceControlType.HONEYWELLTCC) {
                    showNoScheduling(DeviceControlType.HONEYWELLTCC);
                } else {
                    switch (model.getType()) {
                        case THERMOSTAT:
                            BackstackManager.getInstance().navigateToFragment(ScheduleViewFragment.newInstance(model.getName(), CorneaUtils.getDeviceAddress(model.getDeviceId())), true);
                            break;
                        case FAN:
                            BackstackManager.getInstance().navigateToFragment(FanWeeklyScheduleFragment.newInstance(CorneaUtils.getDeviceAddress(model.getDeviceId()), model.getName()), true);
                            break;
                        case VENT:
                            BackstackManager.getInstance().navigateToFragment(VentWeeklyScheduleFragment.newInstance(CorneaUtils.getDeviceAddress(model.getDeviceId()), model.getName()), true);
                            break;
                        case SPACEHEATER:
                            BackstackManager.getInstance().navigateToFragment(SpaceHeaterWeeklyScheduleFragment.newInstance(CorneaUtils.getDeviceAddress(model.getDeviceId()), model.getName()), true);
                            break;
                    }
                }
            }

            @Override
            public void onItemLongClick(CardItemView cardItemView, int i) {
                // No-Op
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mController == null) {
            mController = ScheduleListController.instance();
        }

        mListener = mController.selectAll(RegistrationContext.getInstance().getPlaceModel().getId(), this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
    }

    @Override
    public void updateCard(Card c) {
        materialListView.clear();

        for (ScheduleStateModel model : mDevices) {
            LeftScheduleTextCard card = new LeftScheduleTextCard(getActivity());
            card.showChevron();
            card.showDivider();
            card.setTitle(model.getName());
            card.setDeviceID(model.getDeviceId());
            card.setRadioChecked(model.isChecked());
            card.setSchedIconShown(model.isSchedOn());
            materialListView.add(card);
        }
    }


    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_climate_schedule;
    }

    @Override
    public void showNoSchedulableDevices() {
        logger.debug("No Schedulable devices");
        noDeviceContainer.setVisibility(View.VISIBLE);
        devicesRegion.setVisibility(View.GONE);
        shopBtn.setVisibility(View.VISIBLE);

        noDeviceTitle.setText(getString(R.string.climate_thermostat_no_device_automate_title));
        noDeviceDescription.setText(getString(R.string.climate_thermostat_no_device_des));

        shopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchShopNow();
            }
        });
    }

    @Override
    public void showSchedules(List<ScheduleStateModel> models) {
        logger.debug("Got list of climate device: {}", models);
        noDeviceContainer.setVisibility(View.GONE);
        devicesRegion.setVisibility(View.VISIBLE);

        mDevices = new ArrayList<>();
        mDevices.addAll(models);

        updateCard(null);
    }

    private void showNoScheduling(DeviceControlType type) {
        if (type == DeviceControlType.NESTTHERMOSTAT) {
            ErrorManager.in(getActivity()).show(SchedulingError.CANT_SCHEDULE_NEST);
        } else {
            ErrorManager.in(getActivity()).show(SchedulingError.CANT_SCHEDULE_TCC);
        }
    }
}
