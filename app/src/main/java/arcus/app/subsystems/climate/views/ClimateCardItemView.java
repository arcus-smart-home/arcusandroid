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
package arcus.app.subsystems.climate.views;

import android.content.Context;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.climate.cards.ClimateCard;


public class ClimateCardItemView extends DashboardFlipViewHolder {

    private ImageView serviceImage;
    private Version1TextView serviceName;
    private Context context;
    private Version1TextView temperature;
    private Version1TextView temperatureDescription;
    private Version1TextView humidityDescription;

    public ClimateCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        context = view.getContext();
        temperature = (Version1TextView) view.findViewById(R.id.temperature);
        temperatureDescription= (Version1TextView) view.findViewById(R.id.temperature_description);
        humidityDescription = (Version1TextView) view.findViewById(R.id.humidity_description);

    }

    public void build(@NonNull ClimateCard card) {
        serviceName.setText(context.getString(R.string.card_climate_title));
        serviceImage.setImageResource(R.drawable.dashboard_climate);

        temperature.setText(card.getTempTitle());

        if(!TextUtils.isEmpty(card.getTempDescription())) {
            temperatureDescription.setText(card.getTempDescription());
            temperatureDescription.setVisibility(View.VISIBLE);
        } else {
            temperatureDescription.setVisibility(View.GONE);
        }

        if(!TextUtils.isEmpty(card.getHumidityDescription())) {
            humidityDescription.setText(StringUtils.getSuperscriptSpan(card.getHumidityDescription(),"%"));
            humidityDescription.setVisibility(View.VISIBLE);
        } else {
            humidityDescription.setVisibility(View.GONE);
        }
    }
}
