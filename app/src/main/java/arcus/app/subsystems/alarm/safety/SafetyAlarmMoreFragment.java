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
package arcus.app.subsystems.alarm.safety;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import arcus.cornea.subsystem.safety.SettingsController;
import arcus.cornea.subsystem.safety.model.Settings;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.utils.ActivityUtils;

import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.fragments.BaseFragment;


public class SafetyAlarmMoreFragment extends BaseFragment implements SettingsController.Callback {

    private ToggleButton silentToggle;
    private ToggleButton waterShutOffToggle;
    private boolean      waterShutoffAvailable = false;

    private ListenerRegistration subscription = Listeners.empty();

    @NonNull
    public static SafetyAlarmMoreFragment newInstance(){
        SafetyAlarmMoreFragment fragment = new SafetyAlarmMoreFragment();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        silentToggle = (ToggleButton) view.findViewById(R.id.fragment_safety_alarm_more_silent_toggle);
        waterShutOffToggle = (ToggleButton) view.findViewById(R.id.fragment_safety_alarm_water_valve_toggle);
        // TODO: Unable to determine if Water Safety is available now or not; Add to capability?

        silentToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSettings();
            }
        });

        waterShutOffToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (waterShutoffAvailable) {
                    setSettings();
                }
                else {
                    waterShutOffToggle.setChecked(false);
                    promptForgetSomething();
                }
            }
        });
        return view;
    }

    private void setSettings() {
        SettingsController.instance().setSettings(
              Settings
                    .builder()
                    .withWaterShutoffEnabled(waterShutOffToggle.isChecked())
                    .withSilentAlarm(silentToggle.isChecked())
                    .build());
    }

    private void promptForgetSomething(){
        final AlertFloatingFragment floatingFragment = AlertFloatingFragment.newInstance(getActivity().getString(R.string.safety_alarm_forget_something),
                getActivity().getString(R.string.do_not_have_a_water_valve),
                getActivity().getString(R.string.cancel_text),
                getActivity().getString(R.string.buy_water_shut_off_valve_btn),
                new AlertFloatingFragment.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        ActivityUtils.launchShopNow();
                        return true;
                    }
                });
        BackstackManager.getInstance().navigateToFloatingFragment(floatingFragment, floatingFragment.getClass().getName(), true);
    }

    @Override
    public void onResume() {
        super.onResume();
        subscription = SettingsController.instance().setCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        subscription = Listeners.clear(subscription);
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_safety_alarm_more;
    }

    @Override
    public void showUpdateError(Throwable t, @NonNull final Settings currentSettings) {
        // todo:  check that this is an ok behavior
        ErrorManager
                .in(getActivity())
                .withDialogDismissedListener(new DismissListener() {
                    @Override
                    public void dialogDismissedByReject() {
                        showSettings(currentSettings);
                    }

                    @Override
                    public void dialogDismissedByAccept() {
                        showSettings(currentSettings);
                    }
                })
                .showGenericBecauseOf(t);
    }

    @Override
    public void showSettings(@NonNull Settings settings) {
        waterShutoffAvailable = settings.isWaterShutoffAvailable();
        silentToggle.setChecked(settings.isSilentAlarm());
        waterShutOffToggle.setChecked(settings.isWaterShutoffEnabled());

        if (!waterShutoffAvailable) {
            waterShutOffToggle.setChecked(false);
        }
    }
}
