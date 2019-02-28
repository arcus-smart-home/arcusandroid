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
package arcus.app.subsystems.lawnandgarden.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;

import arcus.cornea.device.lawnandgarden.IrrigationControllerDetailsModel;
import arcus.cornea.device.lawnandgarden.IrrigationControllerState;
import arcus.cornea.device.lawnandgarden.IrrigationDeviceController;
import arcus.cornea.device.lawnandgarden.IrrigationScheduleMode;
import arcus.cornea.model.StringPair;
import arcus.cornea.subsystem.lawnandgarden.utils.LNGDefaults;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.event.ListenerRegistration;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.popups.IrrigationZoneDurationFragment;
import arcus.app.common.popups.RainDelayFragment;
import arcus.app.common.popups.StopWateringPopup;
import arcus.app.common.popups.TupleSelectorPopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.details.IrrigationDelayEvent;
import arcus.app.device.details.IrrigationStopEvent;
import arcus.app.subsystems.lawnandgarden.cards.IrrigationDeviceControlCard;
import arcus.app.subsystems.lawnandgarden.fragments.LawnAndGardenModeSelectionFragment;

import java.util.concurrent.TimeUnit;

public class IrrigationDeviceCardController
      extends DeviceCardController
      implements IrrigationDeviceController.Callback,
      IrrigationDeviceControlCard.OnClickListener,
      IrrigationZoneDurationFragment.Callback,
      TupleSelectorPopup.Callback,
      StopWateringPopup.Callback,
      RainDelayFragment.Callback
{
    private static final String ALLZONES = "ALLZONES";
    private static final String CURRENT_ZONE = "CURRENTZONE";
    private ListenerRegistration mListener;
    private IrrigationDeviceController mController;
    private IrrigationControllerDetailsModel mModel;
    private CountDownTimer wateringCountdown;

    public IrrigationDeviceCardController(String deviceId, Context context) {
        super(deviceId, context);
        IrrigationDeviceControlCard deviceCard = new IrrigationDeviceControlCard(context);

        deviceCard.setLeftImageResource(R.drawable.button_waternow);
        deviceCard.setRightImageResource(R.drawable.button_skip);
        deviceCard.setRightButtonEnabled(false);
        deviceCard.setLeftButtonEnabled(false);

        deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
        deviceCard.setShouldGlow(false);

        deviceCard.setDeviceId(deviceId);
        deviceCard.setCallback(this);

        setCurrentCard(deviceCard);
    }

    @Override public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override public void setCallback(Callback delegate) {
        super.setCallback(delegate);
        mController = IrrigationDeviceController.newController(getDeviceId());
        mListener = mController.setCallback(this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();
        Listeners.clear(mListener);
        mController = null;
        cancelWateringTimer();
    }

    @Override
    public void onLeftButtonClicked() {
        IrrigationDeviceControlCard deviceCard = (IrrigationDeviceControlCard) getCard();
        if(mModel == null || mController == null || deviceCard == null || !deviceCard.isLeftButtonEnabled()) {
            return;
        }

        switch (mModel.getControllerState()) {
            case MANUAL_WATERING:
                onIrrigationStopEvent(new IrrigationStopEvent(deviceCard.getDeviceId(), CURRENT_ZONE));
                break;

            case SCHEDULED_WATERING:
                if (mModel.isMultiZone()) {
                    StopWateringPopup fragment = StopWateringPopup.newInstance(deviceCard.getDeviceId(), "");
                    fragment.setCallback(this);
                    BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
                }
                else {
                    onIrrigationStopEvent(new IrrigationStopEvent(deviceCard.getDeviceId(), CURRENT_ZONE));
                }
                break;

            case IDLE: // Water Now
                if (mModel.isMultiZone()) {
                    IrrigationZoneDurationFragment zoneDurationFragment = IrrigationZoneDurationFragment.newInstance(
                          CorneaUtils.getDeviceAddress(deviceCard.getDeviceId()),
                          LNGDefaults.wateringTimeOptions(),
                          String.valueOf(mController.getDefaultDuration("z1"))
                    );
                    zoneDurationFragment.setCallback(this);
                    BackstackManager.getInstance().navigateToFloatingFragment(zoneDurationFragment, zoneDurationFragment.getClass().getCanonicalName(), true);
                }
                else {
                    TupleSelectorPopup tuple = TupleSelectorPopup.newInstance(
                          LNGDefaults.wateringTimeOptions(),
                          R.string.irrigation_duration,
                          String.valueOf(mController.getDefaultDuration("z1")),
                          true
                    );
                    tuple.setCallback(this);
                    BackstackManager.getInstance().navigateToFloatingFragment(tuple, tuple.getClass().getCanonicalName(), true);
                }
                break;

            default: // No-Op
                break;
        }
    }

    @Override public void onRightButtonClicked() {
        if (mModel == null || mController == null) {
            return;
        }

        if (IrrigationControllerState.SKIPPED.equals(mModel.getControllerState())) {
            cancelSkip();
        }
        else {
            RainDelayFragment fragment = RainDelayFragment.newInstance(
                  mModel.getDeviceAddress(),
                  ArcusApplication.getContext().getString(R.string.watering_delay_text)
            );
            fragment.setCallback(this);
            BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
        }
    }

    @Override public void onTopButtonClicked() {
        navigateToDevice();
    }

    @Override public void onBottomButtonClicked() {
        if (getContext() == null) { // Just to be extra sure we're not null, though we shouldn't be now.
            return;
        }
        BackstackManager.getInstance().navigateToFragment(LawnAndGardenModeSelectionFragment.newInstance(getDeviceId()), true);
    }

    @Override public void onCardClicked() {
        navigateToDevice();
    }

    // Device Controller - Show device state.
    @Override public void showDeviceControls(IrrigationControllerDetailsModel model) {
        IrrigationDeviceControlCard deviceCard = (IrrigationDeviceControlCard) getCard();
        mModel = model;
        cancelWateringTimer();

        if (deviceCard != null) {
            deviceCard.setTitle(model.getDeviceName());
            deviceCard.setDeviceId(Addresses.getId(model.getDeviceAddress()));

            // Handle Offline Mode
            deviceCard.setOffline(!model.isOnline());
            deviceCard.setIsInOta(model.isInOTA());
            deviceCard.setIsInAutoMode(IrrigationControllerState.OFF.equals(mModel.getControllerState()));
            if (deviceCard.isOffline() || deviceCard.isInOta() || deviceCard.isInAutoMode()) {
                deviceCard.setLeftImageResource(0);
                deviceCard.setRightImageResource(0);
                return;
            }

            deviceCard.setNextScheduleTitle(ArcusApplication.getContext().getString(R.string.card_lawn_and_garden_next));
            //If it's a single zone controller and it's watering, don't show the next event or current watering zone
            if (!mModel.isMultiZone() && !TextUtils.isEmpty(mModel.getZoneNameWatering())) {
                mModel.setNextEventZone("");
                mModel.setNextEventTime("");
                mModel.setZoneNameWatering("");
            }

            deviceCard.setScheduleLocation(mModel.getZoneNameWatering());
            deviceCard.setNextScheduleLocation(mModel.getNextEventZone());
            deviceCard.setNextScheduleTime(mModel.getNextEventTime());
            switch (mModel.getControllerState()) {
                case MANUAL_WATERING:
                    deviceCard.setShouldGlow(true);
                    deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
                    deviceCard.setLeftImageResource(R.drawable.button_stop);
                    // If we're in manual mode, don't show the button just because we're watering
                    // Only show the button if we're manually watering but not in manual mode.
                    deviceCard.setRightImageResource(IrrigationScheduleMode.MANUAL.equals(model.getScheduleMode()) ? 0 : R.drawable.button_skip);
                    deviceCard.setRightButtonEnabled(false);
                    deviceCard.setLeftButtonEnabled(true);
                    deviceCard.setNextScheduleLocation(null);
                    deviceCard.setNextScheduleTime(null);
                    showWateringTimer();
                    break;

                case SCHEDULED_WATERING:
                    deviceCard.setShouldGlow(true);
                    deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
                    deviceCard.setLeftImageResource(R.drawable.button_stop);
                    deviceCard.setRightImageResource(R.drawable.button_skip);
                    deviceCard.setRightButtonEnabled(false);
                    deviceCard.setLeftButtonEnabled(true);
                    showWateringTimer();
                    break;

                case SKIPPED:
                    deviceCard.setShouldGlow(false);
                    deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
                    deviceCard.setLeftImageResource(R.drawable.button_waternow);
                    deviceCard.setRightImageResource(R.drawable.button_cancel);
                    deviceCard.setRightButtonEnabled(true);
                    deviceCard.setLeftButtonEnabled(false);

                    // Override above - to show Skipped not next schedule.
                    deviceCard.setNextScheduleTitle(ArcusApplication.getContext().getString(R.string.card_lawn_and_garden_skip_until));
                    deviceCard.setNextScheduleLocation(null);
                    deviceCard.setNextScheduleTime(mModel.getSkipUntilTime());
                    break;

                default:
                case IDLE:
                    deviceCard.setShouldGlow(false);
                    deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
                    deviceCard.setLeftImageResource(R.drawable.button_waternow);
                    deviceCard.setLeftButtonEnabled(true);
                    deviceCard.setRightButtonEnabled(true);
                    deviceCard.setRightImageResource(IrrigationScheduleMode.MANUAL.equals(model.getScheduleMode()) ? 0 : R.drawable.button_skip);
                    break;
            }

            deviceCard.setScheduleMode(mModel.getScheduleMode().asString());
            if (mModel.hasRequestInFlight()) {
                disableAllButtons();
            }
            else {
                deviceCard.setBottomButtonEnabled(true);
            }
        }
    }

    // DeviceController - Error happened.
    @Override public void errorOccurred(Throwable error) {
        if (mModel != null) {
            showDeviceControls(mModel);
        }

        ErrorManager.in(((Activity) getContext())).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    protected void cancelSkip() {
        if (mController == null) {
            return;
        }

        disableAllButtons();
        mController.cancelSkip();
    }

    // MultiZone WaterNow
    @Override public void selectedItem(StringPair selected) {
        if (mModel == null) {
            return;
        }

        disableAllButtons();
        selectionComplete(mModel.getDeviceAddress(), "z1", selected);
    }

    @Override public void selectionComplete(String deviceId, String zone, StringPair selected) {
        int minutesToWater = Integer.parseInt(selected.getKey());
        if (minutesToWater < 1 || mController == null) {
            return;
        }

        disableAllButtons();
        mController.waterNow(zone, minutesToWater);
    }

    // AnyZone RainDelay Start
    @Override public void onIrrigationDelayEvent(IrrigationDelayEvent event) {
        if (mController == null) {
            return;
        }

        disableAllButtons();
        mController.skipWatering(event.getDelayTime());
    }

    // AnyZone Cancel/Stop Watering
    @Override public void onIrrigationStopEvent(IrrigationStopEvent event) {
        if (mController == null || mModel == null) {
            return;
        }

        disableAllButtons();
        mController.stopWatering(ALLZONES.equals(event.getType()));
    }

    protected void disableAllButtons() {
        IrrigationDeviceControlCard deviceCard = (IrrigationDeviceControlCard) getCard();
        if (deviceCard == null) {
            return;
        }

        deviceCard.setLeftButtonEnabled(false);
        deviceCard.setRightButtonEnabled(false);
        deviceCard.setBottomButtonEnabled(false);
    }

    protected void showWateringTimer() {
        if (mModel == null) {
            return;
        }

        wateringCountdown = new CountDownTimer(TimeUnit.SECONDS.toMillis(mModel.getWateringSecondsRemaining()), 1000) {
            @Override public void onFinish() {}
            @Override public void onTick(long millisUntilFinished) {
                IrrigationDeviceControlCard deviceCard = (IrrigationDeviceControlCard) getCard();
                if (mModel == null || deviceCard == null) {
                    return;
                }

                int hoursRemaining = (int) TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                int minutesRemaining = (int) TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                minutesRemaining += TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60 > 0 ? 1 : 0;

                if (minutesRemaining == 60) {
                    hoursRemaining += 1;
                    minutesRemaining = 0;
                }

                String hours = getContext().getResources().getQuantityString(R.plurals.care_hours_plural, hoursRemaining, hoursRemaining);
                String mins  = getContext().getResources().getQuantityString(R.plurals.care_minutes_plural, minutesRemaining, minutesRemaining);
                if (hoursRemaining != 0) {
                    if (minutesRemaining == 0) {
                        deviceCard.setScheduleMode(String.format("%s Remaining", hours));
                    }
                    else {
                        deviceCard.setScheduleMode(String.format("%s %s Remaining", hours, mins));
                    }
                }
                else {
                    deviceCard.setScheduleMode(String.format("%s Remaining", mins));
                }
            }
        };
        wateringCountdown.start();
    }

    protected void cancelWateringTimer() {
        if (wateringCountdown != null) {
            wateringCountdown.cancel();
            wateringCountdown = null;
        }
    }
}
