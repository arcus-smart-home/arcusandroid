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
package arcus.app.subsystems.care.fragment;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ActivityUtils;

public class CareDevicesRequiredFragment extends BaseFragment {

    private final @StringRes int titleRes = R.string.card_care_title;
    private View shopButton;

    public static CareDevicesRequiredFragment newInstance() {
        return new CareDevicesRequiredFragment();
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        shopButton = view.findViewById(R.id.shop_button);
        return view;
    }

    public void onResume() {
        super.onResume();
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchShopCareNow();
            }
        });
    }

    @Override public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        String title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            activity.setTitle(title);
        }
    }

    @Nullable @Override public String getTitle() {
        return getString(titleRes);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_care_devices_required;
    }
}
