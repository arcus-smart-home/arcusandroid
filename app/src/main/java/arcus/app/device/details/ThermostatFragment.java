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

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.ScheduleViewController;
import arcus.cornea.subsystem.climate.model.ScheduleModel;
import arcus.cornea.subsystem.climate.model.ScheduledDay;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.Thermostat;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.ThrottledExecutor;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Map;


public class ThermostatFragment extends ArcusProductFragment implements IShowedFragment, ScheduleViewController.Callback, View.OnClickListener {

    protected static final int THROTTLE_PERIOD_MS = 1000;
    protected static final int QUIESCENT_MS = 5000;

    protected AlertDialog alertDialog;
    protected TextView centerTempTextView;
    protected TextView tempBottomText;
    protected TextView tempTopText;
    protected ImageButton plusButton;
    protected ImageButton minusButton;
    protected Version1TextView nextEventLabel;
    protected Version1TextView nextEventDescription;

    private ScheduleModel mScheduleModel;
    private ScheduleViewController mController;
    private ListenerRegistration mListener;

    protected final int MIN_SETPOINT_SEPARATION = 3;
    protected final int MIN_HEAT_TEMP_VAL = 45;
    protected final int MIN_COOL_TEMP_VAL = 48;
    protected final int MAX_HEAT_TEMP_VAL = 92;
    protected final int MAX_COOL_TEMP_VAL = 95;

    protected final ThrottledExecutor setpointThrottle = new ThrottledExecutor(getThrottleValue());
    protected final ThrottledExecutor modeThrottle = new ThrottledExecutor(getThrottleValue());
    protected boolean alreadySetSelectedAndUnselectedResources = false;

