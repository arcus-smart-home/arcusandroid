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
package arcus.app.device;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.models.SessionModelManager;
import arcus.app.device.adapter.DeviceListAdapter;
import arcus.app.device.details.DeviceDetailParentFragment;
import arcus.app.device.zwtools.controller.ZWaveToolsSequence;

import java.util.ArrayList;
import java.util.List;

public class DeviceListingFragment extends BaseFragment implements BackstackPopListener, SessionModelManager.SessionModelChangeListener {
    private List<DeviceModel> devicesPaired = new ArrayList<>();
    private DeviceListAdapter deviceListAdapter;
    private ListView deviceListView;
    private TextView numOfDevicesTextView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mView = super.onCreateView(inflater, container, savedInstanceState);
        numOfDevicesTextView = (TextView) mView.findViewById(R.id.tvNumOfDevices);
        deviceListView = (ListView) mView.findViewById(R.id.lvDeviceList);

        return (mView);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        deviceListAdapter = new DeviceListAdapter(getActivity(), devicesPaired);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListAdapter.notifyDataSetChanged();

        if(numOfDevicesTextView != null && devicesPaired != null){
            String s = devicesPaired.size()+"";
            numOfDevicesTextView.setText(s);
        }

        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long arg3) {
                removeListener();
                if (deviceListAdapter.getItemViewType(position) == DeviceListAdapter.TYPE_ITEM) {
                    BackstackManager.getInstance()
                            .navigateToFragment(DeviceDetailParentFragment.newInstance(position), true);
                } else {
                    new ZWaveToolsSequence().startSequence(getActivity(), null);
                }
            }
        });

        if (!SessionModelManager.instance().isListenerRegistered(this)) {
            addListener();
        }
        getDevicesPaired();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListener();
        if (SessionModelManager.instance().isListenerRegistered(this))
            removeListener();
    }

    @Override
    public void onPopped() {
        String placeID = "";
        PlaceModel model = RegistrationContext.getInstance().getPlaceModel();
        if (model != null) {
            placeID = model.getId();
        }
        ImageManager.with(getActivity())
              .setWallpaper(Wallpaper.ofPlace(placeID).darkened());
    }

    @NonNull
    @Override
    public String getTitle() {
        return "Devices";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_device_listing;
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_device_list;
    }

    private void addListener() {
        SessionModelManager.instance().addSessionModelChangeListener(this);
    }

    private void removeListener() {
        SessionModelManager.instance().removeSessionModelChangeListener(this);
    }

    @Override
    public void onSessionModelChangeEvent(SessionModelManager.SessionModelChangedEvent event) {
        getDevicesPaired();
    }

    private void getDevicesPaired() {
        devicesPaired = SessionModelManager.instance().getDevicesWithHub();
        if (devicesPaired != null) {
            deviceListAdapter.setDevices(devicesPaired);
            numOfDevicesTextView.setText(String.format("%d", devicesPaired.size()));
        }
    }
}
