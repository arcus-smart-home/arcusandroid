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
package arcus.app.device.removal;

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
import arcus.app.device.removal.controller.DeviceRemovalSequenceController;


public class UnpairingFailureFragment extends SequencedFragment<DeviceRemovalSequenceController> {

    private Version1Button retryButton;
    private Version1Button removeButton;

    @NonNull
    public static UnpairingFailureFragment newInstance () {
        return new UnpairingFailureFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        retryButton = (Version1Button) view.findViewById(R.id.retry_button);
        removeButton = (Version1Button) view.findViewById(R.id.remove_button);
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().retryUnpairing();
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().forceRemove();
            }
        });
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
    }

    @Override
    public String getTitle() {
        return getString(R.string.device_remove_device);
    }


    @Override public boolean onBackPressed() {
        getController().endSequence(getActivity(), false);
        return true;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_unpairing_failure;
    }
}
