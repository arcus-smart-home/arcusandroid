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
package arcus.cornea.subsystem.alarm.model;

import java.util.Set;



public class AlarmModel {

    // TODO: Add TriggerEvent field once fully defined

    private String type;
    private String alertState;
    private Set<String> devices;
    private Set<String> excludedDevices;
    private Set<String> activeDevices;
    private Set<String> offlineDevices;
    private Set<String> triggeredDevices;
    private boolean isMonitored;
    private boolean isSilent;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlertState() {
        return alertState;
    }

    public void setAlertState(String alertState) {
        this.alertState = alertState;
    }

    public Set<String> getDevices() {
        return devices;
    }

    public void setDevices(Set<String> devices) {
        this.devices = devices;
    }

    public Set<String> getExcludedDevices() {
        return excludedDevices;
    }

    public void setExcludedDevices(Set<String> excludedDevices) {
        this.excludedDevices = excludedDevices;
    }

    public Set<String> getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(Set<String> activeDevices) {
        this.activeDevices = activeDevices;
    }

    public Set<String> getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(Set<String> offlineDevices) {
        this.offlineDevices = offlineDevices;
    }

    public Set<String> getTriggeredDevices() {
        return triggeredDevices;
    }

    public void setTriggeredDevices(Set<String> triggeredDevices) {
        this.triggeredDevices = triggeredDevices;
    }

    public boolean isMonitored() {
        return isMonitored;
    }

    public void setMonitored(boolean monitored) {
        isMonitored = monitored;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public void setSilent(boolean silent) {
        isSilent = silent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AlarmModel that = (AlarmModel) o;

        if (isMonitored != that.isMonitored) return false;
        if (isSilent != that.isSilent) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (alertState != null ? !alertState.equals(that.alertState) : that.alertState != null)
            return false;
        if (devices != null ? !devices.equals(that.devices) : that.devices != null) return false;
        if (excludedDevices != null ? !excludedDevices.equals(that.excludedDevices) : that.excludedDevices != null)
            return false;
        if (activeDevices != null ? !activeDevices.equals(that.activeDevices) : that.activeDevices != null)
            return false;
        if (offlineDevices != null ? !offlineDevices.equals(that.offlineDevices) : that.offlineDevices != null)
            return false;
        return triggeredDevices != null ? triggeredDevices.equals(that.triggeredDevices) : that.triggeredDevices == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (alertState != null ? alertState.hashCode() : 0);
        result = 31 * result + (devices != null ? devices.hashCode() : 0);
        result = 31 * result + (excludedDevices != null ? excludedDevices.hashCode() : 0);
        result = 31 * result + (activeDevices != null ? activeDevices.hashCode() : 0);
        result = 31 * result + (offlineDevices != null ? offlineDevices.hashCode() : 0);
        result = 31 * result + (triggeredDevices != null ? triggeredDevices.hashCode() : 0);
        result = 31 * result + (isMonitored ? 1 : 0);
        result = 31 * result + (isSilent ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AlarmModel{" +
                "type='" + type + '\'' +
                ", alertState='" + alertState + '\'' +
                ", devices=" + devices +
                ", excludedDevices=" + excludedDevices +
                ", activeDevices=" + activeDevices +
                ", offlineDevices=" + offlineDevices +
                ", triggeredDevices=" + triggeredDevices +
                ", isMonitored=" + isMonitored +
                ", isSilent=" + isSilent +
                '}';
    }
}
