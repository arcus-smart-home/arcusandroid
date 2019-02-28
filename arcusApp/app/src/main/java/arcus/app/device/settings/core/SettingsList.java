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
package arcus.app.device.settings.core;

import java.util.ArrayList;
import java.util.List;


public class SettingsList {

    private List<Setting> mSettings;
    private Boolean useLightColorScheme = null;

    public SettingsList() {
        mSettings = new ArrayList<>();
    }

    public SettingsList(List<Setting> settings) {
        mSettings = settings;
    }

    public List<Setting> getSettings() {
        return mSettings;
    }

    public void setSettings(List<Setting> settings) {
        if (settings == null) return;

        mSettings = settings;
    }

    public void add(Setting setting) {
        if (mSettings == null) mSettings = new ArrayList<>();

        if (setting == null) return;

        mSettings.add(setting);
    }

    public void add(int location, Setting setting) {
        if (mSettings == null) mSettings = new ArrayList<>();

        if (setting == null) return;

        mSettings.add(location, setting);
    }

    public void remove(Setting setting) {
        if (mSettings == null) return;

        mSettings.remove(setting);
    }

    public void clear () {
        if (mSettings == null) return;

        mSettings.clear();
    }

    public void append(SettingsList settingsList) {
        if (mSettings == null) mSettings = new ArrayList<>();

        mSettings.addAll(settingsList.getSettings());
    }

    public Boolean isUseLightColorScheme() {
        return useLightColorScheme;
    }

    public void setUseLightColorScheme(Boolean useLightColorScheme) {
        this.useLightColorScheme = useLightColorScheme;
    }
}
