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
package arcus.app.subsystems.lightsnswitches.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.lightsnswitches.cards.LightsNSwitchesCard;

import java.util.Locale;


public class LightsNSwitchesCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;
    private View switchesLayout;
    private View dimmersLayout;
    private View lightsLayout;
    private Version1TextView switchesOn;
    private Version1TextView dimmersOn;
    private Version1TextView lightsOn;

    public LightsNSwitchesCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);

        switchesLayout = view.findViewById(R.id.switches_summary);
        dimmersLayout = view.findViewById(R.id.dimmers_summary);
        lightsLayout = view.findViewById(R.id.lights_summary);

        switchesOn = (Version1TextView) view.findViewById(R.id.switches_on);
        dimmersOn = (Version1TextView) view.findViewById(R.id.dimmers_on);
        lightsOn = (Version1TextView) view.findViewById(R.id.lights_on);


        this.context = view.getContext();
    }

    public void build(@NonNull LightsNSwitchesCard card) {
        serviceName.setText(context.getString(R.string.card_lights_and_switches_title));
        serviceImage.setImageResource(R.drawable.dashboard_lightsswitches);

        switchesLayout.setVisibility(card.getSummary().getSwitchesOn() > 0 ? View.VISIBLE : View.GONE);
        dimmersLayout.setVisibility(card.getSummary().getDimmersOn() > 0 ? View.VISIBLE : View.GONE);
        lightsLayout.setVisibility(card.getSummary().getLightsOn() > 0 ? View.VISIBLE : View.GONE);

        switchesOn.setText(String.format(Locale.getDefault(), "%d", card.getSummary().getSwitchesOn()));
        dimmersOn.setText(String.format(Locale.getDefault(), "%d", card.getSummary().getDimmersOn()));
        lightsOn.setText(String.format(Locale.getDefault(), "%d", card.getSummary().getLightsOn()));
    }
}
