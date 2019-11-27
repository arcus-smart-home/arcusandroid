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
package arcus.app.device.removal.zwave.controller;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.device.list.DeviceListingFragment;
import arcus.app.device.removal.zwave.ZWaveUnpairingFragment;
import arcus.app.device.removal.zwave.ZWaveUnpairingSearchFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ZWaveUnpairingSequenceController extends AbstractSequenceController  {

    private static Logger logger = LoggerFactory.getLogger(ZWaveUnpairingSequenceController.class);

    private Sequenceable lastSequence;

    @Override
    public void startSequence (Activity activity, Sequenceable from, Object... data) {
        this.lastSequence = from;
        navigateForward(activity, ZWaveUnpairingFragment.newInstance(), data);
    }

    @Override
    public void endSequence (Activity activity, boolean isSuccess, Object... data) {
        if (lastSequence != null) {
            navigateBack(activity, lastSequence, data);
        } else {
            BackstackManager.getInstance().navigateBackToFragment(new DeviceListingFragment());
        }
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        if (from instanceof ZWaveUnpairingFragment) {
            // Next
            navigateForward(activity, ZWaveUnpairingSearchFragment.newInstance(), data);
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        endSequence(activity, true);
    }
}
