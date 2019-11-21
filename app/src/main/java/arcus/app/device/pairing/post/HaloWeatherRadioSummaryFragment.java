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
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.WeatherRadioSummaryPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.specialty.halo.HaloPlusPairingSequenceController;



public class HaloWeatherRadioSummaryFragment extends SequencedFragment<HaloPlusPairingSequenceController> {

    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    String deviceAddress;
    Version1TextView subTitle;
    View nextButton;

    @NonNull
    public static HaloWeatherRadioSummaryFragment newInstance (String deviceAddress) {
        HaloWeatherRadioSummaryFragment fragment = new HaloWeatherRadioSummaryFragment();

        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        subTitle = (Version1TextView) view.findViewById(R.id.subtitle);
        nextButton = view.findViewById(R.id.next_button);
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        deviceAddress = getArguments().getString(DEVICE_ADDRESS);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(getString(R.string.halo_weather_summary_subtitle));
        builder.append("   ");
        builder.setSpan(new ImageSpan(getActivity(), R.drawable.button_info), builder.length() - 1, builder.length(), 0);
        subTitle.setText(builder);
        subTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFloatingFragment(WeatherRadioSummaryPopup.newInstance(deviceAddress), WeatherRadioSummaryPopup.class.getSimpleName(), true);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_halo_weather_radio_summary;
    }
}
