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
package arcus.app.subsystems.climate.cards;

import android.content.Context;

import arcus.cornea.subsystem.climate.model.ScheduledSetPoint;
import arcus.app.common.cards.SimpleDividerCard;


public class BaseClimateScheduleCard extends SimpleDividerCard {
    public final static String TAG = "BaseClimateScheduleCard";
    private String leftText;
    private String rightText;
    private int mDrawableResource;
    private ScheduledSetPoint scheduledSetPoint;

    public BaseClimateScheduleCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    public void setDrawableResource(int resourceId) {
        mDrawableResource = resourceId;
    }

    public int getDrawableResource() {
        return mDrawableResource;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public String getRightText() {
        return this.rightText;
    }

    public ScheduledSetPoint getScheduledSetPoint() {
        return scheduledSetPoint;
    }

    public void setScheduledSetPoint(ScheduledSetPoint scheduledSetPoint) {
        this.scheduledSetPoint = scheduledSetPoint;
    }
}
