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
package arcus.app.subsystems.alarm.promonitoring.presenters;

import android.content.res.Resources;

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import arcus.app.R;
import arcus.app.subsystems.alarm.promonitoring.models.HistoryListItemModel;

import java.util.List;
import java.util.Set;



public interface AlarmActivityContract {

    enum AlarmActivityFilter {
        SAFETY(R.string.promon_filter_smoke_and_co),
        SECURITY(R.string.promon_filter_security_and_panic),
        WATER(R.string.promon_filter_water_leak);

        private final int stringResId;

        AlarmActivityFilter(int stringResId) {
            this.stringResId = stringResId;
        }

        public String toString(Resources resources) {
            return resources.getString(stringResId);
        }

        public static AlarmActivityFilter fromString(Resources resources, String string) {
            for (AlarmActivityFilter thisFilter : values()) {
                if (thisFilter.toString(resources).equalsIgnoreCase(string)) {
                    return thisFilter;
                }
            }

            return SECURITY;
        }
    }

    interface AlarmActivityView extends PresentedView<List<HistoryListItemModel>> {}

    interface AlarmActivityPresenter extends Presenter<AlarmActivityView> {

        /**
         * Request that the presenter call the view with a list of all available history items (i.e,
         * unfiltered).
         */
        void requestUpdate();

        /**
         * Request that the presenter call the view with a filtered list of available history items.
         * Only history items from the subsystems who appear in the filterSpec will be returned.
         *
         * @param filterSpec A set of {@link AlarmActivityFilter}; only records matching a filter
         *                   will be returned. Pass an empty set to prevent any records from being
         *                   returned.
         */
        void requestUpdate(Set<AlarmActivityFilter> filterSpec);
    }
}
