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
package arcus.app.device.pairing.nohub.alexa;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.nohub.model.NoHubDevice;
import arcus.app.device.pairing.nohub.alexa.controller.VoiceAssistantNoPairingSequenceController;


public class VoiceSkillsFragment extends TutorialBannerFragment<VoiceAssistantNoPairingSequenceController> {

    private final static String NO_PAIR_DEVICE = "NO_PAIR_DEVICE";

    private Version1Button instructionsButton;
    private Version1Button doneButton;
    private Version1TextView title;
    private Version1TextView subtext;

    public static VoiceSkillsFragment newInstance (NoHubDevice noPairDevice) {
        VoiceSkillsFragment instance = new VoiceSkillsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(NO_PAIR_DEVICE, noPairDevice);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public String getProductId() {
        return getNoPairDevice().getProductId();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        title = (Version1TextView) view.findViewById(R.id.title);
        subtext = (Version1TextView) view.findViewById(R.id.subtext);
        instructionsButton = (Version1Button) view.findViewById(R.id.instructions);
        doneButton = (Version1Button) view.findViewById(R.id.alexa_done);

        if (getNoPairDevice().isGoogleHomeDevice()) {
            title.setText(R.string.google_home_skills);
            subtext.setText(R.string.google_home_skills_desc);
            instructionsButton.setText(R.string.google_home_setup_instructions);
        }

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        instructionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getNoPairDevice().isGoogleHomeDevice()) {
                    getController().goGoogleInstructions(getActivity());
                } else {
                    getController().goAlexaInstructions(getActivity());
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().endSequence(getActivity(), true);
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.amazon_alexa);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_voice_skills;
    }

    private NoHubDevice getNoPairDevice () {
        return (NoHubDevice) getArguments().getSerializable(NO_PAIR_DEVICE);
    }
}
