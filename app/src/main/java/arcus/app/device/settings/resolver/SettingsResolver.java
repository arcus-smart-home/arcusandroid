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
package arcus.app.device.settings.resolver;

import android.app.Activity;
import androidx.annotation.Nullable;

import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

/**
 * An object which knows how to determine the settings available to a given device.
 */
public interface SettingsResolver {

    /**
     * Determines the settings available to the device represented by the given model.
     *
     * @param model
     * @return A {@link Setting}, which may be a form of {@link arcus.app.device.settings.core.CompositeSetting}
     * for the given device. Returns null if the device has no available settings.
     */
    @Nullable
    SettingsList getSettings(Activity context, SettingChangedParcelizedListener listener, Object model);
}
