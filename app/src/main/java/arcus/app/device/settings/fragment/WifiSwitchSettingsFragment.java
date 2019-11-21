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
package arcus.app.device.settings.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.WifiRemoveToUpdatePopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1TextView;


public class WifiSwitchSettingsFragment extends SequencedFragment {

    private final static String SSID = "SSID";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Version1TextView ssid;
    private LinearLayout ssidClickRegion;

    public static WifiSwitchSettingsFragment newInstance(String ssid, String deviceAddress) {
        WifiSwitchSettingsFragment instance = new WifiSwitchSettingsFragment();
        Bundle arguments = new Bundle();
        arguments.putString(SSID, ssid);
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ssid = (Version1TextView) view.findViewById(R.id.ssid);
        ssidClickRegion = (LinearLayout) view.findViewById(R.id.network_click_region);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
        ssid.setText(getSsid());

        ssidClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFloatingFragment(WifiRemoveToUpdatePopup.newInstance(getDeviceAddress()), WifiRemoveToUpdatePopup.class.getSimpleName(), true);
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_wifi_settings_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_wifi_switch_settings;
    }

    public String getSsid() {
        return getArguments().getString(SSID, "");
    }
    public String getDeviceAddress() { return getArguments().getString(DEVICE_ADDRESS); }
}
