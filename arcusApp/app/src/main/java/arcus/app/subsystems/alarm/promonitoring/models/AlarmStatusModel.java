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



public abstract class AlarmStatusModel {

    private final int iconResourceId;
    private final String alarmTypeString;
    private boolean isProMonitored;

    public AlarmStatusModel(int iconResourceId, String alarmTypeString) {
        this.iconResourceId = iconResourceId;
        this.alarmTypeString = alarmTypeString;
    }

    public int getIconResourceId() {
        return this.iconResourceId;
    }

    public String getAlarmTypeString() {
        return this.alarmTypeString;
    }

    public boolean isProMonitored() {
        return isProMonitored;
    }

    public void setProMonitored(boolean proMonitored) {
        isProMonitored = proMonitored;
    }

    @Override
    public String toString() {
        return "AlarmStatusModel{" +
                "iconResourceId=" + iconResourceId +
                ", alarmTypeString='" + alarmTypeString + '\'' +
                ", isProMonitored=" + isProMonitored +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlarmStatusModel)) return false;

        AlarmStatusModel that = (AlarmStatusModel) o;

        if (iconResourceId != that.iconResourceId) return false;
        if (isProMonitored != that.isProMonitored) return false;
        return alarmTypeString.equals(that.alarmTypeString);

    }

    @Override
    public int hashCode() {
        int result = iconResourceId;
        result = 31 * result + alarmTypeString.hashCode();
        result = 31 * result + (isProMonitored ? 1 : 0);
        return result;
    }

}
