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
package arcus.app.subsystems.lawnandgarden.zoneorder;

public class IrrigationZoneCard {

    private final String title;
    private final String descriptionString;
    private final int iconDrawableResId;
    private final int smallIconDrawableResId;
    private final String zoneId;
    private int duration;

    IrrigationZoneCard(String title, String descriptionString, int iconDrawableId, int smallIconDrawableId,
                       String zoneId, int duration) {
        this.title = title;
        this.descriptionString = descriptionString;
        this.iconDrawableResId = iconDrawableId;
        this.smallIconDrawableResId = smallIconDrawableId;
        this.duration = duration;
        this.zoneId = zoneId;
    }

    public String getTitle () { return this.title; }
    public String getDescriptionString () { return this.descriptionString; }
    public int getIconDrawableResId () { return this.iconDrawableResId; }
    public int getSmallIconDrawableResId () { return this.smallIconDrawableResId; }

    public String getZoneId() {
        return zoneId;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}
