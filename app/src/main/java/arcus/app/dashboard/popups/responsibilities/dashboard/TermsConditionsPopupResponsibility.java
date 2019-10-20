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

import android.support.v4.app.Fragment;

import arcus.cornea.SessionController;
import arcus.app.account.TermsAndConditionsUpdateFragment;
import arcus.app.common.backstack.BackstackManager;


public class TermsConditionsPopupResponsibility extends DashboardPopupResponsibility {

    @Override
    public boolean isQualified() {
        return SessionController.instance().needsTermsOrPrivacyUpdate();
    }

    @Override
    public void showPopup() {
        Fragment fragment = TermsAndConditionsUpdateFragment.newInstance();
        BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getName(), true);
    }

    @Override
    protected boolean isVisible() {
        return BackstackManager.getInstance().isFragmentOnStack(TermsAndConditionsUpdateFragment.class);
    }
}
