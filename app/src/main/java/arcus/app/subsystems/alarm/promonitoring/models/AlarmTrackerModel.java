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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AlarmTrackerModel {

    private boolean confirmable;
    private boolean cancelable;
    private boolean complete;
    private boolean isProMonitored;
    private int incidentLayoutTint;
    private String alarmTypeTitle;                  // Activity Title for the specific alarm type

    private List<HistoryListItemModel> historyListItems = new ArrayList<>();
    private List<AlarmTrackerStateModel> trackerStates = new ArrayList<>();

    /**
     * When true, the alarm incident is confirmable and the "Confirm" button should be enabled in
     * the view.
     * @return
     */
    public boolean isConfirmable() {
        return confirmable;
    }

    public void setConfirmable(boolean confirmable) {
        this.confirmable = confirmable;
    }

    /**
     * When true, the alarm incident is cancelable and the "Cancel" button should be enabled in the
     * view.
     * @return
     */
    public boolean isCancelable() {
        return cancelable;
    }

    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    /**
     * Returns a list of alarm incident entries that should appear in the view.
     * @return
     */
    public List<HistoryListItemModel> getHistoryListItems() {
        Collections.sort(historyListItems);
        return historyListItems;
    }

    public void setHistoryListItems(List<HistoryListItemModel> historyListItems) {
        this.historyListItems = historyListItems;
    }

    public List<AlarmTrackerStateModel> getTrackerStates() {
        return trackerStates;
    }

    public void setTrackerStates(List<AlarmTrackerStateModel> trackerStates) {
        this.trackerStates = trackerStates;
    }

    public boolean isProMonitored() {
        return isProMonitored;
    }

    public void setProMonitored(boolean proMonitored) {
        isProMonitored = proMonitored;
    }

    public int getIncidentLayoutTint() {
        return incidentLayoutTint;
    }

    public void setIncidentLayoutTint(int incidentLayoutTint) {
        this.incidentLayoutTint = incidentLayoutTint;
    }

    public String getAlarmTypeTitle() {
        return alarmTypeTitle;
    }

    public void setAlarmTypeTitle(String alarmTypeTitle) {
        this.alarmTypeTitle = alarmTypeTitle;
    }
}
