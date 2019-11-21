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
package arcus.app.subsystems.alarm.safety.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.subsystem.safety.DashboardCardController;
import arcus.cornea.subsystem.safety.model.Alarm;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.cards.AlertCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;

import arcus.app.subsystems.alarm.safety.cards.SafetyAlarmCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class SafetyCardController extends AbstractCardController<SimpleDividerCard> implements DashboardCardController.Callback {

    private ListenerRegistration mCallback;
    private Logger logger = LoggerFactory.getLogger(SafetyCardController.class);
    private SafetyAlarmCard safetyAlarmCard;
    private AlertCard alertCard;

    public SafetyCardController(Context context) {
        super(context);
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mCallback = DashboardCardController.instance().setCallback(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void showUnsatisfiableCopy() {
    }

    @Override
    public void showAlarm(@NonNull List<Alarm> alarm) {
        logger.debug("Got alarm: {}", alarm);
        if(alertCard == null){
            alertCard = new AlertCard(getContext(), AlertCard.ALARM_SYSTEM.SAFETY);
        }

        if(alarm == null || alarm.size() == 0) {
            alertCard.setName(getContext().getString(R.string.device_name_unknown));
            alertCard.setTriggeredBy(String.format("Triggered by %s", getContext().getString(R.string.device_name_unknown)));
            alertCard.setDeviceId("");
        }
        else {
            Alarm cause = alarm.get(0);
            if (cause.getMessage().contains("CO")) {
                alertCard.setName(getContext().getString(R.string.safety_alarm_co));
            }
            else if (cause.getMessage().contains("SMOKE")) {
                alertCard.setName(getContext().getString(R.string.safety_alarm_smoke));
            }
            else if (cause.getMessage().contains("WATER")) {
                alertCard.setName(getContext().getString(R.string.safety_alarm_water_leak));
            }
            else {
                alertCard.setName(getContext().getString(R.string.safety_alarm));
            }
            alertCard.setTriggeredBy(String.format("Triggered by %s", cause.getName()));
            alertCard.setDeviceId(cause.getDevId());
        }

        setCurrentCard(alertCard);
    }

    @Override
    public void showSummary(String summary) {
        logger.debug("Got alarm summary: {}", summary);
        if(safetyAlarmCard == null){
            safetyAlarmCard = new SafetyAlarmCard(getContext());
        }
        safetyAlarmCard.setSummary(summary);
        setCurrentCard(safetyAlarmCard);
    }

    @Override
    public void removeCallback() {
        // This is a big mess... home fragment being paused removes the callback which means that if
        // a user adds a new place, the subsystem change will be ignored by the card. So, anytime
        // we feel the need to remove the callback, assume the subsystem has been invalidated.
        showUnsatisfiableCopy();

        super.removeCallback();
        mCallback.remove();
    }
}
