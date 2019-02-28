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


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.capability.Motion;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.adapters.decorator.HeaderDecorator;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.adapters.ProMonitoringSettingsAdapter;
import arcus.app.subsystems.alarm.promonitoring.models.AlertDeviceModel;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmMoreContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmMorePresenter;
import arcus.app.subsystems.alarm.promonitoring.settings.ProMonitoringAlarmRequirementsSettings;
import arcus.app.subsystems.alarm.promonitoring.settings.ProMonitoringAlarmSoundsSettings;
import arcus.app.subsystems.alarm.promonitoring.settings.ProMonitoringNoMotionSensors;
import arcus.app.subsystems.alarm.promonitoring.settings.PromonitoringGracePeriodSettings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;



public class ProMonitoringMoreFragment extends BaseFragment implements AlarmMoreContract.AlarmMoreView, ProMonitoringSettingsAdapter.Callback {

    private AlarmMorePresenter presenter = new AlarmMorePresenter();
    private RecyclerView deviceList;
    private LinearLayout noDevicesLayout;
    private Version1TextView shopButton;

    public static final int ALARM_SOUNDS = 0, NOTIFICATION_LIST = 1, GRACE_PERIODS = 2,
            ALARM_REQUIREMENTS = 3, WATER_SHUTOFF = 4, RECORD_ALARM = 5, SMOKE_SAFETY_SHUT_OFF = 6,
            CO_SAFETY_SHUT_OFF = 7;

