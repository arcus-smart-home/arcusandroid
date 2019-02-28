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
import arcus.cornea.utils.TemperatureUtils;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.WaterHeater;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;

import java.util.Map;

public class WaterHeaterCommand extends AbstractScheduleCommandModel {

    // Group name for all schedule events associated with garage door scheduling
    private final static String GROUP_ID = "WATER";

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);
    public static final int DEFAULT_SETPOINT = 55;

    public WaterHeaterCommand() {
        super(GROUP_ID, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setWaterHeaterTemp(DEFAULT_SETPOINT);
    }

    @Override
    public String getCommandAbstract() {
        return String.valueOf(getWaterHeaterTemp());
    }

    public void setWaterHeaterTemp(int nSetPoint){

        Map<String,Object> attribute = getAttributes();
        Double dTemp = TemperatureUtils.fahrenheitToCelsius(nSetPoint);
        attribute.put(WaterHeater.ATTR_SETPOINT,dTemp.doubleValue()  );
    }

    public int getWaterHeaterTemp() {

        Object setPoint = getAttributes().get(WaterHeater.ATTR_SETPOINT);
        Number number = (Number)setPoint;
        return  TemperatureUtils.roundCelsiusToFahrenheit(number.doubleValue());

    }




}
