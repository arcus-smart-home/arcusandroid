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
import com.iris.client.capability.PetDoor;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;

import java.util.HashMap;
import java.util.Map;


public class PetDoorCommand extends AbstractScheduleCommandModel {

    // Group name for all schedule events associated with garage door scheduling
    private final static String DNL_GROUP_ID = "DOORS";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public PetDoorCommand() {
        super(DNL_GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setLockState(PetDoor.LOCKSTATE_AUTO);
    }

    @Override
    public String getCommandAbstract() {
        switch (getLockState()) {
            case PetDoor.LOCKSTATE_LOCKED:
                return ArcusApplication.getContext().getString(R.string.petdoor_mode_locked);
            case PetDoor.LOCKSTATE_UNLOCKED:
                return ArcusApplication.getContext().getString(R.string.petdoor_mode_unlocked);

            case PetDoor.LOCKSTATE_AUTO:
                default:
                return ArcusApplication.getContext().getString(R.string.petdoor_mode_auto);
        }
    }

    public String getLockState () {
        return getAttributes().get(PetDoor.ATTR_LOCKSTATE) == null ? PetDoor.LOCKSTATE_AUTO : getAttributes().get(PetDoor.ATTR_LOCKSTATE).toString();
    }

    public void setLockState (String lockState) {
        Map<String,Object> attribute = new HashMap<>();
        attribute.put(PetDoor.ATTR_LOCKSTATE, lockState);
        setAttributes(attribute);
    }

}
