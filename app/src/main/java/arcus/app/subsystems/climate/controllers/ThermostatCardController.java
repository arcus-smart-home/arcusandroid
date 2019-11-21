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
package arcus.app.subsystems.climate.controllers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.thermostat.ThermostatController;
import arcus.cornea.device.thermostat.ThermostatMode;
import arcus.cornea.device.thermostat.ThermostatProxyModel;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.EventMessageMonitor;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.utils.EventTimeoutController;
import arcus.app.common.utils.ThrottledDelayedExecutor;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.details.model.ThermostatOperatingMode;
import arcus.app.device.model.DeviceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class ThermostatCardController extends DeviceCardController implements DeviceController.Callback<ThermostatProxyModel>,
        DeviceControlCard.OnClickListener, NumberPickerPopup.OnValueChangedListener, MultiButtonPopup.OnButtonClickedListener, ThermostatController.CommandCommittedCallback, EventTimeoutController.EventTimeoutCallback {

    private Logger logger = LoggerFactory.getLogger(ThermostatCardController.class);

    private static int DEVICE_TIMEOUT = 120000;
    private static int PROCESS_MODELCHANGE_TIMEOUT = 3000;

    @Nullable
    private ThermostatController mController;
    private ThermostatProxyModel mModel;
    private boolean isLow = false;

    private ThermostatMode mMode;
    private EventMessageMonitor eventMessageMonitor;
    private EventTimeoutController eventTimeoutController;
    protected ThrottledDelayedExecutor processModelChanges = new ThrottledDelayedExecutor(PROCESS_MODELCHANGE_TIMEOUT);
    protected ThrottledDelayedExecutor waitDelayThrottle = new ThrottledDelayedExecutor(DEVICE_TIMEOUT);

    public ThermostatCardController(String deviceId, Context context) {
        super(deviceId, context);

        // Construct a Thermostat Card
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

        eventMessageMonitor = EventMessageMonitor.getInstance();
        eventTimeoutController = EventTimeoutController.getInstance();
        eventTimeoutController.setCallback(deviceId, this);

        setCurrentCard(deviceCard);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mController = ThermostatController.newController(getDeviceId(), this);
        mController.setCommandCommimttedCallback(this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        if (mController != null) {
            mController.clearCallback();
        }
        mController = null;
    }

    private String getBottomTextForMode(@NonNull ThermostatMode mode) {
        ThermostatOperatingMode operatingMode = ThermostatOperatingMode.fromThermostatMode(mode);
        return ArcusApplication.getContext().getString(operatingMode.getStringResId(useNestTerminology())).toUpperCase();
    }

    /*
     * Thermostat Callback
     */

    @Override
    public void show(@NonNull ThermostatProxyModel model) {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        mModel = model;

        // Populate card from model
        if (deviceCard != null) {
            deviceCard.setTitle(model.getName());
            deviceCard.setDeviceId(model.getDeviceId());

            if(model.getAuthorizationState() != null) {
                deviceCard.setAuthorizationState(model.getAuthorizationState());
            }
            deviceCard.setRequiresLogin(model.isRequiresLogin());

            checkForOnGoingMessages(model.getDeviceId(), deviceCard);
            // Handle Offline Mode
            deviceCard.setOffline(!model.isOnline());
            deviceCard.setHoneywellTcc(model.isHoneywellDevice());
            if (!model.isOnline()) {
                if(model.isCloudDevice()) {
                    deviceCard.setCloudDevice(true);
                }
                deviceCard.setBottomImageText("OFF");
                deviceCard.setBottomButtonEnabled(false);
                return;
            }
            else {
                deviceCard.setCloudDevice(false);
                if(model.isCloudDevice()) {
                    deviceCard.setCloudDevice(true);
                }
            }

            if (model.isCloudDevice() && !model.isHoneywellDevice()) {
                deviceCard.clearErrors();
                for (String thisError : model.getErrors()) {
                    deviceCard.addError(thisError);
                }
            }

            deviceCard.setBottomImageResource(R.drawable.outline_rounded_button_style);

            // Build Sub String
            StringBuilder sb = new StringBuilder();
            sb.append("Now ");
            sb.append(model.getTemperature());
            sb.append("º");

            if (model.getMode() != null) {
                Integer heat = model.getHeatSetPoint();
                Integer cool = model.getCoolSetPoint();

                switch (model.getMode()) {
                    case AUTO:
                        sb.append(String.format(" Set %dº-%dº", heat, cool));

                        deviceCard.setLeftImageResource(R.drawable.button_low);
                        deviceCard.setRightImageResource(R.drawable.button_high);
                        deviceCard.setLeftButtonVisible(true);
                        deviceCard.setRightButtonVisible(true);
                        break;
                    case COOL:
                        sb.append(String.format(" Set %dº", cool));
                        deviceCard.setLeftImageResource(R.drawable.button_minus);
                        deviceCard.setRightImageResource(R.drawable.button_plus);
                        deviceCard.setLeftButtonVisible(true);
                        deviceCard.setRightButtonVisible(true);
                        break;
                    case HEAT:
                        sb.append(String.format(" Set %dº", heat));

                        deviceCard.setLeftImageResource(R.drawable.button_minus);
                        deviceCard.setRightImageResource(R.drawable.button_plus);
                        deviceCard.setLeftButtonVisible(true);
                        deviceCard.setRightButtonVisible(true);
                        break;
                    default:
                        deviceCard.setLeftImageResource(R.drawable.button_minus);
                        deviceCard.setRightImageResource(R.drawable.button_plus);
                        deviceCard.setLeftButtonVisible(false);
                        deviceCard.setRightButtonVisible(false);
                        break;
                }

                deviceCard.setDescription(sb.toString());
                deviceCard.setBottomButtonEnabled(true);
                deviceCard.setBottomImageText(getBottomTextForMode(model.getMode()));
            }
            else {
                deviceCard.setLeftImageResource(R.drawable.button_minus);
                deviceCard.setRightImageResource(R.drawable.button_plus);
                deviceCard.setLeftButtonEnabled(false);
                deviceCard.setRightButtonEnabled(false);

                deviceCard.setDescription("--");
                deviceCard.setBottomImageText("--");
                deviceCard.setBottomButtonEnabled(true);
            }
            mMode = model.getMode();
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
        if (mMode == null) {
            return;
        }

        addEventTimer();
        // Decrease Mode Value
        if (mMode.equals(ThermostatMode.AUTO)) {
            // Show Picker
            loadAutoLow();
        } else {

            if (mController != null) {
                mController.decrementCurrentSetPoint();
            }
        }
    }

    private void addEventTimer() {
        processModelChanges.execute(new Runnable() {
            @Override
            public void run() {
                if (waitDelayThrottle == null) {
                    waitDelayThrottle = new ThrottledDelayedExecutor(DEVICE_TIMEOUT);
                }
                waitDelayThrottle.execute(new Runnable() {
                    @Override
                    public void run() {
                        EventTimeoutController.timedOut(mModel.getDeviceId());
                    }
                });
                eventTimeoutController.setTimer(mModel.getDeviceId(), "WAIT_TIMER" + mModel.getDeviceId(), waitDelayThrottle);
                eventTimeoutController.removeTimer(mModel.getDeviceId(), "HONEYWELLTCC_PROCESSMODEL_TIMER");
                show(mModel);
                //since the timer is valid in the show() method, we need to disable the buttons here.
                /*DeviceControlCard deviceCard = (DeviceControlCard) getCard();
                deviceCard.setLeftButtonEnabled(false);
                deviceCard.setRightButtonEnabled(false);
                deviceCard.setBottomButtonEnabled(false);*/
            }
        });
        eventTimeoutController.setTimer(mModel.getDeviceId(), "HONEYWELLTCC_PROCESSMODEL_TIMER", processModelChanges);
    }

    @Override
    public void onRightButtonClicked() {
        if (mMode == null) {
            return;
        }

        addEventTimer();
        // Increase Mode Value
        if (mMode.equals(ThermostatMode.AUTO)) {
            // Show Picker
            loadAutoHigh();
        } else {
            mController.incrementCurrentSetPoint();
        }

    }

    private void loadAutoLow() {
        isLow = true;

        NumberPickerPopup popup = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.LOW, mModel.getMinimumSetpoint(), mModel.getMaximumSetpoint(), mModel.getHeatSetPoint());
        popup.setCoolSetPoint(mModel.getCoolSetPoint());
        popup.setHeatSetPoint(mModel.getHeatSetPoint());
        popup.setOnValueChangedListener(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private void loadAutoHigh() {
        isLow = false;
        NumberPickerPopup popup = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.HIGH, mModel.getMinimumSetpoint(), mModel.getMaximumSetpoint(), mModel.getCoolSetPoint());
        popup.setCoolSetPoint(mModel.getCoolSetPoint());
        popup.setHeatSetPoint(mModel.getHeatSetPoint());
        popup.setOnValueChangedListener(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onTopButtonClicked() {
        navigateToDevice();
    }

    @Override
    public void onBottomButtonClicked() {
        ArrayList<String> buttons = new ArrayList<>();

        for (ThermostatOperatingMode operatingMode : ThermostatOperatingMode.fromThermostatModes(mModel.getSupportedModes())) {
            buttons.add(ArcusApplication.getContext().getString(operatingMode.getStringResId(useNestTerminology())).toUpperCase());
        }

        MultiButtonPopup popup = MultiButtonPopup.newInstance(getContext().getString(R.string.hvac_mode_selection), buttons);
        popup.setOnButtonClickedListener(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private boolean useNestTerminology() {
        return DeviceType.fromHint(mModel.getDeviceTypeHint()) == DeviceType.NEST_THERMOSTAT;
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
        if (mController != null){
            ThermostatOperatingMode selectedMode = ThermostatOperatingMode.fromDisplayString(buttonValue);

            logger.debug("Switch to thermostat mode of: {}", ThermostatMode.valueOf(selectedMode.name()));
            mController.updateMode(ThermostatMode.valueOf(selectedMode.name()));
            addEventTimer();
        }

    }

    /***
     * NumberPickerPopup Callback
     */

    @Override
    public void onValueChanged(int value) {
        logger.debug("Got thermostat value changed event :{}", value);
        if(mController!=null){
            if (isLow) {
                mController.updateHeatSetPoint(value);
            } else {
                mController.updateCoolSetPoint(value);
            }
        }
    }

    private boolean checkForOnGoingMessages(String deviceId, DeviceControlCard deviceCard) {
        //we need to check and see if there are messages in progress
        if (!DeviceType.TCC_THERM.equals(DeviceType.fromHint(mModel.getDeviceTypeHint()))) {
            deviceCard.setIsEventInProcess(false);
            eventMessageMonitor.removeScheduledEvents(mModel.getDeviceId());
            return false;
        }

        ThrottledDelayedExecutor timer = eventTimeoutController.getTimer(mModel.getDeviceId(), "HONEYWELLTCC_PROCESSMODEL_TIMER");
        HashMap<String, Long> deviceEventMap = eventMessageMonitor.getScheduleForDevice(deviceId);
        if(deviceEventMap != null) {
            for(Iterator<Map.Entry<String, Long>> it = deviceEventMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Long> entry = it.next();
                if ((entry.getValue()+DEVICE_TIMEOUT) < System.currentTimeMillis()) {
                    it.remove();
                }
                else if ((entry.getValue() < (System.currentTimeMillis()+DEVICE_TIMEOUT)) && timer == null) {
                    deviceCard.setIsEventInProcess(true);
                    return true;
                }
            }
        }
        deviceCard.setIsEventInProcess(false);
        return false;
    }

    @Override
    public void commandCommitted() {
        show(mModel);
    }

    @Override
    public void timeoutReached() {
        eventMessageMonitor.clearStale(mModel.getDeviceId());
        eventMessageMonitor.removeScheduledEvents(mModel.getDeviceId());
        show(mModel);
    }
}
