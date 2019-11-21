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
package arcus.app.dashboard;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.subsystem.alarm.AlarmSubsystemActivationController;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import java.util.HashMap;

public class AlarmSubsystemActivationFragment extends BaseFragment implements AlarmSubsystemActivationController.Callback {

    @NonNull
    public static AlarmSubsystemActivationFragment newInstance() {
        AlarmSubsystemActivationFragment fragment = new AlarmSubsystemActivationFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        hideActionBar();
        AlarmSubsystemActivationController.getInstance().setCallback(this);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        showActionBar();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        View contactSupport = view.findViewById(R.id.clickable_support_link);
        if (contactSupport != null) {
            contactSupport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent callSupportIntent = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
                    try {
                        getActivity().startActivity(callSupportIntent);
                    } catch (Exception ignored) {}
                }
            });
        }

        Version1Button upgradeNow = (Version1Button) view.findViewById(R.id.update_button);
        upgradeNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlarmSubsystemActivationController.getInstance().activate();
            }
        });

        View closeButton = view.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
            }
        });

        return view;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_alarm_subsystem_activation;
    }

    @Override
    public void onError(Throwable error) {
        HashMap<String,String> choice = new HashMap<>();
        choice.put(getString(R.string.call_support).toUpperCase(), "");

        ButtonListPopup editWhichDayPopup = ButtonListPopup.newInstance(
                choice,
                R.string.alarm_upgrade_failed_title,
                R.string.alarm_upgrade_failed_desc);

        editWhichDayPopup.setCallback(new ButtonListPopup.Callback() {
            @Override
            public void buttonSelected(String buttonKeyValue) {
                Intent callSupportIntent = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
                try {
                    getActivity().startActivity(callSupportIntent);
                } catch (Exception ignored) {}
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editWhichDayPopup, editWhichDayPopup.getClass().getSimpleName(), true);
    }

    @Override
    public void onActivateComplete() {
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }
}
