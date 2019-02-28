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
package arcus.app.device.pairing.specialty.halo;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.device.pairing.post.AddToFavoritesFragment;
import arcus.app.device.pairing.post.HaloInfoFragment;
import arcus.app.device.pairing.post.HaloLocationFragment;
import arcus.app.device.pairing.post.HaloPairingTestFragment;
import arcus.app.device.pairing.post.HaloRoomFragment;
import arcus.app.device.pairing.post.HaloStationSelectionFragment;
import arcus.app.device.pairing.post.HaloWeatherRadioSummaryFragment;


public class HaloPlusPairingSequenceController extends AbstractSequenceController {

    private Sequenceable previousSequence;
    private final String deviceAddress;

    public HaloPlusPairingSequenceController(Sequenceable from, String deviceAddress) {
        this.deviceAddress = deviceAddress;
        this.previousSequence = from;
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        if (from instanceof HaloRoomFragment) {
            navigateForward(activity, HaloLocationFragment.newInstance(deviceAddress, false), data);
        } else if (from instanceof HaloLocationFragment) {
            navigateForward(activity, HaloStationSelectionFragment.newInstance(deviceAddress, false));
        } else if (from instanceof HaloStationSelectionFragment) {
            navigateForward(activity, HaloWeatherRadioSummaryFragment.newInstance(deviceAddress));
        } else if (from instanceof HaloWeatherRadioSummaryFragment) {
            navigateForward(activity, HaloPairingTestFragment.newInstance());
        } else if (from instanceof HaloPairingTestFragment) {
            navigateForward(activity, HaloInfoFragment.newInstance());
        } else if (from instanceof HaloInfoFragment) {
            navigateForward(activity, AddToFavoritesFragment.newInstance(deviceAddress));
        } else {
            endSequence(activity, true, data);
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        if (previousSequence != null) {
            navigateForward(activity, previousSequence, data);
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        navigateForward(activity, HaloRoomFragment.newInstance((String)data[1], false), data);
    }

    public void skipToEnd(Activity activity) {
        navigateForward(activity, HaloPairingTestFragment.newInstance());
    }
}
