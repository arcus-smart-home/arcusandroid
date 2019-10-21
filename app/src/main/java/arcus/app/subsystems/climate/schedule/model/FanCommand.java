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
import com.iris.client.capability.Fan;
import com.iris.client.capability.Switch;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;

import java.util.HashMap;
import java.util.Map;


public class FanCommand extends AbstractScheduleCommandModel {

    // Group name for all schedule events associated with garage door scheduling
    private final static String GROUP_ID = "CLIMATE";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public FanCommand() {
        super(GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setFanSpeed("LOW");
    }

    @Override
    public String getCommandAbstract() {
        if(hasSwitch() && !getFanState()) {
            return "OFF";
        }
        return getFanSpeed();
    }

    public boolean hasSwitch() {
        if(getAttributes().get(Switch.ATTR_STATE) == null) {
            return false;
        }
        return true;
    }

    public boolean getFanState() {
        return "ON".equals(getAttributes().get(Switch.ATTR_STATE));
    }

    public void setFanState (boolean isOn) {
        Map<String,Object> attribute = getAttributes();
        if(attribute == null) {
            attribute = new HashMap<>();
        }
        attribute.put(Switch.ATTR_STATE, isOn ? Switch.STATE_ON: Switch.STATE_OFF);
        if (!isOn) { // If the fan is off,
            attribute.put(Fan.ATTR_SPEED, 0.0);
        }
        setAttributes(attribute);
        getAttributes();
    }

    public String getFanSpeed() {
        Number speedDouble = (Number) getAttributes().get(Fan.ATTR_SPEED);
        if (speedDouble == null) {
            setFanSpeed("LOW");
            return "LOW";
        }

        int speed = speedDouble.intValue();
        if (speed == 0) {
            return "OFF";
        }
        else if(speed == 1) {
            return "LOW";
        }
        else if(speed == 2) {
            return "MEDIUM";
        }
        else if(speed == 3) {
            return "HIGH";
        }

        return "OFF";
    }

    public void setFanSpeed(String speed) {
        Map<String,Object> attribute = getAttributes();
        if(attribute == null) {
            attribute = new HashMap<>();
        }
        if(hasSwitch() && !getFanState()) {
            speed = "OFF";
        }

        if("LOW".equals(speed)) {
            attribute.put(Fan.ATTR_SPEED, 1.0);
        }
        else if("MEDIUM".equals(speed)) {
            attribute.put(Fan.ATTR_SPEED, 2.0);
        }
        else if("HIGH".equals(speed)) {
            attribute.put(Fan.ATTR_SPEED, 3.0);
        }
        else {
            attribute.put(Fan.ATTR_SPEED, 0.0);
        }
        setAttributes(attribute);
    }
}
