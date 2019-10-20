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
package arcus.cornea.device.waterheater;


public class WaterHeaterProxyModel {
    private String deviceId;
    private String deviceTypeHint;
    private String name;
    private boolean online;
    private double setPoint;
    private boolean heatingState;
    private String hotWaterLevel;
    private String controlMode;
    private String temperatureScale;
    private int minTemp;
    private int maxTemp;

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

    public double getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }

    public boolean isHeatingState() {
        return heatingState;
    }

    public void setHeatingState(boolean heatingState) {
        this.heatingState = heatingState;
    }

    public String getHotWaterLevel() {
        return hotWaterLevel;
    }

    public void setHotWaterLevel(String hotWaterLevel) {
        this.hotWaterLevel = hotWaterLevel;
    }

    public String getControlMode() {
        return controlMode;
    }

    public void setControlMode(String controlMode) {
        this.controlMode = controlMode;
    }

    public String getTemperatureScale() {
        return temperatureScale;
    }

    public void setTemperatureScale(String temperatureScale) {
        this.temperatureScale = temperatureScale;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WaterHeaterProxyModel that = (WaterHeaterProxyModel) o;

        if (online != that.online) {
            return false;
        }
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) {
            return false;
        }
        if (deviceTypeHint != null ? !deviceTypeHint.equals(that.deviceTypeHint) : that.deviceTypeHint != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (setPoint != that.setPoint) {
            return false;
        }
        if (heatingState != that.heatingState) {
            return false;
        }
        return hotWaterLevel == that.hotWaterLevel;


    }

    @Override
    public int hashCode() {
        int result = deviceId != null ? deviceId.hashCode() : 0;
        result = 31 * result + (deviceTypeHint != null ? deviceTypeHint.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (heatingState ? 1 : 0);
        result = 31 * result + (hotWaterLevel != null ? hotWaterLevel.hashCode() : 0);
        result = 31 * result + (int)setPoint;
        result = 31 * result + (isOnline() ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "WaterHeaterProxyModel{" +
              "deviceId='" + deviceId + '\'' +
              ", deviceTypeHint='" + deviceTypeHint + '\'' +
              ", name='" + name + '\'' +
              ", heatingstate=" + heatingState +
              ", hotwaterlevel=" + hotWaterLevel +
              ", setpoint=" + setPoint +
              ", isOnline=" + isOnline() +
              '}';
    }
}
