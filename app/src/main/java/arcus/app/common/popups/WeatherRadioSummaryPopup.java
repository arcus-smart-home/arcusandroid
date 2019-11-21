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
package arcus.app.common.popups;

import android.os.Bundle;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.device.smokeandco.HaloController;
import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;


public class WeatherRadioSummaryPopup extends ArcusFloatingFragment implements HaloController.Callback, HaloController.alertCallback {
    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private Version1TextView codes;
    private HaloController haloController;
    private String deviceAddress;
    private WeatherRadioSummaryPopup popup;

    public static WeatherRadioSummaryPopup newInstance (String deviceAddress) {
        WeatherRadioSummaryPopup fragment = new WeatherRadioSummaryPopup();

        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        showFullScreen(true);

        popup = this;
        deviceAddress = getArguments().getString(DEVICE_ADDRESS);
        haloController = new HaloController(
                DeviceModelProvider.instance().getModel(deviceAddress == null ? "DRIV:dev:" : deviceAddress),
                CorneaClientFactory.getClient(),
                null
        );
        haloController.setCallback(this);
    }

    public void onSuccess(DeviceModel deviceModel) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                haloController.setAlertCallback(popup);
                haloController.getEasCodes();
            }
        });
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override
    public void doContentSection() {
        codes = (Version1TextView) contentView.findViewById(R.id.eascodelist);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_weather_radio_summary;
    }

    @Nullable
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void codesLoaded(final ArrayList<String> easCodes) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                codes.setText(R.string.default_eas_strings);
            }
        });

// TODO: Replacing platform-provided EAS codes with static values for release/1.16
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                String easCodeString = "";
//
//                for (String code : easCodes) {
//                    easCodeString += code + "\n";
//                }
//                codes.setText(easCodeString);
//            }
//        });
    }
}
