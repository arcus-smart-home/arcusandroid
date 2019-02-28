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
package arcus.app.device.details.model;

import arcus.cornea.device.thermostat.ThermostatMode;
import com.iris.client.capability.Thermostat;
import arcus.app.ArcusApplication;
import arcus.app.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public enum ThermostatOperatingMode {
    HEAT(0, R.string.hvac_heat_title, R.string.hvac_heat_title, Thermostat.HVACMODE_HEAT),
    COOL(1, R.string.hvac_cool_title, R.string.hvac_cool_title, Thermostat.HVACMODE_COOL),
    AUTO(2, R.string.hvac_auto_title, R.string.hvac_heatcool_title, Thermostat.HVACMODE_AUTO),
    ECO(3, R.string.hvac_eco_title, R.string.hvac_eco_title, Thermostat.HVACMODE_ECO),
    OFF(4, R.string.hvac_off_title, R.string.hvac_off_title, Thermostat.HVACMODE_OFF);

    private final int stringResId;
    private final int nestStringResId;
    private final String platformName;
    private final int displayOrder;

    ThermostatOperatingMode(int displayOrder, int displayStringResId, int nestStringResId, String platformName) {
        this.displayOrder = displayOrder;
        this.stringResId = displayStringResId;
        this.nestStringResId = nestStringResId;
        this.platformName = platformName;
    }

    public int getStringResId(boolean isNestDevice) {
        return isNestDevice ? this.nestStringResId : this.stringResId;
    }

    public String getPlatformName() {
        return this.platformName;
    }

    public static ThermostatOperatingMode fromDisplayString(String displayString) {
        for (ThermostatOperatingMode thisMode : ThermostatOperatingMode.values()) {
            if (displayString.equalsIgnoreCase(thisMode.name()) ||
                    displayString.equalsIgnoreCase(ArcusApplication.getContext().getString(thisMode.stringResId)) ||
                    displayString.equalsIgnoreCase(ArcusApplication.getContext().getString(thisMode.nestStringResId))) {
                return thisMode;
            }
        }

        throw new IllegalArgumentException("No such thermostat operating mode named " + displayString);
    }

    public static List<ThermostatMode> toThermostatModes(List<ThermostatOperatingMode> opModes) {
        ArrayList<ThermostatMode> modes = new ArrayList<>();

        for (ThermostatOperatingMode thisOpMode : opModes) {
            modes.add(ThermostatMode.valueOf(thisOpMode.name()));
        }

        return modes;
    }

    public static List<ThermostatOperatingMode> fromThermostatModes(Collection<ThermostatMode> modes) {
        List<ThermostatOperatingMode> operatingModes = new ArrayList<>();
        for (ThermostatMode thisMode : modes) {
            operatingModes.add(fromThermostatMode(thisMode));
        }

        Collections.sort(operatingModes, getThermostatOperatingModeComparator());
        return operatingModes;
    }

    public static ThermostatOperatingMode fromThermostatMode(ThermostatMode thermostatMode) {
        switch (thermostatMode) {
            case OFF: return OFF;
            case ECO: return ECO;
            case HEAT: return HEAT;
            case COOL: return COOL;
            case AUTO: return AUTO;
            default: throw new IllegalArgumentException("Bug! Unimplemented thermostat mode.");
        }
    }

    public ThermostatMode thermostatMode() {
        switch (this) {
            case HEAT: return ThermostatMode.HEAT;
            case COOL: return ThermostatMode.COOL;
            case AUTO: return ThermostatMode.AUTO;
            case ECO: return ThermostatMode.ECO;
            case OFF: return ThermostatMode.OFF;

            default: throw new IllegalArgumentException("Bug! Unimplemented thermostat operating mode.");
        }
    }

    public static ThermostatOperatingMode fromPlatformValue(String platformName) {
        for (ThermostatOperatingMode value : values()) {
            if (value.platformName.equalsIgnoreCase(platformName)) {
                return value;
            }
        }

        throw new IllegalArgumentException("Bug! No such thermostat operating mode called: " + platformName);
    }

    public static Comparator<ThermostatOperatingMode> getThermostatOperatingModeComparator() {
        return new Comparator<ThermostatOperatingMode>() {
            @Override
            public int compare(ThermostatOperatingMode t0, ThermostatOperatingMode t1) {
                return Integer.compare(t0.displayOrder, t1.displayOrder);
            }
        };
    }

}
