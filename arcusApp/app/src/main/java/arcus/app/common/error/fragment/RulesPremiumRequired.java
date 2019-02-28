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
package arcus.app.common.error.fragment;

import arcus.app.R;


public class RulesPremiumRequired extends FullScreenErrorFragment {

    @Override
    public int getErrorIcon() {
        return R.drawable.icon_rules_white;
    }

    @Override
    public String getErrorTitle() {
        return getString(R.string.error_premium_required);
    }

    @Override
    public int getBodyLayoutId() {
        return R.layout.fragment_rules_premium_required;
    }

    @Override
    public int getPostItLayoutId() {
        return R.layout.fragment_postit_upgrade;
    }
}
