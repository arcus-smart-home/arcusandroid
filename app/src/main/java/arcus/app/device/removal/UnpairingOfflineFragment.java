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
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.device.removal.controller.DeviceRemovalSequenceController;


public class UnpairingOfflineFragment extends SequencedFragment<DeviceRemovalSequenceController> {

    private Version1Button removeButton;
    private Version1Button cancelButton;

    @NonNull
    public static UnpairingOfflineFragment newInstance () {
        return new UnpairingOfflineFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        cancelButton = (Version1Button) view.findViewById(R.id.cancel_button);
        removeButton = (Version1Button) view.findViewById(R.id.remove_button);
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endSequence(true);
            }
        });

        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().forceRemove();
            }
        });
    }

    @Override
    public String getTitle() {
        return getString(R.string.device_remove_device);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_unpairing_offline;
    }
}
