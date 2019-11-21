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
package arcus.app.device.more;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import arcus.cornea.device.hub.HubMVPContract;
import arcus.cornea.device.hub.HubPresenter;
import arcus.cornea.device.hub.HubProxyModel;
import arcus.app.R;

import arcus.app.activities.PermissionsActivity;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.IntentRequestCode;
import arcus.app.common.popups.ScleraPopup;
import arcus.app.pairing.device.steps.bledevice.BlePairingStepsActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class HubConnectivityAndPowerFragment extends BaseFragment implements HubMVPContract.View {

    private DateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
    private TextView powerSupply, onlineSince, internetConnection, wifiNetwork, cellularIMEI, cellularSimID;
    private TextView onlineSinceLabel;
    private TextView wifiButtonDescriptionText;
    private TextView onlineOfflineText;
    private Button connectToWiFiButton;
    private View connectToWiFiContainer;
    private View wifiSignalStrengthContainer;
    private TextView wifiSignalStrengthImageTV;
    private View internetContainer, networkContainer, cellIMEIContainer, cellSimIDContainer;
    private HubMVPContract.Presenter presenter;
    private boolean justSetupWiFi = false;
    private final Runnable resetJustSetupWiFiRunnable = () -> justSetupWiFi = false;

    @NonNull
    public static HubConnectivityAndPowerFragment newInstance(String hubID) {
        return new HubConnectivityAndPowerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        wifiButtonDescriptionText = view.findViewById(R.id.wifi_button_description_text);
        connectToWiFiContainer = view.findViewById(R.id.connect_to_wifi_container);
        connectToWiFiButton = view.findViewById(R.id.connect_to_wifi_button);

        onlineSinceLabel = view.findViewById(R.id.hub_online_since_label);
        onlineSince = (TextView) view.findViewById(R.id.hub_online_since);
        powerSupply = (TextView) view.findViewById(R.id.hub_current_power_supply);
        internetConnection = (TextView) view.findViewById(R.id.hub_current_internet_connection);
        wifiNetwork = (TextView) view.findViewById(R.id.hub_current_network_name);
        cellularIMEI = (TextView) view.findViewById(R.id.hub_cellular_imei);
        cellularSimID = (TextView) view.findViewById(R.id.hub_cellular_sim_card_id);

        internetContainer = view.findViewById(R.id.current_internet_container);
        networkContainer = view.findViewById(R.id.current_network_container);
        wifiSignalStrengthContainer = view.findViewById(R.id.wifi_signal_strength_container);
        wifiSignalStrengthImageTV = view.findViewById(R.id.signal_strength_text_view);
        cellIMEIContainer = view.findViewById(R.id.cellular_imei_container);
        cellSimIDContainer = view.findViewById(R.id.cellular_sim_id_container);
        onlineOfflineText = view.findViewById(R.id.status_online_offline);
    }

    @Override
    public void onResume() {
        super.onResume();
        setTitle();
        if (presenter == null) {
            presenter = new HubPresenter(this);
            presenter.load();
            presenter.refresh();
        } else {
            presenter.refresh();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.clear();
        }

        presenter = null;
        dateFormat = null;
    }

    @Override
    public void show(HubProxyModel hub) {
        if (hub.isOnline()) {
            onlineOfflineText.setText(R.string.online);

            if (hub.isBroadbandConnection() || hub.isWifiConnection()) {
                internetContainer.setVisibility(View.VISIBLE);
            } else {
                hideContainerOrSetText(cellIMEIContainer, cellularIMEI, hub.getImei());
                hideContainerOrSetText(cellSimIDContainer, cellularSimID, hub.getSimID());
            }

            onlineSinceLabel.setText(R.string.hub_connectivity_online_since);
        } else {
            onlineOfflineText.setText(R.string.offline);
            onlineSinceLabel.setText(R.string.hub_connectivity_offine_since);
            internetConnection.setText(R.string.not_connected);
            internetContainer.setVisibility(View.VISIBLE);
            networkContainer.setVisibility(View.GONE);
            wifiSignalStrengthContainer.setVisibility(View.GONE);
        }

        if (hub.isACConnection()) {
            powerSupply.setText(getString(R.string.power_source_ac));
        } else if (hub.isBatteryConnection()) {
            powerSupply.setText(getString(R.string.battery));
        } else {
            powerSupply.setText("");
        }

        // Always set this info, even though we may not always show it
        internetConnection.setText(getString(hub.isBroadbandConnection() ? R.string.hub_connection_ethernet : R.string.hub_connection_wifi));

        String currentWiFi = hub.getWifiNetwork();
        if (currentWiFi != null && currentWiFi.length() > 0) {
            wifiNetwork.setText(currentWiFi);
        } else {
            wifiSignalStrengthContainer.setVisibility(View.GONE);
            wifiNetwork.setText(R.string.not_connected);
        }

        // This will either be the "online since", or "offline since", time of the hub. Either way - set it
        onlineSince.setText(String.format("%s%n%s", dateFormat.format(hub.getLastChangedOrNow()), getDateStringFrom(hub)));

        if (hub.isV3Hub()) {
            if (hub.hasWiFiCredentials()) {
                // Regardless of online/offline we can update via BLE
                connectToWiFiButton.setText(R.string.hub_connectivity_update_wifi);
                networkContainer.setVisibility(View.VISIBLE);

                if (hub.isOnline() && hub.isWifiConnection()) {
                    setWiFiSignalStrenthImage(hub.getWifiSignal());
                    wifiSignalStrengthContainer.setVisibility(View.VISIBLE);
                } else {
                    // Either offline or not using wifi connection...
                    wifiSignalStrengthContainer.setVisibility(View.GONE);
                }
            } else if (hub.isOnline()) {
                // Can only setup wifi information if hub is online, show setup wifi button
                connectToWiFiButton.setText(R.string.hub_connectivity_connect_wifi);
                wifiSignalStrengthContainer.setVisibility(View.GONE); // No creds, so can't possibly be using WiFi
                networkContainer.setVisibility(View.VISIBLE);
            } else {
                // Hub is offline with no wifi credentials - don't show the update/setup wifi button
                networkContainer.setVisibility(View.GONE); // Hub is offline
                wifiSignalStrengthContainer.setVisibility(View.GONE);
            }

            // Setup what the Connect to wifi button does.
            if (hub.isOnline() && hub.isBroadbandConnection()) {
                connectToWiFiButton.setOnClickListener(v -> showHubOnBroadbandCantUpdateOrSetWiFiError()); // Online on Broadband
            } else {
                updateWiFiInformationOnConnectClick();
            }

            setWifiButtonDescriptionText(justSetupWiFi, hub.isWifiConnection());
            connectToWiFiContainer.setVisibility(View.VISIBLE);
        } else {
            // V1 and V2 hubs cannot connect to WiFi
            connectToWiFiContainer.setVisibility(View.GONE);
        }
    }

    private void setWifiButtonDescriptionText(boolean wifiJustSetup, boolean onWiFi) {
        if (wifiJustSetup) {
            // Just after we setup WiFi - show this..
            wifiButtonDescriptionText.setText(R.string.hub_post_pairing_wifi_reconnect);
            wifiButtonDescriptionText.removeCallbacks(resetJustSetupWiFiRunnable);
            wifiButtonDescriptionText.postDelayed(resetJustSetupWiFiRunnable, 500);
        } else {
            if (onWiFi) {
                // On WiFi text
                wifiButtonDescriptionText.setText(R.string.hub_wifi_not_reliable);
            } else {
                // On Ethernet text
                wifiButtonDescriptionText.setText(R.string.hub_connect_to_wifi_suggestion);
            }
        }
    }

    private void setWiFiSignalStrenthImage(@IntRange(from = 0, to = 100) int rssi) {
        int level = WifiManager.calculateSignalLevel(rssi, 5) + 1;
        Context context = getContext();
        if (context != null) {
            String packageName = context.getApplicationContext().getPackageName();
            String drawableName = "wifi_white_" + level + "_24x20";
            int id = getResources().getIdentifier(drawableName,"drawable", packageName);

             wifiSignalStrengthImageTV.setCompoundDrawablesWithIntrinsicBounds(
                    id,
                    0,
                    0,
                    0
            );
        }
    }

    private void updateWiFiInformationOnConnectClick() {
        connectToWiFiButton.setOnClickListener(v -> {
            Activity activity = getActivity();
            startActivityForResult(
                    BlePairingStepsActivity.createIntentForV3HubReconnect(v.getContext()),
                    IntentRequestCode.HUB_WIFI_PAIRING_REQUEST.requestCode
            );

            if (activity != null) {
                activity.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.fade_out);
            }
        });
    }

    private void showHubOnBroadbandCantUpdateOrSetWiFiError() {
        ScleraPopup.newInstance(
                R.string.unplug_ethernet_title,
                R.string.unplug_ethernet_desc,
                R.string.close_text,
                -1,
                true,
                true
        ).show(getFragmentManager());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean requestCodeMatches = requestCode == IntentRequestCode.HUB_WIFI_PAIRING_REQUEST.requestCode;
        boolean successfulResult = resultCode == Activity.RESULT_OK;
        justSetupWiFi = requestCodeMatches && successfulResult;

        if (justSetupWiFi) {
            setWifiButtonDescriptionText(true, false);
            Activity activity = getActivity();
            if (data == null || !(activity instanceof PermissionsActivity)) {
                return; // Can't show snackbar!
            }

            String network = data.getStringExtra(Intent.EXTRA_TEXT);
            justSetupWiFi = data.getBooleanExtra(Intent.EXTRA_RETURN_RESULT, true);
            final String networkName = network == null ? "" : network;
            ((PermissionsActivity) activity).showSnackbar(layout -> ConnectedToWiFiSnackBar.make(layout).setNetworkName(networkName, justSetupWiFi));
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            ScleraPopup.newInstance(
                    R.string.device_is_offline,
                    R.string.device_is_offline_sub_text,
                    R.string.ok,
                    R.string.cancel,
                    true,
                    true
            ).show(getFragmentManager());
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // What to do?
    }

    protected String getDateStringFrom(@NonNull HubProxyModel hub) {
        return String.format(
                getString(R.string.hub_online_for_days_hours_minutes),
                hub.getOnlineDays(),
                hub.getOnlineHours(),
                hub.getOnlineMinutes()
        );
    }

    protected void hideContainerOrSetText(@NonNull View container, @NonNull TextView textView, @Nullable String text) {
        container.setVisibility(text != null ? View.VISIBLE : View.GONE);
        textView.setText(text);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.connectivity_and_power);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_hub_connectivity_and_power;
    }
}
