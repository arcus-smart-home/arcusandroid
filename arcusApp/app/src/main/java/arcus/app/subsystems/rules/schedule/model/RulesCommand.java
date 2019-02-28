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
package arcus.app.subsystems.rules.schedule.model;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.Rule;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;


public class RulesCommand extends AbstractScheduleCommandModel {

    public enum State {ACTIVE, INACTIVE}

    // Group name for all schedule events associated with garage door scheduling
    private final static String DNL_GROUP_ID = "RULES";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public RulesCommand() {
        super(DNL_GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setRulesCommandState(State.INACTIVE);
    }

    public RulesCommand.State getRawState() {
        return Rule.CMD_ENABLE.equals(getCommandMessageType()) ? State.ACTIVE : State.INACTIVE;
    }

    @Override
    public String getCommandAbstract () {
        return isRuleStateActive() ? ArcusApplication.getContext().getString(R.string.rules_schedule_active) : ArcusApplication.getContext().getString(R.string.rules_schedule_inactivate);
    }

    public boolean isRuleStateActive() {
       return getRawState() == State.ACTIVE;
    }

    public void setRulesCommandState(RulesCommand.State state) {
        if (state == State.ACTIVE) {
            setCommandMessageType(Rule.CMD_ENABLE);
        } else {
            setCommandMessageType(Rule.CMD_DISABLE);
        }
    }
}