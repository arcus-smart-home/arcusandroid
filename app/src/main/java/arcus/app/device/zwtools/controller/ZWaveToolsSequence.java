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
package arcus.app.device.zwtools.controller;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;

import arcus.app.device.list.DeviceListingFragment;
import arcus.app.device.removal.zwave.controller.ZWaveUnpairingSequenceController;
import arcus.app.device.zwtools.ZWaveToolsFragment;



public class ZWaveToolsSequence extends AbstractSequenceController {

    public enum ZWaveTool {
        REPAIR_NETWORK,
        REMOVE_DEVICE
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        ZWaveTool selectedTool = unpackArgument(0, ZWaveTool.class, data);

        switch (selectedTool) {
            case REPAIR_NETWORK:
                navigateForward(activity, new ZWaveNetworkRepairSequence(ZWaveNetworkRepairSequence.SequenceVariant.SHOW_INFO_SCREEN));
                break;
            case REMOVE_DEVICE:
                navigateForward(activity, new ZWaveUnpairingSequenceController());
                break;
            default:
                throw new IllegalArgumentException("Bug! Unimplemented tool selection.");
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        endSequence(activity, true);
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        BackstackManager.getInstance().navigateBackToFragment(new DeviceListingFragment());
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        navigateForward(activity, ZWaveToolsFragment.newInstance());
    }
}
