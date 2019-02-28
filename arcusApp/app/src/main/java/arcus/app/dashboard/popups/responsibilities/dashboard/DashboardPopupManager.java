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
package arcus.app.dashboard.popups.responsibilities.dashboard;

import arcus.app.dashboard.popups.PopupManager;
import arcus.app.dashboard.popups.PopupResponsibility;

import java.util.ArrayList;
import java.util.List;

public class DashboardPopupManager extends PopupManager {

    private final static DashboardPopupManager instance = new DashboardPopupManager();
    private final List<PopupResponsibility> popups = new ArrayList<>();

    public static DashboardPopupManager getInstance() {
        return instance;
    }

    /**
     * Create a DashboardPopupManager with popups ordered in priority order (if multiple popups
     * are qualified to be displayed, the highest-priority popup will be displayed).
     */
    private DashboardPopupManager() {
        popups.add(new TermsConditionsPopupResponsibility());
        popups.add(new FingerPrintPopupResponsibility());
        popups.add(new WhatsNewPopupResponsibility());
        popups.add(new DashboardTutorialPopupResponsibility());
    }

    @Override
    public List<PopupResponsibility> getPopupResponsibilities() {
        return popups;
    }
}
