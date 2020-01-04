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
package arcus.app.dashboard.popups.responsibilities.alarm;

import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;



public class AlarmTutorialPopupResponsibility extends AlarmResponsibility {

    @Override
    protected boolean isQualified() {
        return !PreferenceCache.getInstance().getBoolean(PreferenceUtils.ALARMS_WALKTHROUGH_DONT_SHOW_AGAIN, false);
    }

    @Override
    protected void showPopup() {
        WalkthroughBaseFragment alarms = WalkthroughBaseFragment.newInstance(WalkthroughType.SECURITY);
        BackstackManager.getInstance().navigateToFloatingFragment(alarms, alarms.getClass().getName(), true);
    }

    @Override
    protected boolean isVisible() {
        return BackstackManager.getInstance().isFragmentOnStack(WalkthroughBaseFragment.class);
    }

}
