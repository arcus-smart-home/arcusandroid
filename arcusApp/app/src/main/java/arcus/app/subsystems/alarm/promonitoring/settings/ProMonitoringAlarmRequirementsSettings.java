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
package arcus.app.subsystems.alarm.promonitoring.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import arcus.cornea.common.PresentedView;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.security.SecuritySettingsController;
import arcus.cornea.subsystem.security.model.SettingsModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.AlarmRequirementPickerPopup;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmProviderOfflinePresenter;


public class ProMonitoringAlarmRequirementsSettings extends BaseFragment implements SecuritySettingsController.Callback, PresentedView {

    private SecuritySettingsController securitySettingsController;
    private AlarmProviderOfflinePresenter presenter = new AlarmProviderOfflinePresenter();
    private ListenerRegistration security = Listeners.empty();
    private Version1TextView onAlarmRequirementCount, partialAlarmRequirementCount, title, subtitle;
    private ImageView onChevron;
    private ImageView partialChevron;
    private Version1Button button;
    private LinearLayout editSection;
    private int onAlarmSensitivity, partialAlarmSensitivity;
    private int onDevicesCount, partialDevicesCount;

    @NonNull
    public static ProMonitoringAlarmRequirementsSettings newInstance(){
        return new ProMonitoringAlarmRequirementsSettings();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        partialAlarmRequirementCount = (Version1TextView) view.findViewById(R.id.partial_alarm_requirement_count);
        onAlarmRequirementCount = (Version1TextView) view.findViewById(R.id.on_alarm_requirement_count);
        title = (Version1TextView) view.findViewById(R.id.title);
        subtitle = (Version1TextView) view.findViewById(R.id.subtitle);
        onChevron = (ImageView) view.findViewById(R.id.on_alarm_requirement_chevron);
        partialChevron = (ImageView) view.findViewById(R.id.partial_alarm_requirement_chevron);
        button = (Version1Button)view.findViewById(R.id.learn_more_button);
        editSection = view.findViewById(R.id.edit_region);

        View partialAlarmRequirementView = view.findViewById(R.id.partial_alarm_requirement_cell);
        View onAlarmRequirementView = view.findViewById(R.id.on_alarm_requirement_cell);

        onAlarmRequirementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!onAlarmRequirementCount.getText().equals("0")) {
                    AlarmRequirementPickerPopup fragment = AlarmRequirementPickerPopup.newInstance("ALARM REQUIREMENT", // Title
                            "DEVICE(S)", // Left Title
                            onAlarmSensitivity, // Left Value
                            1, // Min
                            onDevicesCount); // Max
                    fragment.setOnTimeChangedListener(new AlarmRequirementPickerPopup.OnTimeChangedListener() {
                        @Override
                        public void onClose(int value) {
                            onAlarmRequirementCount.setText(String.valueOf(value));
                            securitySettingsController.setAlarmSensitivityOnMode(value);
                        }
                    });
                    BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
                }
            }
        });

        partialAlarmRequirementView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!partialAlarmRequirementCount.getText().equals("0")) {
                    AlarmRequirementPickerPopup fragment = AlarmRequirementPickerPopup.newInstance("ALARM REQUIREMENT", // Title
                            "DEVICE(S)", // Left Title
                            partialAlarmSensitivity, // Left Value
                            1, // Min
                            partialDevicesCount); // Max
                    fragment.setOnTimeChangedListener(new AlarmRequirementPickerPopup.OnTimeChangedListener() {
                        @Override
                        public void onClose(int value) {
                            partialAlarmRequirementCount.setText(String.valueOf(value));
                            securitySettingsController.setAlarmSensitivityPartialMode(value);
                        }
                    });
                    BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.startPresenting(this);

        if (securitySettingsController == null) {
            securitySettingsController = SecuritySettingsController.instance();
        }

        security = securitySettingsController.setCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
        security = Listeners.clear(security);
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.alarm_requirements);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pro_monitoring_alarm_reqs;
    }

    @Override
    public void updateSettings(SettingsModel model) {

        this.onDevicesCount = model.getOnMotionSensorsCount();
        this.partialDevicesCount = model.getPartialMotionSensorsCount();

        this.onAlarmSensitivity = model.getOnAlarmSensitivity();
        this.partialAlarmSensitivity = model.getPartialAlarmSensitivity();

        onChevron.setVisibility(onDevicesCount == 0 ? View.INVISIBLE : View.VISIBLE);
        partialChevron.setVisibility(partialDevicesCount == 0 ? View.INVISIBLE : View.VISIBLE);

        onAlarmRequirementCount.setText(onDevicesCount == 0 ? String.valueOf(0) : String.valueOf(onAlarmSensitivity));
        partialAlarmRequirementCount.setText(partialDevicesCount == 0 ? String.valueOf(0) : String.valueOf(partialAlarmSensitivity));

        if (!devicesParticipating()) {
            title.setText(R.string.alarm_requirements_no_sensors_title);
            subtitle.setText(R.string.alarm_requirements_no_sensors_subtitle);
            editSection.setVisibility(View.GONE);
            button.setVisibility(View.VISIBLE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ActivityUtils.launchReduceAlarms();
                }
            });
        }
    }

    @Override
    public void showError(ErrorModel error) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(new RuntimeException(error.getMessage()));
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        // Nothing to do.
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        // Nothing to do.
    }

    @Override
    public void updateView(@NonNull Object model) {
        // Nothing to do.
    }

    private boolean devicesParticipating() {
        return onDevicesCount + partialDevicesCount > 0;
    }
}