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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dexafree.materialList.model.Card;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.climate.ControlDeviceController;
import arcus.cornea.subsystem.climate.model.DeviceControlModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.climate.controllers.SpaceHeaterCardController;
import arcus.app.subsystems.climate.controllers.FanCardController;
import arcus.app.subsystems.climate.controllers.ThermostatCardController;
import arcus.app.subsystems.climate.controllers.VentCardController;

import java.util.ArrayList;
import java.util.List;



public class ClimateFragment extends BaseFragment implements ControlDeviceController.Callback, AbstractCardController.Callback {

    private MaterialListView mListView;

    private LinearLayout climateDevicesNoDeviceContainer;
    private Version1TextView shopButton;


    private ControlDeviceController mController;
    private ListenerRegistration mListener;

    private List<AbstractCardController> mCardControllers;

    private ArrayList<DeviceControlCard> mDeviceCardList;

    @NonNull
    public static ClimateFragment newInstance() {
        ClimateFragment fragment = new ClimateFragment();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mController == null) {
            mController = ControlDeviceController.instance();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        climateDevicesNoDeviceContainer = (LinearLayout) view.findViewById(R.id.climate_devices_no_device_container);
        shopButton = (Version1TextView) view.findViewById(R.id.climate_devices_no_device_shop_btn);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchShopNow();
            }
        });


        mListView = (MaterialListView) view.findViewById(R.id.material_listview);

        mCardControllers = new ArrayList<>();

        return view;
    }

    private void toggleListOn(boolean bListOn) {
        if (bListOn) {
            climateDevicesNoDeviceContainer.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        } else {
            climateDevicesNoDeviceContainer.setVisibility(View.VISIBLE);
            mListView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mListener = mController.setCallback(this);

        getActivity().setTitle(getTitle());

        if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.CLIMATE_WALKTHROUGH_DONT_SHOW_AGAIN, false)) {
            WalkthroughBaseFragment climate = WalkthroughBaseFragment.newInstance(WalkthroughType.CLIMATE);
            BackstackManager.getInstance().navigateToFloatingFragment(climate, climate.getClass().getName(), true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
        for (AbstractCardController controller : mCardControllers) {
            controller.removeCallback();
        }
    }

    private void populateCards(@NonNull List<DeviceControlModel> controls) {

        if (controls != null && controls.size() > 0) {
            toggleListOn(true);
            mListView.clear();
            mCardControllers = new ArrayList<>();

            if (mDeviceCardList == null || mDeviceCardList.size() <= 0)
                mDeviceCardList = new ArrayList<>();

            for (DeviceControlModel model : controls) {
                AbstractCardController controller = null;

                // Need a device id to continue
                if (model.getDeviceId() == null) continue;

                switch (model.getType()) {
                    case THERMOSTAT:
                        controller = new ThermostatCardController(model.getDeviceId(), getActivity());
                        break;
                    case FAN:
                        controller = new FanCardController(model.getDeviceId(), getActivity());
                        break;
                    case VENT:
                        controller = new VentCardController(model.getDeviceId(), getActivity());
                        break;
                    case SPACEHEATER:
                        controller = new SpaceHeaterCardController(model.getDeviceId(), getActivity());
                        break;
                }

                if (controller != null) {
                    controller.setCallback(this);
                    mCardControllers.add(controller);
                    mListView.add(controller.getCard());
                }
            }
        } else {
            toggleListOn(false);
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.climate);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_climate;
    }

    /*
     * Climate Callback
     */

    @Override
    public void showDeviceControls(@NonNull List<DeviceControlModel> controls) {
        logger.debug("Got climate device control models: {}", controls);
        populateCards(controls);
    }

    @Override
    public void updateCard(Card card) {

    }
}
