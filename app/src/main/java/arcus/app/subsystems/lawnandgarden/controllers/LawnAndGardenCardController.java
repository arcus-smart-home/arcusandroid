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
package arcus.app.subsystems.lawnandgarden.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.subsystem.lawnandgarden.LawnAndGardenDashboardCardController;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenDashboardCardModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.cards.StatusCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.lawnandgarden.cards.LawnAndGardenCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;


public class LawnAndGardenCardController extends AbstractCardController<SimpleDividerCard> implements LawnAndGardenDashboardCardController.Callback {

    private ListenerRegistration mListener;

    public LawnAndGardenCardController(Context context) {
        super(context);

        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.LAWN_AND_GARDEN));
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mListener = LawnAndGardenDashboardCardController.instance().setCallback(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mListener.remove();
    }

    @Override
    public void showUnsatisfiableCopy() {
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.LAWN_AND_GARDEN));
    }

    @Override
    public void showNoActivityCopy() {
        setCurrentCard(new StatusCard(getContext(),
                R.drawable.icon_service_lawngarden,
                getContext().getString(R.string.lawn_and_garden),
                getContext().getString(R.string.no_lawn_and_garden_activity_to_show)));
    }

    @Override
    public void showSummary(@NonNull LawnAndGardenDashboardCardModel model) {
        LawnAndGardenCard card = new LawnAndGardenCard(getContext());

        card.setTitle(model.getTitle());
        card.setCurrentlyWaterZoneCount(model.getCurrentlyWateringZoneCount());
        card.setNextEventTitle(model.getNextEventTitle());
        card.setNextEventTime(model.getNextEventTime());
        card.setDeviceId(model.getDeviceId());

        setCurrentCard(card);
    }
}
