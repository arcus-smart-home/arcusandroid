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
package arcus.app.subsystems.lawnandgarden.views;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.lawnandgarden.cards.LawnAndGardenCard;

import java.util.Date;


public class LawnAndGardenCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;
    View zoneCount;
    Version1TextView zoneCountText;
    ImageView timeImage;

    public LawnAndGardenCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        this.context = view.getContext();
        zoneCount = view.findViewById(R.id.zonecount);
        zoneCountText = (Version1TextView) view.findViewById(R.id.water_zone_count);
        timeImage = (ImageView) view.findViewById(R.id.time_image);
    }

    public void build(@NonNull LawnAndGardenCard card) {
        serviceName.setText(context.getString(R.string.card_lawn_and_garden_title));
        serviceImage.setImageResource(R.drawable.dashboard_lawngarden);

        if(card.getCurrentlyWaterZoneCount() ==0) {
            timeImage.setVisibility(View.GONE);
            if(card.getNextEventTime() != 0) {
                zoneCount.setVisibility(View.VISIBLE);
                zoneCountText.setText(StringUtils.getDateStringDayAndTime(new Date(card.getNextEventTime())));
            } else {
                zoneCount.setVisibility(View.GONE);
            }
        } else {
            zoneCount.setVisibility(View.VISIBLE);
            if(card.getCurrentlyWaterZoneCount() != 1) {
                zoneCountText.setText(Integer.toString(card.getCurrentlyWaterZoneCount()));
            } else {
                zoneCountText.setText("");
            }
            timeImage.setVisibility(View.VISIBLE);
        }
    }
}
