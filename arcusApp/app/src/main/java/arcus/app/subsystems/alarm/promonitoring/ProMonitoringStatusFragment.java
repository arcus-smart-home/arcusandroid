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
package arcus.app.subsystems.alarm.promonitoring;


import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import arcus.cornea.subsystem.alarm.model.AlertDeviceStateModel;
import com.iris.client.capability.AlarmSubsystem;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.backstack.TransitionEffect;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.BypassedAndOfflineDeviceScrollablePopup;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.subsystems.alarm.promonitoring.adapters.ProMonitoringAlarmTypeAdapter;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.AlertingAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.InactiveAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.PanicAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.SecurityAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmStatusContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmStatusPresenter;

import java.util.ArrayList;
import java.util.List;




public class ProMonitoringStatusFragment extends BaseFragment implements ProMonitoringAlarmTypeAdapter.Callback, AlarmStatusContract.AlarmStatusView, IShowedFragment {

    AlarmStatusPresenter presenter = new AlarmStatusPresenter();

    RecyclerView alarmTypeList;
    RelativeLayout promonAd;
    ProMonitoringAlarmTypeAdapter adapter;
    AlarmStatusContract.AlarmStatusPresenterModel model;
    boolean isArming;
    private Uri adTarget;

    @NonNull
    public static ProMonitoringStatusFragment newInstance() {
        return new ProMonitoringStatusFragment();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.startPresenting(this);
        presenter.requestUpdate();
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pro_monitoring_status;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);

        alarmTypeList = (RecyclerView) view.findViewById(R.id.recyclerview);

