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
package arcus.app.subsystems.water.views;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.water.cards.WaterCard;


public class WaterCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;
    ImageView waterImage;
    Version1TextView primaryDisplay;
    Version1TextView secondaryDisplay;

    public WaterCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        context = view.getContext();

        waterImage = (ImageView) view.findViewById(R.id.state_image);
        primaryDisplay = (Version1TextView) view.findViewById(R.id.primary_display);
        secondaryDisplay = (Version1TextView) view.findViewById(R.id.secondary_display);
    }

    public void build(@NonNull WaterCard card) {
        serviceName.setText(context.getString(R.string.card_water_title));
        serviceImage.setImageResource(R.drawable.dashboard_water);

        if(card.getImageValue() <= 0) {
            waterImage.setVisibility(View.GONE);
        } else {
            waterImage.setVisibility(View.VISIBLE);
            waterImage.setImageResource(card.getImageValue());
        }
        if(TextUtils.isEmpty(card.getDisplayPrimary())) {
            primaryDisplay.setVisibility(View.GONE);
        } else {
            primaryDisplay.setText(card.getDisplayPrimary());
        }
        if(TextUtils.isEmpty(card.getDisplaySecondary())) {
            secondaryDisplay.setVisibility(View.GONE);
        } else {
            secondaryDisplay.setText(card.getDisplaySecondary());
        }
    }
}
