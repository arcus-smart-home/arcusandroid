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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.security.SecuritySettingsController;
import arcus.cornea.subsystem.security.model.SettingsModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.AlarmRequirementPickerPopup;
import arcus.app.common.view.Version1TextView;



public class MoreFragment extends BaseFragment implements SecuritySettingsController.Callback{

    private SecuritySettingsController mSettingsController;
    private ListenerRegistration mListener;

    private Version1TextView mAlarmRequirementCount;
    private ListenerRegistration mCallbackListener;
    private int alarmSensitivity;
    private int totalDevices = 2;


    @NonNull
    public static MoreFragment newInstance(){
        MoreFragment fragment = new MoreFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        View devicesView = view.findViewById(R.id.alarm_devices_cell);
        View graceView = view.findViewById(R.id.grace_periods_cell);
        View alarmRequirementView = view.findViewById(R.id.alarm_requirement_cell);
        View soundsView = view.findViewById(R.id.sounds_cell);

        mAlarmRequirementCount = (Version1TextView) view.findViewById(R.id.alarm_requirement_count);


        devicesView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(DevicesConfigListFragment.newInstance(), true);
            }
        });

        graceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(GracePeriodFragment.newInstance(), true);
            }
        });

        soundsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(SoundsConfigFragment.newInstance(), true);
            }
        });

        alarmRequirementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmRequirementPickerPopup fragment = AlarmRequirementPickerPopup.newInstance("ALARM REQUIREMENT", // Title
                        "DEVICE(S)", // Left Title
                        alarmSensitivity, // Left Value
                        1, // Min
                        totalDevices); // Max
                fragment.setOnTimeChangedListener(new AlarmRequirementPickerPopup.OnTimeChangedListener() {
                    @Override
                    public void onClose(int value) {
                        mAlarmRequirementCount.setText(String.valueOf(value));
                        mSettingsController.setAlarmSensitivity(value);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mSettingsController == null) {
            mSettingsController = SecuritySettingsController.instance();
        }

        mListener = mSettingsController.setCallback(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        mListener.remove();
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_security_alarm_more;
    }

    @Override
    public void updateSettings(@NonNull SettingsModel model) {
        if(model.getTotalOnDevices()==0){
            totalDevices=2;
        }else{
            this.totalDevices=model.getTotalOnDevices();
        }
        this.alarmSensitivity = model.getAlarmSensitivity();
        mAlarmRequirementCount.setText(String.valueOf(alarmSensitivity));
    }

    @Override
    public void showError(ErrorModel error) {

    }

}
