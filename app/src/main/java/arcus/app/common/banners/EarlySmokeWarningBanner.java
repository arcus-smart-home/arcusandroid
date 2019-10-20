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
package arcus.app.common.banners;

import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.core.ClickableBanner;
import arcus.app.subsystems.alarm.safety.EarlySmokeWarningFragment;

public class EarlySmokeWarningBanner extends ClickableBanner {

    public EarlySmokeWarningBanner() {
        super(R.layout.early_smoke_warning_banner);
    }

    @Override public View getBannerView(ViewGroup parent) {
        setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(EarlySmokeWarningFragment.newInstance(), true);
            }
        });
        return super.getBannerView(parent);
    }
}
