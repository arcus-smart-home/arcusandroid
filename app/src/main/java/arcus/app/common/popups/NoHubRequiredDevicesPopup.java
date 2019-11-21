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

import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import arcus.app.R;


public class NoHubRequiredDevicesPopup extends ArcusFloatingFragment {

    private ListView deviceList;

    public static NoHubRequiredDevicesPopup newInstance () {
        return new NoHubRequiredDevicesPopup();
    }

    @Override
    public void onResume() {
        super.onResume();
        showFullScreen(true);
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override
    public void doContentSection() {
        deviceList = (ListView) contentView.findViewById(R.id.device_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.cell_no_hub_device, R.id.device_name);
        adapter.addAll(getResources().getStringArray(R.array.no_hub_devices_list));
        deviceList.setAdapter(adapter);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_no_hub_required;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.no_hub_devices);
    }
}
