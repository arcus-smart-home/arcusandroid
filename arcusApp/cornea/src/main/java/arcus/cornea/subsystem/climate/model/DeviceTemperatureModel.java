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
package arcus.cornea.subsystem.climate.model;

import arcus.cornea.device.thermostat.ThermostatMode;


public class DeviceTemperatureModel {
    private String deviceId;
    private String name;
    private String label;
    private int temperature;
    private Integer humidity;
    private ThermostatMode thermostatMode;
    private int coolSetPoint;
    private int heatSetPoint;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public boolean isThermostat() {
        return thermostatMode != null;
    }

    public boolean hasHumidity() {
        return humidity != null;
    }

    public ThermostatMode getThermostatMode() {
        return thermostatMode;
    }

    public void setThermostatMode(ThermostatMode thermostatMode) {
        this.thermostatMode = thermostatMode;
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

    @Override
    public String toString() {
        return "DeviceTemperatureModel{" +
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", temperature=" + temperature +
                ", humidity=" + humidity +
                ", thermostatMode=" + thermostatMode +
                ", coolSetPoint=" + coolSetPoint +
                ", heatSetPoint=" + heatSetPoint +
                '}';
    }
}
