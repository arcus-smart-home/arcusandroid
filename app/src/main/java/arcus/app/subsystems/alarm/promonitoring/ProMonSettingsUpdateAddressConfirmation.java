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
package arcus.app.subsystems.alarm.promonitoring;



import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;

import arcus.app.common.fragments.BaseFragment;




public class ProMonSettingsUpdateAddressConfirmation extends BaseFragment {


    public static ProMonSettingsUpdateAddressConfirmation newInstance(@NonNull String placeID) {
        ProMonSettingsUpdateAddressConfirmation fragment = new ProMonSettingsUpdateAddressConfirmation();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        View rootView = getView();
        setTitle();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.settings_promon_permit_view_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_promon_settings_permit;
    }
}

