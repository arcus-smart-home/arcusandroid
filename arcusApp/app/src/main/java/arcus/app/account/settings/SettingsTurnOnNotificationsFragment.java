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
package arcus.app.account.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.utils.GlobalSetting;

public class SettingsTurnOnNotificationsFragment extends Fragment {

    @NonNull public static SettingsTurnOnNotificationsFragment newInstance() {
        return new SettingsTurnOnNotificationsFragment();
    }

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_account_settings_push_detail, container, false);
    }

    @Override public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null) {
            return;
        }

        View closeContainer = root.findViewById(R.id.push_notifications_close_container);
        if (closeContainer != null) {
            closeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().finish();
                }
            });
        }
        View contactSupport = root.findViewById(R.id.contact_support_button_container);
        if (contactSupport != null) {
            contactSupport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callSupportIntent = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
                    try {
                        getActivity().startActivity(callSupportIntent);
                    } catch (Exception ignored) {} // Have to manually enter it.. Lost reference to the activity?
                }
            });
        }
    }

}
