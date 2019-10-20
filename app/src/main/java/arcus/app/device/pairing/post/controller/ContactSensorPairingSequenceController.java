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
package arcus.app.device.pairing.post.controller;

import android.app.Activity;

import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.device.pairing.post.ContactSensorPairingFragment;


public class ContactSensorPairingSequenceController extends AbstractSequenceController {

    private Sequenceable previousSequencable;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        endSequence(activity, true, data);
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        endSequence(activity, false, data);
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        navigateBack(activity, previousSequencable);
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        this.previousSequencable = from;
        String deviceName = unpackArgument(0, String.class, data);
        String deviceAddress = unpackArgument(1, String.class, data);

        navigateForward(activity, ContactSensorPairingFragment.newInstance(deviceName, deviceAddress));
    }
}
