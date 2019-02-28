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

import com.google.common.collect.ImmutableList;

import java.util.List;


public class DashboardCardModel {
    private int temperature;
    private boolean temperatureDeviceOffline;
    private String thermostatLabel;
    private List<ClimateBadge> badges = ImmutableList.of();
    private boolean isTemperatureCloudDevice = false;
    private String primaryTemperatureDeviceId;

    public boolean isPrimaryTemperatureOffline() { return temperatureDeviceOffline; }

    public void setPrimaryTemperatureOffline(boolean temperatureDeviceOffline) {
        this.temperatureDeviceOffline = temperatureDeviceOffline;
    }

    public boolean isThermostatAvailable() {
        return thermostatLabel != null;
    }

    public boolean isBadgeAvailable() {
        return badges != null && !badges.isEmpty();
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public String getThermostatLabel() {
        return thermostatLabel;
    }

    public void setThermostatLabel(String thermostatLabel) {
        this.thermostatLabel = thermostatLabel;
    }

    public List<ClimateBadge> getBadges() {
        return badges;
    }

    public void setBadges(List<ClimateBadge> badges) {
        this.badges = badges;
    }

    public boolean isTemperatureCloudDevice() {
        return isTemperatureCloudDevice;
    }

    public void setIsTemperatureCloudDevice(boolean isTemperatureCloudDevice) {
        this.isTemperatureCloudDevice = isTemperatureCloudDevice;
    }

    public String getPrimaryTemperatureDeviceId() {
        return primaryTemperatureDeviceId;
    }

    public void setPrimaryTemperatureDeviceId(String primaryTemperatureDeviceId) {
        this.primaryTemperatureDeviceId = primaryTemperatureDeviceId;
    }
}
