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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.model.DeviceModel;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.subsystems.alarm.promonitoring.models.AlertDeviceModel;
import arcus.app.R;
import arcus.app.common.adapters.decorator.HeaderDecorator;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.subsystems.alarm.promonitoring.adapters.ProMonitoringTypeDeviceAdapter;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmDeviceListContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmDeviceListPresenter;
import arcus.app.subsystems.alarm.security.DeviceConfigFragment;

import java.util.List;




public class ProMonitoringTypeDeviceListFragment extends BaseFragment implements AlarmDeviceListContract.AlarmDeviceListView, ProMonitoringTypeDeviceAdapter.OnDeviceClickListener {

    private static String ALARM_TYPE_KEY = "ALARM_TYPE";

    private RecyclerView deviceList;
    private AlarmDeviceListPresenter presenter = new AlarmDeviceListPresenter();
    private View noDevices;

    @NonNull
    public static ProMonitoringTypeDeviceListFragment newInstance(@NonNull String alarmType){
        ProMonitoringTypeDeviceListFragment fragment = new ProMonitoringTypeDeviceListFragment();

        Bundle bundle = new Bundle(1);
        bundle.putSerializable(ALARM_TYPE_KEY, alarmType);
        fragment.setArguments(bundle);

        return fragment;
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
        presenter.requestUpdate(getAlarmType());
        getActivity().setTitle(getTitle());
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.security_alarm_devices);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pro_monitoring_activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        deviceList = (RecyclerView) view.findViewById(R.id.promon_device_list);
        deviceList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        deviceList.getItemAnimator().setRemoveDuration(0);
        deviceList.getItemAnimator().setMoveDuration(0);
        deviceList.getItemAnimator().setChangeDuration(0);
        deviceList.getItemAnimator().setAddDuration(0);

        noDevices = view.findViewById(R.id.alarm_more_no_devices);
        noDevices.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        showProgressBar();
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void updateView(@NonNull List<AlertDeviceModel> model) {
        final ProMonitoringTypeDeviceAdapter adapter = new ProMonitoringTypeDeviceAdapter(model);

        if (AlarmSubsystem.ACTIVEALERTS_SECURITY.equalsIgnoreCase(getAlarmType())) {
            adapter.setOnDeviceClickedListener(this);
        }

        deviceList.setAdapter(adapter);
        deviceList.addItemDecoration(new HeaderDecorator(1));
    }

    private String getAlarmType() {
        return getArguments().getString(ALARM_TYPE_KEY);
    }

    @Override
    public void onDeviceClicked(DeviceModel deviceModel) {
        BackstackManager.getInstance().navigateBackToFragment(DeviceConfigFragment.newInstance(deviceModel.getName(), deviceModel.getId()));
    }
}