    protected int getThrottleValue() {
        return THROTTLE_PERIOD_MS;
    }
    @NonNull
    public static ThermostatFragment newInstance() {
        return new ThermostatFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        nextEventLabel.setVisibility(View.INVISIBLE);
        nextEventDescription.setVisibility(View.INVISIBLE);
        Thermostat device = getCapability(Thermostat.class);

        if (mController == null) {
            mController = ScheduleViewController.instance();
        }
        if (device != null) {
            DayOfWeek selectedDay = DayOfWeek.MONDAY;
            mListener = mController.select(device.getAddress(), this, selectedDay);
        }

        setControlPoints();
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {
        // Nothing to do
        //device_top_schdule_event
        //device_top_schdule_time
        nextEventLabel = (Version1TextView) topView.findViewById(R.id.device_top_schdule_event);
        nextEventLabel.setText(getString(R.string.next_event_label));
        nextEventDescription = (Version1TextView) topView.findViewById(R.id.device_top_schdule_time);
    }

    @Override
    public void doStatusSection() {


        View tempView = statusView.findViewById(R.id.thermostat_status_temp);

        tempTopText = (TextView) tempView.findViewById(R.id.top_status_text);
        tempBottomText = (TextView) tempView.findViewById(R.id.bottom_status_text);
        minusButton = (ImageButton) statusView.findViewById(R.id.thermostat_minus_btn);
        plusButton = (ImageButton) statusView.findViewById(R.id.thermostat_plus_btn);

        ImageButton modeBtn = (ImageButton) statusView.findViewById(R.id.thermostat_mode_btn);

        minusButton.setOnClickListener(this);
        plusButton.setOnClickListener(this);
        modeBtn.setOnClickListener(this);

        seekArc.bringToFront();
        seekArc.setRoundedEdges(true);
        seekArc.setRangeEnabled(true);
        seekArc.setTouchInSide(false);
        seekArc.setTextEnabled(true);
        seekArc.setMinValue(MIN_HEAT_TEMP_VAL);
        seekArc.setMaxValue(MAX_COOL_TEMP_VAL);
        seekArc.setMinRangeDistance(MIN_SETPOINT_SEPARATION);
        seekArc.setProgressColor(DeviceSeekArc.THUMB_LOW, Color.TRANSPARENT);
        seekArc.setProgressColor(DeviceSeekArc.THUMB_HIGH, Color.TRANSPARENT);

        /**
         * Band-Aid Placement.
         *
         * The fragment is being loaded and the setUiActiveThumb(**) method is being called correctly to set the
         * correct thumb for increment / decrement. However, since this method is called up to 8 times for a single load
         * the setSelectedResource method was being called again and again.  Looking at the setSelectedResource method
         * this resets the "mLastPressedThumb to mThumbLow" - If we're in cool mode, this causes us to set the heat set
         * point instead of the cool set point.
         *
         * We really need to redo this fragment so it uses the same controller as the  Service Card or is, at least,
         * easier to read/maintain.
         *
         * **Note, this only impacted when you entered this screen while your thermostat was in cool mode.  If you came in
         * set to HEAT or AUTO, this would not impact the settings (We were setting the heat set point while in cool
         * mode when this happened, and we weren't udpating the set-to text between the + and -).
         *
         */
        if (!alreadySetSelectedAndUnselectedResources) {
            alreadySetSelectedAndUnselectedResources = true;
            seekArc.setSelectedResource(R.drawable.icon_thermostat_selected, null);
            seekArc.setUnSelectedResource(R.drawable.icon_thermostat_unselected, null);
        }
        /**
         * End of Band-Aid Placement.
         */

        seekArc.setOnSeekArcChangeListener(new DeviceSeekArc.OnSeekArcChangeListener() {
            @Override
            public void onProgressChanged(@NonNull DeviceSeekArc seekArc, int selectedThumb, int progress, boolean fromUser) {
                boolean rangeEnabled = seekArc.isRangeEnabled();
                String mode = getPlatformHvacMode();

                if((Thermostat.HVACMODE_COOL.equals(mode) && selectedThumb == DeviceSeekArc.THUMB_LOW) ||
                        (Thermostat.HVACMODE_HEAT.equals(mode) && selectedThumb == DeviceSeekArc.THUMB_HIGH)) {
                    //mode and thumb don't match, don't update the text
                } else {
                    if(selectedThumb == seekArc.getActiveProgress()) {
                        setUiSetPointText(mode, progress);
                    }
                }

                // If value is less than 3 away, push the other value back one
                if (selectedThumb == DeviceSeekArc.THUMB_LOW && rangeEnabled && fromUser) {
                    if (progress >= seekArc.getProgress(DeviceSeekArc.THUMB_HIGH) - MIN_SETPOINT_SEPARATION) {
                        seekArc.setProgress(DeviceSeekArc.THUMB_HIGH, progress + MIN_SETPOINT_SEPARATION);
                        // need to reset active progress so temp values don't interchange
                        setUiActiveThumb(Thermostat.HVACMODE_HEAT);
                        if (progress <= MAX_HEAT_TEMP_VAL && progress >= MIN_HEAT_TEMP_VAL) {
                            setUiSetPointText(mode, progress);
                        }
                    }
                } else if (selectedThumb == DeviceSeekArc.THUMB_HIGH && rangeEnabled && fromUser) {
                    if (progress <= seekArc.getProgress(DeviceSeekArc.THUMB_LOW) + MIN_SETPOINT_SEPARATION) {
                        seekArc.setProgress(DeviceSeekArc.THUMB_LOW, progress - MIN_SETPOINT_SEPARATION);
                        // need to reset active progress so temp values don't interchange
                        setUiActiveThumb(Thermostat.HVACMODE_COOL);
                        if (progress <= MAX_COOL_TEMP_VAL && progress >= MIN_COOL_TEMP_VAL) {
                            setUiSetPointText(mode, progress);
                        }
                    }
                }

                // Sync selected UI set point to platform
                if (fromUser) {
                    requestUpdatePlatformSetPoints();
                }
            }

            @Override
            public void onStartTrackingTouch(DeviceSeekArc seekArc, int selectedThumb, int progress) {
                // Nothing to do
            }

            @Override
            public void onStopTrackingTouch(DeviceSeekArc seekArc, int selectedThumb, int progress) {
                // Nothing to do
            }
        });
    }

    @Override
    public Integer deviceImageSectionLayout() {
        return R.layout.thermostat_image_section;
    }

    @Override
    public void doDeviceImageSection() {
        seekArc = (DeviceSeekArc) deviceImageView.findViewById(R.id.seekArc);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) seekArc.getLayoutParams();
        params.width = (int) (getActivity().getResources().getDisplayMetrics().widthPixels * 0.8);
        seekArc.setLayoutParams(params);

        centerTempTextView = (TextView) deviceImageView.findViewById(R.id.thermostat_center_status_temp);

        deviceImage = (GlowableImageView) deviceImageView.findViewById(R.id.fragment_device_info_image);
        deviceImage.setImageDrawable(getResources().getDrawable(R.drawable.empty_large_circle_size));
        deviceImage.setGlowMode(GlowableImageView.GlowMode.ON_OFF);
        deviceImage.setGlowing(false);
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.thermostat_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        setControlPoints();
    }

