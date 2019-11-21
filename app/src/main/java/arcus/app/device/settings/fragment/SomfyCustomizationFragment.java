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
package arcus.app.device.settings.fragment;

import androidx.annotation.Nullable;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;


public class SomfyCustomizationFragment extends BaseFragment {

    public static SomfyCustomizationFragment newInstance () {
        return new SomfyCustomizationFragment();
    }

    @Nullable
    @Override
    public String getTitle() {

        //TODO: For what ever reason this is not sending the information for the header.
        return getResources().getString(R.string.customization);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_somfy_customization;
    }

}
