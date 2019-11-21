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
package arcus.app.device.pairing.post;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.device.pairing.post.controller.PostPairingSequenceController;


public class VentWarningFragment extends SequencedFragment<PostPairingSequenceController> {

    private Version1Button nextButton;

    public static VentWarningFragment newInstance () {
        return new VentWarningFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        nextButton = (Version1Button) view.findViewById(R.id.vent_pairing_extra_next_btn);

        return view;
    }

    @Override
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

    @Override
    public String getTitle() {
        return getString(R.string.vent_extra_pairing_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_vent_pairing_extra;
    }
}
