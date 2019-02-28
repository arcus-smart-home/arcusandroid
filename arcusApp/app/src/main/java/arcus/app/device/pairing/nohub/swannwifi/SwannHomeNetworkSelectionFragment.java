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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.google.common.base.Predicate;
import arcus.cornea.device.camera.model.AvailableNetworkModel;
import arcus.cornea.device.camera.model.WiFiSecurityType;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.LocationUtils;
import arcus.app.common.validation.NotEmptyValidator;
import arcus.app.common.validation.SecureWifiValidator;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.wifi.PhoneWifiHelper;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannWifiPairingSequenceController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;

import java.util.concurrent.atomic.AtomicBoolean;


public class SwannHomeNetworkSelectionFragment extends SequencedFragment<SwannWifiPairingSequenceController> implements PhoneWifiHelper.WifiSsidSelectionListener {

    private final static String STEP_NUMBER_ARG = "step-number";

    private WiFiSecurityType selectedSecurityType;
    private AtomicBoolean userStartedEditing = new AtomicBoolean(false);

    // When not null, the user has selected an SSID from the picker (or entered manually)
    private AvailableNetworkModel userSelectedNetwork;

    private Version1EditText wifiSelection;
    private Version1EditText wifiPassword;
    private RelativeLayout wifiSelectionClickRegion;
    private RelativeLayout showPasswordClickRegion;
    private LinearLayout wifiPasswordRegion;
    private Version1TextView showPasswordLabel;
    private Version1Button nextButton;
    private LinearLayout wifiEnabledElements;
    private LinearLayout wifiDisabledElements;
    private ToggleButton wifiToggleButton;
    private ImageView pairingStepIcon;

    private Predicate<AvailableNetworkModel> homeNetworkFilter = new Predicate<AvailableNetworkModel>() {
        @Override
        public boolean apply(AvailableNetworkModel input) {
            return !SwannAccessPointSelectionFragment.isSwannSmartPlugAp(input.getSSID());
        }
    };

