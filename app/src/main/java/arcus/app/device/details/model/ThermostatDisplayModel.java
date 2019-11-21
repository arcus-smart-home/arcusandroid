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
package arcus.app.device.details.model;

import androidx.annotation.NonNull;


public class ThermostatDisplayModel {

    /**
     * When non-null, the humidity value to be displayed. Null indicates that the thermostat is
     * incapable of reporting humidity.
     */
    private Integer relativeHumidity;

    /**
     * The current operating mode of the thermostat
     */
    private
    @NonNull
    ThermostatOperatingMode operatingMode;

    /**
     * The current temperature reported by the thermostat
     */
    private int currentTemperature;

    /**
     * When non-null, the current cool setpoint. Null indicates that the current operating mode
     * does not support a cool setpoint.
     */
    private Integer coolSetpoint;

    /**
     * When non-null, the current heat setpoint. Null indicates that the current operating mode
     * does not support a heat setpoint.
     */
    private Integer heatSetpoint;

    /**
     * Minimum allowable setpoint value allowed by the thermostat / HVAC system.
     */
    private int minSetpoint;

    /**
     * Maximum allowable setpoint value allowed by the thermostat / HVAC system.
     */
    private int maxSetpoint;

    /**
     * Minimum allowable separation (difference) between the cool setpoint and the heat setpoint
     * (used to assure the AC and furnace aren't running simultaneously in competition with each
     * other)
     */
    private int minSetpointSeparation;

    /**
     * When non-null, a minimum allowable setpoint value that may be greater than
     * {@link #minSetpoint}. A stop value models a "temperature lock", that is, user definable
     * min/max values that are more restrictive than the what the device/HVAC system
     * limits ({@link #minSetpoint} or {@link #maxSetpoint}).
     */
    private Integer minSetpointStopValue;

    /**
     * When non-null, a maximum allowable setpoint value that may be less than
     * {@link #maxSetpoint}. A stop value models a "temperature lock", that is, user definable
     * min/max values that are more restrictive than the what the device/HVAC system
     * limits ({@link #minSetpoint} or {@link #maxSetpoint}).
     */
    private Integer maxSetpointStopValue;

    /**
     * When true, the thermostat's settings indicate that it's operating in an mode that is "eco
     * friendly" (i.e., AC isn't on too much)
     */
    private boolean leafEnabled;

    /**
     * When true the heat/AC is actively running.
     */
    private boolean isRunning;

    /**
     * The text to be displayed inside the center circle. Typically the current heat or cool setpoint.
     */
    private CharSequence setpointsText;

    /**
     * Indicates whether control of this device is disabled (generally as a result of a connection error)
     */
    private boolean isControlDisabled;

    private boolean isCloudConnected;

    private boolean useNestTerminology;

    public boolean isCoolRunning() {
        return (getOperatingMode() == ThermostatOperatingMode.COOL || getOperatingMode() == ThermostatOperatingMode.AUTO) && getCurrentTemperature() > getCoolSetpoint();
    }

    public boolean isHeatRunning() {
        return (getOperatingMode() == ThermostatOperatingMode.HEAT || getOperatingMode() == ThermostatOperatingMode.AUTO) && getCurrentTemperature() < getHeatSetpoint();
    }

    public boolean isUseNestTerminology() {
        return useNestTerminology;
    }

    public void setUseNestTerminology(boolean useNestTerminology) {
        this.useNestTerminology = useNestTerminology;
    }

    public boolean isControlDisabled() {
        return isControlDisabled;
    }

    public void setControlDisabled(boolean controlDisabled) {
        isControlDisabled = controlDisabled;
    }

    public Integer getRelativeHumidity() {
        return relativeHumidity;
    }

    public void setRelativeHumidity(Integer relativeHumidity) {
        this.relativeHumidity = relativeHumidity;
    }

    @NonNull
    public ThermostatOperatingMode getOperatingMode() {
        return operatingMode;
    }

    public void setOperatingMode(@NonNull ThermostatOperatingMode operatingMode) {
        this.operatingMode = operatingMode;
    }

    public int getCurrentTemperature() {
        return currentTemperature;
    }

    public void setCurrentTemperature(int currentTemperature) {
        this.currentTemperature = currentTemperature;
    }

    public Integer getCoolSetpoint() {
        return coolSetpoint;
    }

    public void setCoolSetpoint(Integer coolSetpoint) {
        this.coolSetpoint = coolSetpoint;
    }

    public Integer getHeatSetpoint() {
        return heatSetpoint;
    }

    public void setHeatSetpoint(Integer heatSetpoint) {
        this.heatSetpoint = heatSetpoint;
    }

    public int getMinSetpoint() {
        return minSetpoint;
    }

    public void setMinSetpoint(int minSetpoint) {
        this.minSetpoint = minSetpoint;
    }

    public int getMaxSetpoint() {
        return maxSetpoint;
    }

    public void setMaxSetpoint(int maxSetpoint) {
        this.maxSetpoint = maxSetpoint;
    }