        promonAd = (RelativeLayout) view.findViewById(R.id.promon_ad);
        promonAd.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener(){
            @Override public void onGlobalLayout() {
                setPromonAd(null);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        alarmTypeList.setLayoutManager(layoutManager);

        ArrayList<Integer> childIds = new ArrayList<Integer>();
        childIds.add(R.id.on_button);
        childIds.add(R.id.partial_button);
        childIds.add(R.id.off_button);

        return view;
    }

    @Override
    public void onItemClicked(View child) {
        int position = alarmTypeList.getChildAdapterPosition(child);
        AlarmStatusModel statusModel = adapter.getItemAt(position);

        if (statusModel == null) {
            return;
        }

        //If the model is alerting or the alarm is ON and entrance delay
        if (statusModel instanceof AlertingAlarmStatusModel ||
                (statusModel instanceof SecurityAlarmStatusModel &&
                        ((SecurityAlarmStatusModel) statusModel).getAlarmState().equals(SecurityAlarmStatusModel.SecurityAlarmArmingState.ON) &&
                        ((SecurityAlarmStatusModel) statusModel).getPrealertSecondsRemaining() > 0
                )
            ) {
            presenter.presentCurrentIncident();
        }

        else if (statusModel instanceof InactiveAlarmStatusModel) {

            switch (statusModel.getAlarmTypeString().toUpperCase()) {
                case AlarmSubsystem.ACTIVEALERTS_WATER:
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(ProMonitoringMoreDevicesNeededFragment.newInstance(R.drawable.promon_leak_white_filled,
                            getString(R.string.water_leak_status_title).toUpperCase(), getString(R.string.waterleak_alarm_devices_needed_copy), getString(R.string.waterleak_alarm_devices_needed_subtitle),
                            getString(R.string.waterleak_alarm_devices_needed_description), getString(R.string.waterleak_alarm_devices_needed_button)), true);
                    break;
                case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(ProMonitoringMoreDevicesNeededFragment.newInstance(R.drawable.promon_security_white_filled,
                            getString(R.string.tutorials_security).toUpperCase(), getString(R.string.security_alarm_devices_needed_copy), getString(R.string.security_alarm_devices_needed_subtitle),
                            getString(R.string.security_alarm_devices_needed_description), getString(R.string.security_alarm_devices_needed_button)), true);
                    break;
                case AlarmSubsystem.ACTIVEALERTS_CO:
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(ProMonitoringMoreDevicesNeededFragment.newInstance(R.drawable.promon_co_white_filled,
                            getString(R.string.co_status_title).toUpperCase(), getString(R.string.co_alarm_devices_needed_copy), getString(R.string.co_alarm_devices_needed_subtitle),
                            getString(R.string.co_alarm_devices_needed_description), getString(R.string.co_alarm_devices_needed_button)), true);
                    break;
                case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(ProMonitoringMoreDevicesNeededFragment.newInstance(R.drawable.promon_smoke_white_filled,
                            getString(R.string.smoke_status_title), getString(R.string.smoke_alarm_devices_needed_copy), getString(R.string.smoke_alarm_devices_needed_subtitle),
                            getString(R.string.smoke_alarm_devices_needed_description), getString(R.string.smoke_alarm_devices_needed_button)), true);
                    break;
                default:
                    break;
            }

        }

        // Show participating devices list unless clicking on panic
        else if (! (statusModel instanceof PanicAlarmStatusModel)) {
            BackstackManager.withAnimation(TransitionEffect.FADE).navigateToFragment(ProMonitoringTypeDeviceListFragment.newInstance(statusModel.getAlarmTypeString()), true);
        }
    }

    @Override
    public void arm() {
        isArming = true;
        presenter.armSecurityAlarm(false);
    }

    @Override
    public void disarm() {
        isArming = false;
        presenter.disarmSecurityAlarm();
    }

    @Override
    public void partial() {
        isArming = true;
        presenter.armPartialSecurityAlarm(false);
    }

    @Override
    public void onPromptUnsecured(List<AlertDeviceStateModel> unsecuredDevices, final boolean isPartialMode) {
        final BypassedAndOfflineDeviceScrollablePopup floatingFragment = BypassedAndOfflineDeviceScrollablePopup.newInstance(
                Version1ButtonColor.MAGENTA,
                getActivity().getString(R.string.cancel),
                Version1ButtonColor.BLACK,
                getActivity().getString(R.string.continue_to_arm),
                new ArrayList<>(unsecuredDevices),
                new BypassedAndOfflineDeviceScrollablePopup.Callback() {
                    @Override
                    public boolean onTopButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean onBottomButtonClicked() {
                        if (isPartialMode) {
                            presenter.armPartialSecurityAlarm(true);
                        } else {
                            presenter.armSecurityAlarm(true);
                        }
                        return true;
                    }
                });
        adapter.notifyDataSetChanged();
        BackstackManager.getInstance().navigateToFloatingFragment(floatingFragment, floatingFragment.getClass().getName(), true);
    }

    @Override
    public void onPromptNotEnoughSecurityDevices() {
        AlertPopup popup = AlertPopup.newInstance(
                getString(R.string.alarm_unable_to_arm),
                getString(R.string.alarm_unable_to_arm_desc),
                getString(R.string.alarm_status_okay).toUpperCase(),
                null,
                AlertPopup.ColorStyle.PINK,
                new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return true;
                    }

                    @Override
                    public void close() {

                    }
                });
        adapter.notifyDataSetChanged();
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onPromptWaitingForMonitoringStation() {
        AlertPopup popup = AlertPopup.newInstance(
                getString(R.string.alarm_unable_to_arm),
                Html.fromHtml(getString(R.string.alarm_unable_to_arm_pending_desc, GlobalSetting.PRO_MONITORING_STATION_NUMBER)),
                getString(R.string.alarm_status_okay).toUpperCase(),
                null,
                AlertPopup.ColorStyle.PINK,
                new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return true;
                    }

                    @Override
                    public void close() {

                    }
                });
        adapter.notifyDataSetChanged();
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onPromptRequestTimedOut() {
        String requestTimeoutTitle;
        String requestTimeoutMessage;

        if (isArming) {
            requestTimeoutTitle = getString(R.string.hub_local_offline_timeout_arm_title);
            requestTimeoutMessage = getString(R.string.hub_local_offline_timeout_arm_message);
        }
        else {
            requestTimeoutTitle = getString(R.string.hub_local_offline_timeout_disarm_title);
            requestTimeoutMessage = getString(R.string.hub_local_offline_timeout_disarm_message);
        }

        AlertPopup popup = AlertPopup.newInstance(
                requestTimeoutTitle,
                requestTimeoutMessage,
                getString(R.string.alarm_status_okay).toUpperCase(),
                null,
                AlertPopup.ColorStyle.PINK,
                new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return true;
                    }

                    @Override
                    public void close() {

                    }
                });
        adapter.notifyDataSetChanged();
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onPromptHubDisarming() {
        String requestTimeoutTitle;
        String requestTimeoutMessage;

        requestTimeoutTitle = getString(R.string.hub_local_offline_hub_disarming_title);
        requestTimeoutMessage = getString(R.string.hub_local_offline_hub_disarming_message);

        AlertPopup popup = AlertPopup.newInstance(
                requestTimeoutTitle,
                requestTimeoutMessage,
                getString(R.string.alarm_status_okay).toUpperCase(),
                null,
                AlertPopup.ColorStyle.PINK,
                new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return true;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return true;
                    }

                    @Override
                    public void close() {

                    }
                });
        adapter.notifyDataSetChanged();
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {

    }

    @Override
    public void onError(@NonNull Throwable throwable) {
    }

    @Override
    public void updateView(@NonNull AlarmStatusContract.AlarmStatusPresenterModel model) {
        adapter = new ProMonitoringAlarmTypeAdapter(model.getAlarmModels());
        adapter.setCallback(this);
        alarmTypeList.setAdapter(adapter);
        setPromonAd(model.getAdTarget());
    }

    @Override
    public void onShowedFragment() {
    }

    private void setPromonAd( Uri adTarget) {
        if (adTarget != null) {
            this.adTarget = adTarget;
        }

        if (this.adTarget != null && isAdded()) {
            int promonAdDrawableHeight = getResources().getDrawable(R.drawable.place_person_header_313x132).getIntrinsicHeight();

            Rect promonAdImageViewBounds = new Rect();
            promonAd.getGlobalVisibleRect(promonAdImageViewBounds);
            int promonAdImageViewHeight = promonAdImageViewBounds.height();

            if (promonAdImageViewHeight >= promonAdDrawableHeight) {
                promonAd.setVisibility(View.VISIBLE);
                promonAd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ActivityUtils.launchUrl(ProMonitoringStatusFragment.this.adTarget);
                    }
                });
            }
            else {
                promonAd.setVisibility(View.INVISIBLE);
            }
        }
    }
}
