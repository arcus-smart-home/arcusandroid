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
package arcus.app.device.settings.style;

import arcus.app.device.settings.core.Setting;

import java.util.List;

/**
 * Renders a setting cell containing a title, description, selection abstract (optional) and a
 * chevron (>). Clicking the cell produces a popup allowing the user to select one element from a
 * predefined set of strings.
 *
 * This is generally a bad idea, as this setting type doesn't automatically localize the strings.
 * However, this is useful for situations when a set of string values are being provided from the
 * platform and should be displayed verbatim.
 */
public class ListSelectionSetting extends AbstractEnumeratedSetting implements Setting {

    public ListSelectionSetting(String title, String description, List<String> enumValues, String currentSelection, String selectionAbstract) {
        super(title, description, enumValues, currentSelection, selectionAbstract);
    }

    public ListSelectionSetting(String title, String description, List<String> enumValues, String currentSelection) {
        super(title, description, enumValues, currentSelection);
    }

    public ListSelectionSetting(String title, String description, List<String> enumTitles, List<String> enumDescriptions, String currentSelection, String selectionAbstract) {
        super(title, description, enumTitles, enumDescriptions, currentSelection, selectionAbstract);
    }
}
