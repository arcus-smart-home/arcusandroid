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
package arcus.app.subsystems.water.schedule.model;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.Valve;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;

import java.util.Map;

public class WaterValveCommand extends AbstractScheduleCommandModel {

    // Group name for all schedule events associated with garage door scheduling
    private final static String GROUP_ID = "WATER";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public WaterValveCommand() {
        super(GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setWaterValveState(false);
    }

    @Override
    public String getCommandAbstract() {

        return getWaterValveState();
    }



    public String getWaterValveState() {
        return (String) getAttributes().get(Valve.ATTR_VALVESTATE);


    }

    public boolean getWaterValveBooleanState() {



       String strState = (String) getAttributes().get(Valve.ATTR_VALVESTATE);
        if (strState == null){
            return false;
        }
        switch (strState){
            case "OPEN":
                return true;
            case "OPENING":
                return true;
            case "CLOSED":
                return false;
            case "CLOSING":
                return false;
            default:
                return false;

        }


    }


    public void setWaterValveState(boolean bState){

        Map<String,Object> attribute = getAttributes();

        if (bState){
            attribute.put(Valve.ATTR_VALVESTATE, Valve.VALVESTATE_OPEN);
        } else {
            attribute.put(Valve.ATTR_VALVESTATE, Valve.VALVESTATE_CLOSED);
        }

    }

}
