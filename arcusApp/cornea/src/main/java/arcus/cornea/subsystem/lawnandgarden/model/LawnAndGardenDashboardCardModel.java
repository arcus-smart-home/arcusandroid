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
package arcus.cornea.subsystem.lawnandgarden.model;


public class LawnAndGardenDashboardCardModel {
    private String deviceId;
    private String title;
    private String nextEventTitle;
    private long nextEventTime = -1;
    private int currentlyWateringZoneCount = 0;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNextEventTitle() {
        return nextEventTitle;
    }

    public void setNextEventTitle(String nextEventTitle) {
        this.nextEventTitle = nextEventTitle;
    }

    public long getNextEventTime() {
        return nextEventTime;
    }

    public void setNextEventTime(double nextEventTime) {
        this.nextEventTime = (long)nextEventTime;
    }

    public void setNextEventTime(long nextEventTime) {
        this.nextEventTime = nextEventTime;
    }

    public int getCurrentlyWateringZoneCount() {
        return currentlyWateringZoneCount;
    }

    public void setCurrentlyWateringZoneCount(int currentlyWateringZoneCount) {
        this.currentlyWateringZoneCount = currentlyWateringZoneCount;
    }
}
