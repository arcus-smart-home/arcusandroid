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
package arcus.app.subsystems.climate.schedule.model;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.Vent;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;

import java.util.HashMap;
import java.util.Map;


public class VentCommand extends AbstractScheduleCommandModel {

    // Group name for all schedule events associated with garage door scheduling
    private final static String GROUP_ID = "CLIMATE";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public VentCommand() {
        super(GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setVentLevel(100.0);
    }

    @Override
    public String getCommandAbstract() {
        int ventLevel = (int) getVentLevel();
        String display = String.format("%d%% Open", ventLevel);
        return display;
    }

    public double getVentLevel() {
        return (double) getAttributes().get(Vent.ATTR_LEVEL);
    }

    public void setVentLevel (double openPercentage) {
        Map<String,Object> attribute = new HashMap<>();
        attribute.put(Vent.ATTR_LEVEL, openPercentage);
        setAttributes(attribute);
    }
}
