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
package arcus.app.device.ota.controller;

import android.app.Activity;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.StaticContentFragment;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.ota.OtaFirmwareUpdateFailedFragment;
import arcus.app.device.ota.OtaFirmwareUpdateRequiredFragment;


public class FirmwareUpdateSequenceController extends AbstractSequenceController implements FirmwareUpdateController.UpdateSequenceCallback {

    private final Activity activity;

    public FirmwareUpdateSequenceController(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void startSequence (Activity activity, Sequenceable from, Object... data) {
        FirmwareUpdateController.getInstance().startNewDeviceFirmwareUpdateMonitor((Activity) activity, this);
    }

    @Override
    public void goNext(@NonNull Activity activity, Sequenceable from, Object... data) {
        if (from instanceof OtaFirmwareUpdateRequiredFragment) {
            SequencedFragment updatingFragment = StaticContentFragment.newInstance(activity.getString(R.string.ota_update), R.layout.fragment_ota_firmware_update_inprogress, true);
            navigateForward(activity, updatingFragment);
        }

        // Better never happen... indicates a controller/fragment bug somewhere
        else {
            throw new IllegalStateException("Bug! Only OtaFirmwareUpdateRequiredFragment defines a goNext() behavior.");
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        FirmwareUpdateController.getInstance().stopNewDeviceFirmwareUpdateMonitor();
    }

    @Override
    public void onLoading() {
        if (getActiveFragment() != null && getActiveFragment() instanceof SequencedFragment) {
            ((SequencedFragment) getActiveFragment()).showProgressBar();
        }
    }

    @Override
    public void onFirmwareUpdateRequired() {
        SequencedFragment updateRequiredFragment = OtaFirmwareUpdateRequiredFragment.newInstance();
        navigateForward(activity, updateRequiredFragment);
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccesful, Object... data) {
        FirmwareUpdateController.getInstance().stopNewDeviceFirmwareUpdateMonitor();
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);
    }

    @Override
    public void onFirmwareUpdateNotRequired() {
        endSequence(activity, true);
    }

    @Override
    public void onFirmwareUpdateFailed() {
        OtaFirmwareUpdateFailedFragment.newInstance();
    }

    @Override
    public void onFirmwareUpdateSucceeded() {
        SequencedFragment successFragment = StaticContentFragment.newInstance(activity.getString(R.string.ota_update), R.layout.fragment_ota_firmware_update_complete, true);
        navigateForward(activity, successFragment);
    }
}
