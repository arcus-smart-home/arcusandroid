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
package arcus.app.subsystems.alarm;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import arcus.cornea.subsystem.safety.SafetyStatusController;
import arcus.cornea.subsystem.security.SecurityStatusController;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.view.Version1TextView;



public class DeprecatedAlertFragment extends BaseFragment {
    public static String TYPE = "TYPE";
    public static String SAFETY_ALARM = "SAFETY_ALARM";
    public static String SECURITY_ALARM = "SECURITY_ALARM";

    public DeprecatedAlertFragment() {
        // Required empty public constructor
    }

    @NonNull
    public static DeprecatedAlertFragment newInstance(String type){
        DeprecatedAlertFragment fragment = new DeprecatedAlertFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TYPE, type);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public String getTitle() {
        return getString(R.string.alert);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_deprecated_alert;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Version1TextView title = (Version1TextView) view.findViewById(R.id.alert_title);
        Button cancel = (Button) view.findViewById(R.id.cancel_button);
        cancel.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.button_cancel));
        View cancelLayout = view.findViewById(R.id.cancel_layout);

        Bundle bundle = getArguments();
        String type = bundle.getString(TYPE);
        if(type.equals(SAFETY_ALARM)) {
            title.setText(getString(R.string.alert_safety_subtitle));
            title.setTextColor(ContextCompat.getColor(getContext(), R.color.safety_color));
            cancelLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.safety_color));
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SafetyStatusController.instance().cancel();
                    BackstackManager.getInstance().navigateBack();
                }
            });
        } else if(type.equals(SECURITY_ALARM)) {
            title.setText(getString(R.string.alert_security_subtitle));
            title.setTextColor(ContextCompat.getColor(getContext(), R.color.security_color));
            cancelLayout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.security_color));
            cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SecurityStatusController.instance().disarm();
                    BackstackManager.getInstance().navigateBack();
                }
            });
        }
        return view;
    }

}
