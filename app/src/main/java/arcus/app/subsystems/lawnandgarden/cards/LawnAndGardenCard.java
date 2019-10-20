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
package arcus.app.subsystems.lawnandgarden.cards;

import android.content.Context;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;


public class LawnAndGardenCard extends SimpleDividerCard {

    public final static String TAG = ServiceCard.LAWN_AND_GARDEN.toString();
    private String deviceId;
    private String title;
    private String description;
    private String nextEventTitle;
    private long nextEventTime = 0;
    private int currentlyWaterZoneCount = 0;


    public LawnAndGardenCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    @Override
    public int getLayout() {
        return R.layout.card_lawn_and_garden;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
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

    public void setNextEventTime(long nextEventTime) {
        this.nextEventTime = nextEventTime;
    }

    public int getCurrentlyWaterZoneCount() {
        return currentlyWaterZoneCount;
    }

    public void setCurrentlyWaterZoneCount(int currentlyWaterZoneCount) {
        this.currentlyWaterZoneCount = currentlyWaterZoneCount;
    }
}
