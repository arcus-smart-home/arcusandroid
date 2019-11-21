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

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.popups.adapter.PopupDeviceListAdapter;
import arcus.app.common.events.FloatingDayOrDeviceSelected;
import arcus.app.common.models.SessionModelManager;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class DevicePickerPopup extends ArcusFloatingFragment {
    public static final String DEVICE_SELECTED = "DEVICE.SELECTED";
    public static final String DEVICE_LIST = "DEVICE.LIST";
    private Callback callback;

    @NonNull
    public static DevicePickerPopup newInstance(String deviceSelected) {
        DevicePickerPopup fragment = new DevicePickerPopup();

        Bundle bundle = new Bundle(1);
        bundle.putString(DEVICE_SELECTED, deviceSelected);
        fragment.setArguments(bundle);

        return fragment;
    }

    /**
     *
     * Setup a trimmed down version of the device list picker based.
     *
     * @param deviceSelected previous device selected
     * @param modelIDs list of device model ID's/Adresses to include
     *
     * @return
     */
    @NonNull
    public static DevicePickerPopup newInstance(String deviceSelected, @NonNull List<String> modelIDs) {
        DevicePickerPopup fragment = new DevicePickerPopup();

        ArrayList<String> deviceArrayList = new ArrayList<>(modelIDs);

        Bundle bundle = new Bundle(2);
        bundle.putString(DEVICE_SELECTED, deviceSelected);
        bundle.putStringArrayList(DEVICE_LIST, deviceArrayList);
        fragment.setArguments(bundle);

        return fragment;
    }

    public DevicePickerPopup() {}

    @Override
    public void setFloatingTitle() {
        title.setText(getResources().getString(R.string.choose_device_text));
    }

    @Override
    public void doContentSection() {
        final List<DeviceModel> models = SessionModelManager.instance().getDevices();
        if(models != null) {
            pruneModels(models);

            ListView devicesListView = (ListView) contentView.findViewById(R.id.floating_list_view);
            final PopupDeviceListAdapter adapter;
            adapter = new PopupDeviceListAdapter(getActivity(), models, getNonNullArguments().getString(DEVICE_SELECTED, ""));

            devicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.onItemClick(parent, view, position, id);
                    if (callback != null) {
                        callback.selectedItem(models.get(position).getId());
                    } else {
                        EventBus.getDefault().post(new FloatingDayOrDeviceSelected(models.get(position)));
                    }
                }
            });
            devicesListView.setAdapter(adapter);
        }
    }

    private void pruneModels(@NonNull List<DeviceModel> models) {
        List<String> modelIDS = getNonNullArguments().getStringArrayList(DEVICE_LIST);

        if (modelIDS != null) {
            List<DeviceModel> results = new ArrayList<>();
            for (DeviceModel model : models) {
                if (modelIDS.contains(model.getId()) || modelIDS.contains(model.getAddress())) {
                    results.add(model);
                }
            }

            models.retainAll(results);
        }
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_list_picker_fragment;
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
