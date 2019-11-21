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
package arcus.app.subsystems.alarm.promonitoring.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.ArcusApplication;

import java.util.ArrayList;
import java.util.List;



public class SettingsListItemModel {

    public enum SettingListItemStyle {
        SECTION_HEADING,            // A list section header
        BINARY_SETTING_SET,         // List item with a binary toggle set ('on' position)
        BINARY_SETTING_CLEARED,     // List item with a binary toggle cleared ('off' position)
        DISCLOSURE_SETTING          // List item with a chevron '>'
    }

    private SettingListItemStyle style;
    private String title;
    private String subtitle;
    private String abstractString;
    private List<Integer> icons = new ArrayList<>();

    public static class Builder {

        private final SettingsListItemModel model = new SettingsListItemModel();

        private Builder (SettingListItemStyle style) {
            model.setStyle(style);
        }

        public static Builder sectionHeading() {
            return new Builder(SettingListItemStyle.SECTION_HEADING);
        }

        public static Builder binarySetting(boolean initiallySet) {
            return new Builder(initiallySet ? SettingListItemStyle.BINARY_SETTING_SET : SettingListItemStyle.BINARY_SETTING_CLEARED);
        }

        public static Builder disclosureSetting() {
            return new Builder(SettingListItemStyle.DISCLOSURE_SETTING);
        }

        public Builder withIcon(Integer iconResId) {
            if (iconResId != null) {
                this.model.addIcon(iconResId);
            }
            return this;
        }

        public Builder withTitle(int stringResId) {
            this.model.setTitle(ArcusApplication.getContext().getString(stringResId));
            return this;
        }

        public Builder withSubtitle(int stringResId) {
            this.model.setSubtitle(ArcusApplication.getContext().getString(stringResId));
            return this;
        }

        public SettingsListItemModel build() {
            return this.model;
        }
    }

    public void setStyle(SettingListItemStyle style) {
        this.style = style;
    }

    /**
     * @return Style of this setting item.
     */
    public SettingListItemStyle getStyle() {
        return style;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The title of the cell, localized if as needed to the current region.
     */
    @NonNull public String getTitle() {
        return title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    /**
     * @return The subtitle to be displayed or null if no subtitle should be displayed for this
     * item.
     */
    @Nullable public String getSubtitle() {
        return subtitle;
    }

    public void setAbstract (String abstractString) {
        this.abstractString = abstractString;
    }

    /**
     * @return A String representing the setting abstract (typically a short description of
     * this setting's current value, i.e., "30 sec", "Closed" or "10 %") or null if this setting
     * provides no abstract.
     */
    @Nullable public String getAbstract() {
        return abstractString;
    }

    public void addIcon(int icon) {
        icons.add(icon);
    }

    /**
     * @return A list of zero or more icon drawable resource IDs to render in the setting item.
     */
    @NonNull public List<Integer> getIcons() {
        return icons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettingsListItemModel)) return false;

        SettingsListItemModel that = (SettingsListItemModel) o;

        if (style != that.style) return false;
        if (!title.equals(that.title)) return false;
        if (!subtitle.equals(that.subtitle)) return false;
        if (!abstractString.equals(that.abstractString)) return false;
        return icons.equals(that.icons);

    }

    @Override
    public int hashCode() {
        int result = style.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + subtitle.hashCode();
        result = 31 * result + abstractString.hashCode();
        result = 31 * result + icons.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SettingsListItemModel{" +
                "style=" + style +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", abstractString='" + abstractString + '\'' +
                ", icons=" + icons +
                '}';
    }
}
