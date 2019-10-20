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



public class DeviceWaterHeaterModel {
    private String deviceId;
    private String name;
    private String label;
    private int setpoint;
    private Integer saltlevel;
    private String waterlevel;
    private boolean isWaterHeater;
    private boolean isWaterSoftenerEnabled;
    private boolean isHeating;


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


    public int getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(int setpoint) {
        this.setpoint = setpoint;
    }

    public Integer getSaltlevel() {
        return saltlevel;
    }

    public void setSaltlevel(Integer saltlevel) {
        this.saltlevel = saltlevel;
    }

    public String getWaterlevel() {
        return waterlevel;
    }

    public void setWaterlevel(String waterlevel) {
        this.waterlevel = waterlevel;
    }

    public boolean isWaterHeater() {
        return isWaterHeater;
    }

    public void isWaterHeater(boolean isWaterHeater) {
        this.isWaterHeater = isWaterHeater;
    }

    public boolean isWaterSoftenerEnabled() {
        return isWaterSoftenerEnabled;
    }

    public void isWaterSoftenerEnabled(boolean isWaterSoftenerEnabled) {
        this.isWaterSoftenerEnabled = isWaterSoftenerEnabled;
    }

    public boolean isHeating() {
        return isHeating;
    }

    public void isHeating(boolean isHeating) {
        this.isHeating = isHeating;
    }

    @Override
    public String toString() {
        return "DeviceWaterHeaterModel{" +
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", label='" + label + '\'' +
                ", setpoint=" + setpoint +
                ", saltlevelt=" + saltlevel +
                ", waterlevel=" + waterlevel+

                '}';
    }
}
