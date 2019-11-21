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
package arcus.app.subsystems.alarm.security.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.security.cards.SecurityAlarmCard;


public class SecurityAlarmCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;

    public SecurityAlarmCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        context = view.getContext();
    }

    public void build(@NonNull SecurityAlarmCard card) {
        serviceName.setText(context.getString(R.string.card_security_alarm_title));
        serviceImage.setImageResource(R.drawable.icon_service_safetyalarm_small);
    }
}
