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
package arcus.cornea.subsystem.water.model;

import arcus.cornea.device.thermostat.ThermostatMode;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;

import java.io.Serializable;
import java.util.Set;


public class ScheduledWaterSetPoint implements Serializable {
    private String id;
    private TimeOfDay timeOfDay;
    private ThermostatMode mode;
    private int coolSetPoint;
    private int heatSetPoint;
    private Set<DayOfWeek> repeatsOn;
    private String repetitionText;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public ThermostatMode getMode() {
        return mode;
    }

    public void setMode(ThermostatMode mode) {
        this.mode = mode;
    }

    public boolean isAutoSetPoint() {
        return this.mode == ThermostatMode.AUTO;
    }

    public int getCurrentSetPoint() {
        switch(mode) {
        case COOL:
            return getCoolSetPoint();
        case HEAT:
            return getHeatSetPoint();

        default:
            // invalid
            return 0;
        }
    }

    public void setCurrentSetPoint(int setPoint) {
        switch(mode) {
        case COOL:
            setCoolSetPoint(setPoint);
            return;

        case HEAT:
            setHeatSetPoint(setPoint);
            return;

        default:
            throw new IllegalArgumentException("Can't use setCurrentSetPoint when in OFF or AUTO mode");
        }
    }

    public int getCoolSetPoint() {
        return coolSetPoint;
    }

    public void setCoolSetPoint(int coolSetPoint) {
        this.coolSetPoint = coolSetPoint;
    }

    public int getHeatSetPoint() {
        return heatSetPoint;
    }

    public void setHeatSetPoint(int heatSetPoint) {
        this.heatSetPoint = heatSetPoint;
    }

    public boolean isRepeating() {
        return repeatsOn.size() > 1;
    }

    public Set<DayOfWeek> getRepeatsOn() {
        return repeatsOn;
    }

    public void setRepeatsOn(Set<DayOfWeek> repeatsOn) {
        this.repeatsOn = repeatsOn;
    }

    public String getRepetitionText() {
        return repetitionText;
    }

    public void setRepetitionText(String repetitionText) {
        this.repetitionText = repetitionText;
    }

    @Override
    public String toString() {
        return "ScheduledWaterSetPoint{" +
                "id='" + id + '\'' +
                ", timeOfDay=" + timeOfDay +
                ", mode=" + mode +
                ", coolSetPoint=" + coolSetPoint +
                ", heatSetPoint=" + heatSetPoint +
                ", repeatsOn=" + repeatsOn +
                ", repetitionText='" + repetitionText + '\'' +
                '}';
    }
}
