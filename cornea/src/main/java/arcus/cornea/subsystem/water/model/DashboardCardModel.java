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
package arcus.cornea.subsystem.water.model;

import com.google.common.collect.ImmutableList;

import java.util.List;


public class DashboardCardModel {
    private int temperature;
    private boolean waterHeaterDeviceOffline;
    private String waterHeaterLabel;
    private List<WaterBadge> badges = ImmutableList.of();
    private String waterHeaterWaterLevel;
    private boolean isHeating;

    private String waterHeaterDeviceId;
    private String waterSoftenerDeviceId;
    private String valveDeviceId;

    public boolean isPrimaryWaterHeaterOffline() { return waterHeaterDeviceOffline; }

    public void setPrimaryWaterHeaterOffline(boolean temperatureDeviceOffline) {
        this.waterHeaterDeviceOffline = temperatureDeviceOffline;
    }

    public boolean isWaterHeaterAvailable() {
        return waterHeaterLabel != null;
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

    public String getWaterHeaterLabel() {
        return waterHeaterLabel;
    }

    public void setWaterHeaterLabel(String thermostatLabel) {
        this.waterHeaterLabel = thermostatLabel;
    }

    public List<WaterBadge> getBadges() {
        return badges;
    }

    public void setBadges(List<WaterBadge> badges) {
        this.badges = badges;
    }

    public String getWaterHeaterWaterLevel() {
        return waterHeaterWaterLevel;
    }

    public void setWaterHeaterWaterLevel(String waterHeaterWaterLevel) {
        this.waterHeaterWaterLevel = waterHeaterWaterLevel;
    }

    public boolean isHeating() {
        return isHeating;
    }

    public void isHeating(boolean isHeating) {
        this.isHeating = isHeating;
    }

    public String getWaterHeaterDeviceId() {
        return waterHeaterDeviceId;
    }

    public void setWaterHeaterAddress(String waterHeaterDeviceId) {
        this.waterHeaterDeviceId = waterHeaterDeviceId;
    }

    public String getWaterSoftenerDeviceId() {
        return waterSoftenerDeviceId;
    }

    public void setWaterSoftenerDeviceId(String waterSoftenerDeviceId) {
        this.waterSoftenerDeviceId = waterSoftenerDeviceId;
    }

    public String getValveDeviceId() {
        return valveDeviceId;
    }

    public void setValveDeviceId(String valveDeviceId) {
        this.valveDeviceId = valveDeviceId;
    }
}
