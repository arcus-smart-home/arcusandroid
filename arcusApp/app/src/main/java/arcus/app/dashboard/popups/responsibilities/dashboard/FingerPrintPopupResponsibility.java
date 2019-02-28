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

import arcus.app.common.popups.UseFingerPrintPopup;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.backstack.ScleraTransitionManager;
import arcus.app.common.utils.BiometricLoginUtils;
import arcus.app.common.utils.PreferenceUtils;



public class FingerPrintPopupResponsibility extends DashboardPopupResponsibility {

    private boolean canFingerprint = BiometricLoginUtils.canFingerprint();

        @Override
        public boolean isQualified() {
           return (!PreferenceUtils.getHasSeenFingerPrint() && canFingerprint);
           //return true;
        }

        @Override
        public void showPopup() {
            PreferenceUtils.setHasSeenFingerPrint(true);
            Fragment fragment = UseFingerPrintPopup.newInstance();
            ScleraTransitionManager.displaySheet(fragment);
        }

        @Override
        protected boolean isVisible() {
            return BackstackManager.getInstance().isFragmentOnStack(UseFingerPrintPopup.class);
        }
}
