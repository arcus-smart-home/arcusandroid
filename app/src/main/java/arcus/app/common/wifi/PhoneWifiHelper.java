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
package arcus.app.common.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import arcus.cornea.device.camera.model.AvailableNetworkModel;
import arcus.cornea.device.camera.model.WiFiSecurityType;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.WiFiPickerPopup;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PhoneWifiHelper {

    public interface WifiScanCompleteListener {
        void onWifiScanComplete (List<AvailableNetworkModel> results);
    }

    public interface WifiSsidSelectionListener {
        void onWifiSsidSelected (AvailableNetworkModel selectedNetwork);
    }

    /**
     * Invokes a callback containing all of the currently available WiFi networks.
     *
     * @param context
     * @param callback
     */
    public static void scanForAvailableNetworks(final Context context, final WifiScanCompleteListener callback) {
        final WifiManager wifiManager = getWiFiManager(context);

        BroadcastReceiver mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    new Handler(Looper.getMainLooper()).post(() -> callback.onWifiScanComplete(toAvailableNetworkModels(wifiManager.getScanResults())));
                }

                context.unregisterReceiver(this);
            }
        };

        context.registerReceiver(mWifiScanReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }

    /**
     * Gets WifiInfo record of the currently active WiFi network or null if no network is currently
     * active or if the connection cannot be determined for some reason.
     *
     * @param context
     * @return A WifiInfo object describing the current network or null if no Wifi network is
     * active to describe.
     */
    public static WifiInfo getCurrentWifiNetwork (Context context) {
        final WifiManager wifiManager = getWiFiManager(context);
        return wifiManager == null || wifiManager.getConnectionInfo() == null ?
                null : wifiManager.getConnectionInfo();
    }


    /**
     * Returns the unquoted name (SSID) of the currently active WiFi network or null if no network
     * is currently active or if the connection cannot be determined.
     *
     * @param context
     * @return
     */
    public static String getCurrentWifiSsid (Context context) {
        WifiInfo wifiInfo = getCurrentWifiNetwork(context);

        if (wifiInfo != null && !StringUtils.isEmpty(wifiInfo.getSSID())) {
            return getUnquotedSsid(wifiInfo.getSSID());
        }

        return null;
    }

    /**
     * Attempts to determine the security protocol in use for a given WiFiConfiguration.
     * @param config
     * @return
     */
    @NonNull
    public static WiFiSecurityType getSecurityType(@NonNull WifiConfiguration config) {
            if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
                return WiFiSecurityType.WPA_PSK;
            }
            if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
                return WiFiSecurityType.WPA_ENTERPRISE;
            }
            return (config.wepKeys[0] != null) ? WiFiSecurityType.WEP : WiFiSecurityType.NONE;
    }

    public static String getUnquotedSsid (String ssid) {

        if (ssid == null) {
            return null;
        }

        // All versions of Android are supposed to return the SSID inside quotes; versions before
        // 4.2 incorrectly dropped the quotes.
        // See: http://stackoverflow.com/questions/13563032/jelly-bean-issue-wifimanager-getconnectioninfo-getssid-extra
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            return ssid.substring(1, ssid.length()-1);
        } else {
            return ssid;
        }
    }

    public static WifiConfiguration getConfiguredNetwork (Context context, String ssid) {
        final WifiManager wifiManager = getWiFiManager(context);

        for (WifiConfiguration i : wifiManager.getConfiguredNetworks()) {
            String thisSsid = getUnquotedSsid(i.SSID);
            if (!StringUtils.isEmpty(thisSsid) && thisSsid.equals(getUnquotedSsid(ssid))) {
                return i;
            }
        }

        return null;
    }

    public static boolean disconnectFromWifi (Context context) {
        final WifiManager wifiManager = getWiFiManager(context);
        return wifiManager.disconnect();
    }

    public static boolean removeConfiguredNetwork (Context context, int networkId) {
        final WifiManager wifiManager = getWiFiManager(context);
        return wifiManager.removeNetwork(networkId);
    }

    public static boolean isNetworkConfigured (Context context, String ssid) {
        return getConfiguredNetwork(context, ssid) != null;
    }

    public static boolean isWifiEnabled () {
        final WifiManager wifiManager = getWiFiManager(ArcusApplication.getArcusApplication());
        return wifiManager.isWifiEnabled();
    }

    public static boolean setWifiEnabled (boolean enabled) {
        final WifiManager wifiManager = getWiFiManager(ArcusApplication.getArcusApplication());
        return wifiManager.setWifiEnabled(enabled);
    }

    public static WifiConfiguration getConfigurationForNetwork (@NonNull Context context, @NonNull String ssid, @Nullable String password) {

        WifiConfiguration configuration;

        // If the phone "knows" this network, use the known configuration
        // TODO: WARN: This ignores the user-provided password. Hmm.... Bug or a feature???
        if (isNetworkConfigured(context, ssid)) {
            configuration = getConfiguredNetwork(context, ssid);
        }

        // ... otherwise, build a configuration for it
        else {
            WifiConfigurationBuilder builder = new WifiConfigurationBuilder(ssid);

            if (!StringUtils.isEmpty(password)) {
                builder.forProtectedNetwork(password);
            } else {
                builder.forOpenNetwork();
            }

            configuration = builder.build();
        }

        return configuration;
    }

    /**
     * This returns only 2.4GHz networks!!
     */
    public static void promptToPickSsid(@NonNull final Activity context, boolean allowManualSsidEntry, final Predicate<AvailableNetworkModel> filter, @NonNull final WifiSsidSelectionListener listener) {
        final ArrayList<AvailableNetworkModel> networks = new ArrayList<>();
        final WiFiPickerPopup wpp = WiFiPickerPopup.newInstance(networks, true, allowManualSsidEntry);
        wpp.setTitleStringResId(R.string.swann_wifi_choose_network);

        wpp.setCallback(new WiFiPickerPopup.Callback() {
            @Override
            public void selectedItem(AvailableNetworkModel model) {
                // Close pop-up window...
                BackstackManager.getInstance().navigateBack();
                listener.onWifiSsidSelected(model);
            }

            @Override
            public void onEnterSsid() {
                // Close pop-up window...
                BackstackManager.getInstance().navigateBack();

                // Ick... now show manual SSID entry sequence
                new ManualSsidEntrySequenceController().startSequence(context, null, new ManualSsidEntrySequenceController.ManualSsidEntryListener() {
                    @Override
                    public void onManualSsidEntry(String ssid) {
                        AvailableNetworkModel availableNetworkModel = new AvailableNetworkModel();
                        availableNetworkModel.setSsid(ssid);
                        availableNetworkModel.setSecurity(null);
                        listener.onWifiSsidSelected(availableNetworkModel);
                    }
                });
            }

            @Override
            public void close() {
                // Close pop-up window
                BackstackManager.getInstance().navigateBack();
                listener.onWifiSsidSelected(null);
            }
        });

        PhoneWifiHelper.scanForAvailableNetworks(context, new WifiScanCompleteListener() {
            @Override
            public void onWifiScanComplete(List<AvailableNetworkModel> results) {
                ArrayList<AvailableNetworkModel> visibleNetworks = new ArrayList<AvailableNetworkModel>();
                List<AvailableNetworkModel> filteredResults = get24GhzNetworkModels(results);
                for (AvailableNetworkModel thisNetwork : filteredResults) {
                    if (filter.apply(thisNetwork)) {
                        visibleNetworks.add(thisNetwork);
                    }
                }

                wpp.showWifiNetworks(visibleNetworks);
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(wpp, wpp.getClass().getSimpleName(), true);
    }

    /**
     * Converts a list of Android ScanResult objects to the model {@link AvailableNetworkModel}
     * containing only unique SSIDs.
     *
     * @param scanResults
     * @return
     */
    private static List<AvailableNetworkModel> toAvailableNetworkModels(List<ScanResult> scanResults) {
        ArrayList<AvailableNetworkModel> models = new ArrayList<>();

        for (ScanResult thisResult : getUniqueSsids(scanResults)) {
            AvailableNetworkModel model = new AvailableNetworkModel();

            model.setSsid(thisResult.SSID);
            model.setSignal(WifiManager.calculateSignalLevel(thisResult.level, 100));
            model.setFrequency(thisResult.frequency);

            if (thisResult.capabilities.contains("WEP")) model.setSecurity(WiFiSecurityType.WEP);
            else if (thisResult.capabilities.contains("WPA")) model.setSecurity(WiFiSecurityType.WPA_PSK);
            else if (thisResult.capabilities.contains("WPA2")) model.setSecurity(WiFiSecurityType.WPA2_PSK);
            else model.setSecurity(WiFiSecurityType.NONE);

            models.add(model);
        }

        return models;
    }

    private static List<ScanResult> getUniqueSsids(List<ScanResult> results) {
        Map<String, ScanResult> resultMap = new HashMap<>();

        for (ScanResult thisResult : results) {
            resultMap.put(thisResult.SSID, thisResult);
        }

        return Lists.newArrayList(resultMap.values());
    }

    public static List<AvailableNetworkModel> get24GhzNetworkModels(List<AvailableNetworkModel> networkModels){
        ArrayList<AvailableNetworkModel> models = new ArrayList<>();

        for (AvailableNetworkModel thisModel : networkModels) {
            Integer frequency = thisModel.getfrequency();
            if( frequency > 2400 && frequency < 2500 ) {
                models.add(thisModel);
            }
        }
        return models;
    }

    private static WifiManager getWiFiManager(final Context context) {
        return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
    }
}
