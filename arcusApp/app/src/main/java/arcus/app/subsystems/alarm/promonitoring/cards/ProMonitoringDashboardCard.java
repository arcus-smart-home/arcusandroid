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
package arcus.app.subsystems.alarm.promonitoring.cards;

import android.content.Context;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;


public class ProMonitoringDashboardCard extends SimpleDividerCard {

    public final static String TAG = ServiceCard.SECURITY_ALARM.toString();
    private AlarmState mAlarmState = AlarmState.NORMAL;
    private String summary;
    private boolean subsystemAvailable = false;

    public enum AlarmState {
        NORMAL,
        ALERT
    }

    public ProMonitoringDashboardCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    @Override
    public int getLayout() {
        return R.layout.card_safety_alarm;
    }

    public void setAlarmState(AlarmState state) {
        this.mAlarmState = state;
    }

    public AlarmState getAlarmState() {
        return mAlarmState;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public boolean isSubsystemAvailable() {
        return subsystemAvailable;
    }

    public void setSubsystemAvailable(boolean subsystemAvailable) {
        this.subsystemAvailable = subsystemAvailable;
    }
}
