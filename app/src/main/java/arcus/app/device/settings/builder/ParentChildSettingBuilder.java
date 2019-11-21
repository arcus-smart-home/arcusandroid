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
import androidx.annotation.Nullable;

import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.style.ParentChildSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A builder to create a {@link ParentChildSetting} class. This class likely needs refactoring
 * in the future: Settings builder methods probably belong elsewhere.
 */
public class ParentChildSettingBuilder implements SettingBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ParentChildSettingBuilder.class);
    @NonNull
    private final ParentChildSetting parentChildSetting;

    // When true, if only one child setting exists, it will be returned in lieu of a composite setting
    private boolean promoteOnlyChild = true;

    // When true, null will be built/returned if no children are present
    private boolean suppressChildlessSetting = true;

    private ParentChildSettingBuilder(String title, String description) {
        this.parentChildSetting = new ParentChildSetting(title, description);
    }

    private ParentChildSettingBuilder(String title, String description, String assigned) {
        this.parentChildSetting = new ParentChildSetting(title, description, assigned);
    }

    @NonNull
    public static ParentChildSettingBuilder with(String title, String description) {
        return new ParentChildSettingBuilder(title, description);
    }

    @NonNull
    public static ParentChildSettingBuilder with(String title, String description, String selectionAbstract) {
        return new ParentChildSettingBuilder(title, description, selectionAbstract);
    }

    @NonNull
    public ParentChildSettingBuilder dontPromoteOnlyChild() {
        this.promoteOnlyChild = false;
        return this;
    }

    @NonNull
    public ParentChildSettingBuilder withTitle(String title) {
        this.parentChildSetting.setScreenTitle(title);
        return this;
    }

    @NonNull
    public ParentChildSettingBuilder dontSuppressChildlessSetting() {
        this.suppressChildlessSetting = false;
        return this;
    }

    @NonNull
    public ParentChildSettingBuilder addChildSetting (@NonNull Setting setting, boolean inheritSelectionAbstract) {
        this.parentChildSetting.addSetting(setting);
        if (inheritSelectionAbstract) {
            this.parentChildSetting.setSelectionAbstract(setting.getSelectionAbstract());
        }
        return this;
    }

    @Nullable
    public Setting build() {

        // Don't emit a ParentChild setting without children
        if (suppressChildlessSetting && parentChildSetting.getSettings().size() == 0) {
            return null;
        }

        // If there's only one child, just emit the child setting (ignore the parent)
        if (promoteOnlyChild && parentChildSetting.getSettings().size() == 1) {
            return parentChildSetting.getSettings().get(0);
        }

        return parentChildSetting;
    }

}
