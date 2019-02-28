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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.ToggleSettingModel;
import arcus.app.subsystems.alarm.promonitoring.adapters.ProMonitoringToggleAdapter;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmSoundsContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmSoundsContract.AlarmSoundsModel;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmSoundsPresenter;

import java.util.ArrayList;
import java.util.Set;

import static arcus.app.subsystems.alarm.promonitoring.presenters.AlarmSoundsContract.AlarmSoundsModel.SECURITY_AND_PANIC;
import static arcus.app.subsystems.alarm.promonitoring.presenters.AlarmSoundsContract.AlarmSoundsModel.SMOKE_AND_CO;
import static arcus.app.subsystems.alarm.promonitoring.presenters.AlarmSoundsContract.AlarmSoundsModel.WATER_LEAK;


public class ProMonitoringAlarmSoundsSettings extends BaseFragment implements AlarmSoundsContract.AlarmSoundsView, ProMonitoringToggleAdapter.Callback {

    private AlarmSoundsPresenter presenter = new AlarmSoundsPresenter();

    private ArrayList<ToggleSettingModel> items = new ArrayList<>();
    private ToggleSettingModel securityToggle;
    private ToggleSettingModel smokeCOToggle;
    private ToggleSettingModel waterLeakToggle;

    private RecyclerView recyclerView;
    private ProMonitoringToggleAdapter adapter;

    @NonNull
    public static ProMonitoringAlarmSoundsSettings newInstance(){
        ProMonitoringAlarmSoundsSettings fragment = new ProMonitoringAlarmSoundsSettings();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        if(securityToggle== null) {
            securityToggle = new ToggleSettingModel(getString(R.string.security_panic_alarm), "", false, SECURITY_AND_PANIC);
        }
        if(smokeCOToggle == null) {
            smokeCOToggle = new ToggleSettingModel(getString(R.string.smoke_and_co_alarm), "", false, SMOKE_AND_CO);
        }
        if(waterLeakToggle == null) {
            waterLeakToggle = new ToggleSettingModel(getString(R.string.water_leak_alarm), "", false, WATER_LEAK);
        }

        items.clear();
        items.add(securityToggle);
        items.add(smokeCOToggle);
        items.add(waterLeakToggle);

        adapter = new ProMonitoringToggleAdapter(items, getContext());
        adapter.setCallback(this);
        recyclerView.setAdapter(adapter);

        return view;
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
        return R.layout.fragment_pro_monitoring_alarmsounds;
    }

    @Override
    public void updateSelected(ToggleSettingModel selectedItem) {

        switch(selectedItem.getId()) {
            case SECURITY_AND_PANIC:
                presenter.setSecurityPanicSilent(selectedItem.isOn());
                break;
            case SMOKE_AND_CO:
                presenter.setSmokeCoSilent(selectedItem.isOn());
                break;
            case WATER_LEAK:
                presenter.setWaterSilent(selectedItem.isOn());
                break;
        }
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        // Nothing to do
    }

    @Override
    public void onError(Throwable t) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(t);
    }

    @Override
    public void updateView(@NonNull AlarmSoundsModel model) {
        switch (model.alarmType) {
            case AlarmSoundsModel.SECURITY_AND_PANIC:
                securityToggle.setOn(!model.isSilent);
                break;

            case AlarmSoundsModel.SMOKE_AND_CO:
                smokeCOToggle.setOn(!model.isSilent);
                break;

            case AlarmSoundsModel.WATER_LEAK:
                waterLeakToggle.setOn(!model.isSilent);
                break;
        }

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onAvailableAlarmsChanged(@Nullable Set<String> alarmsAvailable) {

        items.clear();

        if(alarmsAvailable.contains("SECURITY") || alarmsAvailable.contains("PANIC")) {
            items.add(securityToggle);
        }

        if(alarmsAvailable.contains("SMOKE") || alarmsAvailable.contains("CO")) {
            items.add(smokeCOToggle);
        }

        if(alarmsAvailable.contains("WATER")) {
            items.add(waterLeakToggle);
        }

        adapter = new ProMonitoringToggleAdapter(items, getContext());
        adapter.setCallback(this);
        recyclerView.setAdapter(adapter);
    }
}
