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
package arcus.app.device.pairing.nohub.swannwifi;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannWifiPairingSequenceController;


public class SwannScheduleFragment extends SequencedFragment<SwannWifiPairingSequenceController> {

    private Version1Button acceptScheduleButton;
    private Version1Button declineScheduleButton;

    public static SwannScheduleFragment newInstance() {
        return new SwannScheduleFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        acceptScheduleButton = (Version1Button) view.findViewById(R.id.accept_button);
        declineScheduleButton = (Version1Button) view.findViewById(R.id.decline_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        acceptScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext(true);
            }
        });

        declineScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext(false);
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_smart_plug);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_swann_schedule;
    }
}