    @NonNull
    public static ProMonitoringMoreFragment newInstance(){
        return new ProMonitoringMoreFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.startPresenting(this);
        presenter.requestUpdate();
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pro_monitoring_activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = super.onCreateView(inflater, container, savedInstanceState);

        deviceList = (RecyclerView) view.findViewById(R.id.promon_device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        deviceList.addItemDecoration(new HeaderDecorator(1));

        noDevicesLayout = (LinearLayout) view.findViewById(R.id.alarm_more_no_devices);

        shopButton = (Version1TextView) view.findViewById(R.id.alarm_more_no_devices_button);
        shopButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ActivityUtils.launchShopNow();
            }
        });

        return view;
    }

    private void loadItems(AlarmMoreContract.AlarmMoreModel model) {
        List<AlertDeviceModel> global = loadGlobalSettings(model);
        List<AlertDeviceModel> security = new ArrayList<>();
        List<AlertDeviceModel> smokeAndCo = new ArrayList<>();
        List<AlertDeviceModel> water = new ArrayList<>();
        if (model.isSecurityAvailable) {
            security = loadSecuritySettings(model);
        }
        if (model.isWaterAvailable) {
            water = loadWaterLeakSettings(model);
        }
        if (model.fanShutOffSupported) {
            smokeAndCo = loadSmokeAndCoSettings(model);
        }

        ProMonitoringSettingsAdapter adapter = (ProMonitoringSettingsAdapter) deviceList.getAdapter();
        if(adapter == null) {
            adapter = new ProMonitoringSettingsAdapter(global, security, smokeAndCo, water, getContext());
            adapter.setCallback(this);
            deviceList.setAdapter(adapter);
        } else {
            adapter.putLists(global, security, smokeAndCo, water);
        }
    }

    private List<AlertDeviceModel> loadGlobalSettings(AlarmMoreContract.AlarmMoreModel model) {
        List<AlertDeviceModel> list = new ArrayList<>();

        AlertDeviceModel sounds = createItem(R.string.security_alarm_config_sounds_title, R.string.control_alarm_sounds, ALARM_SOUNDS);
        list.add(sounds);

        AlertDeviceModel notifications = createItem(R.string.security_alarm_notification_list, R.string.security_alarm_notification_list_desc, NOTIFICATION_LIST);
        list.add(notifications);

        AlertDeviceModel header = AlertDeviceModel.headerModelType(getString(R.string.global_settings));
        header.hasSecurity = model.isSecurityAvailable;
        header.hasSmoke = model.fanShutOffSupported && model.isSmokeAvailable;
        header.hasCO =  model.fanShutOffSupported && model.isCoAvailable;
        header.hasWaterLeak = model.isWaterAvailable;
        list.add(0, header);
        return list;
    }

    private List<AlertDeviceModel> loadSecuritySettings(AlarmMoreContract.AlarmMoreModel model) {
        List<AlertDeviceModel> list = new ArrayList<>();

        AlertDeviceModel grace = createItem(R.string.grace_periods, R.string.grace_periods_desc, GRACE_PERIODS);
        list.add(grace);

        AlertDeviceModel alarmReq = createItem(R.string.alarm_requirements, R.string.alarm_requirements_desc, ALARM_REQUIREMENTS);
        list.add(alarmReq);

        if (model.isRecordSupported) {
            AlertDeviceModel recordOnAlarm = createToggleItem(R.string.record_alarm, R.string.record_alarm_desc, RECORD_ALARM, model.recordOnSecurity);
            list.add(recordOnAlarm);
        }

        AlertDeviceModel header = AlertDeviceModel.headerModelType(getString(R.string.security_settings));
        header.hasSecurity = true;
        list.add(0, header);
        return list;
    }


    private List<AlertDeviceModel> loadSmokeAndCoSettings(AlarmMoreContract.AlarmMoreModel model) {
        List<AlertDeviceModel> list = new ArrayList<>();

        if (model.isSmokeAvailable) {
            AlertDeviceModel smokeSafetyShutOff = createToggleItem(R.string.smoke_safety_shut_off, R.string.smoke_safety_shut_off_desc, SMOKE_SAFETY_SHUT_OFF, model.shutOffFansOnSmoke);
            list.add(smokeSafetyShutOff);
        }

        if (model.isCoAvailable) {
            AlertDeviceModel coSafetyShutOff = createToggleItem(R.string.co_safety_shut_off, R.string.co_safety_shut_off_desc, CO_SAFETY_SHUT_OFF, model.shutOffFansOnCO);
            list.add(coSafetyShutOff);
        }

        if (model.isSmokeAvailable || model.isCoAvailable) {
            AlertDeviceModel header = AlertDeviceModel.headerModelType(getString(R.string.smoke_co_settings));
            if (model.isSmokeAvailable) {
                header.hasSmoke = true;
            }
            if (model.isCoAvailable) {
                header.hasCO = true;
            }
            list.add(0, header);
        }

        return list;
    }

    private List<AlertDeviceModel> loadWaterLeakSettings(AlarmMoreContract.AlarmMoreModel model) {
        List<AlertDeviceModel> list = new ArrayList<>();

        AlertDeviceModel water = createItem(R.string.water_shut_off_valve_title, R.string.turn_water_off, WATER_SHUTOFF);
        water.waterShutoffEnabled = model.waterShutoffEnabled;
        list.add(water);

        AlertDeviceModel header = AlertDeviceModel.headerModelType(getString(R.string.waterleak_settings));
        header.hasWaterLeak = true;
        list.add(0, header);
        return list;
    }

    private AlertDeviceModel createItem(int title, int subTitle, int id) {
        AlertDeviceModel item = new AlertDeviceModel("current");
        item.mainText = getString(title);
        item.subText = getString(subTitle);
        item.id = id;
        return item;
    }

    private AlertDeviceModel createToggleItem(int title, int subTitle, int id, boolean toggleState) {
        AlertDeviceModel item = new AlertDeviceModel("current");
        item.mainText = getString(title);
        item.subText = getString(subTitle);
        item.id = id;

        switch (id) {
            case RECORD_ALARM:
                item.setRecordingSupported(toggleState);
                break;
            case SMOKE_SAFETY_SHUT_OFF:
                item.setShutOffFansOnSmoke(toggleState);
                break;
            case CO_SAFETY_SHUT_OFF:
                item.setShutOffFansOnCO(toggleState);
                break;
        }

        item.setRecordingSupported(toggleState);
        return item;
    }

    @Override
    public void updateSelected(AlertDeviceModel selectedItem) {
        switch (selectedItem.id) {
            case ALARM_SOUNDS:
                BackstackManager.getInstance().navigateToFragment(ProMonitoringAlarmSoundsSettings.newInstance(), true);
                break;
            case GRACE_PERIODS:
                BackstackManager.getInstance().navigateToFragment(PromonitoringGracePeriodSettings.newInstance(), true);
                break;
            case ALARM_REQUIREMENTS:
                DeviceModelProvider
                        .instance()
                        .reload()
                        .onSuccess(models -> {
                            List<DeviceModel> nnModels = models == null ? Collections.emptyList() : models;
                            for (DeviceModel model : nnModels) {
                                Collection<String> caps = model.getCaps();
                                if (caps != null && caps.contains(Motion.NAMESPACE)) {
                                    navigateOnMainTo(ProMonitoringAlarmRequirementsSettings.newInstance());
                                    return;
                                }
                            }

                            navigateOnMainTo(ProMonitoringNoMotionSensors.newInstance());
                        })
                        .onFailure(error -> logger.error("Can't navigate cause we can't load devices.", error));
                break;
            case NOTIFICATION_LIST:
                BackstackManager.getInstance().navigateToFragment(ProMonitoringAlarmCallListFragment.newInstance(), true);
                break;
        }
    }

    private void navigateOnMainTo(Fragment fragment) {
        LooperExecutor
            .getMainExecutor()
            .execute(() -> BackstackManager.getInstance().navigateToFragment(fragment, true));
    }
    @Override
    public void updateToggleValue(int modelId, boolean on) {

        switch(modelId) {
            case WATER_SHUTOFF:
                presenter.setWaterShutOffValue(on);
                break;
            case RECORD_ALARM:
                presenter.setRecordOnAlarmValue(on);
                break;
            case CO_SAFETY_SHUT_OFF:
                presenter.setShutFansOffOnCOValue(on);
                break;
            case SMOKE_SAFETY_SHUT_OFF:
                presenter.setShutFansOffOnSmokeValue(on);
                break;
        }
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        showProgressBar();
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        hideProgressBar();
    }

    @Override
    public void updateView(@NonNull AlarmMoreContract.AlarmMoreModel model) {

        deviceList.setVisibility(View.VISIBLE);
        noDevicesLayout.setVisibility(View.GONE);
        hideProgressBar();
        loadItems(model);
    }

    @Override
    public void presentNoDevicesAvailable() {
        deviceList.setVisibility(View.GONE);
        noDevicesLayout.setVisibility(View.VISIBLE);
    }
}
