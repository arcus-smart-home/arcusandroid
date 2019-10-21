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
import arcus.cornea.utils.TemperatureUtils;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.SpaceHeater;

import arcus.app.common.schedule.model.AbstractScheduleCommandModel;

import java.util.HashMap;
import java.util.Map;


public class SpaceHeaterCommand extends AbstractScheduleCommandModel {

    // Group name for all schedule events associated with garage door scheduling
    private final static String GROUP_ID = "CLIMATE";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public SpaceHeaterCommand() {
        super(GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setState(SpaceHeater.HEATSTATE_OFF);
        setSetPoint(75.0);
    }

    @Override
    public String getCommandAbstract() {
        return getState();
    }

    public String getState() {
        return (String) getAttributes().get(SpaceHeater.ATTR_HEATSTATE);
    }

    public void setState(String value) {
        Map<String,Object> attribute = getAttributes();
        if(attribute == null) {
            attribute = new HashMap<>();
        }
        attribute.put(SpaceHeater.ATTR_HEATSTATE, value.toUpperCase());
    }

    public double getSetPoint() {
        return TemperatureUtils.roundCelsiusToFahrenheit(((Number)getAttributes().get(SpaceHeater.ATTR_SETPOINT)).doubleValue());
    }

    public void setSetPoint(double setPoint) {
        Map<String,Object> attribute = getAttributes();
        if(attribute == null) {
            attribute = new HashMap<>();
        }
        attribute.put(SpaceHeater.ATTR_SETPOINT, TemperatureUtils.fahrenheitToCelsius(setPoint));
    }

    public int getMinTemperature() {
        Number temp = (Number)getAttributes().get(SpaceHeater.ATTR_MINSETPOINT);
        if(temp == null) {
            return 50;
        }
        return TemperatureUtils.celsiusToFahrenheit(temp.doubleValue()).intValue();
    }

    public int getMaxTemperature() {
        Number temp = (Number)getAttributes().get(SpaceHeater.ATTR_MAXSETPOINT);
        if(temp == null) {
            return 97;
        }
        return TemperatureUtils.celsiusToFahrenheit(temp.doubleValue()).intValue();
    }
}
