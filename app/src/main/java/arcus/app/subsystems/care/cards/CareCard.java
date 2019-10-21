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
package arcus.app.subsystems.care.cards;

import android.content.Context;
import android.text.SpannableString;

import arcus.cornea.subsystem.care.model.ActivityLine;
import arcus.cornea.subsystem.care.model.AlarmState;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;

import java.util.List;

public class CareCard extends SimpleDividerCard {
    private static final String TAG = ServiceCard.CARE.name();
    private List<ActivityLine> activityLines;
    private SpannableString lastActivity;
    private String alarmMode;
    private int totalBehaviors;
    private int activeBehaviors;
    private boolean isAlerting = false;
    private AlarmState.AlertActor causedByActor;
    private String causedByText;

    public CareCard(Context context) {
        super(context);
        setTag(TAG);
        showDivider();
    }

    @Override public int getLayout() {
        return R.layout.card_care;
    }

    public List<ActivityLine> getActivityLines() {
        return activityLines;
    }

    public void setActivityLines(List<ActivityLine> activityLines) {
        this.activityLines = activityLines;
    }

    public String getAlarmMode() {
        return alarmMode;
    }

    public void setAlarmMode(String alarmMode) {
        this.alarmMode = alarmMode;
    }

    public int getActiveBehaviors() {
        return activeBehaviors;
    }

    public void setActiveBehaviors(int activeBehaviors) {
        this.activeBehaviors = activeBehaviors;
    }

    public int getTotalBehaviors() {
        return totalBehaviors;
    }

    public void setTotalBehaviors(int totalBehaviors) {
        this.totalBehaviors = totalBehaviors;
    }

    public boolean isAlerting() {
        return isAlerting;
    }

    public void setIsAlerting(boolean isAlerting) {
        this.isAlerting = isAlerting;
    }

    public AlarmState.AlertActor getCausedByActor() {
        return causedByActor;
    }

    public void setCausedByActor(AlarmState.AlertActor causedByActor) {
        this.causedByActor = causedByActor;
    }

    public String getCausedByText() {
        return causedByText;
    }

    public void setCausedByText(String causedByText) {
        this.causedByText = causedByText;
    }

    public SpannableString getLastActivityTimeStamp() {
        return lastActivity;
    }

    public void setLastActivity(SpannableString lastActivity) {
        this.lastActivity = lastActivity;
    }
}
