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
package arcus.app.subsystems.alarm.security;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.security.SecuritySettingsController;
import arcus.cornea.subsystem.security.model.SettingsModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;

public class SoundsConfigFragment extends BaseFragment implements SecuritySettingsController.Callback {
    private ListenerRegistration registration;
    private ToggleButton soundsToggle;
    private ToggleButton silentToggle;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SoundsConfigFragment.
     */
    @NonNull
    public static SoundsConfigFragment newInstance() {
        SoundsConfigFragment fragment = new SoundsConfigFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SoundsConfigFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public String getTitle() {
        return "SOUNDS";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_security_alarm_sounds_config;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        soundsToggle = (ToggleButton) view.findViewById(R.id.security_alarm_config_sounds_toggle);
        silentToggle = (ToggleButton) view.findViewById(R.id.security_alarm_config_silent_toggle);

        soundsToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SecuritySettingsController.instance().setEnableSounds(isChecked);
            }
        });
        silentToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SecuritySettingsController.instance().setSilentAlarm(isChecked);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        registration = SecuritySettingsController.instance().setCallback(this);
    }

    @Override
    public void onPause() {
        registration = Listeners.clear(registration);
        super.onPause();
    }

    @Override
    public void updateSettings(@NonNull SettingsModel model) {
        soundsToggle.setChecked(model.isEnableSounds());
        silentToggle.setChecked(model.isSilentAlarm());
    }

    @Override
    public void showError(ErrorModel error) {
//        ErrorManager
//                .in(getActivity())
//                .
    }
}
