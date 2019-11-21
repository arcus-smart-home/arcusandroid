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

import arcus.cornea.subsystem.security.SecurityDeviceStatusController;
import arcus.cornea.subsystem.security.model.AlarmDeviceSection;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.subsystems.alarm.security.adapters.DevicesListAdapter;
import arcus.app.common.fragments.BaseFragment;

import java.util.List;


public class DevicesFragment extends BaseFragment implements SecurityDeviceStatusController.Callback {

    private static title titles;

    enum title{
        PARTIAL,
        ON,
        NULL
    }

    private static final String PARTIAL_KEY = "PARTIAL";

    private DevicesListAdapter mAdapter;

    private SecurityDeviceStatusController mStatusController;
    private ListenerRegistration mCallbackListener;

    private boolean isPartial = false;

    @NonNull
    public static DevicesFragment newInstance(boolean partial) {
        DevicesFragment fragment = new DevicesFragment();

        Bundle bundle = new Bundle();
        bundle.putBoolean(PARTIAL_KEY, partial);
        fragment.setArguments(bundle);

        return fragment;
    }

    public static DevicesFragment newInstance(boolean partial, title t) {
        DevicesFragment frag = newInstance(partial);
        titles = t;
        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            isPartial = arguments.getBoolean(PARTIAL_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        ListView mListView = (ListView) view.findViewById(R.id.device_list);
        mAdapter = new DevicesListAdapter(getActivity());
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mStatusController == null) {
            if (isPartial) {
                mStatusController = SecurityDeviceStatusController.partial();
            } else {
                mStatusController = SecurityDeviceStatusController.all();
            }
        }

        mCallbackListener = mStatusController.setCallback(this);

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

        if(titles == title.ON) return "ON DEVICES";
        else if(titles == title.PARTIAL) return "PARTIAL DEVICES";
        else return "SECURITY ALARM DEVICES";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_security_alarm_devices;
    }

    @Override
    public void updateSections(@NonNull List<AlarmDeviceSection> sections) {
        mAdapter.clear();

        for (AlarmDeviceSection section : sections) {
            mAdapter.addItemsForSection(section.getTitle(), section.getDevices());
        }
    }

    @Override
    public void updateSection(AlarmDeviceSection section) {

    }
}
