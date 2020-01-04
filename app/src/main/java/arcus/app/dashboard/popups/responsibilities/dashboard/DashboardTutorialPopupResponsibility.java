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

import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.utils.PreferenceUtils;


public class DashboardTutorialPopupResponsibility extends DashboardPopupResponsibility {

    private final static int DELAY_UNTIL_TUTORIAL_MS = 2000;

    @Override
    public boolean isQualified() {
        return !PreferenceUtils.hasCompletedTutorial();
    }

    @Override
    public void showPopup() {
        PreferenceUtils.setCompletedTutorial(true);
        WalkthroughBaseFragment climate = WalkthroughBaseFragment.newInstance(WalkthroughType.INTRO);
        BackstackManager.getInstance().navigateToFloatingFragment(climate, climate.getClass().getName(), true);
    }

    @Override
    protected boolean isVisible() {
        return BackstackManager.getInstance().isFragmentOnStack(WalkthroughBaseFragment.class);
    }

    @Override
    public int executionDelayMs() {
        return DELAY_UNTIL_TUTORIAL_MS;
    }
}
