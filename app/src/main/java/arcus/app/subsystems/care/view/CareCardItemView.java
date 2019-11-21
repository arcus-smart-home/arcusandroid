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
package arcus.app.subsystems.care.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.care.cards.CareCard;



public class CareCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;
    Version1TextView lastEvent;
    View cardLayout;

    public CareCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        context = view.getContext();
        lastEvent = (Version1TextView) view.findViewById(R.id.last_event_timestamp);
        cardLayout = view.findViewById(R.id.flipper);
    }

    public void build(@NonNull CareCard card) {
        serviceName.setText(context.getString(R.string.card_care_title));
        serviceImage.setImageResource(R.drawable.dashboard_care);
        if(card.isAlerting()) {
            cardLayout.setBackgroundColor(context.getResources().getColor(R.color.care_alarm_purple));
        } else {
            cardLayout.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        }

        if(TextUtils.isEmpty(card.getLastActivityTimeStamp())) {
            lastEvent.setText("");
        } else {
            lastEvent.setText(card.getLastActivityTimeStamp());
        }
    }
}
