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
package arcus.app.device.details.presenters;

import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import arcus.cornea.common.BasePresenter;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.climate.ScheduleViewController;
import arcus.cornea.subsystem.climate.model.ScheduleModel;
import arcus.cornea.subsystem.climate.model.ScheduledDay;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.Capability;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.RelativeHumidity;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.Thermostat;
import com.iris.client.model.DeviceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.banners.core.Banner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.Range;
import arcus.app.common.utils.ThrottledExecutor;
import arcus.app.device.details.model.ThermostatDisplayModel;
import arcus.app.device.details.model.ThermostatOperatingMode;
import arcus.app.device.model.DeviceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public abstract class BaseThermostatPresenter extends BasePresenter<ThermostatPresenterContract.ThermostatControlView> implements
        ThermostatPresenterContract.ThermostatPresenter,
        ScheduleViewController.Callback,
        PropertyChangeListener
{
    protected final static Logger logger = LoggerFactory.getLogger(BaseThermostatPresenter.class);
    private final static int THROTTLE_PERIOD_MS = 5000;

    private final static int QUIESCENT_PERIOD_MS = 10000;

    private final static int DFLT_MIN_SEPARATION_F = 3;
    private final static int DFLT_MIN_HEAT_TEMP_F = 45;
    private final static int DFLT_MIN_COOL_TEMP_F = 48;
    private final static int DFLT_MAX_HEAT_TEMP_F = 92;
    private final static int DFLT_MAX_COOL_TEMP_F = 95;

    abstract boolean isCloudConnected();
    abstract Range<Integer> getRestrictedSetpointRange();
    abstract boolean isLeafEnabled();
    abstract boolean isControlDisabled();

    private boolean isCoolActiveThumb;
    private Set<Class> presentedBanners = new HashSet<>();

    protected ThrottledExecutor throttle = new ThrottledExecutor(THROTTLE_PERIOD_MS);

    @Override
    public void startPresenting(ThermostatPresenterContract.ThermostatControlView presentedView) {
        super.startPresenting(presentedView);

        // Make us a listener of device model changes
        addListener(Thermostat.class.getSimpleName(), getDeviceModel().addPropertyChangeListener(this));
        ScheduleViewController.instance().select(getDeviceModel().getAddress(), this, DayOfWeek.MONDAY);
    }

    @Override
    public void selectMode() {
        getPresentedView().onDisplayModeSelection(getSupportedOperatingModes(get(Thermostat.class)), useNestTerminology());
    }

    @Override
    public void setSetpoint(int value) {
        if (getOperatingMode() == ThermostatOperatingMode.COOL) {
            setCoolSetpointF(value);
        } else {
            setHeatSetpointF(value);
        }
    }

    @Override
    public void toggleActiveSetpoint() {
        this.isCoolActiveThumb = !this.isCoolActiveThumb;
        updateView();
    }

    @Override
    public void incrementSetpoint() {
        if (isCoolSetpointActive()) {
            setCoolSetpointF(getCoolSetpointF() + 1);
        } else if (isHeatSetpointActive()) {
            setHeatSetpointF(getHeatSetpointF() + 1);
        }
    }

    @Override
    public void decrementSetpoint() {
        if (isCoolSetpointActive()) {
            setCoolSetpointF(getCoolSetpointF() - 1);
        } else if (isHeatSetpointActive()) {
            setHeatSetpointF(getHeatSetpointF() - 1);
        }
    }

    @Override
    public void setOperatingMode(ThermostatOperatingMode mode) {
        get(Thermostat.class).setHvacmode(mode.getPlatformName());
        commitThrottled();
    }

    @Override
    public void notifyAdjustmentInProgress() {
        throttle.delayExecution();
    }

    @Override
    public void showScheduleDisabled(ScheduleModel model) {
        updateScheduleText(null);
    }

    @Override
    public void showScheduleOff(ScheduleModel model) {
        updateScheduleText(null);
    }

    @Override
    public void showSchedule(ScheduleModel model) {
        updateScheduleText(model.getNextEvent());
    }

    @Override
    public void showSelectedDay(ScheduledDay model) {
        // Nothing to do
    }

    @Override
    public void showIfDaysHaveSchedules(Map<DayOfWeek,ScheduledDay> weekScheduledDayMap){
        // Nothing to do
    }

    @Override
    public void onError(ErrorModel error) {
        // Nothing to do
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        updateView();
    }

    protected ThermostatOperatingMode getOperatingMode() {
        if (getDeviceModel() == null || get(Thermostat.class).getHvacmode() == null) {
            return ThermostatOperatingMode.OFF;
        }

        return ThermostatOperatingMode.fromPlatformValue(get(Thermostat.class).getHvacmode());
    }

    @Override
    public void setSetpointRange(int coolSetpointF, int heatSetpointF) {
        boolean dirty = false;

        // If the Heat setpoint has changed, verify requested heat setpoint is in allowable range
        if (getHeatSetpointF() != heatSetpointF) {
            if (heatSetpointF > getMaxAllowableSetpointForCurrentOperatingMode()) {
                heatSetpointF = getMaxAllowableSetpointForCurrentOperatingMode();
            } else if (heatSetpointF < getMinAllowableSetpointForCurrentOperatingMode()) {
                heatSetpointF = getMinAllowableSetpointForCurrentOperatingMode();
            }
        }

        // If the Cool setpoint has changed, verify requested cool setpoint is in allowable range
        if (getCoolSetpointF() != coolSetpointF) {
            if (coolSetpointF > getMaxAllowableSetpointForCurrentOperatingMode()) {
                coolSetpointF = getMaxAllowableSetpointForCurrentOperatingMode();
            } else if (coolSetpointF < getMinAllowableSetpointForCurrentOperatingMode()) {
                coolSetpointF = getMinAllowableSetpointForCurrentOperatingMode();
            }
        }

        // Only set dirty attributes (logic required for Honeywell thermostats)
        if (getHeatSetpointF() != heatSetpointF) {
            get(Thermostat.class).setHeatsetpoint(TemperatureUtils.fahrenheitToCelsius(heatSetpointF));
            dirty = true;
        }
        if (getCoolSetpointF() != coolSetpointF) {
            get(Thermostat.class).setCoolsetpoint(TemperatureUtils.fahrenheitToCelsius(coolSetpointF));
            dirty = true;
        }

        if (dirty) {
            commitThrottled();
        }
    }

    private void setHeatSetpointF(double heatSetpointF) {
        int coolSetpoint = (int) Math.round(getCoolSetpointF());

        // Check for minimum separation
        if (getOperatingMode() == ThermostatOperatingMode.AUTO && (heatSetpointF >= coolSetpoint - getMinimumSetpointSeparationF(get(Thermostat.class)))) {
            coolSetpoint = (int) Math.round(heatSetpointF) + getMinimumSetpointSeparationF(get(Thermostat.class));

            // Don't set anything if doing so would violate min/max setpoints
            if (coolSetpoint > getMaxAllowableSetpointForCurrentOperatingMode() || coolSetpoint < getMinAllowableSetpointForCurrentOperatingMode()) {
                return;
            }
        }

        setSetpointRange(coolSetpoint, (int) Math.round(heatSetpointF));
    }

    private void setCoolSetpointF(double coolSetpointF) {
        int heatSetpoint = (int) Math.round(getHeatSetpointF());

        // Check for minimum separation
        if (getOperatingMode() == ThermostatOperatingMode.AUTO && (coolSetpointF <= heatSetpoint + getMinimumSetpointSeparationF(get(Thermostat.class)))) {
            heatSetpoint = (int) Math.round(coolSetpointF) - getMinimumSetpointSeparationF(get(Thermostat.class));

            // Don't set anything if doing so would violate min/max setpoints
            if (heatSetpoint > getMaxAllowableSetpointForCurrentOperatingMode() || heatSetpoint < getMinAllowableSetpointForCurrentOperatingMode()) {
                return;
            }
        }

        setSetpointRange((int) Math.round(coolSetpointF), heatSetpoint);
    }

    protected double getHeatSetpointF() {
        Double heatSetpointC = get(Thermostat.class).getHeatsetpoint();
        return heatSetpointC == null ? getMinSetpointForCurrentOperatingMode() : TemperatureUtils.roundCelsiusToFahrenheit(heatSetpointC);
    }

    protected double getCoolSetpointF() {
        Double coolSetpointC = get(Thermostat.class).getCoolsetpoint();
        return coolSetpointC == null ? getMaxSetpointForCurrentOperatingMode() : TemperatureUtils.roundCelsiusToFahrenheit(coolSetpointC);
    }

    protected void commitThrottled() {

        // If throttle period has changed, create a new ThrottledExecutor with the new setting
        if (throttle.getThrottlePeriodMs() != getThrottlePeriodMs()) {
            throttle = new ThrottledExecutor(getThrottlePeriodMs());
        }

        throttle.execute(new Runnable() {
            @Override
            public void run() {
                if (isPresenting()) {
                    logger.debug("Committing model changes to platform.");
                    getDeviceModel().commit();
                }
            }
        });

        throttle.executeAfterQuiescence(new Runnable() {
            @Override
            public void run() {
                if (isPresenting()) {
                    logger.debug("Quiescence reached; syncing view to platform.");
                    updateView();
                }
            }
        }, getQuiescentPeriodMs());
    }

    @Override
    public void updateView() {
        updateView(buildDisplayModel());
    }

    protected void updateView(final ThermostatDisplayModel model) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isPresenting()) {
                    logger.debug("Presenting thermostat model: {}", model);
                    getPresentedView().updateView(model);
                }
            }
        });
    }

    protected int getThrottlePeriodMs() {
        return THROTTLE_PERIOD_MS;
    }

    protected int getQuiescentPeriodMs() {
        return QUIESCENT_PERIOD_MS;
    }

    private void updateScheduleText(final String eventText) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isPresenting()) {
                    getPresentedView().onShowScheduleEvent(eventText == null ? null : eventText.toUpperCase());
                }
            }
        });
    }

    private ThermostatDisplayModel buildDisplayModel() {
        ThermostatDisplayModel model = new ThermostatDisplayModel();
        Double temp = get(Temperature.class).getTemperature();
        int currentTemp;
        if (temp == null) {
            currentTemp = 99;
        } else {
            currentTemp = TemperatureUtils.roundCelsiusToFahrenheit(temp);
        }

        model.setCoolSetpoint((int) getCoolSetpointF());
        model.setHeatSetpoint((int) getHeatSetpointF());
        model.setCurrentTemperature(currentTemp);
        model.setMaxSetpointStopValue(getRestrictedSetpointRange().getUpper());
        model.setMinSetpointStopValue(getRestrictedSetpointRange().getLower());
        model.setMinSetpoint(getMinSetpointForCurrentOperatingMode());
        model.setMaxSetpoint(getMaxSetpointForCurrentOperatingMode());
        model.setOperatingMode(getOperatingMode());
        model.setRelativeHumidity(getRelativeHumidity());
        model.setMinSetpointSeparation(getMinimumSetpointSeparationF(get(Thermostat.class)));
        model.setLeafEnabled(isLeafEnabled());
        model.setRunning(Thermostat.ACTIVE_RUNNING.equals(get(Thermostat.class).getActive()));
        model.setSetpointsText(getSetpointsText(model));
        model.setCloudConnected(isCloudConnected());
        model.setControlDisabled(isControlDisabled());
        model.setUseNestTerminology(useNestTerminology());

        return model;
    }

    private boolean useNestTerminology() {
        if (getDeviceModel() == null || get(DeviceAdvanced.class).getDevtypehint() == null) {
            return false;
        }

        return DeviceType.fromHint(get(DeviceAdvanced.class).getDevtypehint()) == DeviceType.NEST_THERMOSTAT;
    }

    private CharSequence getSetpointsText(ThermostatDisplayModel model) {
        switch (model.getOperatingMode()) {
            case HEAT:
                return ArcusApplication.getContext().getString(R.string.temperature_degrees, model.getHeatSetpoint());
            case COOL:
                return ArcusApplication.getContext().getString(R.string.temperature_degrees, model.getCoolSetpoint());
            case OFF:
                return ArcusApplication.getContext().getString(R.string.hvac_off);
            case ECO:
                return ArcusApplication.getContext().getString(R.string.hvac_eco);
            case AUTO:
                String rangeString = ArcusApplication.getContext().getString(R.string.temperature_range_degrees, model.getHeatSetpoint(), model.getCoolSetpoint());
                int bulletLoc = rangeString.indexOf(ArcusApplication.getContext().getString(R.string.bullet));
                Spannable s = new SpannableString(rangeString);
                int start = isCoolActiveThumb ? 0 : bulletLoc;
                int end = isCoolActiveThumb ? bulletLoc : rangeString.length();
                s.setSpan(new ForegroundColorSpan(0x80FFFFFF), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                return s;
            default:
                throw new IllegalArgumentException("Bug! Unimplemented operating mode case.");
        }
    }

    private int getMinAllowableSetpointForCurrentOperatingMode() {
        Integer minRestricted = getRestrictedSetpointRange().getLower();
        int minSetpoint = getMinSetpointForCurrentOperatingMode();

        return minRestricted == null ? minSetpoint : Math.max(minRestricted, minSetpoint);
    }

    private int getMaxAllowableSetpointForCurrentOperatingMode() {
        Integer maxRestricted = getRestrictedSetpointRange().getUpper();
        int maxSetpoint = getMaxSetpointForCurrentOperatingMode();

        return maxRestricted == null ? maxSetpoint : Math.min(maxRestricted, maxSetpoint);
    }

    private int getMinSetpointForCurrentOperatingMode() {
        return BaseThermostatPresenter.getMinimumSetpointF(get(Thermostat.class));
    }

    private int getMaxSetpointForCurrentOperatingMode() {
        return BaseThermostatPresenter.getMaximumSetpointF(get(Thermostat.class));
    }

    private Integer getRelativeHumidity() {
        try {
            return (int) Math.round(get(RelativeHumidity.class).getHumidity());
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isHeatSetpointActive() {
        return getOperatingMode() == ThermostatOperatingMode.HEAT || !isCoolActiveThumb;
    }

    private boolean isCoolSetpointActive() {
        return getOperatingMode() == ThermostatOperatingMode.COOL || isCoolActiveThumb;
    }

    protected boolean isDeviceConnected() {
        if (getDeviceModel() == null) {
            return false;
        }

        return DeviceConnection.STATE_ONLINE.equals(get(DeviceConnection.class).getState());
    }

    protected DeviceModel getDeviceModel() {
        return getPresentedView().getThermostatDeviceModel();
    }

    protected <C extends Capability> C get(Class<C> clazz) {
        C cap = CorneaUtils.getCapability(getDeviceModel(), clazz);

        if (cap == null) {
            throw new IllegalStateException("Bug! This thermostat does not support the capability: " + clazz.getSimpleName());
        }

        return cap;
    }

    public static List<ThermostatOperatingMode> getSupportedOperatingModes(Thermostat deviceModel) {
        List<ThermostatOperatingMode> displayedModes = new ArrayList<>();
        Set<String> supportedModes = deviceModel.getSupportedmodes();
        Boolean supportsAuto = deviceModel.getSupportsAuto();

        if (supportedModes == null || supportedModes.isEmpty()) {
            displayedModes = getDefaultOperatingModes(supportsAuto == null || supportsAuto);
        } else {
            for (String thisMode : supportedModes) {
                displayedModes.add(ThermostatOperatingMode.fromPlatformValue(thisMode));
            }
        }

        Collections.sort(displayedModes, ThermostatOperatingMode.getThermostatOperatingModeComparator());
        return displayedModes;
    }

    public static List<ThermostatOperatingMode> getDefaultOperatingModes(boolean includeAuto) {
        if (includeAuto) {
            return Arrays.asList(
                    ThermostatOperatingMode.COOL,
                    ThermostatOperatingMode.HEAT,
                    ThermostatOperatingMode.AUTO,
                    ThermostatOperatingMode.OFF);
        } else {
            return Arrays.asList(
                    ThermostatOperatingMode.COOL,
                    ThermostatOperatingMode.HEAT,
                    ThermostatOperatingMode.OFF);
        }
    }

    public static int getMinimumSetpointF(Thermostat deviceModel) {
        if (deviceModel == null || deviceModel.getMinsetpoint() == null) {
            return DFLT_MIN_COOL_TEMP_F;
        }

        Double platformMin = deviceModel.getMinsetpoint();

        if (platformMin != null) {
            return TemperatureUtils.roundCelsiusToFahrenheit(platformMin);
        } else {
            return getDefaultMinimumSetpointF(deviceModel);
        }
    }

    public static int getMaximumSetpointF(Thermostat deviceModel) {
        if (deviceModel == null || deviceModel.getMaxsetpoint() == null) {
            return DFLT_MIN_COOL_TEMP_F;
        }

        Double platformMax = deviceModel.getMaxsetpoint();

        if (platformMax != null) {
            return TemperatureUtils.roundCelsiusToFahrenheit(platformMax);
        } else {
            return getDefaultMaximumSetpointF(deviceModel);
        }
    }

    public static int getMinimumSetpointSeparationF(Thermostat deviceModel) {
        if (deviceModel == null || deviceModel.getSetpointseparation() == null) {
            return DFLT_MIN_SEPARATION_F;
        }

        Double platformSeparation = deviceModel.getSetpointseparation();

        if (platformSeparation != null) {
            return TemperatureUtils.roundCelsiusDeltaToFahrenheit(platformSeparation);
        } else {
            return DFLT_MIN_SEPARATION_F;
        }
    }

    private static int getDefaultMinimumSetpointF(Thermostat deviceModel) {
        if (deviceModel == null ||
                deviceModel.getHvacmode() == null ||
                ThermostatOperatingMode.fromPlatformValue(deviceModel.getHvacmode()) == ThermostatOperatingMode.HEAT)
        {
            return DFLT_MIN_HEAT_TEMP_F;
        } else {
            return DFLT_MIN_COOL_TEMP_F;
        }
    }

    private static int getDefaultMaximumSetpointF(Thermostat deviceModel) {
        if (deviceModel == null ||
                deviceModel.getHvacmode() == null ||
                ThermostatOperatingMode.fromPlatformValue(deviceModel.getHvacmode()) == ThermostatOperatingMode.COOL)
        {
            return DFLT_MAX_COOL_TEMP_F;
        } else {
            return DFLT_MAX_HEAT_TEMP_F;
        }
    }

}
