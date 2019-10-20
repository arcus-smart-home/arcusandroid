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
package arcus.app.device.pairing.specialty.petdoor.controller;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.device.pairing.specialty.petdoor.PetDoorKeyListFragment;
import arcus.app.device.pairing.specialty.petdoor.PetDoorSmartKeyNameFragment;
import arcus.app.device.pairing.specialty.petdoor.PetDoorSmartKeyPairingStep1Fragment;
import arcus.app.device.pairing.specialty.petdoor.PetDoorSmartKeyPairingStep2Fragment;


public class PetDoorSmartKeyPairingSequenceController extends AbstractSequenceController {

    private Sequenceable previousSequence;
    private int tokenId;
    private String petDoorAddress;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof PetDoorKeyListFragment) {
            navigateForward(activity, PetDoorSmartKeyPairingStep1Fragment.newInstance(), data);
        }

        else if (from instanceof PetDoorSmartKeyPairingStep1Fragment) {
            navigateForward(activity, PetDoorSmartKeyPairingStep2Fragment.newInstance(), data);
        }

        else if (from instanceof PetDoorSmartKeyPairingStep2Fragment) {
            this.tokenId = unpackArgument(0, Integer.class, data);
            navigateForward(activity, PetDoorSmartKeyNameFragment.newInstance(petDoorAddress, tokenId, false), data);
        }

        else {
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
            navigateBack(activity, previousSequence, data);
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        this.previousSequence = from;
        this.petDoorAddress = unpackArgument(0, String.class, data);

        navigateForward(activity, PetDoorSmartKeyPairingStep1Fragment.newInstance());
    }

    public String getPetDoorAddress () { return this.petDoorAddress; }
}
