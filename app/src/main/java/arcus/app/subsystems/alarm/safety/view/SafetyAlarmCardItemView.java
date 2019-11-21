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
package arcus.app.subsystems.alarm.safety.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.safety.cards.SafetyAlarmCard;


public class SafetyAlarmCardItemView extends DashboardFlipViewHolder {
    ImageView serviceImage;
    Version1TextView serviceName;
    Version1TextView alarmStatus;
    Context context;

    public SafetyAlarmCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        alarmStatus = (Version1TextView) view.findViewById(R.id.safety_alarm_status);
        this.context = view.getContext();
    }

    public void build(@NonNull SafetyAlarmCard card) {
        serviceName.setText(context.getString(R.string.card_safety_alarm_title));
        serviceImage.setImageResource(R.drawable.icon_service_safetyalarm_small);
        alarmStatus.setText(card.getSummary());
    }
}
