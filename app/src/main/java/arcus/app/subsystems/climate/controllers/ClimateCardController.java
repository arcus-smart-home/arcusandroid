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
package arcus.app.subsystems.climate.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.subsystem.climate.ClimateDashboardCardController;
import arcus.cornea.subsystem.climate.model.ClimateBadge;
import arcus.cornea.subsystem.climate.model.DashboardCardModel;
import com.iris.client.event.ListenerRegistration;

import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.climate.cards.ClimateCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;


public class ClimateCardController extends AbstractCardController<SimpleDividerCard> implements ClimateDashboardCardController.Callback {

    private ListenerRegistration mListener;

    public ClimateCardController(Context context) {
        super(context);

        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.CLIMATE));
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mListener = ClimateDashboardCardController.instance().setCallback(this);
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
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.CLIMATE));
    }

    @Override
    public void showNoActivityCopy() {
        ClimateCard card = new ClimateCard(getContext());
        card.setClimateDevicesEnabled(false);
        setCurrentCard(card);
    }

    @Override
    public void showSummary(@NonNull DashboardCardModel model) {
        ClimateCard card = new ClimateCard(getContext());


        if(model.getPrimaryTemperatureDeviceId() != null) {
            card.setPrimaryTemperatureDeviceId(model.getPrimaryTemperatureDeviceId());
            if(model.isPrimaryTemperatureOffline()){
                card.setIsPrimaryTemperatureOffline(true);
            }else {
                card.setTempTitle(model.getTemperature() + "º");
                card.setTempDescription(model.getThermostatLabel());
            }
        }

        card.setIsPrimaryTemperatureCloudDevice(model.isTemperatureCloudDevice());

        if (model.isBadgeAvailable()) {
            for (ClimateBadge badge : model.getBadges()) {
                switch (badge.getType()) {
                    case HUMIDITY:
                        card.setHumidityDescription(badge.getLabel());
                        break;
                    case VENT:
                        card.setVentDescription(badge.getLabel());
                        break;
                    case FAN:
                        card.setFanDescription(badge.getLabel());
                        break;
                    case HEATER:
                        card.setHeaterDescription(badge.getLabel());
                }
            }
        }

        setCurrentCard(card);
    }
}
