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
package arcus.cornea.model.editors;

import arcus.cornea.model.RuleEditorCallbacks;
import arcus.cornea.model.SelectorType;

public class ShowNonIdentifierEditor implements ShowEditor {
    private SelectorType type;

    public ShowNonIdentifierEditor(SelectorType type) {
        this.type = type;
    }

    public SelectorType getType() {
        return this.type;
    }

    @Override
    public void show(RuleEditorCallbacks callbacks) {
        switch (type) {
            case DAY_OF_WEEK:
                callbacks.showDayOfWeekSelector();
                break;
            case DURATION:
                callbacks.showDurationSelector();
                break;
            case TIME_OF_DAY:
                callbacks.showTimeOfDaySelector();
                break;
            case TIME_RANGE:
                callbacks.showTimeRangeSelector();
                break;
        }
    }
}
