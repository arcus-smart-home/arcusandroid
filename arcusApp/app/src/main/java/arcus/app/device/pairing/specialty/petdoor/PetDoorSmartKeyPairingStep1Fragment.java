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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.device.pairing.specialty.petdoor.controller.PetDoorSmartKeyPairingSequenceController;


public class PetDoorSmartKeyPairingStep1Fragment extends SequencedFragment<PetDoorSmartKeyPairingSequenceController> {

    private Version1Button nextButton;

    public static PetDoorSmartKeyPairingStep1Fragment newInstance () {
        return new PetDoorSmartKeyPairingStep1Fragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);
        return view;
    }

    public void onResume () {
        super.onResume();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.petdoor_smart_key);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_pet_door_smart_key_pairing_step_1;
    }
}