    public Integer getMinSetpointStopValue() {
        return minSetpointStopValue;
    }

    public void setMinSetpointStopValue(Integer minSetpointStopValue) {
        this.minSetpointStopValue = minSetpointStopValue;
    }

    public Integer getMaxSetpointStopValue() {
        return maxSetpointStopValue;
    }

    public void setMaxSetpointStopValue(Integer maxSetpointStopValue) {
        this.maxSetpointStopValue = maxSetpointStopValue;
    }

    public int getMinSetpointSeparation() {
        return minSetpointSeparation;
    }

    public void setMinSetpointSeparation(int minSetpointSeparation) {
        this.minSetpointSeparation = minSetpointSeparation;
    }

    public boolean hasLeaf() {
        return leafEnabled;
    }

    public void setLeafEnabled(boolean leafEnabled) {
        this.leafEnabled = leafEnabled;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public CharSequence getSetpointsText() {
        return setpointsText;
    }

    public void setSetpointsText(CharSequence setpointsText) {
        this.setpointsText = setpointsText;
    }

    public boolean isCloudConnected() {
        return isCloudConnected;
    }

    public void setCloudConnected(boolean cloudConnected) {
        isCloudConnected = cloudConnected;
    }

    @Override
    public String toString() {
        return "ThermostatDisplayModel{" +
                "relativeHumidity=" + relativeHumidity +
                ", operatingMode=" + operatingMode +
                ", currentTemperature=" + currentTemperature +
                ", coolSetpoint=" + coolSetpoint +
                ", heatSetpoint=" + heatSetpoint +
                ", minSetpoint=" + minSetpoint +
                ", maxSetpoint=" + maxSetpoint +
                ", minSetpointSeparation=" + minSetpointSeparation +
                ", minSetpointStopValue=" + minSetpointStopValue +
                ", maxSetpointStopValue=" + maxSetpointStopValue +
                ", leafEnabled=" + leafEnabled +
                ", isRunning=" + isRunning +
                ", setpointsText=" + setpointsText +
                ", isControlDisabled=" + isControlDisabled +
                ", isCloudConnected=" + isCloudConnected +
                ", useNestTerminology=" + useNestTerminology +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThermostatDisplayModel)) return false;

        ThermostatDisplayModel model = (ThermostatDisplayModel) o;

        if (currentTemperature != model.currentTemperature) return false;
        if (minSetpoint != model.minSetpoint) return false;
        if (maxSetpoint != model.maxSetpoint) return false;
        if (minSetpointSeparation != model.minSetpointSeparation) return false;
        if (leafEnabled != model.leafEnabled) return false;
        if (isRunning != model.isRunning) return false;
        if (isControlDisabled != model.isControlDisabled) return false;
        if (isCloudConnected != model.isCloudConnected) return false;
        if (useNestTerminology != model.useNestTerminology) return false;
        if (relativeHumidity != null ? !relativeHumidity.equals(model.relativeHumidity) : model.relativeHumidity != null)
            return false;
        if (operatingMode != model.operatingMode) return false;
        if (coolSetpoint != null ? !coolSetpoint.equals(model.coolSetpoint) : model.coolSetpoint != null)
            return false;
        if (heatSetpoint != null ? !heatSetpoint.equals(model.heatSetpoint) : model.heatSetpoint != null)
            return false;
        if (minSetpointStopValue != null ? !minSetpointStopValue.equals(model.minSetpointStopValue) : model.minSetpointStopValue != null)
            return false;
        if (maxSetpointStopValue != null ? !maxSetpointStopValue.equals(model.maxSetpointStopValue) : model.maxSetpointStopValue != null)
            return false;
        return setpointsText != null ? setpointsText.equals(model.setpointsText) : model.setpointsText == null;

    }

    @Override
    public int hashCode() {
        int result = relativeHumidity != null ? relativeHumidity.hashCode() : 0;
        result = 31 * result + operatingMode.hashCode();
        result = 31 * result + currentTemperature;
        result = 31 * result + (coolSetpoint != null ? coolSetpoint.hashCode() : 0);
        result = 31 * result + (heatSetpoint != null ? heatSetpoint.hashCode() : 0);
        result = 31 * result + minSetpoint;
        result = 31 * result + maxSetpoint;
        result = 31 * result + minSetpointSeparation;
        result = 31 * result + (minSetpointStopValue != null ? minSetpointStopValue.hashCode() : 0);
        result = 31 * result + (maxSetpointStopValue != null ? maxSetpointStopValue.hashCode() : 0);
        result = 31 * result + (leafEnabled ? 1 : 0);
        result = 31 * result + (isRunning ? 1 : 0);
        result = 31 * result + (setpointsText != null ? setpointsText.hashCode() : 0);
        result = 31 * result + (isControlDisabled ? 1 : 0);
        result = 31 * result + (isCloudConnected ? 1 : 0);
        result = 31 * result + (useNestTerminology ? 1 : 0);
        return result;
    }

}
