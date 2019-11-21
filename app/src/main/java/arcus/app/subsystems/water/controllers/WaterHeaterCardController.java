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
package arcus.app.subsystems.water.controllers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableString;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.waterheater.WaterHeaterController;
import arcus.cornea.device.waterheater.WaterHeaterMode;
import arcus.cornea.device.waterheater.WaterHeaterProxyModel;
import arcus.cornea.error.ErrorModel;
import com.iris.client.capability.AOSmithWaterHeaterController;
import com.iris.client.capability.WaterHeater;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.models.DeviceMode;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.DeviceModePopup;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.details.DeviceDetailParentFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class WaterHeaterCardController extends DeviceCardController implements DeviceController.Callback<WaterHeaterProxyModel>, DeviceControlCard.OnClickListener, NumberPickerPopup.OnValueChangedListener, MultiButtonPopup.OnButtonClickedListener {

    private Logger logger = LoggerFactory.getLogger(WaterHeaterCardController.class);

    @Nullable
    private WaterHeaterController mController;
    private WaterHeaterProxyModel mModel;
    private WaterHeaterMode mMode;
    public WaterHeaterCardController(String deviceId, Context context) {
        super(deviceId, context);

        // Construct a Water Heater Card
        DeviceControlCard deviceCard = new DeviceControlCard(context);

        deviceCard.setLeftImageResource(R.drawable.button_minus);
        deviceCard.setRightImageResource(R.drawable.button_plus);
        deviceCard.setTopImageResource(R.drawable.sidemenu_settings_whitecircle);
        deviceCard.setBottomImageResource(R.drawable.outline_rounded_button_style);
        deviceCard.setDeviceId(deviceId);
        deviceCard.setShouldGlow(false);
        deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);

        deviceCard.setDeviceId(deviceId);
        deviceCard.setCallback(this);

        setDeviceId(deviceId);

        setCurrentCard(deviceCard);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mController = WaterHeaterController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mController.clearCallback();
        mController = null;
    }

    private String getBottomTextForMode(@NonNull String mode) {
        SpannableString modeText = new SpannableString("");
        switch (mode) {
            case AOSmithWaterHeaterController.CONTROLMODE_ENERGY_SMART:
                modeText = StringUtils.getSuperscriptSpan(getContext().getString(R.string.energy_smart), getContext().getString(R.string.registered_symbol));
                break;
            case AOSmithWaterHeaterController.CONTROLMODE_STANDARD:
                modeText = new SpannableString(getContext().getString(R.string.standard));
                break;
            case AOSmithWaterHeaterController.CONTROLMODE_VACATION:
                modeText = new SpannableString(getContext().getString(R.string.vacation));
                break;
        }
        return modeText.toString();
    }

    /*
     * WaterHeater Callback
     */

    @Override
    public void show(@NonNull WaterHeaterProxyModel model) {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        mModel = model;

        // Populate card from model
        if (deviceCard != null) {
            deviceCard.setTitle(model.getName());
            deviceCard.setDeviceId(model.getDeviceId());
            deviceCard.setUseSpecifiedTopImage(true);
            if(model.getHotWaterLevel().equals(WaterHeater.HOTWATERLEVEL_HIGH)) {
                if(model.isHeatingState()) {
                    deviceCard.setTopImageResource(R.drawable.water_heater_available_heat);
                }
                else {
                    deviceCard.setTopImageResource(R.drawable.water_heater_available);
                }
            }
            else if(model.getHotWaterLevel().equals(WaterHeater.HOTWATERLEVEL_MEDIUM)) {
                if(model.isHeatingState()) {
                    deviceCard.setTopImageResource(R.drawable.water_heater_limited_heat);
                }
                else {
                    deviceCard.setTopImageResource(R.drawable.water_heater_limited);
                }
            }
            else if(model.getHotWaterLevel().equals(WaterHeater.HOTWATERLEVEL_LOW)) {
                if(model.isHeatingState()) {
                    deviceCard.setTopImageResource(R.drawable.water_heater_no_heat);
                }
                else {
                    deviceCard.setTopImageResource(R.drawable.water_heater_no);
                }
            }
            // Handle Offline Mode
            deviceCard.setOffline(!model.isOnline());
            if (!model.isOnline()) {
                deviceCard.setBottomImageText("OFF");
                deviceCard.setBottomButtonEnabled(false);
                return;
            }
            else {
                deviceCard.setBottomButtonEnabled(true);
            }

            deviceCard.setBottomImageResource(R.drawable.outline_rounded_button_style);

            // Build Sub String
            StringBuilder sb = new StringBuilder();

            if(model.getControlMode() != null) {
                deviceCard.setBottomImageText(model.getControlMode());
                sb.append(String.format("Set to %dº", (int)model.getSetPoint()));
                deviceCard.setLeftButtonEnabled(true);
                deviceCard.setRightButtonEnabled(true);

                deviceCard.setDescription(sb.toString());
                deviceCard.setBottomButtonEnabled(true);
                deviceCard.setBottomImageText(getBottomTextForMode(model.getControlMode()));
            }
            else {
                deviceCard.setLeftButtonEnabled(false);
                deviceCard.setRightButtonEnabled(false);

                deviceCard.setDescription("--");
                deviceCard.setBottomImageText("--");
                deviceCard.setBottomButtonEnabled(true);
            }
            deviceCard.setLeftImageResource(R.drawable.button_minus);
            deviceCard.setRightImageResource(R.drawable.button_plus);

            if(model.getSetPoint() <= model.getMinTemp()) {
                deviceCard.setLeftButtonEnabled(false);
            }
            else {
                deviceCard.setLeftButtonEnabled(true);
            }

            if(model.getSetPoint() >= model.getMaxTemp()) {
                deviceCard.setRightButtonEnabled(false);
            }
            else {
                deviceCard.setRightButtonEnabled(true);
            }
        }
    }

    @Override
    public void onError(ErrorModel error) {

    }

    /***
     * DeviceCard Button Callbacks
     */


    @Override
    public void onLeftButtonClicked() {
        mController.decActiveProgress();
    }

    @Override
    public void onRightButtonClicked() {
        mController.incActiveProgress();
    }

    @Override
    public void onTopButtonClicked() {
        navigateToDevice();
    }

    @Override
    public void onBottomButtonClicked() {
        ArrayList<DeviceMode> modes = new ArrayList<>();
        modes.add(new DeviceMode(getContext().getString(R.string.water_heater_mode_standard), getContext().getString(R.string.water_heater_mode_standard_prompt)));
        modes.add(new DeviceMode(getContext().getString(R.string.water_heater_mode_energy_smart), getContext().getString(R.string.water_heater_mode_energy_smart_prompt)));

        DeviceModePopup popup = DeviceModePopup.newInstance(getBottomTextForMode(mModel.getControlMode()), modes);
        popup.setCallback(new DeviceModePopup.Callback() {
            @Override
            public void selectedItem(String mode) {
                onButtonClicked(mode);
                BackstackManager.getInstance().navigateBack();
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onCardClicked() {
        navigateToDevice();
    }

    /***
     * MultiButtonPopup Callback
     */

    @Override
    public void onButtonClicked(String buttonValue) {
        logger.debug("Switch to thermostat mode of: {}", buttonValue);
        if(buttonValue.equals(getContext().getString(R.string.water_heater_mode_energy_smart))) {
            mController.updateMode(AOSmithWaterHeaterController.CONTROLMODE_ENERGY_SMART);
        }
        else if(buttonValue.equals(getContext().getString(R.string.standard))) {
            mController.updateMode(AOSmithWaterHeaterController.CONTROLMODE_STANDARD);
        }
        else if(buttonValue.equals(getContext().getString(R.string.vacation))) {
            mController.updateMode(AOSmithWaterHeaterController.CONTROLMODE_VACATION);
        }
    }

    /***
     * NumberPickerPopup Callback
     */

    @Override
    public void onValueChanged(int value) {
        logger.debug("Got waterheater value changed event :{}", value);
        mController.updateCurrentSetPoint(value);
    }

   @Override
    public void navigateToDevice() {
        int position;
        position = SessionModelManager.instance().indexOf(getDeviceId(), true);

        if (position == -1) return;

        BackstackManager.getInstance()
                .navigateToFragment(DeviceDetailParentFragment.newInstance(position), true);
    }

}
