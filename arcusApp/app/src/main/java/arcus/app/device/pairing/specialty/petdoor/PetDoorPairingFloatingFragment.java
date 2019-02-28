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
package arcus.app.device.pairing.specialty.petdoor;

import android.support.annotation.Nullable;

import arcus.app.R;
import arcus.app.common.popups.ArcusFloatingFragment;


public class PetDoorPairingFloatingFragment extends ArcusFloatingFragment {

    public static PetDoorPairingFloatingFragment newInstance () {
        return new PetDoorPairingFloatingFragment();
    }

    @Override
    public void setFloatingTitle() {
        // Nothing to do
    }

    @Override
    public void doContentSection() {
        // Nothing to do
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_pet_door_pairing;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }
}
