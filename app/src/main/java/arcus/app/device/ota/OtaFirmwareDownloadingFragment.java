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
package arcus.app.device.ota;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.adapter.CheckedDeviceListAdapter;
import arcus.app.device.ota.controller.FirmwareUpdateController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class OtaFirmwareDownloadingFragment extends BaseFragment implements FirmwareUpdateController.UpdateCallback {

    private static Logger logger = LoggerFactory.getLogger(OtaFirmwareDownloadingFragment.class);

    private ListView deviceList;
    private CheckedDeviceListAdapter deviceListAdapter;

    @NonNull
    public static OtaFirmwareDownloadingFragment newInstance () {
        return new OtaFirmwareDownloadingFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        deviceList = (ListView) view.findViewById(R.id.firmware_device_list);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        ImageManager.with(getActivity())
                .putDefaultWallpaper()
                .intoWallpaper(AlphaPreset.LIGHTEN)
                .execute();

        deviceListAdapter = new CheckedDeviceListAdapter(getActivity());
        deviceList.setAdapter(deviceListAdapter);

        FirmwareUpdateController.getInstance().startFirmwareUpdateStatusMonitor(getActivity(), this);
        FirmwareUpdateController.getInstance().fireCurrentStatus();
    }

    @Override
    public void onPause () {
        super.onPause();
        FirmwareUpdateController.getInstance().stopFirmwareUpdateStatusMonitor();
    }

    @Override
    public String getTitle() {
        return getString(R.string.ota_firmware_updates);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_ota_firmware_downloading;
    }

    @Override
    public void onDevicesUpdating(@NonNull List<DeviceModel> deviceList) {
        for (DeviceModel thisDevice : deviceList) {
            if (!deviceExistsInList(thisDevice)) {
                deviceListAdapter.add(thisDevice);
            }
        }
    }

    @Override
    public void onDeviceFirmwareUpdateStatusChange(@NonNull DeviceModel device, boolean isUpdating, boolean otherDevicesUpdating) {

        logger.debug("Got firmware status change. Firmware is updating {} on device {}.", isUpdating, device.getAddress());

        if (isUpdating && !deviceExistsInList(device)) {
            logger.debug("Adding device {} to list of updating devices.", device.getAddress());
            deviceListAdapter.add(device);
        } else if (!isUpdating && deviceExistsInList(device)) {
            logger.debug("Marking device {} as completed.", device.getAddress());
            deviceListAdapter.markAsComplete(device);
        }

        if (deviceListAdapter.isListComplete()) {
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);
        }
    }

    @Override
    public void onDeviceFirmwareUpdateProgressChange(DeviceModel device, Double progress) {

        logger.debug("Got firmware progress change for device {} = {}.", device.getAddress(), progress);

        if (deviceExistsInList(device)) {
            deviceListAdapter.updateProgress(device, progress);
        }
    }

    private boolean deviceExistsInList (@NonNull DeviceModel device) {
        return deviceListAdapter.deviceExist(device);
    }
}
