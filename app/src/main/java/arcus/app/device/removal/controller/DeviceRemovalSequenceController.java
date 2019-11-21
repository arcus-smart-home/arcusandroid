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
package arcus.app.device.removal.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.controller.DeviceRemovalController;
import arcus.app.common.fragments.StaticContentFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.removal.ForceRemovalFailureFragment;
import arcus.app.device.removal.UnpairingFailureFragment;
import arcus.app.device.removal.UnpairingOfflineFragment;
import arcus.app.device.removal.UnpairingZWaveDeviceFragment;
import arcus.app.device.removal.UnpairingZigbeeDeviceFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeviceRemovalSequenceController extends AbstractSequenceController implements DeviceRemovalController.Callback {

    private static final Logger logger = LoggerFactory.getLogger(DeviceRemovalSequenceController.class);

    @Nullable
    private DeviceModel deviceModel;
    private Activity activity;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        throw new IllegalStateException("Bug! Next not supported in device removal sequence.");
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        // Nothing to do; back not allowed in this sequence.
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {

        if (data.length != 1 && ! (data[0] instanceof DeviceModel)) {
            throw new IllegalArgumentException("First data argument must be a DeviceModel");
        }

        this.activity = activity;
        this.deviceModel = (DeviceModel) data[0];

        if (deviceModel != null) {
            ImageManager.with(activity)
                    .setWallpaper(Wallpaper.ofDevice(deviceModel.getPlace(), deviceModel).lightend());
            logger.debug("Starting to un-pair device {}.", deviceModel.getName());
            DeviceRemovalController.instance().remove(deviceModel.getAddress(), this);
        }
    }

    public void retryUnpairing () {
        if (deviceModel == null) {
            throw new IllegalStateException("Can't retry before starting; call startUnpairing() first.");
        }

        logger.debug("Retrying to un-pair device {}.", deviceModel.getName());
        startSequence(activity, null, deviceModel);
    }

    public void forceRemove () {
        if (deviceModel == null) {
            throw new IllegalStateException("Can't force remove before trying to un-pair; call startUnpairing() first.");
        }

        logger.debug("Trying to force-remove device {}.", deviceModel.getName());
        DeviceRemovalController.instance().forceRemove(deviceModel.getAddress(), this);
    }

    public void cancel () {
        logger.debug("Canceling device removal.");
        DeviceRemovalController.instance().cancelRemove(this);
    }

    @Override
    public void deviceUnpairing(@NonNull DeviceRemovalController.DeviceType type, DeviceRemovalController.RemovalType removalType) {
        logger.debug("deviceUnpairing() handler invoked for {} in mode {}.", type, removalType);

        switch (type) {
            case MOCK:
            case CAMERA:
            case IPCD:
            case ZIGBEE:
                navigateForward(activity, UnpairingZigbeeDeviceFragment.newInstance());
                break;
            case ZWAVE:
                navigateForward(activity, UnpairingZWaveDeviceFragment.newInstance(deviceModel.getProductId()));
                break;

            case OTHER:
                default:
                    throw new IllegalStateException("Bug! Don't know how to un-pair device of type " + type);
        }
    }

    @Override
    public void unpairingSuccess(DeviceRemovalController.DeviceType type, @NonNull DeviceRemovalController.RemovalType removalType) {
        logger.debug("unpairingSuccess() handler invoked for {} in mode {}.", type, removalType);

        switch (removalType) {
            case NORMAL:
                navigateForward(activity, StaticContentFragment.newInstance(activity.getString(R.string.device_remove_device), R.layout.fragment_unpairing_success, true));
                break;
            case FORCE:
                navigateForward(activity, StaticContentFragment.newInstance(activity.getString(R.string.device_remove_device), R.layout.fragment_force_remove_success, true));
                break;

                default:
                    throw new IllegalStateException("Bug! Removal handled for type " + removalType);
        }
    }

    @Override
    public void unpairingFailure(DeviceRemovalController.DeviceType type, @NonNull DeviceRemovalController.RemovalType removalType) {
        logger.debug("unpairingFailure() handler invoked for {} in mode {}.", type, removalType);

        switch (removalType) {
            case FORCE:
                navigateForward(activity, ForceRemovalFailureFragment.newInstance());
                break;
            case NORMAL:
                navigateForward(activity, UnpairingFailureFragment.newInstance());
                break;

                default:
                    throw new IllegalStateException("Bug! Un-pairing case not handled for type " + removalType);
        }
    }

    @Override
    public void deviceOffline(DeviceRemovalController.DeviceType type, DeviceRemovalController.RemovalType removalType) {
        logger.debug("deviceOffline() handler invoked for {} in mode {}.", type, removalType);
        navigateForward(activity, UnpairingOfflineFragment.newInstance(), false);
    }

    @Override
    public void showLoading() {
        logger.debug("showLoading() handler invoked.");
    }

}
