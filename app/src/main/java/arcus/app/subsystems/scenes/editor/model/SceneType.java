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
package arcus.app.subsystems.scenes.editor.model;

import androidx.annotation.StringRes;

import com.google.common.base.Strings;
import arcus.app.R;

public enum SceneType {
    LIGHT("light", R.string.scene_lights_and_switches_action_format),
    FAN("fan", R.string.scene_fans_action_format),
    LOCK("lock", R.string.scene_doors_action_format),
    WATERVALVE("watervalve", R.string.scene_valve_action_format),
    VALVE("valve", R.string.scene_valve_action_format),
    GARAGE("garage", R.string.scene_garagedoors_action_format),
    VENT("vent", R.string.scene_vents_action_format),
    BLIND("blind", R.string.scene_blinds_action_format),
    THERMOSTAT("thermostat", R.string.scene_thermostat_action_format),
    SECURITY("security", R.string.scene_security_action_format),
    CAMERA("camera", R.string.scene_camera_action_format),
    WATERHEATER("waterheater", R.string.scene_waterheater_action_format),
    SPACEHEATER("spaceheater", R.string.scene_spaceheater_action_format),
    UNKNOWN("UNKNOWN", R.string.water_heater_oops);

    private String sceneName;
    private @StringRes Integer topTextRes;

    SceneType(String name, Integer topTextRes) {
        this.sceneName = name;
        this.topTextRes = topTextRes;
    }

    public @StringRes Integer getHeadingText() {
        return topTextRes;
    }

    public String getName() {
        return this.sceneName;
    }

    public static SceneType fromName(String name) {
        if (Strings.isNullOrEmpty(name)) {
            return SceneType.UNKNOWN;
        }

        for (SceneType sceneType : SceneType.values()) {
            if (name.equalsIgnoreCase(sceneType.getName())) {
                return sceneType;
            }
        }

        return SceneType.UNKNOWN;
    }
}
