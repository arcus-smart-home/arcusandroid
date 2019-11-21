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
package arcus.app.device.removal.zwave;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.device.removal.zwave.controller.ZWaveUnpairingSequenceController;


public class ZWaveUnpairingFragment extends SequencedFragment<ZWaveUnpairingSequenceController> {

    @NonNull
    public static ZWaveUnpairingFragment newInstance () {
        return new ZWaveUnpairingFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        Version1Button button = (Version1Button) view.findViewById(R.id.remove_zwave_devices);
        button.setColorScheme(Version1ButtonColor.WHITE);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });

        return view;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.zwave_remove_devices);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_zwave_unpairing;
    }
}
