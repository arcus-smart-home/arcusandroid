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
package arcus.app.subsystems.lawnandgarden.models;

import java.io.Serializable;


public class IrrigationZoneInfo implements Serializable, Comparable<IrrigationZoneInfo> {
    private String displayName;
    private String zoneId;
    private String zoneDisplay;
    private int zoneNumber;
    private int duration;
    private boolean visible = false;

    public IrrigationZoneInfo() {

    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getZoneDisplay() {
        return zoneDisplay;
    }

    public void setZoneDisplay(String zoneDisplay) {
        this.zoneDisplay = zoneDisplay;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    @Override
    public int compareTo(IrrigationZoneInfo irrigationZoneInfo) {
        return Integer.valueOf(this.zoneNumber).compareTo(irrigationZoneInfo.zoneNumber);
    }
}
