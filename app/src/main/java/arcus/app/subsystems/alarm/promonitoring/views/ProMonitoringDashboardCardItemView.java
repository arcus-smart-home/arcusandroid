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
package arcus.app.subsystems.alarm.promonitoring.views;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import arcus.cornea.controller.SubscriptionController;
import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.cards.ProMonitoringDashboardCard;


public class ProMonitoringDashboardCardItemView extends DashboardFlipViewHolder {

    private ImageView serviceImage;
    private ImageView promonImage;
    private Version1TextView serviceName;
    private Version1TextView alarmStatus;
    private Context context;
    private boolean alarmSubsystemEnabled = false;
    private boolean alerting = false;

    public ProMonitoringDashboardCardItemView(View view) {
        super(view);
        this.context = view.getContext();

        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        alarmStatus = (Version1TextView) view.findViewById(R.id.alarm_status);
        promonImage = (ImageView) view.findViewById(R.id.promon_badge);
    }

    public void build(@NonNull ProMonitoringDashboardCard card) {
        alarmSubsystemEnabled = card.isSubsystemAvailable();
        //alarmStatus.setVisibility(alarmSubsystemEnabled ? View.VISIBLE : View.INVISIBLE);
        serviceName.setText(context.getString(R.string.card_alarms_title));
        serviceImage.setImageResource(R.drawable.dashboard_alarm);
        alarmStatus.setText(card.getSummary() == null ? "" : card.getSummary());
        promonImage.setVisibility(View.GONE);

        if (card.getAlarmState() == ProMonitoringDashboardCard.AlarmState.ALERT) {
            alerting = true;
            itemView.setBackgroundColor(context.getResources().getColor(card.getBackgroundColor()));
        } else {
            itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            alerting = false;
        }
    }

    public boolean isAlerting() {
        return alerting;
    }

    public boolean isAlarmSubsystemEnabled() {
        return alarmSubsystemEnabled;
    }
}
