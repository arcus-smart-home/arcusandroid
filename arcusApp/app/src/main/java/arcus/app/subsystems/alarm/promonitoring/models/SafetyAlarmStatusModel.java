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
package arcus.app.subsystems.alarm.promonitoring.models;



public class SafetyAlarmStatusModel extends AlarmStatusModel {

    private String subtext;
    private int participatingDevicesCount;
    private int offlineDevicesCount;
    private int bypassedDevicesCount;
    private int totalDevicesCount;
    private int tiggeredDevicesCount;
    private int activeDevicesCount;

    public SafetyAlarmStatusModel(int iconResourceId, String alarmTypeString) {
        super(iconResourceId, alarmTypeString);
    }

    public String getSubtext() {
        return subtext;
    }

    public void setSubtext(String subtext) {
        this.subtext = subtext;
    }

    public int getParticipatingDevicesCount() {
        return participatingDevicesCount;
    }

    public void setParticipatingDevicesCount(int participatingDevicesCount) {
        this.participatingDevicesCount = participatingDevicesCount;
    }

    public int getOfflineDevicesCount() {

        return offlineDevicesCount;
    }

    public void setOfflineDevicesCount(int offlineDevicesCount) {
        this.offlineDevicesCount = offlineDevicesCount;
    }

    public int getBypassedDevicesCount() {
        return bypassedDevicesCount;
    }

    public void setBypassedDevicesCount(int bypassedDevicesCount) {
        this.bypassedDevicesCount = bypassedDevicesCount;
    }

    public int getTotalDevicesCount() {
        return totalDevicesCount;
    }

    public void setTotalDevicesCount(int totalDevicesCount) {
        this.totalDevicesCount = totalDevicesCount;
    }

    public int getTiggeredDevicesCount() {
        return tiggeredDevicesCount;
    }

    public void setTiggeredDevicesCount(int tiggeredDevicesCount) {
        this.tiggeredDevicesCount = tiggeredDevicesCount;
    }

    public int getActiveDevicesCount() {
        return activeDevicesCount;
    }

    public void setActiveDevicesCount(int activeDevicesCount) {
        this.activeDevicesCount = activeDevicesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SafetyAlarmStatusModel that = (SafetyAlarmStatusModel) o;

        if (participatingDevicesCount != that.participatingDevicesCount) return false;
        if (offlineDevicesCount != that.offlineDevicesCount) return false;
        if (bypassedDevicesCount != that.bypassedDevicesCount) return false;
        if (totalDevicesCount != that.totalDevicesCount) return false;
        if (tiggeredDevicesCount != that.tiggeredDevicesCount) return false;
        if (activeDevicesCount != that.activeDevicesCount) return false;
        return subtext != null ? subtext.equals(that.subtext) : that.subtext == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (subtext != null ? subtext.hashCode() : 0);
        result = 31 * result + participatingDevicesCount;
        result = 31 * result + offlineDevicesCount;
        result = 31 * result + bypassedDevicesCount;
        result = 31 * result + totalDevicesCount;
        result = 31 * result + tiggeredDevicesCount;
        result = 31 * result + activeDevicesCount;
        return result;
    }

    @Override
    public String toString() {
        return "SafetyAlarmStatusModel{" +
                "subtext='" + subtext + '\'' +
                ", participatingDevicesCount=" + participatingDevicesCount +
                ", offlineDevicesCount=" + offlineDevicesCount +
                ", bypassedDevicesCount=" + bypassedDevicesCount +
                ", totalDevicesCount=" + totalDevicesCount +
                ", tiggeredDevicesCount=" + tiggeredDevicesCount +
                ", activeDevicesCount=" + activeDevicesCount +
                '}';
    }

}
