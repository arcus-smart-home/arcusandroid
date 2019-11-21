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
package arcus.app.device.pairing.post;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.waterheater.WaterHeaterController;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.WaterHeater;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.WaterHeaterPickerPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.post.controller.AddToFavoritesFragmentController;

import java.util.TreeMap;



public class WaterHeaterTemperatureFragment extends SequencedFragment implements AddToFavoritesFragmentController.Callbacks, DeviceController.Callback {

    private final static int MIN_SETPOINT_F = 60;
    private final static int MIN_SETPOINT_C = 16;
    private final static int MAX_SETPOINT_C = 100;
    private final static int DEFAULT_MAX_SETPOINT_F = 120;
    private final static int DEFAULT_SETPOINT_F = 120;

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private WaterHeaterController controller;
    private Version1TextView temperatureTV;
    private Version1Button nextButton;
    private WaterHeater model;

    @NonNull
    public static WaterHeaterTemperatureFragment newInstance(String deviceAddress) {
        WaterHeaterTemperatureFragment fragment = new WaterHeaterTemperatureFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        View temperature = parentGroup.findViewById(R.id.temperature_zone);
        temperature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TreeMap<Integer,Integer> treeMap = new TreeMap<>();

                int tempIndex = 0;
                for (int tempF = MIN_SETPOINT_F; tempF <= getMaxSetpointF(); tempF++) {
                    treeMap.put(tempIndex++, tempF);
                }

                WaterHeaterPickerPopup percentPicker = WaterHeaterPickerPopup.newInstance(getSetpointF(), treeMap);

                percentPicker.setOnValueChangedListener(new WaterHeaterPickerPopup.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int value) {
                        controller.updateCurrentSetPoint(value);
                        temperatureTV.setText(getSetpointF() + "º");
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(percentPicker, percentPicker.getClass().getSimpleName(), true);
            }
        });
        temperatureTV = (Version1TextView) parentGroup.findViewById(R.id.temperature);
        nextButton = (Version1Button) parentGroup.findViewById(R.id.next_btn);
        return parentGroup;
    }

    @Override
    public void onResume () {
        super.onResume();
        final String deviceAddress = getArguments().getString(DEVICE_ADDRESS);
        String deviceId = CorneaUtils.getIdFromAddress(deviceAddress);
        controller = WaterHeaterController.newController(deviceId, this);
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
        model = (WaterHeater) SessionModelManager.instance().getDeviceWithId(deviceId, false);
        temperatureTV.setText(getSetpointF() + "º");
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
    }

    @Override
    public void onPause () {
        super.onPause();
    }

    @Override
    public String getTitle() {
        return getString(R.string.water_heater_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_water_heater_temperature_fragment;
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
    }

    @Override
    public void onFailure(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void show(Object model) {

    }

    @Override
    public void onError(ErrorModel error) {

    }

    private int getSetpointF() {
        return (model.getSetpoint() == null || model.getSetpoint() == 0 || model.getSetpoint() >= MAX_SETPOINT_C)
                ? DEFAULT_SETPOINT_F : TemperatureUtils.roundCelsiusToFahrenheit(model.getSetpoint());
    }

    private int getMaxSetpointF() {
        return (model.getMaxsetpoint() == null || model.getMaxsetpoint() < MIN_SETPOINT_C || model.getMaxsetpoint() >= MAX_SETPOINT_C)
                ? DEFAULT_MAX_SETPOINT_F : TemperatureUtils.roundCelsiusToFahrenheit(model.getMaxsetpoint());
    }
}
