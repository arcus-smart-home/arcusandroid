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
package arcus.app.common.popups;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.models.DeviceMode;
import arcus.app.common.popups.adapter.PopupDeviceModeListAdapter;

import java.util.ArrayList;
import java.util.List;

public class DeviceModePopup extends ArcusFloatingFragment {
    public static final String DEVICE_MODE_SELECTED = "DEVICE.MODE.SELECTED";
    public static final String DEVICE_MODE_LIST = "DEVICE.MODE.LIST";
    private Callback callback;

    @NonNull
    public static DeviceModePopup newInstance(String deviceModeSelected) {
        DeviceModePopup fragment = new DeviceModePopup();

        Bundle bundle = new Bundle(1);
        bundle.putString(DEVICE_MODE_SELECTED, deviceModeSelected);
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     *
     * Setup a trimmed down version of the device list picker based.
     *
     * @param deviceModeSelected previous device selected
     * @param device modes  list of device model ID's/Adresses to include
     *
     * @return
     */
    @NonNull
    public static DeviceModePopup newInstance(String deviceModeSelected, @NonNull List<DeviceMode> deviceModeList) {
        DeviceModePopup fragment = new DeviceModePopup();

        ArrayList<DeviceMode> deviceArrayList = new ArrayList<>(deviceModeList);

        Bundle bundle = new Bundle(2);
        bundle.putString(DEVICE_MODE_SELECTED, deviceModeSelected);
        bundle.putSerializable(DEVICE_MODE_LIST, deviceArrayList);
        fragment.setArguments(bundle);

        return fragment;
    }

    public DeviceModePopup() {}

    @Override
    public void setFloatingTitle() {
        title.setText(getResources().getString(R.string.device_choose_mode));
    }

    @Override
    public void doContentSection() {
        final ArrayList<DeviceMode> modes = (ArrayList<DeviceMode>)getNonNullArguments().getSerializable(DEVICE_MODE_LIST);

        ListView devicesListView = (ListView) contentView.findViewById(R.id.floating_list_view);
        final PopupDeviceModeListAdapter adapter;
        adapter = new PopupDeviceModeListAdapter(getActivity(), (ArrayList<DeviceMode>)getNonNullArguments().getSerializable(DEVICE_MODE_LIST), getNonNullArguments().getString(DEVICE_MODE_SELECTED, ""));

        devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.onItemClick(parent, view, position, id);
                if (callback != null) {
                    adapter.setSelection(modes.get(position).getTitle());
                    callback.selectedItem(modes.get(position).getTitle());
                    adapter.notifyDataSetChanged();
                }
            }
        });
        devicesListView.setAdapter(adapter);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker_fullscreen;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_list_picker_fragment_fullscreen;
    }

    public Bundle getNonNullArguments() {
        if (getArguments() == null) {
            return new Bundle();
        }
        else {
            return getArguments();
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void selectedItem(String id);
    }
}
