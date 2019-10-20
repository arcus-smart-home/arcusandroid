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
package arcus.app.account.registration;

import arcus.app.R;


public class AccountPremiumPlanFragment extends AccountCreationStepFragment {

    @Override
    public boolean submit() {
        transitionToNextState();
        return true;
    }

    @Override
    public boolean validate() {
        // Nothing to validate
        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.account_registration_premium_plan);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_account_premium_plan;
    }
}
