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
package arcus.app.device.details;

import android.app.Activity;
import android.graphics.Color;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import arcus.cornea.device.lawnandgarden.IrrigationControllerDetailsModel;
import arcus.cornea.device.lawnandgarden.IrrigationControllerState;
import arcus.cornea.device.lawnandgarden.IrrigationDeviceController;
import arcus.cornea.device.lawnandgarden.IrrigationScheduleMode;
import arcus.cornea.model.StringPair;
import arcus.cornea.subsystem.lawnandgarden.utils.LNGDefaults;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.IrrigationAutoModeBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.popups.IrrigationZoneDurationFragment;
import arcus.app.common.popups.RainDelayFragment;
import arcus.app.common.popups.StopWateringPopup;
import arcus.app.common.popups.TupleSelectorPopup;
import arcus.app.common.view.GlowableImageView;

import java.beans.PropertyChangeEvent;
import java.util.concurrent.TimeUnit;

public class IrrigationFragment extends ArcusProductFragment
      implements IrrigationDeviceController.Callback,
      StopWateringPopup.Callback,
      RainDelayFragment.Callback,
      TupleSelectorPopup.Callback,
      IrrigationZoneDurationFragment.Callback,
      View.OnClickListener,
      IShowedFragment, IClosedFragment
{
    private static final String CURRENT_ZONE = "CURRENTZONE";
    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;
    protected TextView eventStatusText;
    protected TextView eventStatusTime;
    protected View eventImage;
    protected ImageButton startStopButton;
    protected ImageButton delayButton;
    protected View powerStatusView;
    protected CountDownTimer wateringCountdown;

    private IrrigationDeviceController controller;
    private ListenerRegistration controllerListener;
    private IrrigationControllerDetailsModel controllerDetailsModel;

    @NonNull public static IrrigationFragment newInstance() {
        return new IrrigationFragment();
    }

    @Override public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override public void doTopSection() { // TODO: Get water drop icon.
        eventStatusText = (TextView) topView.findViewById(R.id.device_top_schdule_event);
        eventStatusText.setTextColor(getResources().getColor(R.color.overlay_white_with_60));

        eventStatusTime = (TextView) topView.findViewById(R.id.device_top_schdule_time);
        eventStatusTime.setTextColor(Color.WHITE);

        eventImage = topView.findViewById(R.id.device_top_schedule_image);
    }

    @Override
    public void doStatusSection() {
        startStopButton = (ImageButton) statusView.findViewById(R.id.open_close_button);
        delayButton = (ImageButton) statusView.findViewById(R.id.delay_button);

        powerStatusView = statusView.findViewById(R.id.irrigation_status);
        TextView batteryTopText = (TextView) powerStatusView.findViewById(R.id.top_status_text);
        TextView batteryBottomText = (TextView) powerStatusView.findViewById(R.id.bottom_status_text);
        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);

        startStopButton.setOnClickListener(this);
        delayButton.setOnClickListener(this);

        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
    }

    @Override public void onClick(View v) {
        if (v == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.open_close_button:
                startOrStopWatering();
                break;

            case R.id.delay_button:
                skipOrCancelSkipWatering();
                break;

            default:
                break;
        }
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.irrigation_controller_status;
    }

    @Override public void onResume() {
        super.onResume();

        DeviceModel model = getDeviceModel();
        if (model == null) {
            return;
        }

        controller = IrrigationDeviceController.newController(model.getId());
        controllerListener = controller.setCallback(this);
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(controllerListener);
        hideProgressBar();
        cancelExistingTimer();
    }

    @Override
    public void setEnabled (boolean isEnabled) {
        super.setEnabled(isEnabled);

        if (isEnabled) {
            showIrrigationAutoModeBanner();
        }
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        super.propertyUpdated(event);

        if (event.getPropertyName().equals(DeviceConnection.ATTR_STATE) &&
                event.getNewValue().equals(DeviceConnection.STATE_ONLINE)) {

            showIrrigationAutoModeBanner();
        }
    }

    public void skipOrCancelSkipWatering() {
        if (controller == null || controllerDetailsModel == null) {
            return;
        }

        if (IrrigationControllerState.SKIPPED.equals(controllerDetailsModel.getControllerState())) {
            showProgressAndAlphaOutButtons();
            controller.cancelSkip();
        }
        else {
            RainDelayFragment fragment = RainDelayFragment.newInstance(
                  controllerDetailsModel.getDeviceAddress(),
                  getString(R.string.watering_delay_text)
            );
            fragment.setCallback(this);
            BackstackManager
                  .getInstance()
                  .navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
        }
    }

    public void startOrStopWatering() {
        if (controllerDetailsModel == null) {
            return;
        }

        ArcusFloatingFragment transitionTo = null;
        switch (controllerDetailsModel.getControllerState()) {
            case MANUAL_WATERING:
                onIrrigationStopEvent(new IrrigationStopEvent(controllerDetailsModel.getDeviceAddress(), CURRENT_ZONE));
                break;

            case SCHEDULED_WATERING:
                if (controllerDetailsModel.isMultiZone()) {
                    StopWateringPopup popup = StopWateringPopup.newInstance(controllerDetailsModel.getDeviceAddress(), "");
                    popup.setCallback(this);
                    transitionTo = popup;
                }
                else {
                    // If Single zone don't show stop all zones (only has 1 zone)
                    onIrrigationStopEvent(new IrrigationStopEvent(controllerDetailsModel.getDeviceAddress(), CURRENT_ZONE));
                }
                break;

            case IDLE:
                if (controllerDetailsModel.isMultiZone()) {
                    IrrigationZoneDurationFragment durationPopup = IrrigationZoneDurationFragment.newInstance(
                          controllerDetailsModel.getDeviceAddress(),
                          LNGDefaults.wateringTimeOptions(),
                          String.valueOf(controller.getDefaultDuration("z1"))
                    );
                    durationPopup.setCallback(this);
                    transitionTo = durationPopup;
                }
                else {
                    TupleSelectorPopup tuple = TupleSelectorPopup.newInstance(
                          LNGDefaults.wateringTimeOptions(),
                          R.string.irrigation_duration,
                          String.valueOf(controller.getDefaultDuration("z1")),
                          true
                    );
                    tuple.setCallback(this);
                    transitionTo = tuple;
                }
                break;

            case SKIPPED:
            default:
                break; /* no-op */
        }

        if (transitionTo != null) {
            BackstackManager
                  .getInstance()
                  .navigateToFloatingFragment(transitionTo, transitionTo.getClass().getCanonicalName(), true);
        }
    }

    // SingleZone "Water now"
    @Override public void selectedItem(StringPair selected) {
        selectionComplete(controllerDetailsModel.getDeviceAddress(), "z1", selected);
    }

    // AnyZone RainDelay Start
    @Override public void onIrrigationDelayEvent(IrrigationDelayEvent event) {
        if (controller == null) {
            return;
        }

        showProgressAndAlphaOutButtons();
        controller.skipWatering(event.getDelayTime());
    }

    // AnyZone Cancel/Stop Watering
    @Override public void onIrrigationStopEvent(IrrigationStopEvent event) {
        if (controller == null) {
            return;
        }

        showProgressAndAlphaOutButtons();
        controller.stopWatering("ALLZONES".equals(event.getType())); // Just use a boolean attribute?
    }

    @Override public void selectionComplete(
          String deviceId,
          String zone,
          StringPair selected
    ) {
        if (controller == null) {
            return;
        }

        showProgressAndAlphaOutButtons();
        controller.waterNow(zone, Integer.valueOf(selected.getKey()));
    }

    @Override public void showDeviceControls(IrrigationControllerDetailsModel model) {


        if (model == null) {
            return;
        }

        controllerDetailsModel = model;
        cancelExistingTimer();
        if (model.getControllerState() == null) {
            return;
        }

        switch (model.getControllerState()) {
            case MANUAL_WATERING:
                showWateringTimer();
                break;

            case SCHEDULED_WATERING:
                showWateringTimer();
                break;

            case SKIPPED:
                eventStatusText.setText(String.format("%s ", getString(R.string.card_lawn_and_garden_skip_until)).toUpperCase());
                eventStatusTime.setText(model.getSkipUntilTimeUpperCase());
                break;

            case OFF:
                eventStatusText.setText("");
                eventStatusTime.setText("");
                break;

            case IDLE:
            default:
                if (model.hasNextEvent()) {
                    eventStatusText.setText(String.format("%s ", getString(R.string.next_event)));
                    eventStatusTime.setText(model.getNextEventTimeUpperCase());
                }
                else {
                    eventStatusText.setText("");
                    eventStatusTime.setText("");
                }
                break;
        }

        showIrrigationAutoModeBanner();
    }

    @Override public void errorOccurred(Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    protected void enableStartAndDelay(
          final boolean start,
          final int startBackground,
          final boolean delay,
          final int delayBackground
    ) {
        delayButton.post(new Runnable() {
            @Override public void run() {
                if (delayBackground == 0) {
                    delayButton.setVisibility(View.GONE);
                }
                else {
                    delayButton.setVisibility(View.VISIBLE);
                    delayButton.setEnabled(delay);
                    delayButton.setAlpha(delay ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

                    Activity activity = getActivity();
                    if (activity != null) {
                        delayButton.setBackground(ContextCompat.getDrawable(activity, delayBackground));
                    }
                }
            }
        });
        startStopButton.post(new Runnable() {
            @Override public void run() {
                startStopButton.setEnabled(start);
                startStopButton.setAlpha(start ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

                Activity activity = getActivity();
                if (activity != null) {
                    startStopButton.setBackground(ContextCompat.getDrawable(activity, startBackground));
                }
            }
        });
    }

    protected void showProgressAndAlphaOutButtons() {
        showProgressBar();
        if (controllerDetailsModel != null) {
            controllerDetailsModel.setHasRequestInFlight(true);
        }

        startStopButton.setEnabled(false);
        startStopButton.setAlpha(BUTTON_DISABLED_ALPHA);

        delayButton.setEnabled(false);
        delayButton.setAlpha(BUTTON_DISABLED_ALPHA);
    }

    protected void setImageGlowOn(boolean isWatering) {
        if (deviceImage != null) {
            deviceImage.setGlowing(isWatering);
        }
    }

    protected void cancelExistingTimer() {
        if (wateringCountdown != null) {
            wateringCountdown.cancel();
        }
        wateringCountdown = null;
        if (eventImage != null) {
            eventImage.setVisibility(View.GONE);
        }
    }

    protected void showWateringTimer() {
        if (controllerDetailsModel == null) {
            return;
        }

        if (eventImage != null) {
            eventImage.setVisibility(View.VISIBLE);
        }

        wateringCountdown = new CountDownTimer(TimeUnit.SECONDS.toMillis(controllerDetailsModel.getWateringSecondsRemaining()), 1000) {
            @Override public void onFinish() {}
            @Override public void onTick(long millisUntilFinished) {
                if (eventStatusTime == null || eventStatusText == null || controllerDetailsModel == null) {
                    return;
                }

                int hoursRemaining = (int) TimeUnit.MILLISECONDS.toHours(millisUntilFinished);
                int minutesRemaining = (int) TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                minutesRemaining += TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60 > 0 ? 1 : 0;

                if (minutesRemaining == 60) {
                    hoursRemaining += 1;
                    minutesRemaining = 0;
                }

                eventStatusText.setText(controllerDetailsModel.getZoneNameWatering());
                String hours = getResources().getQuantityString(R.plurals.care_hours_plural, hoursRemaining, hoursRemaining);
                String mins  = getResources().getQuantityString(R.plurals.care_minutes_plural, minutesRemaining, minutesRemaining);
                if (hoursRemaining != 0) {
                    if (minutesRemaining == 0) {
                        eventStatusTime.setText(String.format("%s Remaining", hours));
                    }
                    else {
                        eventStatusTime.setText(String.format("%s %s Remaining", hours, mins));
                    }
                }
                else {
                    eventStatusTime.setText(String.format("%s Remaining", mins));
                }
            }
        };
        wateringCountdown.start();
    }

    private void updateUI() {
        if (controllerDetailsModel == null) {
            return;
        }

        BannerManager.in(getActivity()).removeBanner(IrrigationAutoModeBanner.class);
        if (controllerDetailsModel.hasRequestInFlight()) {
            enableStartAndDelay(false, R.drawable.button_waternow, false, R.drawable.button_skip);
            showProgressBar();
        }
        else if (!controllerDetailsModel.isOnline() || controllerDetailsModel.isInOTA()) {
            enableStartAndDelay(false, R.drawable.button_waternow, false, R.drawable.button_skip);
        }
        else {
            switch (controllerDetailsModel.getControllerState()) {
                case MANUAL_WATERING:
                    enableStartAndDelay(true, R.drawable.button_stop, false, 0);
                    setImageGlowOn(true);
                    break;

                case SCHEDULED_WATERING:
                    enableStartAndDelay(true, R.drawable.button_stop, false, R.drawable.button_skip);
                    setImageGlowOn(true);
                    break;

                case SKIPPED:
                    enableStartAndDelay(false, R.drawable.button_waternow, true, R.drawable.button_cancel);
                    setImageGlowOn(false);
                    break;

                case OFF:
                    showIrrigationAutoModeBanner();
                    break;

                case IDLE:
                default:
                    int icon = 0;
                    if (!IrrigationScheduleMode.MANUAL.equals(controllerDetailsModel.getScheduleMode())) {
                        icon = R.drawable.button_skip;
                    }
                    enableStartAndDelay(true, R.drawable.button_waternow, true, icon);
                    setImageGlowOn(false);
                    break;
            }
            hideProgressBar();
        }

        if (powerStatusView != null) {
            powerStatusView.setVisibility(controllerDetailsModel.isMultiZone() ? View.GONE : View.VISIBLE);
        }
    }

    private void showIrrigationAutoModeBanner() {
        if (getCurrentFragment() instanceof IrrigationFragment) {
            if (controllerDetailsModel != null &&
                    IrrigationControllerState.OFF.equals(controllerDetailsModel.getControllerState())) {

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        BannerManager.in(getActivity()).showBanner(new IrrigationAutoModeBanner());
                        enableStartAndDelay(false, R.drawable.button_waternow, false, R.drawable.button_skip);
                    }
                });
            }
            else { updateUI(); }
        }
    }

    @Override public void onShowedFragment() {
        checkConnection();
        showIrrigationAutoModeBanner();
    }

    @Override public void onClosedFragment() {
        hideProgressBar();
        BannerManager.in(getActivity()).removeBanner(IrrigationAutoModeBanner.class);
    }
}
