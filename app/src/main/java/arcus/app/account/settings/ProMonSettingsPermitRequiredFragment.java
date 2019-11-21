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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import arcus.app.R;

import arcus.app.common.fragments.BaseFragment;



public class ProMonSettingsPermitRequiredFragment extends BaseFragment {
    Button saveButton;

    public static ProMonSettingsPermitRequiredFragment newInstance() {
        ProMonSettingsPermitRequiredFragment fragment = new ProMonSettingsPermitRequiredFragment();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        saveButton = (Button) rootView.findViewById(R.id.settings_promon_permit_required_button);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_promon_settings_permit_required;
    }

}
