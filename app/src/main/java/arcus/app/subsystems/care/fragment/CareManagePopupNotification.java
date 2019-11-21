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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.dashboard.HomeFragment;

public class CareManagePopupNotification extends BaseFragment {
    View closeButton;
    ImageView checkBox;
    Boolean isChecked = true;

    @NonNull public static CareManagePopupNotification newInstance() {
        return new CareManagePopupNotification();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((BaseActivity)getActivity()).getSupportActionBar().hide();
    }

    @Override public View onCreateView(
          LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {

            checkBox = (ImageView) view.findViewById(R.id.checkbox_care_dont_show_again);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    isChecked = !isChecked;
                    checkBox.setImageResource(isChecked ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);
                }
            });


            closeButton = view.findViewById(R.id.exit_view);
            closeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PreferenceCache.getInstance().putBoolean(
                            PreferenceUtils.CARE_BEHAVIORS_DONT_SHOW_AGAIN,
                            isChecked
                    );
                    BackstackManager.getInstance().rewindToFragment(HomeFragment.newInstance());
                }
            });

        }
        return view;
    }

    @Override public boolean onBackPressed() {
        return true; // Don't let back press close this screen.
    }

    @Nullable
    @Override public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.care_manage_popup_notification_fragment;
    }

    @Override
    public void onPause() {
        super.onPause();
        ((BaseActivity)getActivity()).getSupportActionBar().show();
    }
}
