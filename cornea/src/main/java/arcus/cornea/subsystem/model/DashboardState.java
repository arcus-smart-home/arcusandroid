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
package arcus.cornea.subsystem.model;


public class DashboardState {

    private boolean securityAlarmActivated = false;
    private boolean safetyAlarmActivated = false;
    private boolean careAlarmActivated = false;
    private boolean weatherAlertActivated = false;
    private boolean alarmAlertActivated = false;
    private boolean alarmPreAlertActivated = false;
    private boolean presmokeAlertActivated = false;

    public String getPrimaryActiveAlarm() {
        return primaryActiveAlarm;
    }

    public void setPrimaryActiveAlarm(String primaryActiveAlarm) {
        this.primaryActiveAlarm = primaryActiveAlarm;
    }

    private String primaryActiveAlarm;

    private String alarmIncidentAddress;

    public void setCareAlarmActivated(boolean careAlarmActivated) {
        this.careAlarmActivated = careAlarmActivated;
    }

    public boolean isCareAlarmActivated() {
        return careAlarmActivated;
    }

    public boolean isWeatherAlertActivated() {
        return weatherAlertActivated;
    }

    public void setWeatherAlertActivated(boolean weatherAlertActivated) {
        this.weatherAlertActivated = weatherAlertActivated;
    }

    public boolean isAlarmAlertActivated() {
        return alarmAlertActivated;
    }

    public void setAlarmAlertActivated(boolean alarmAlertActivated) {
        this.alarmAlertActivated = alarmAlertActivated;
    }

    public String getAlarmIncidentAddress() {
        return alarmIncidentAddress;
    }

    public void setAlarmIncidentAddress(String alarmIncidentAddress) {
        this.alarmIncidentAddress = alarmIncidentAddress;
    }

    public boolean isAlarmPreAlertActivated() {
        return alarmPreAlertActivated;
    }

    public void setAlarmPreAlertActivated(boolean alarmPreAlertActivated) {
        this.alarmPreAlertActivated = alarmPreAlertActivated;
    }

    public boolean isPresmokeAlertActivated() {
        return presmokeAlertActivated;
    }

    public void setPresmokeAlertActivated(boolean presmokeAlertActivated) {
        this.presmokeAlertActivated = presmokeAlertActivated;
    }

    public void setSecurityAlarmActivated(boolean securityAlarmActivated) {
        this.securityAlarmActivated = securityAlarmActivated;
    }

    public void setSafetyAlarmActivated(boolean safetyAlarmActivate) {
        this.safetyAlarmActivated = safetyAlarmActivate;
    }

    public boolean getSecurityActivated() {
        return this.securityAlarmActivated;
    }

    public boolean getSafetyActivated() {
        return this.safetyAlarmActivated;
    }


    public State getDeprecatedState() {
        if (careAlarmActivated || securityAlarmActivated || safetyAlarmActivated) {
            return State.ALERT;
        }
        return State.NORMAL;
    }
    /**
     * Returns the alerts in order of their
     * @return
     */
    public State getState() {

        if (careAlarmActivated || alarmAlertActivated || securityAlarmActivated || safetyAlarmActivated) {
            return State.ALERT;
        }

        if (alarmPreAlertActivated || presmokeAlertActivated) {
            return State.PRE_ALERT;
        }

        if (weatherAlertActivated) {
            return State.WEATHER_ALERT;
        }

        return State.NORMAL;
    }

    public boolean isAlerting() {
        return getState() == State.ALERT;
    }

    // could add offline or something like that here as well
    public enum State {
        NORMAL,
        PRE_ALERT,
        ALERT,
        WEATHER_ALERT
    }

    @Override
    public String toString() {
        return "DashboardState{" +
                "careAlarmActivated=" + careAlarmActivated +
                ", weatherAlertActivated=" + weatherAlertActivated +
                ", alarmAlertActivated=" + alarmAlertActivated +
                ", alarmPreAlertActivated=" + alarmPreAlertActivated +
                ", presmokeAlertActivated=" + presmokeAlertActivated +
                ", primaryActiveAlarm='" + primaryActiveAlarm + '\'' +
                ", alarmIncidentAddress='" + alarmIncidentAddress + '\'' +
                '}';
    }
}
