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
package arcus.app.subsystems.water.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.subsystem.water.model.WaterBadge;
import arcus.cornea.subsystem.water.model.DashboardCardModel;
import arcus.cornea.subsystem.water.WaterDashboardCardController;
import com.iris.client.capability.Valve;
import com.iris.client.capability.WaterHeater;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.cards.StatusCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;
import arcus.app.subsystems.water.cards.WaterCard;


public class WaterCardController extends AbstractCardController<SimpleDividerCard> implements WaterDashboardCardController.Callback {

    private ListenerRegistration mListener;
    private Context context;

    public WaterCardController(Context context) {
        super(context);
        this.context = context;

        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.WATER));
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mListener = WaterDashboardCardController.instance().setCallback(this);
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
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.WATER));
    }

    @Override
    public void showNoActivityCopy() {
        setCurrentCard(new StatusCard(getContext(),
                R.drawable.icon_service_water,
                getContext().getString(R.string.card_water_title),
                getContext().getString(R.string.no_climate_activity_to_show)));
    }

    @Override
    public void showSummary(@NonNull DashboardCardModel model) {
        WaterCard card = new WaterCard(getContext());

        String displayPrimary = "";
        String lowSalt = "";
        String shutOff = "";
        if (model.isBadgeAvailable()) {
            for (WaterBadge badge : model.getBadges()) {
                switch (badge.getType()) {
                    case WATER_SOFTENER:
                        if(!badge.getLabel().equals("")) {
                            int saltLevel = Integer.parseInt(badge.getLabel());
                            if (saltLevel <= 25) {
                                lowSalt = saltLevel + "%";
                            }
                        }
                        break;
                    case WATER_VALVE:
                        if (badge.getLabel().equals(Valve.VALVESTATE_CLOSED)) {
                            shutOff = context.getString(R.string.valve_shut_off);
                            break;
                        }
                }
            }
        }

        if(!shutOff.equals("")) {
            displayPrimary = shutOff;
            card.setImageValue(-1);
        } else if(model.getWaterHeaterWaterLevel() != null) {
            displayPrimary = String.format(context.getString(R.string.water_heater_card_set_to), model.getTemperature());
            if (model.getTemperature() == 60) {
                card.setDisplaySecondary(context.getString(R.string.water_heater_card_low_target));
            } else {
                if (model.isHeating()) {
                    card.setImageValue(R.drawable.dashboard_waterheat);
                } else {
                    card.setImageValue(-1);
                }
                switch (model.getWaterHeaterWaterLevel()) {
                    case WaterHeater.HOTWATERLEVEL_HIGH:
                        if(!lowSalt.equals("")) {
                            displayPrimary = lowSalt;
                            card.setDisplaySecondary("");
                            card.setImageValue(-1);
                        } else {
                            card.setDisplaySecondary(context.getString(R.string.water_heater_card_hot_water_available));
                        }
                        break;
                    case WaterHeater.HOTWATERLEVEL_MEDIUM:
                        card.setDisplaySecondary(context.getString(R.string.water_heater_card_hot_water_limited));
                        break;
                    case WaterHeater.HOTWATERLEVEL_LOW:
                        card.setDisplaySecondary(context.getString(R.string.water_heater_card_hot_water_unavailable));
                        break;
                }
            }
        } else if(!lowSalt.equals("")) {
            displayPrimary = lowSalt;
            card.setDisplaySecondary("");
            card.setImageValue(-1);
        }
        card.setDisplayPrimary(displayPrimary);
        setCurrentCard(card);
    }


}
