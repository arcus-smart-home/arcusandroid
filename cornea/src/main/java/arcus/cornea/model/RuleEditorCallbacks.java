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
package arcus.cornea.model;

import java.util.Collection;
import java.util.List;

/**
 * !!! These may change... !!!
 */
public interface RuleEditorCallbacks {
    /**
     * Called when the rule selected is not satisfiable.
     */
    void showUnavailable(RuleDisplayModel model);

    /**
     * Called when the rule selected is satisfiable.
     */
    void showEditable(RuleDisplayModel model);

    /**
     * Called when SelectorType is LIST
     */
    void showModelListSelector(Collection<String> identifiers);

    /**
     * Called when SelectorType is TIME_RANGE
     */
    void showTimeRangeSelector();

    /**
     * Called when SelectorType is DAY_OF_WEEK
     */
    void showDayOfWeekSelector();

    /**
     * Called when SelectorType is TIME_OF_DAY
     */
    void showTimeOfDaySelector();

    /**
     * Called when SelectorType is DURATION
     */
    void showDurationSelector();

    /**
     * Called when SelectorType is LIST, but unable to determine model types.
     */
    void showTupleEditor(List<StringPair> displayValues);

    void showLoading();
    void saveSuccess();
    void errorOccurred(Throwable throwable);
    void showTextEditor();

    void showScheduleDialog();

    void allowScheduling(String strState, String strModelAddress, String strName);

}
