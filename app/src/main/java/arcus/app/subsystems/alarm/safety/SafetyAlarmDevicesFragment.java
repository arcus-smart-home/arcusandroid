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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import arcus.cornea.subsystem.safety.DeviceListController;
import arcus.cornea.subsystem.safety.model.SafetyDevice;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.subsystems.alarm.safety.adapter.SafetyAlarmDevicesFragmentListAdapter;
import arcus.app.common.fragments.BaseFragment;

import java.util.List;


public class SafetyAlarmDevicesFragment extends BaseFragment implements DeviceListController.Callback{

    private SafetyAlarmDevicesFragmentListAdapter mAdapter;

    private DeviceListController deviceListController;
    private ListenerRegistration mCallbackListener;

    @NonNull
    public static SafetyAlarmDevicesFragment newInstance() {
        SafetyAlarmDevicesFragment fragment = new SafetyAlarmDevicesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ListView mListView = (ListView) view.findViewById(R.id.fragment_safety_alarm_devices_list);
        mAdapter = new SafetyAlarmDevicesFragmentListAdapter(getActivity());
        mListView.setAdapter(mAdapter);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(deviceListController == null){
            deviceListController = DeviceListController.instance();
        }

        mCallbackListener = deviceListController.setCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mCallbackListener ==null || !mCallbackListener.isRegistered()){
            return;
        }
        mCallbackListener.remove();
    }

    @Override
    public String getTitle() {
        return getString(R.string.safety_alarm_devices);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_safety_alarm_devices;
    }

    @Override
    public void showDevices(@NonNull List<SafetyDevice> devices) {
        logger.debug("Got list of alarm devices: {}", devices);
        mAdapter.clear();
        mAdapter.addItemsForSection(devices);
    }

    @Override
    public void updateDevice(@NonNull SafetyDevice device) {
        logger.debug("One device is updated: {}", device);
        mAdapter.refreshList(device);
    }
}

