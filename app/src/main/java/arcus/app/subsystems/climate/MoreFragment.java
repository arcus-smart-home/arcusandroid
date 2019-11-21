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
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.MoreSettingsController;
import arcus.cornea.subsystem.climate.model.DeviceSettingsModel;
import arcus.cornea.subsystem.climate.model.MoreSettingsModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.PopupCard;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.DevicePickerPopup;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.popups.ArcusFloatingFragment;

import java.util.ArrayList;
import java.util.List;


public class MoreFragment extends BaseFragment implements MoreSettingsController.Callback {

    private MoreSettingsController mSettingsController;
    private ListenerRegistration mListener;

    private MaterialListView mListView;

    private MoreSettingsModel mMoreSettingModel;


    @NonNull
    public static MoreFragment newInstance(){
        MoreFragment fragment = new MoreFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        mListView = (MaterialListView) view.findViewById(R.id.material_listview);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSettingsController == null) {
            mSettingsController = MoreSettingsController.instance();
        }

        mListener = mSettingsController.setCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
    }

    @Override
    public String getTitle() {
        return getString(R.string.card_climate_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_climate_more;
    }

    private void populateCards() {
        mListView.clear();

        /*List<ThermostatSettingsModel> thermostatSettings = mMoreSettingModel.getThermostats();

        if (thermostatSettings != null && thermostatSettings.size() > 0) {

            LeftTextCard topCard = new LeftTextCard(getActivity());
            topCard.setTitle(getString(R.string.climate_more_schedule_on_off));
            topCard.setDescription(getString(R.string.climate_more_schedule_on_off_text));
            topCard.showDivider();
            mListView.add(topCard);

            for (ThermostatSettingsModel model : mMoreSettingModel.getThermostats()) {
                final String modelID = model.getDeviceId();
                BinarySwitchCard card = new BinarySwitchCard(getActivity());
                card.setTitle(model.getName());
                card.showDivider();
                card.setToggleChecked(model.isScheduled());
                card.setClickListener(new BinarySwitchCard.ClickListener() {
                    @Override
                    public void onToggleChanged(@NonNull ToggleButton button) {  // Debounce?
                        mSettingsController.setThermostatEnabled(modelID, button.isChecked());
                    }
                });
                mListView.add(card);
            }
        }*/

        PopupCard temperatureCard = new PopupCard(getActivity());
        temperatureCard.setTitle(getString(R.string.climate_more_temperature_title));
        String deviceName = null;
        if (mMoreSettingModel.getDashboardTemperature() != null) {
            deviceName = mMoreSettingModel.getDashboardTemperature().getName();
        }
        temperatureCard.setRightText(deviceName);
        temperatureCard.setDescription(getString(R.string.climate_more_temperature_text));
        temperatureCard.showDivider();
        temperatureCard.setClickListener(new PopupCard.ClickListener() {
            @Override
            public void cardClicked(View view) {
                mSettingsController.selectDashboardTemperatureDevice();
            }
        });
        mListView.add(temperatureCard);

        PopupCard humidityCard = new PopupCard(getActivity());
        humidityCard.setTitle(getString(R.string.climate_more_humidity_title));
        deviceName = null;
        if (mMoreSettingModel.getDashboardHumidity() != null) {
            deviceName = mMoreSettingModel.getDashboardHumidity().getName();
        }
        humidityCard.setRightText(deviceName);
        humidityCard.setDescription(getString(R.string.climate_more_humidity_text));
        humidityCard.showDivider();
        humidityCard.setClickListener(new PopupCard.ClickListener() {
            @Override
            public void cardClicked(View view) {
                mSettingsController.selectDashboardHumidityDevice();
            }
        });
        mListView.add(humidityCard);
    }

    @Override
    public void showSettings(MoreSettingsModel model) {
        mMoreSettingModel = model;
        populateCards();
    }

    @Override
    public void promptSelectTemperatureDevice(@NonNull List<DeviceSettingsModel> devices) {
        List<String> modelIDs = getModelIDs(devices);
        ArcusFloatingFragment popup;

        if (modelIDs.isEmpty()) {
            popup = InfoTextPopup.newInstance(R.string.humidity_devices_pared_error_desc, R.string.humidity_devices_pared_error_title);
        }
        else {
            popup = DevicePickerPopup.newInstance(mMoreSettingModel.getDashboardTemperature().getDeviceId(), modelIDs);
            ((DevicePickerPopup)popup).setCallback(new DevicePickerPopup.Callback() {
                @Override
                public void selectedItem(String id) {
                    mSettingsController.setDashboardTemperature(id);
                    BackstackManager.getInstance().navigateBack();
                }
            });
        }
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void promptSelectHumidityDevice(@NonNull List<DeviceSettingsModel> devices) {
        List<String> modelIDs = getModelIDs(devices);
        ArcusFloatingFragment popup;

        if (modelIDs.isEmpty()) {
            popup = InfoTextPopup.newInstance(R.string.climate_more_device_required_text, R.string.error_generic_title);
        }
        else {
            popup = DevicePickerPopup.newInstance(mMoreSettingModel.getDashboardHumidity().getDeviceId(), modelIDs);
            ((DevicePickerPopup)popup).setCallback(new DevicePickerPopup.Callback() {
                @Override
                public void selectedItem(String id) {
                    mSettingsController.setDashboardHumidity(id);
                    BackstackManager.getInstance().navigateBack();
                }
            });
        }
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void promptError(ErrorModel error) {
        ErrorManager.in(getActivity()).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    @NonNull
    private List<String> getModelIDs(@NonNull List<DeviceSettingsModel> source) {
        List<String> modelIDs = new ArrayList<>();

        for (DeviceSettingsModel model : source) {
            modelIDs.add(model.getDeviceId());
        }

        return modelIDs;
    }
}
