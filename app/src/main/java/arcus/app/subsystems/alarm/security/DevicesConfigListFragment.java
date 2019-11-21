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
import android.widget.ListView;

import arcus.cornea.subsystem.security.SecurityDeviceConfigController;
import arcus.cornea.subsystem.security.model.ConfigDeviceModel;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.subsystems.alarm.security.adapters.DevicesConfigListAdapter;
import arcus.app.common.fragments.BaseFragment;

import java.util.List;


public class DevicesConfigListFragment extends BaseFragment implements SecurityDeviceConfigController.Callback, SecurityDeviceConfigController.SelectedDeviceCallback {

    private DevicesConfigListAdapter mAdapter;

    private SecurityDeviceConfigController mConfigController;
    private ListenerRegistration mCallbackListener;


    @NonNull
    public static DevicesConfigListFragment newInstance() {
        DevicesConfigListFragment fragment = new DevicesConfigListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        ListView mListView = (ListView) view.findViewById(R.id.device_list);
        mAdapter = new DevicesConfigListAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mConfigController == null) {
            mConfigController = SecurityDeviceConfigController.instance();
        }

        mCallbackListener = mConfigController.setCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        mCallbackListener.remove();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "ALARM DEVICES";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_security_alarm_devices;
    }

    @Override
    public void updateDevices(@NonNull List<ConfigDeviceModel> models) {
        mAdapter.clear();
        mAdapter.addItems(models);
    }

    @Override
    public void updateDevice(ConfigDeviceModel model) {

    }

    @Override
    public void updateSelected(String name, SecurityDeviceConfigController.Mode mode) {

    }
}
