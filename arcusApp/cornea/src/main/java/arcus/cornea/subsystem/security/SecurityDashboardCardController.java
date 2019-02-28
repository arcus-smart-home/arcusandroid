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
package arcus.cornea.subsystem.security;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.security.model.AlarmStatus;
import arcus.cornea.subsystem.security.model.Trigger;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.Model;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.RuleModel;
import com.iris.client.model.SubsystemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SecurityDashboardCardController extends BaseSecurityController<SecurityDashboardCardController.Callback> {

    public interface Callback {
        void showUnsatisfiableCopy();
        void showAlarm(Trigger trigger);
        void showSummary(AlarmStatus summary);
    }

    private static final SecurityDashboardCardController instance = new SecurityDashboardCardController(
            SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE)
    );
    private final Comparator<Trigger> triggerSorter = new Comparator<Trigger>() {
        @Override
        public int compare(Trigger lhs, Trigger rhs) {
            if(lhs.getTriggeredSince() == null) {
                return rhs.getTriggeredSince() == null ? 0 : -1;
            }
            if(rhs.getTriggeredSince() == null) {
                return 1;
            }
            return lhs.getTriggeredSince().compareTo(rhs.getTriggeredSince());
        }
    };

    public static SecurityDashboardCardController instance() {
        return instance;
    }

    SecurityDashboardCardController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
        init();
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(
            changes.contains(SecuritySubsystem.ATTR_AVAILABLE) ||
            changes.contains(SecuritySubsystem.ATTR_ALARMSTATE) ||
            changes.contains(SecuritySubsystem.ATTR_ALARMMODE)
        ) {
            updateView();
        }
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        updateView();
    }

    @Override
    protected void updateView(Callback callback) {
        if (!RuleModelProvider.instance().isLoaded()) {
            RuleModelProvider.instance().load().onSuccess(new Listener<List<RuleModel>>() {
                @Override
                public void onEvent(List<RuleModel> ruleModels) {
                    updateView();
                }
            });
        }

        // If the alarm is going off (button/rule) this will show unsatisfiable if they dont' have other devices....
        // Both iOS and Android currently have this behavior. How to handle?
        SecuritySubsystem subsystem = getSecuritySubsystem();
        if(subsystem == null || !Boolean.TRUE.equals(subsystem.getAvailable())) {
            callback.showUnsatisfiableCopy();
            return;
        }

        String state = subsystem.getAlarmState();
        switch(state) {
        case SecuritySubsystem.ALARMSTATE_DISARMED:
        case SecuritySubsystem.ALARMSTATE_CLEARING:
            callback.showSummary( getDisarmedStatus(subsystem) );
            break;

        case SecuritySubsystem.ALARMSTATE_ARMING:

            callback.showSummary( getArmingStatus(subsystem) );
            break;

        case SecuritySubsystem.ALARMSTATE_ARMED:
            callback.showSummary( getArmedStatus(subsystem) );
            break;

        case SecuritySubsystem.ALARMSTATE_ALERT:
        case SecuritySubsystem.ALARMSTATE_SOAKING:
            callback.showAlarm( getTrigger(subsystem) );
            break;

        default:
            super.updateView(callback);
        }

    }

    protected AlarmStatus getDisarmedStatus(SecuritySubsystem subsystem) {
        AlarmStatus status = new AlarmStatus();
        status.setState(AlarmStatus.STATE_DISARMED);
        status.setMode(AlarmStatus.MODE_OFF);
        status.setDate(date(subsystem.getLastDisarmedTime()));
        return status;
    }

    protected AlarmStatus getArmingStatus(SecuritySubsystem subsystem) {
        AlarmStatus status = new AlarmStatus();
        status.setState(AlarmStatus.STATE_ARMING);
        status.setMode(subsystem.getAlarmMode());
        return status;
    }

    protected AlarmStatus getArmedStatus(SecuritySubsystem subsystem) {
        AlarmStatus status = new AlarmStatus();
        status.setState(AlarmStatus.STATE_ARMED);
        status.setMode(subsystem.getAlarmMode());
        status.setDate(date(subsystem.getLastArmedTime()));
        return status;
    }

    protected Trigger getTrigger(SecuritySubsystem security) {
        List<Trigger> triggers = new ArrayList<>();
        Map<String, Date> currentTriggers = security.getLastAlertTriggers();
        if (currentTriggers == null || currentTriggers.isEmpty()) {
            return Trigger.empty();
        }

        for (Map.Entry<String, Date> item : currentTriggers.entrySet()) {
            Model model = CorneaClientFactory.getModelCache().get(item.getKey());
            triggers.add(Trigger.wrap(model, item.getValue()));
        }

        Collections.sort(triggers, triggerSorter); // Sorting so most recent is at last index
        return triggers.isEmpty() ? Trigger.empty() : triggers.get(0);
    }
}