    @Override public void onPause() {
        super.onPause();
        alreadySetSelectedAndUnselectedResources = false; // So we redraw when we come back...
    }

    protected void setControlPoints() {
        /**
         * FIXME: 2/12/16
         * If the device is offline, this seems to get called always before the doDeviceImageSection
         * which causes us to crash because we are trying to set HVAC modes/temps etc on the seek arc which was not init'ed
         *
         * For online devices the calls go:
         * ... doDeviceImageSection() -> onShowedFragment() ...
         * For offline devices the calls go:
         * ... onShowedFragment() -> Crash (seekArc != initialized)
         * FIXME: 2/12/16 - This was only designed to be a temp fix before release and would like to understand why this happens.
         */
        if (seekArc == null || getDeviceModel() == null) {
            return;
        }

        // Initialize the thermostat mode
        setUiHvacMode(getPlatformHvacMode()); // Method (currently) guards against null values

        // Initialize the cool set point
        setUiCoolSetPoint(Math.max(getPlatformCoolsetpoint(), MIN_COOL_TEMP_VAL));

        // Set current heat point
        setUiHeatSetPoint(Math.min(getPlatformHeatsetpoint(), MAX_HEAT_TEMP_VAL));

        // Set current temperature value
        Integer temperature = getPlatformTemperature();
        if (temperature < 0) {
            logger.error("Cannot set center temperature; device model or platform value is null.");
        } else {
            centerTempTextView.setText(String.format("%d", temperature));
        }

        // setUiSetPointText(mode); this is invoked by setUiHvacMode(mode) above IF mode != null
        updateUiHvacActive();
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        if (event.getNewValue() != null && event.getOldValue() != null) {
            if (event.getOldValue().equals(event.getNewValue())) {
                return; // Don't process non updates.
            }
        }

        Handler handler =  new Handler(Looper.getMainLooper());

        switch (event.getPropertyName()) {
            case Thermostat.ATTR_COOLSETPOINT:
            case Thermostat.ATTR_HEATSETPOINT:
                // Update set points only after user has stopped interacting with UI
                setpointThrottle.executeAfterQuiescence(new Runnable() {
                    @Override
                    public void run() {
                        seekArc.setProgress(DeviceSeekArc.THUMB_HIGH, getPlatformCoolsetpoint());
                        seekArc.setProgress(DeviceSeekArc.THUMB_LOW, getPlatformHeatsetpoint());
                        setUiSetPointText(getPlatformHvacMode());
                        resetWaitTimeoutCheck();
                    }
                }, QUIESCENT_MS);
                break;

            case Thermostat.ATTR_FANMODE:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        updateUiHvacActive();
                    }
                });
                break;

            case Thermostat.ATTR_HVACMODE:
                // Update mode settings only after user has stopped interacting with UI
                modeThrottle.executeAfterQuiescence(new Runnable() {
                    @Override
                    public void run() {
                        resetWaitTimeoutCheck();
                        setControlPoints();
                    }
                }, QUIESCENT_MS);
                break;

            case Temperature.ATTR_TEMPERATURE:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        centerTempTextView.setText(String.format("%d", getPlatformTemperature()));
                    }
                });
                break;

            default:
                logger.debug("Received Thermostat update: {} -> {}", event.getPropertyName(), event.getNewValue());
                super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.thermostat_minus_btn:
                decrementActiveProgress();
                break;
            case R.id.thermostat_plus_btn:
                incrementActiveProgress();
                break;
            case R.id.thermostat_mode_btn:
                showHvacModeSelectionDialog();
                break;
        }
    }

    protected void showHvacModeSelectionDialog() {
        ArrayList<String> buttons = new ArrayList<>();
        buttons.add(getString(R.string.hvac_cool));
        buttons.add(getString(R.string.hvac_heat));
        buttons.add(getString(R.string.hvac_auto));
        buttons.add(getString(R.string.hvac_off));

        MultiButtonPopup popup = MultiButtonPopup.newInstance(getString(R.string.hvac_mode_selection), buttons);
        popup.setOnButtonClickedListener(new MultiButtonPopup.OnButtonClickedListener() {
            @Override
            public void onButtonClicked(String buttonValue) {
                if (buttonValue.equals(getString(R.string.hvac_heat))) {
                    setUiHvacMode(Thermostat.HVACMODE_HEAT);
                    requestUpdateHvacMode(Thermostat.HVACMODE_HEAT);
                }
                else if (buttonValue.equals(getString(R.string.hvac_cool))) {
                    setUiHvacMode(Thermostat.HVACMODE_COOL);
                    requestUpdateHvacMode(Thermostat.HVACMODE_COOL);
                }
                else if (buttonValue.equals(getString(R.string.hvac_auto))) {
                    setUiHvacMode(Thermostat.HVACMODE_AUTO);
                    requestUpdateHvacMode(Thermostat.HVACMODE_AUTO);
                    if (seekArc.getSelectedProgress() == DeviceSeekArc.THUMB_LOW) {
                        setUiActiveThumb(Thermostat.HVACMODE_HEAT);
                    } else if (seekArc.getSelectedProgress() == DeviceSeekArc.THUMB_HIGH) {
                        setUiActiveThumb(Thermostat.HVACMODE_COOL);
                    }
                }
                else {
                    setUiHvacMode(Thermostat.HVACMODE_OFF);
                    requestUpdateHvacMode(Thermostat.HVACMODE_OFF);
                }
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    protected void setUiHvacMode(@Nullable String mode) {
        if (mode == null) {
            logger.error("Ignoring request to set UI HVAC mode to null. Something ain't right!");
            return;
        }

        plusButton.setEnabled(Thermostat.HVACMODE_OFF != mode);
        minusButton.setEnabled(Thermostat.HVACMODE_OFF != mode);

        switch (String.valueOf(mode)) {
            case Thermostat.HVACMODE_AUTO:
                enableUiThermostat();
                tempTopText.setText(getString(R.string.hvac_auto));
                break;
            case Thermostat.HVACMODE_COOL:
                setUiModeCool();
                setUiActiveThumb(mode);
                tempTopText.setText(getString(R.string.hvac_cool));
                break;
            case Thermostat.HVACMODE_HEAT:
                setUiModeHeat();
                setUiActiveThumb(mode);
                tempTopText.setText(getString(R.string.hvac_heat));
                break;

            case Thermostat.HVACMODE_OFF:
            default:
                disableUiThermostat();
                tempTopText.setText(getString(R.string.hvac_off));
                logger.info("Setting UI to render to OFF. Thermostat mode parsed was: {}", mode);
                break;
        }

        setUiSetPointText(mode);
    }

    protected void disableUiThermostat() {
        seekArc.setVisibility(View.GONE);
        deviceImage.setVisibility(View.VISIBLE);
        changeVisibility(R.id.thermostat_center_cool_icon, View.INVISIBLE);
        changeVisibility(R.id.thermostat_center_heat_icon, View.INVISIBLE);
    }

    protected void enableUiThermostat() {
        seekArc.setVisibility(View.VISIBLE);
        deviceImage.setVisibility(View.GONE);
        changeVisibility(R.id.thermostat_center_cool_icon, View.VISIBLE);
        changeVisibility(R.id.thermostat_center_heat_icon, View.VISIBLE);
    }

    protected void setUiModeHeat() {
        seekArc.setVisibility(View.GONE);
        deviceImage.setVisibility(View.VISIBLE);
        changeVisibility(R.id.thermostat_center_cool_icon, View.GONE);
        changeVisibility(R.id.thermostat_center_heat_icon, View.VISIBLE);
    }

    protected void setUiModeCool() {
        seekArc.setVisibility(View.GONE);
        deviceImage.setVisibility(View.VISIBLE);
        changeVisibility(R.id.thermostat_center_heat_icon, View.GONE);
        changeVisibility(R.id.thermostat_center_cool_icon, View.VISIBLE);
    }

    protected void setUiCoolSetPoint(int temperatureF) {
        seekArc.setProgress(DeviceSeekArc.THUMB_HIGH, temperatureF);
    }

    protected void setUiHeatSetPoint(int temperatureF) {
        seekArc.setProgress(DeviceSeekArc.THUMB_LOW, temperatureF);
    }

    protected void setUiSetPointText(String mode) {
        if (mode == null) {
            logger.error("Ignoring request to update UI set point text for null mode.");
            return;
        }

        switch (mode) {
            case Thermostat.HVACMODE_COOL:
                setUiSetPointText(mode, getPlatformCoolsetpoint());
                break;
            case Thermostat.HVACMODE_HEAT:
                setUiSetPointText(mode, getPlatformHeatsetpoint());
                break;
            case Thermostat.HVACMODE_OFF:
                setUiSetPointText(mode, 0);
                break;
            case Thermostat.HVACMODE_AUTO:
                setUiSetPointText(mode, seekArc.getSelectedProgress() == DeviceSeekArc.THUMB_LOW ? getPlatformHeatsetpoint() : getPlatformCoolsetpoint());
                break;

            default:
                logger.error("Bug! Unimplemented HVAC mode: {}.", mode);
        }
    }

    protected void setUiSetPointText(String mode, int setpoint) {
        if (mode != null) {
            if (Thermostat.HVACMODE_OFF.equals(mode))
                tempBottomText.setText("--");
            else {
                tempBottomText.setText(String.format("%dº", setpoint));
            }
        } else {
            logger.error("Ignoring request to update UI set point text for null mode.");
        }
        updateUiHvacActive();
    }

    protected void updateUiHvacActive() {
        if (getDeviceModel() != null) {
            if(getDeviceModel().get(Thermostat.ATTR_FANMODE) == null) {
                changeVisibility(R.id.thermostat_center_wave_icon, View.GONE);
            }
            else {
                if (1 == ((Number)getDeviceModel().get(Thermostat.ATTR_FANMODE)).intValue()) {
                    changeVisibility(R.id.thermostat_center_wave_icon, View.VISIBLE);
                } else {
                    changeVisibility(R.id.thermostat_center_wave_icon, View.GONE);
                }
            }
        } else {
            logger.error("Ignoring request to update HVAC activity on null device model.");
        }
    }

    protected int getPlatformCoolsetpoint() {
        Thermostat device = getCapability(Thermostat.class);
        if (device != null && device.getCoolsetpoint() != null)
            return TemperatureUtils.roundCelsiusToFahrenheit(device.getCoolsetpoint());

        logger.error("Cannot determine platform cool set point; device model is null.");
        return -1;
    }

    protected void setPlatformSetPoints(double heat, double cool) {
        Thermostat device = getCapability(Thermostat.class);

        if (device != null) {
            device.setHeatsetpoint(TemperatureUtils.fahrenheitToCelsius(heat));
            device.setCoolsetpoint(TemperatureUtils.fahrenheitToCelsius(cool));
            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Error updating thermostat set points.", throwable);
                }
            });
        } else {
            logger.error("Ignoring request to set platform heat set point on null device model.");
        }
    }

    protected int getPlatformHeatsetpoint() {
        Thermostat device = getCapability(Thermostat.class);
        if (device != null && device.getHeatsetpoint() != null)
            return TemperatureUtils.roundCelsiusToFahrenheit(device.getHeatsetpoint());

        logger.error("Cannot determine platform heat set point; device model is null.");
        return -1;
    }

    protected void setPlatformHvacMode(String mode) {
        Thermostat device = getCapability(Thermostat.class);

        if (device != null) {
            device.setHvacmode(mode);
            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Error updating thermostat mode.", throwable);
                }
            });
        } else {
            logger.error("Ignoring request to set HVAC mode on null device model.");
        }
    }

    protected String getPlatformHvacMode() {
        Thermostat device = getCapability(Thermostat.class);
        if (device != null)
            return device.getHvacmode();

        logger.error("Cannot determine platform HVAC mode on null device model.");
        return null;
    }

    protected void setUiActiveThumb(@NonNull String mode) {
        if (Thermostat.HVACMODE_COOL.equals(mode)) {
            seekArc.setActiveProgress(DeviceSeekArc.THUMB_HIGH);
        } else if (Thermostat.HVACMODE_HEAT.equals(mode)) {
            seekArc.setActiveProgress(DeviceSeekArc.THUMB_LOW);
        }
    }

    private void incrementActiveProgress() {
        seekArc.incrementActiveProgress();
        requestUpdatePlatformSetPoints();
    }

    private void decrementActiveProgress() {
        seekArc.decrementActiveProgress();
        requestUpdatePlatformSetPoints();
    }

    protected void requestUpdateHvacMode (final String mode) {
        modeThrottle.execute(new Runnable() {
            @Override
            public void run() {
                setPlatformHvacMode(mode);
            }
        });
    }

    protected void requestUpdatePlatformSetPoints() {
        final int heatProgress = seekArc.getProgress(DeviceSeekArc.THUMB_LOW);
        final int coolProgress = seekArc.getProgress(DeviceSeekArc.THUMB_HIGH);

        setpointThrottle.execute(new Runnable() {
            @Override
            public void run() {
                setPlatformSetPoints(heatProgress, coolProgress);
            }
        });
    }

    protected int getPlatformTemperature() {
        Temperature device = getCapability(Temperature.class);
        if (device != null && device.getTemperature() != null) {
            return TemperatureUtils.roundCelsiusToFahrenheit(device.getTemperature());
        }

        logger.error("Cannot determine platform temperature on null device model.");
        return -1;
    }

    protected void updateScheduleText(boolean show) {
        if(mScheduleModel.getNextEvent().trim().equals("") || !show) {
            nextEventLabel.setVisibility(View.INVISIBLE);
            nextEventDescription.setVisibility(View.INVISIBLE);
        }
        else {
            nextEventDescription.setText(mScheduleModel.getNextEvent());
            nextEventLabel.setVisibility(View.VISIBLE);
            nextEventDescription.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void showScheduleDisabled(ScheduleModel model) {
        mScheduleModel = model;
        updateScheduleText(false);
    }

    @Override
    public void showScheduleOff(ScheduleModel model) {
        mScheduleModel = model;
        updateScheduleText(false);
    }

    @Override
    public void showSchedule(ScheduleModel model) {
        mScheduleModel = model;
        updateScheduleText(true);
    }

    @Override
    public void showSelectedDay(ScheduledDay model) {
        //Nothing to see here
    }

    @Override
    public void showIfDaysHaveSchedules(Map<DayOfWeek,ScheduledDay> weekScheduledDayMap){
        //Nothing to see here
    }

    @Override
    public void onError(ErrorModel error) {
    }

    protected void changeVisibility(@IdRes int id, @ViewVisibility int visibility) {
        try {
            View root = getView();
            if (root != null) {
                root.findViewById(id).setVisibility(visibility);
            }
        }
        catch (Exception ex) {
            logger.debug("Could not change visibility on view [{}] to [{}]", id, visibility);
        }
    }

    protected void resetWaitTimeoutCheck() {
    }
}
