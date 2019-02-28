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
package arcus.app.subsystems.lightsnswitches.cards;

import android.content.Context;

import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesSummary;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;


public class LightsNSwitchesCard extends SimpleDividerCard {

    public final static String TAG = ServiceCard.LIGHTS_AND_SWITCHES.toString();
    private LightsNSwitchesSummary summary;

    public LightsNSwitchesCard(Context context, LightsNSwitchesSummary summary) {
        super(context);
        setTag(TAG);
        showDivider();

        this.summary = summary;
    }

    @Override
    public int getLayout() {
        return R.layout.card_lightsnswitches;
    }

    public LightsNSwitchesSummary getSummary () {
        return summary;
    }
}
