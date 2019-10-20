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
package arcus.cornea.device.thermostat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ThermostatProxyModel {
    private String deviceId;
    private String deviceTypeHint;
    private String name;
    private boolean online;
    private FanMode fanMode;
    private boolean fanBlowing;
    private ThermostatMode mode;
    private int coolSetPoint;
    private int heatSetPoint;
    private int minimumSetpoint;
    private int maximumSetpoint;
    private int temperature;
    private int humidity;
    private boolean isRunning;
    private boolean isCloudDevice;
    private boolean isHoneywellDevice;
    private String authorizationState;
    private boolean requiresLogin;
    private List<ThermostatMode> supportedModes = new ArrayList<>();
    private Set<String> errors = new HashSet<>();

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceTypeHint() {
        return deviceTypeHint;
    }

    public void setDeviceTypeHint(String deviceTypeHint) {
        this.deviceTypeHint = deviceTypeHint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public FanMode getFanMode() {
        return fanMode;
    }

    public void setFanMode(FanMode fanMode) {
        this.fanMode = fanMode;
    }

    public boolean isFanBlowing() {
        return fanBlowing;
    }

    public void setFanBlowing(boolean fanBlowing) {
        this.fanBlowing = fanBlowing;
    }

    public ThermostatMode getMode() {
        return mode;
    }

    public void setMode(ThermostatMode mode) {
        this.mode = mode;
    }

    public int getCoolSetPoint() {
        return coolSetPoint;
    }

    public void setCoolSetPoint(int coolSetPoint) {
        this.coolSetPoint = coolSetPoint;
    }

    public int getHeatSetPoint() {
        return heatSetPoint;
    }

    public void setHeatSetPoint(int heatSetPoint) {
        this.heatSetPoint = heatSetPoint;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public boolean isHumiditySupported() {
        return humidity > -1;
    }

    public int getHumidity() {
        return humidity;
    }

    public void setHumidity(int humidity) {
        this.humidity = humidity;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public boolean isCloudDevice() {
        return isCloudDevice;
    }

    public void setIsCloudDevice(boolean isCloudDevice) {
        this.isCloudDevice = isCloudDevice;
    }

    public String getAuthorizationState() {
        return authorizationState;
    }

    public void setAuthorizationState(String authorizationState) {
        this.authorizationState = authorizationState;
    }

    public boolean isRequiresLogin() {
        return requiresLogin;
    }

    public void setRequiresLogin(boolean requiresLogin) {
        this.requiresLogin = requiresLogin;
    }

    public List<ThermostatMode> getSupportedModes() {
        return supportedModes;
    }

    public void setSupportedModes(List<ThermostatMode> supportedModes) {
        this.supportedModes = supportedModes;
    }

    public boolean isHoneywellDevice() {
        return isHoneywellDevice;
    }

    public void setHoneywellDevice(boolean honeywellDevice) {
        isHoneywellDevice = honeywellDevice;
    }

    public int getMinimumSetpoint() {
        return minimumSetpoint;
    }

    public void setMinimumSetpoint(int minimumSetpoint) {
        this.minimumSetpoint = minimumSetpoint;
    }

    public int getMaximumSetpoint() {
        return maximumSetpoint;
    }

    public void setMaximumSetpoint(int maximumSetpoint) {
        this.maximumSetpoint = maximumSetpoint;
    }

    public Set<String> getErrors() {
        return errors;
    }

    public void setErrors(Set<String> errors) {
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThermostatProxyModel)) return false;

        ThermostatProxyModel that = (ThermostatProxyModel) o;

        if (online != that.online) return false;
        if (fanBlowing != that.fanBlowing) return false;
        if (coolSetPoint != that.coolSetPoint) return false;
        if (heatSetPoint != that.heatSetPoint) return false;
        if (minimumSetpoint != that.minimumSetpoint) return false;
        if (maximumSetpoint != that.maximumSetpoint) return false;
        if (temperature != that.temperature) return false;
        if (humidity != that.humidity) return false;
        if (isRunning != that.isRunning) return false;
        if (isCloudDevice != that.isCloudDevice) return false;
        if (isHoneywellDevice != that.isHoneywellDevice) return false;
        if (requiresLogin != that.requiresLogin) return false;
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null)
            return false;
        if (deviceTypeHint != null ? !deviceTypeHint.equals(that.deviceTypeHint) : that.deviceTypeHint != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (fanMode != that.fanMode) return false;
        if (mode != that.mode) return false;
        if (authorizationState != null ? !authorizationState.equals(that.authorizationState) : that.authorizationState != null)
            return false;
        if (supportedModes != null ? !supportedModes.equals(that.supportedModes) : that.supportedModes != null)
            return false;
        return errors != null ? errors.equals(that.errors) : that.errors == null;

    }

    @Override
    public int hashCode() {
        int result = deviceId != null ? deviceId.hashCode() : 0;
        result = 31 * result + (deviceTypeHint != null ? deviceTypeHint.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (online ? 1 : 0);
        result = 31 * result + (fanMode != null ? fanMode.hashCode() : 0);
        result = 31 * result + (fanBlowing ? 1 : 0);
        result = 31 * result + (mode != null ? mode.hashCode() : 0);
        result = 31 * result + coolSetPoint;
        result = 31 * result + heatSetPoint;
        result = 31 * result + minimumSetpoint;
        result = 31 * result + maximumSetpoint;
        result = 31 * result + temperature;
        result = 31 * result + humidity;
        result = 31 * result + (isRunning ? 1 : 0);
        result = 31 * result + (isCloudDevice ? 1 : 0);
        result = 31 * result + (isHoneywellDevice ? 1 : 0);
        result = 31 * result + (authorizationState != null ? authorizationState.hashCode() : 0);
        result = 31 * result + (requiresLogin ? 1 : 0);
        result = 31 * result + (supportedModes != null ? supportedModes.hashCode() : 0);
        result = 31 * result + (errors != null ? errors.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ThermostatProxyModel{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceTypeHint='" + deviceTypeHint + '\'' +
                ", name='" + name + '\'' +
                ", online=" + online +
                ", fanMode=" + fanMode +
                ", fanBlowing=" + fanBlowing +
                ", mode=" + mode +
                ", coolSetPoint=" + coolSetPoint +
                ", heatSetPoint=" + heatSetPoint +
                ", minimumSetpoint=" + minimumSetpoint +
                ", maximumSetpoint=" + maximumSetpoint +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", isRunning=" + isRunning +
                ", isCloudDevice=" + isCloudDevice +
                ", isHoneywellDevice=" + isHoneywellDevice +
                ", authorizationState='" + authorizationState + '\'' +
                ", requiresLogin=" + requiresLogin +
                ", supportedModes=" + supportedModes +
                ", errors=" + errors +
                '}';
    }
}
