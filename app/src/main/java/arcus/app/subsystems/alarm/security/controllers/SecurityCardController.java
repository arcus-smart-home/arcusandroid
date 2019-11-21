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
package arcus.app.subsystems.alarm.security.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.subsystem.security.SecurityDashboardCardController;
import arcus.cornea.subsystem.security.model.AlarmStatus;
import arcus.cornea.subsystem.security.model.Trigger;
import com.iris.client.event.ListenerRegistration;
import arcus.app.common.cards.AlertCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;

import arcus.app.subsystems.alarm.security.cards.SecurityAlarmCard;


public class SecurityCardController extends AbstractCardController<SimpleDividerCard> implements SecurityDashboardCardController.Callback {

    private ListenerRegistration mListener;

    public SecurityCardController(Context context) {
        super(context);
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mListener = SecurityDashboardCardController.instance().setCallback(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void showUnsatisfiableCopy() {
    }

    @Override
    public void showAlarm(@NonNull Trigger trigger) {
        AlertCard alertCard = new AlertCard(getContext(), AlertCard.ALARM_SYSTEM.SECURITY);
        alertCard.setName(trigger.getName());
        alertCard.setDeviceId(trigger.getId());
        setCurrentCard(alertCard);
    }

    @Override
    public void showSummary(@NonNull AlarmStatus summary) {
        SecurityAlarmCard card = new SecurityAlarmCard(getContext());
        card.setState(summary.getState());
        card.setMode(summary.getMode());
        card.setDate(summary.getDate());
        setCurrentCard(card);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mListener.remove();
    }
}
