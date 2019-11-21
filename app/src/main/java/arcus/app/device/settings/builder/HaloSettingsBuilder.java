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
package arcus.app.device.settings.builder;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.View;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.style.OnClickActionSetting;

public class HaloSettingsBuilder implements SettingBuilder {

    HaloSettingsBuilder() {}

    private String title, description, abstractText;
    private Fragment fragment;

    public static HaloSettingsBuilder settingsFor() {
        return new HaloSettingsBuilder();
    }

    public HaloSettingsBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public HaloSettingsBuilder withDescription(String description) {
        this.description = description;
        return this;
    }

    public HaloSettingsBuilder withAbstractText(String abstractText) {
        this.abstractText = abstractText;
        return this;
    }

    public HaloSettingsBuilder clickOpensFragment(@NonNull Fragment fragment) {
        this.fragment = fragment;
        return this;
    }

    @Override public Setting build() {
        return new OnClickActionSetting(title, description, abstractText, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (fragment != null) {
                    BackstackManager.getInstance().navigateToFragment(fragment, true);
                }
            }
        });
    }

}
