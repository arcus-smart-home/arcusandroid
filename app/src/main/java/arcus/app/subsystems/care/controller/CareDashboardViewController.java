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
package arcus.app.subsystems.care.controller;

import android.content.Context;
import androidx.annotation.Nullable;
import android.text.SpannableString;
import android.text.TextUtils;

import arcus.cornea.subsystem.care.CareDashboardModelController;
import arcus.cornea.subsystem.care.model.AlarmState;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.utils.StringUtils;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.care.cards.CareCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;

import java.util.Date;

public class CareDashboardViewController
      extends AbstractCardController<SimpleDividerCard>
      implements CareDashboardModelController.Callback {

    private CareCard dashboardCareCard;
    private ListenerRegistration cardListener;

    public CareDashboardViewController(Context context) {
        super(context);
        showLearnMore();
    }

    @Nullable @Override public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override public void removeCallback() {
        super.removeCallback();
        cardListener = Listeners.clear(cardListener);
        CareDashboardModelController.instance().clearHistoryListener();
    }

    @Override public void setCallback(Callback delegate) {
        super.setCallback(delegate);
        cardListener = CareDashboardModelController.instance().setCallback(this);
    }

    @Override public void showSummary(AlarmState alarmState) {
        if (dashboardCareCard == null) {
            dashboardCareCard = new CareCard(getContext());
        }

        dashboardCareCard.setIsAlerting(false);
        dashboardCareCard.setActivityLines(alarmState.getEvents());
        dashboardCareCard.setAlarmMode(alarmState.getAlarmMode());
        dashboardCareCard.setActiveBehaviors(alarmState.getActiveBehaviors());
        dashboardCareCard.setTotalBehaviors(alarmState.getTotalBehaviors());

        setCurrentCard(dashboardCareCard);
    }

    @Override public void showAlerting(AlarmState alarmState) {
        if (dashboardCareCard == null) {
            dashboardCareCard = new CareCard(getContext());
        }

        dashboardCareCard.setIsAlerting(true);
        dashboardCareCard.setCausedByActor(alarmState.getAlertActor());
        dashboardCareCard.setCausedByText(alarmState.getAlertCause());

        setCurrentCard(dashboardCareCard);
    }

    @Override public void showLearnMore() {
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.CARE));
    }

    @Override
    public void updateLastEvent(Date lastEvent) {
        if (dashboardCareCard == null) {
            dashboardCareCard = new CareCard(getContext());
        }
        SpannableString lastEventTime = StringUtils.getDashboardDateString(lastEvent);
        if(dashboardCareCard.getTotalBehaviors() > 0) {
            lastEventTime = new SpannableString(TextUtils.concat(lastEventTime, "    "+dashboardCareCard.getAlarmMode()));
        }
        dashboardCareCard.setLastActivity(lastEventTime);

        setCurrentCard(dashboardCareCard);
    }
}
