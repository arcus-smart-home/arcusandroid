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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlarmState {
    private AlertActor alertActor;
    private String alertCause;
    private boolean isAlert;
    private String alarmMode;
    private int totalBehaviors;
    private int activeBehaviors;
    private List<ActivityLine> events;
    private Date lastEvent;

    public enum AlertActor {
        PANIC,
        BEHAVIOR
    }

    public AlarmState() {
    }

    public String getAlarmMode() {
        return alarmMode;
    }

    public void setAlarmMode(String alarmMode) {
        this.alarmMode = alarmMode;
    }

    public int getTotalBehaviors() {
        return totalBehaviors;
    }

    public void setTotalBehaviors(int totalBehaviors) {
        this.totalBehaviors = totalBehaviors;
    }

    public int getActiveBehaviors() {
        return activeBehaviors;
    }

    public void setActiveBehaviors(int activeBehaviors) {
        this.activeBehaviors = activeBehaviors;
    }

    public List<ActivityLine> getEvents() {
        if (events == null) {
            events = new ArrayList<>(1);
        }

        return events;
    }

    public void setEvents(List<ActivityLine> events) {
        this.events = events;
    }

    public AlertActor getAlertActor() {
        return alertActor;
    }

    public void setAlertActor(AlertActor alertActor) {
        this.alertActor = alertActor;
    }

    public String getAlertCause() {
        return alertCause;
    }

    public void setAlertCause(String alertCause) {
        this.alertCause = alertCause;
    }

    public boolean isAlert() {
        return isAlert;
    }

    public void setIsAlert(boolean isAlert) {
        this.isAlert = isAlert;
    }

    public Date getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(Date lastEvent) {
        this.lastEvent = lastEvent;
    }
}
