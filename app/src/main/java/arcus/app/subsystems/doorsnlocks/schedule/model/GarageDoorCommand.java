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
package arcus.app.subsystems.doorsnlocks.schedule.model;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.MotorizedDoor;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;

import java.util.HashMap;
import java.util.Map;


public class GarageDoorCommand extends AbstractScheduleCommandModel {

    // Group name for all schedule events associated with garage door scheduling
    private final static String DNL_GROUP_ID = "DOORS";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public GarageDoorCommand() {
        super(DNL_GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setGarageDoorStateOpen(true);
    }

    @Override
    public String getCommandAbstract() {
        String open = ArcusApplication.getArcusApplication().getString(R.string.doors_and_locks_state_open);
        String close = ArcusApplication.getArcusApplication().getString(R.string.doors_and_locks_state_close);

        return isGarageDoorStateOpen() ? open : close;
    }

    public boolean isGarageDoorStateOpen() {
        return getAttributes() != null && MotorizedDoor.DOORSTATE_OPEN.equals(getAttributes().get(MotorizedDoor.ATTR_DOORSTATE));
    }

    public void setGarageDoorStateOpen (boolean isOpen) {
        Map<String,Object> attribute = new HashMap<>();
        attribute.put(MotorizedDoor.ATTR_DOORSTATE, isOpen ? MotorizedDoor.DOORSTATE_OPEN : MotorizedDoor.DOORSTATE_CLOSED);
        setAttributes(attribute);
    }
}
