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

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.BiometricLoginUtils;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1TextView;



public class SettingsFingerprintFragment  extends BaseFragment {

    private Version1TextView title;
    private Version1TextView description;
    private ToggleButton toggler;

    public static SettingsFingerprintFragment newInstance() {
        SettingsFingerprintFragment fragment = new SettingsFingerprintFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v =  super.onCreateView(inflater, container, savedInstanceState);

        title = (Version1TextView) v.findViewById(R.id.title);
        description = (Version1TextView) v.findViewById(R.id.description);
        toggler = (ToggleButton) v.findViewById(R.id.toggle);

        title.setText(R.string.fingerprint_login_yes);
        description.setText(R.string.fingerprint_unlock_desc);
        toggler.setChecked(PreferenceUtils.getUsesFingerPrint());

        toggler.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggler.setChecked(isChecked);
                PreferenceUtils.setUseFingerPrint(isChecked);
            }
        });
        return v;
    }

    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());

        String message = BiometricLoginUtils.fingerprintUnavailable();
        if(message.length() > 0) {
            toggler.setChecked(false);
            toggler.setEnabled(false);
        } else {
            toggler.setEnabled(true);
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return "FINGERPRINT";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_settings_fingerprint;
    }

}
