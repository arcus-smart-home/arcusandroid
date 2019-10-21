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
package arcus.cornea.subsystem.climate.model;

import arcus.cornea.device.thermostat.ThermostatMode;


public class ThermostatBadge {
    private ThermostatMode mode;
    private int coolSetPoint;
    private int heatSetPoint;
    private int temperature;

    public ThermostatMode getMode() {
        return mode;
    }

    public void setMode(ThermostatMode mode) {
        this.mode = mode;
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

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }
}