    public static SwannHomeNetworkSelectionFragment newInstance(int stepNumber) {
        SwannHomeNetworkSelectionFragment instance = new SwannHomeNetworkSelectionFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(STEP_NUMBER_ARG, stepNumber);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        wifiSelection = (Version1EditText) view.findViewById(R.id.wifi_network_selection);
        wifiPassword = (Version1EditText) view.findViewById(R.id.wifi_password);
        wifiPasswordRegion = (LinearLayout) view.findViewById(R.id.wifi_password_region);
        wifiSelectionClickRegion = (RelativeLayout) view.findViewById(R.id.wifi_network_selection_click_region);
        showPasswordClickRegion = (RelativeLayout) view.findViewById(R.id.show_password_click_region);
        showPasswordLabel = (Version1TextView) view.findViewById(R.id.show_password);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);
        wifiDisabledElements = (LinearLayout) view.findViewById(R.id.wifi_disabled_elements);
        wifiEnabledElements = (LinearLayout) view.findViewById(R.id.wifi_enabled_elements);
        wifiToggleButton = (ToggleButton) view.findViewById(R.id.toggle_button);
        pairingStepIcon = (ImageView) view.findViewById(R.id.pairing_step);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());

        showWifiEnabledElements(PhoneWifiHelper.isWifiEnabled());
        if (PhoneWifiHelper.isWifiEnabled()) {
            populateCurrentWifiSettings();
        }

        pairingStepIcon.setImageResource(getPairingStepIcon());

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (new NotEmptyValidator(getContext(), wifiSelection).isValid() && new SecureWifiValidator(wifiPassword, selectedSecurityType).isValid()) {
                    getController().setHomeNetworkSsid(wifiSelection.getText().toString());
                    getController().setHomeNetworkPassword(wifiPassword.getText().toString());
                    goNext();
                }
            }
        });

        showPasswordClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (wifiPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)) {
                    wifiPassword.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPasswordLabel.setText(getString(R.string.swann_wifi_hide_password));
                } else {
                    wifiPassword.setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                    showPasswordLabel.setText(getString(R.string.swann_wifi_show_password));
                }

                wifiPassword.resetArcusStyle();
            }
        });

        wifiPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                userStartedEditing.set(true);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Nothing to do
            }
        });

        wifiSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userStartedEditing.set(true);
                PhoneWifiHelper.promptToPickSsid(getActivity(), true, homeNetworkFilter, SwannHomeNetworkSelectionFragment.this);
            }
        });

        wifiToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationUtils.requestEnableLocation(getActivity());
                showProgressBar();
                if (wifiToggleButton.isChecked()) {
                    PhoneWifiHelper.setWifiEnabled(true);
                } else {
                    PhoneWifiHelper.setWifiEnabled(false);
                }
            }
        });

        getActivity().registerReceiver(this.WifiStateChangedReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        getActivity().registerReceiver(this.WifiConnectionChangedReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(this.WifiStateChangedReceiver);
        getActivity().unregisterReceiver(this.WifiConnectionChangedReceiver);
    }

    private BroadcastReceiver WifiConnectionChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.isConnected()) {
                    populateCurrentWifiSettings();
                }
            }
        }
    };


    private BroadcastReceiver WifiStateChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            hideProgressBar();

            int extraWifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

            switch (extraWifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                case WifiManager.WIFI_STATE_DISABLING:
                case WifiManager.WIFI_STATE_ENABLING:
                case WifiManager.WIFI_STATE_UNKNOWN:
                    showWifiEnabledElements(false);
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                    showWifiEnabledElements(true);
                    break;
            }
        }
    };

    private void showWifiEnabledElements(boolean wifiEnabled) {
        wifiDisabledElements.setVisibility(wifiEnabled ? View.GONE : View.VISIBLE);
        wifiEnabledElements.setVisibility(wifiEnabled ? View.VISIBLE : View.GONE);
        wifiToggleButton.setChecked(wifiEnabled);
    }

    private void populateCurrentWifiSettings() {

        // User has selected network from picker
        if (userSelectedNetwork != null) {
            wifiSelection.setText(userSelectedNetwork.getSSID());
            selectedSecurityType = userSelectedNetwork.getSecurity();

            // Hide password field for unprotected networks
            boolean acceptsPassword = userSelectedNetwork.getSecurity() == null || userSelectedNetwork.getSecurity() != WiFiSecurityType.NONE;
            wifiPasswordRegion.setVisibility(acceptsPassword ? View.VISIBLE : View.GONE);
            wifiPassword.setError(null);    // Clear any previous errors
        }

        // User not not yet made a selection; populate with phone's current network
        else if (!userStartedEditing.get()) {

            // Attempt to get current WiFi network
            String currentNetworkSsid = PhoneWifiHelper.getCurrentWifiSsid(getActivity());

            // Populate SSID/password fields with current network selection as available
            if (currentNetworkSsid != null) {
                // Determine current network's security profile and hide/show password field as required.
                WifiConfiguration currentConfiguration = PhoneWifiHelper.getConfiguredNetwork(getContext(), currentNetworkSsid);

                if (currentConfiguration != null) {
                    selectedSecurityType = PhoneWifiHelper.getSecurityType(currentConfiguration);
                    wifiPasswordRegion.setVisibility(selectedSecurityType == WiFiSecurityType.NONE ? View.GONE : View.VISIBLE);
                }
            }
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_smart_plug);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_swann_home_network_selection;
    }

    public int getPairingStepIcon() {
        return AbstractPairingStepFragment.getStepNumberDrawableResId(getArguments().getInt(STEP_NUMBER_ARG));
    }

    /**
     * Invoked to indicate the user has chosen an SSID from the picker, or entered an SSID value
     * manually.
     *
     * WARNING: This method may fire before or after the fragment lifecycle methods (i.e.,
     * {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}, or {@link #onResume()}).
     *
     * @param selectedNetwork Information about the selected network, or null if the user cancelled
     *                        selection
     */
    @Override
    public void onWifiSsidSelected(AvailableNetworkModel selectedNetwork) {
        this.userSelectedNetwork = selectedNetwork;
        populateCurrentWifiSettings();
    }
}
