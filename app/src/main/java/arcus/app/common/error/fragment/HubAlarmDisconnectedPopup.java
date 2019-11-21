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
package arcus.app.common.error.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import com.iris.client.model.AlarmSubsystemModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.dashboard.HomeFragment;
import arcus.app.subsystems.alarm.promonitoring.ProMonitoringIncidentFragment;
import arcus.app.subsystems.alarm.promonitoring.presenters.HubAlarmDisconnectedContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.HubAlarmDisconnectedPresenter;





public class HubAlarmDisconnectedPopup extends ArcusFloatingFragment implements HubAlarmDisconnectedContract.HubAlarmDisconnectedView {

    private static final String OFFLINE_SINCE = "OFFLINE_SINCE";
    private static final String LAST_KNOWN_MODE = "LAST_KNOWN_MODE";
    private static final String IS_PROMON = "IS_PROMON";
    private static final String IS_SECURITY_AVAILABLE = "IS_SECURITY_AVAILABLE";
    private static final String IS_ALARMING_STATE = "IS_ALARMING_STATE";

    private Callback callback;
    private HubAlarmDisconnectedPresenter presenter = new HubAlarmDisconnectedPresenter();
    private Version1TextView offlineSinceText, lastKnownModeText, lastKnownModeLabel, proMonText;
    private Version1Button hubOfflineGoToDashboardButton, hubOfflineGetSupportButton, hubOfflineAlarmTrackerButton;
    private View offlineSinceTextContainer, alarmEventInProgressText;


    public interface Callback {
        void closed();
    }

    @SuppressWarnings({"ConstantConditions"})
    @NonNull
    public static HubAlarmDisconnectedPopup newInstance(String offlineSince, String lastKnownMode, boolean isAlarmingState, boolean isProMon, boolean isSecurityAvailable) {
        HubAlarmDisconnectedPopup fragment = new HubAlarmDisconnectedPopup();
        Bundle arguments = new Bundle();

        arguments.putString(OFFLINE_SINCE, offlineSince);
        arguments.putString(LAST_KNOWN_MODE, lastKnownMode);
        arguments.putBoolean(IS_PROMON, isProMon);
        arguments.putBoolean(IS_SECURITY_AVAILABLE, isSecurityAvailable);
        arguments.putBoolean(IS_ALARMING_STATE, isAlarmingState);
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void setFloatingTitle() {
        // Nothing to do.
    }

    @Override
    public void doContentSection() {
        showFullScreen(true);

        offlineSinceTextContainer = contentView.findViewById(R.id.offline_since_text_container);
        offlineSinceText = (Version1TextView) contentView.findViewById(R.id.hub_offline_since_value);
        if(getArguments().getString(OFFLINE_SINCE) != null && !getArguments().getString(OFFLINE_SINCE).isEmpty()) {
            offlineSinceTextContainer.setVisibility(View.VISIBLE);
            offlineSinceText.setText(getArguments().getString(OFFLINE_SINCE));
        } else {
            offlineSinceTextContainer.setVisibility(View.GONE);
        }

        lastKnownModeLabel = (Version1TextView) contentView.findViewById(R.id.hub_offline_last_mode);
        lastKnownModeText = (Version1TextView) contentView.findViewById(R.id.hub_offline_last_mode_value);
        if (getArguments().getBoolean(IS_SECURITY_AVAILABLE)
                && !getArguments().getString(LAST_KNOWN_MODE).equals(AlarmSubsystemModel.SECURITYMODE_INACTIVE)
                && !getArguments().getBoolean(IS_ALARMING_STATE)) {
            lastKnownModeLabel.setVisibility(View.VISIBLE);
            lastKnownModeText.setVisibility(View.VISIBLE);
            lastKnownModeText.setText(getLastKnownModeText(getArguments().getString(LAST_KNOWN_MODE)));
        }
        else {
            lastKnownModeLabel.setVisibility(View.GONE);
            lastKnownModeText.setVisibility(View.GONE);
        }

        proMonText = (Version1TextView) contentView.findViewById(R.id.hub_offline_promon_text);
        if (getArguments().getBoolean(IS_PROMON)) {
            proMonText.setVisibility(View.VISIBLE);
        }
        else {
            proMonText.setVisibility(View.GONE);
        }

        hubOfflineGoToDashboardButton = (Version1Button) contentView.findViewById(R.id.hub_offline_return_button);
        hubOfflineGoToDashboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);
            }
        });

        hubOfflineGetSupportButton = (Version1Button) contentView.findViewById(R.id.hub_offline_support_button);
        hubOfflineGetSupportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                ActivityUtils.launchUrl(GlobalSetting.NO_CONNECTION_HUB_SUPPORT_URL);
            }
        });

        final String currentIncident = AlarmSubsystemController.getInstance().getCurrentIncident();
        alarmEventInProgressText = contentView.findViewById(R.id.alarm_event_in_progress_text);
        hubOfflineAlarmTrackerButton = (Version1Button) contentView.findViewById(R.id.hub_offline_alarm_tracker_button);
        hubOfflineAlarmTrackerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                BackstackManager.getInstance().navigateBack();
                BackstackManager.getInstance().navigateToFragment(ProMonitoringIncidentFragment.newInstance(currentIncident), true);
            }
        });
        if (getArguments().getBoolean(IS_ALARMING_STATE) && !StringUtils.isEmpty(currentIncident)) {
            alarmEventInProgressText.setVisibility(View.VISIBLE);
            hubOfflineAlarmTrackerButton.setVisibility(View.VISIBLE);
            hubOfflineGetSupportButton.setVisibility(View.GONE);
        }
        else {
            alarmEventInProgressText.setVisibility(View.GONE);
            hubOfflineAlarmTrackerButton.setVisibility(View.GONE);
            hubOfflineGetSupportButton.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        showFullScreen(true);
        setHasCloseButton(false);
        showTitleLogo(false);
        showTitle(false);
        presenter.startPresenting(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        showFullScreen(false);
        presenter.stopPresenting();
    }

    @Override
    public boolean onBackPressed() {
        //Disable Back Button
        return true;
    }

    @Override
    public void setSecurityModeChanged(String newMode) {
        final String securityMode = newMode;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lastKnownModeText.setText(getLastKnownModeText(securityMode));
            }
        });
    }

    @Override
    public void setHubLastChangedTime(final String hubLastChangedTime) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                offlineSinceText.setText(hubLastChangedTime);
            }
        });
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
    public void updateView(@NonNull HubAlarmDisconnectedContract.HubAlarmDisconnectedModel model) {
        // Nothing to do.
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_hub_alarm_disconnected;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fullscreen_arcus_popup_fragment_nopadding;
    }

    public void setCallback(HubAlarmDisconnectedPopup.Callback callback) {
        this.callback = callback;
    }

    private String getLastKnownModeText(String newLastKnownMode) {
        String lastKnownMode = "";
        if (newLastKnownMode.equals(AlarmSubsystemModel.SECURITYMODE_DISARMED)) {
            lastKnownMode = getResources().getString(R.string.security_alarm_off);
        }
        else if (newLastKnownMode.equals(AlarmSubsystemModel.SECURITYMODE_ON)){
            lastKnownMode = getResources().getString(R.string.security_alarm_on);
        }
        else if (newLastKnownMode.equals(AlarmSubsystemModel.SECURITYMODE_PARTIAL)){
            lastKnownMode = getResources().getString(R.string.security_alarm_partial);
        }

        return lastKnownMode;
    }
}
