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
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.LocationUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannWifiPairingSequenceController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;




public class SwannAirplaneModeFragment extends SequencedFragment<SwannWifiPairingSequenceController> {

    private static final String PAIRING_STEP_NUMBER = "pairing_step";

    private Version1Button nextButton;
    private Version1Button learnButton;
    private ImageView pairingStep;
    private Version1TextView supportCopy;

    public static SwannAirplaneModeFragment newInstance(int pairingStepNumber) {
        SwannAirplaneModeFragment instance = new SwannAirplaneModeFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(PAIRING_STEP_NUMBER, pairingStepNumber);
        instance.setArguments(arguments);
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        nextButton = (Version1Button) view.findViewById(R.id.next_button);
        learnButton = (Version1Button) view.findViewById(R.id.learn_more_button);
        pairingStep = (ImageView) view.findViewById(R.id.pairing_step);
        supportCopy = (Version1TextView) view.findViewById(R.id.support_copy);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        // Re-enable "No Internet" popup
        ((BaseActivity) getActivity()).setNoNetworkErrorSupressed(false);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Do not show "No Internet" popup on this screen
        ((BaseActivity) getActivity()).setNoNetworkErrorSupressed(true);

        pairingStep.setImageResource(AbstractPairingStepFragment.getStepNumberDrawableResId(getArguments().getInt(PAIRING_STEP_NUMBER)));

        supportCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.callSupport();
            }
        });

        learnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityUtils.launchUrl(GlobalSetting.SWANN_PAIRING_SUPPORT_URI);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationUtils.requestEnableLocation(getActivity());
                goNext();
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_swann_airplane_mode;
    }
}
