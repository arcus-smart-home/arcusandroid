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
package arcus.app.subsystems.rules.model;

import arcus.app.R;


public enum Day {

    SUNDAY(R.string.rules_sunday),
    MONDAY(R.string.rules_monday),
    TUESDAY(R.string.rules_tuesday),
    WEDNESDAY(R.string.rules_wednesday),
    THURSDAY(R.string.rules_thursday),
    FRIDAY(R.string.rules_friday),
    SATURDAY(R.string.rules_saturday);

    private final int nameResId;

    Day (int nameResId) {
        this.nameResId = nameResId;
    }

    public int getNameResId () {
        return this.nameResId;
    }
}
