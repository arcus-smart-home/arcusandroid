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
package arcus.app.dashboard.popups.responsibilities.history;

import arcus.cornea.controller.SubscriptionController;
import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.dashboard.popups.PopupResponsibility;



public class HistoryTutorialPopupResponsibility extends PopupResponsibility {

    @Override
    protected boolean isQualified() {
        return SubscriptionController.isPremiumOrPro() && !PreferenceCache.getInstance().getBoolean(PreferenceUtils.HISTORY_WALKTHROUGH_DONT_SHOW_AGAIN, false);
    }

    @Override
    protected void showPopup() {
        WalkthroughBaseFragment historyWalkthrough = WalkthroughBaseFragment.newInstance(WalkthroughType.HISTORY);
        BackstackManager.getInstance().navigateToFloatingFragment(historyWalkthrough, historyWalkthrough.getClass().getName(), true);
    }

    @Override
    protected boolean isVisible() {
        return BackstackManager.getInstance().isFragmentOnStack(WalkthroughBaseFragment.class);
    }
}
