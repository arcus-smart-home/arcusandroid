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



public class SecurityAlarmStatusModel extends SafetyAlarmStatusModel {

    public enum SecurityAlarmArmingState {
        INACTIVE,
        ON,
        OFF,
        DISARMED,
        ARMING,
        ALERT
    }

    private SecurityAlarmArmingState alarmState;
    private int armingSecondsRemaining;
    private int prealertSecondsRemaining;

    public SecurityAlarmStatusModel(int iconResourceId, String alarmTypeString) {
        super(iconResourceId, alarmTypeString);
    }

    public SecurityAlarmArmingState getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(SecurityAlarmArmingState alarmState) {
        this.alarmState = alarmState;
    }

    public int getArmingSecondsRemaining() {
        return armingSecondsRemaining;
    }

    public void setArmingSecondsRemaining(int armingSecondsRemaining) {
        this.armingSecondsRemaining = armingSecondsRemaining;
    }

    public int getPrealertSecondsRemaining() {
        return prealertSecondsRemaining;
    }

    public void setPrealertSecondsRemaining(int prealertSecondsRemaining) {
        this.prealertSecondsRemaining = prealertSecondsRemaining;
    }

    public boolean isInGracePeriod() {
        return (getArmingSecondsRemaining() > 0 || getPrealertSecondsRemaining() > 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        SecurityAlarmStatusModel that = (SecurityAlarmStatusModel) o;

        if (armingSecondsRemaining != that.armingSecondsRemaining) return false;
        if (prealertSecondsRemaining != that.prealertSecondsRemaining) return false;
        return alarmState == that.alarmState;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (alarmState != null ? alarmState.hashCode() : 0);
        result = 31 * result + armingSecondsRemaining;
        result = 31 * result + prealertSecondsRemaining;
        return result;
    }

    @Override
    public String toString() {
        return "SecurityAlarmStatusModel{" +
                "alarmState=" + alarmState +
                ", armingSecondsRemaining=" + armingSecondsRemaining +
                ", prealertSecondsRemaining=" + prealertSecondsRemaining +
                '}';
    }


}
