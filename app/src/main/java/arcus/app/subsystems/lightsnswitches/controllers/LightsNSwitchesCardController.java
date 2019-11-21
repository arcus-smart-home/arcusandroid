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
package arcus.app.subsystems.lightsnswitches.controllers;

import android.content.Context;
import androidx.annotation.Nullable;

import arcus.cornea.subsystem.lightsnswitches.LightsNSwitchesDashCardController;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesSummary;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;
import arcus.app.subsystems.lightsnswitches.cards.LightsNSwitchesCard;


public class LightsNSwitchesCardController extends AbstractCardController<SimpleDividerCard> implements LightsNSwitchesDashCardController.Callback {

    public LightsNSwitchesCardController(Context context) {
        super(context);

        showLearnMore();
        LightsNSwitchesDashCardController.instance().setCallback(this);
    }

    @Nullable
    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void showLearnMore() {
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.LIGHTS_AND_SWITCHES));
    }

    @Override
    public void showSummary(LightsNSwitchesSummary summary) {
        setCurrentCard(new LightsNSwitchesCard(getContext(), summary));
    }
}
