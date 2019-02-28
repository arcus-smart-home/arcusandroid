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


public class DeviceScheduleModel {
    private String deviceId;
    private String deviceTypeHint;
    private String name;
    private ScheduleState scheduleState;

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

    public void setScheduleState(ScheduleState state) {
        this.scheduleState = state;
    }

    public ScheduleState getScheduleState() {
        return this.scheduleState;
    }

    @Override
    public String toString() {
        return "DeviceScheduleModel{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceTypeHint='" + deviceTypeHint + '\'' +
                ", name='" + name + '\'' +
                ", scheduleState=" + scheduleState.name() +
                '}';
    }
}
