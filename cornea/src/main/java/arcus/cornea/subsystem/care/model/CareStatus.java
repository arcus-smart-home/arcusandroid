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
package arcus.cornea.subsystem.care.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class CareStatus {
    private AlertTrigger alertTriggeredBy;
    private List<AlertTrigger> allAlertTriggers;

    private AlarmMode alarmMode;
    private int totalBehaviors;
    private int activeBehaviors;
    private List<String> notificationList;
    private String lastAlertString;

    public CareStatus() {}

    public void setAlertTriggeredBy(AlertTrigger alertTriggeredBy) {
        this.alertTriggeredBy = alertTriggeredBy;
    }

    public void setAllAlertTriggers(List<AlertTrigger> allAlertTriggers) {
        this.allAlertTriggers = allAlertTriggers;
    }

    public void setAlarmMode(AlarmMode alarmMode) {
        this.alarmMode = alarmMode;
    }

    public void setTotalBehaviors(int totalBehaviors) {
        this.totalBehaviors = totalBehaviors;
    }

    public void setActiveBehaviors(int activeBehaviors) {
        this.activeBehaviors = activeBehaviors;
    }

    public void setNotificationList(List<String> notificationList) {
        this.notificationList = notificationList;
    }

    public void setLastAlertString(@NonNull String lastAlertString) {
        this.lastAlertString = lastAlertString;
    }

    public @Nullable AlertTrigger getAlertTriggeredBy() {
        return alertTriggeredBy;
    }

    public @Nullable List<AlertTrigger> getAllAlertTriggers() {
        return allAlertTriggers;
    }

    public @Nullable AlarmMode getAlarmMode() {
        return alarmMode;
    }

    public int getTotalBehaviors() {
        return totalBehaviors;
    }

    public int getActiveBehaviors() {
        return activeBehaviors;
    }

    public @Nullable List<String> getNotificationList() {
        return notificationList;
    }

    public String getLastAlertString() {
        return lastAlertString;
    }

    public boolean isAlert() {
        return alertTriggeredBy != null;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CareStatus that = (CareStatus) o;

        if (totalBehaviors != that.totalBehaviors) {
            return false;
        }
        if (activeBehaviors != that.activeBehaviors) {
            return false;
        }
        if (alertTriggeredBy != null ? !alertTriggeredBy.equals(that.alertTriggeredBy) : that.alertTriggeredBy != null) {
            return false;
        }
        if (allAlertTriggers != null ? !allAlertTriggers.equals(that.allAlertTriggers) : that.allAlertTriggers != null) {
            return false;
        }
        if (alarmMode != that.alarmMode) {
            return false;
        }
        if (notificationList != null ? !notificationList.equals(that.notificationList) : that.notificationList != null) {
            return false;
        }
        return !(lastAlertString != null ? !lastAlertString.equals(that.lastAlertString) : that.lastAlertString != null);

    }

    @Override public int hashCode() {
        int result = alertTriggeredBy != null ? alertTriggeredBy.hashCode() : 0;
        result = 31 * result + (allAlertTriggers != null ? allAlertTriggers.hashCode() : 0);
        result = 31 * result + (alarmMode != null ? alarmMode.hashCode() : 0);
        result = 31 * result + totalBehaviors;
        result = 31 * result + activeBehaviors;
        result = 31 * result + (notificationList != null ? notificationList.hashCode() : 0);
        result = 31 * result + (lastAlertString != null ? lastAlertString.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "CareStatus{" +
              "alertTriggeredBy=" + alertTriggeredBy +
              ", allAlertTriggers=" + allAlertTriggers +
              ", alarmMode=" + alarmMode +
              ", totalBehaviors=" + totalBehaviors +
              ", activeBehaviors=" + activeBehaviors +
              ", notificationList=" + notificationList +
              ", lastAlertString='" + lastAlertString + '\'' +
              '}';
    }
}
