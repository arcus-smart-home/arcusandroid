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
package arcus.app.device.model;

import androidx.annotation.NonNull;

import arcus.app.common.utils.StringUtils;


public enum DeviceType {

    VENT("vent"),
    SWITCH("switch"),
    DIMMER("dimmer"),
    THERMOSTAT("thermostat"),
    SMOKECO("smokeco"),
    SMOKE("smoke"),
    CONTACT("contact"),
    TILT_SENSOR("tilt"),
    WATER_HEATER("waterheater"),
    WATER_LEAK_SENSOR("waterleak"),
    WATER_VALVE("watervalve"),
    WATER_SOFTENER("watersoftener"),
    IRRIGATION("irrigation"),
    PENDANT("pendant"),
    KEYFOB("keyfob"),
    KEYPAD("keypad"),
    MOTION_SENSOR("motion"),
    BUTTON("button"),
    LIGHT("light"),
    FAN_CONTROL("fancontrol"),
    GARAGE_DOOR("garagedoor"),
    GENIE_GARAGE_DOOR("garagedoor"),
    GARAGE_DOOR_CONTROLLER("garagedoorcontroller"),
    GENIE_GARAGE_DOOR_CONTROLLER("geniealaddincontroller"),
    LOCK("lock"),
    GLASS_BREAK_DETECTOR("glassbreak"),
    SIREN("siren"),
    MAIN_HUB("hub"),
    CAMERA("camera"),
    PET_DOOR("petdoor"),
    TCC_THERM("tccthermostat"),
    V1_RANGE_EXTENDER("rangeextender"),
    ACCESSORY("accessory"),
    SOMFYV1BRIDGE("somfyv1bridge"),
    SOMFYV1BLINDS("somfyv1blind"),
    ALEXA("alexa"),
    SPACE_HEATER("spaceheater"),
    HUE_BRIDGE("huebridge"),
    HUE_FALLBACK("huefallback"),
    HALO("halo"),
    SHADE("shade"),
    GOOGLE_ASSISTANT("GoogleAssistant"),
    NEST_THERMOSTAT("NestThermostat"),
    LUTRON_BRIDGE("LutronBridge"),
    NOT_SUPPORTED("not supported");

    private final String devTypeHint;

    DeviceType(String devTypeHint) {
        this.devTypeHint = devTypeHint;
    }

    public String getHint() {
        return this.devTypeHint;
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    public static DeviceType fromHint (@NonNull String devTypeHint) {

        if (devTypeHint == null){
            return NOT_SUPPORTED;
        }

        for (DeviceType thisDevType : DeviceType.values()) {
               try {
                    if (thisDevType.getHint().equalsIgnoreCase(StringUtils.sanitize(devTypeHint))) {
                        return thisDevType;
                    }
                } catch (Exception ignored) {} // What are we catching here?
            }
        return NOT_SUPPORTED;
    }

    public boolean isCloudConnected() {
        return this == NEST_THERMOSTAT || this == TCC_THERM;
    }
}
