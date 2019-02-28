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

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;

import arcus.app.ArcusApplication;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ChangeWifiNetworkTask extends AsyncTask<Object, Integer, Boolean> {

    private final static int CONNECTION_TIMEOUT_MS = 25 /* seconds */ * 1000; /* ms per seconds */
    private final static int CONNECTION_CHECK_INTERVAL_MS = 250;

    private final Logger logger = LoggerFactory.getLogger(ChangeWifiNetworkTask.class);
    private final ChangeWifiNetworkListener callback;

    public interface ChangeWifiNetworkListener {
        void onWifiChangeComplete (boolean success, String currentSsid);
    }

    public ChangeWifiNetworkTask(Context context, ChangeWifiNetworkListener callback) {
        this.callback = callback;
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        Object networkIdentifier = params[0];
        boolean success = false;
        String toSsid = null;

        if (networkIdentifier instanceof Integer) {
            int networkId = (Integer) networkIdentifier;
            success = setCurrentWifiNetwork(networkId);
        }

        else if (networkIdentifier instanceof String) {
            toSsid = (String) networkIdentifier;
            success = setCurrentWifiNetwork(toSsid);
        }

        else if (networkIdentifier instanceof WifiConfiguration) {
            WifiConfiguration configuration = (WifiConfiguration) networkIdentifier;
            toSsid = configuration.SSID;
            success = setCurrentWifiNetwork(configuration);
        }

        // If we were able to successfully change networks, wait for the OS to fully connect
        if (success) {
            return waitUntilConnected(toSsid);
        }

        // Otherwise, notify caller that we failed
        else {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        String ssid = PhoneWifiHelper.getCurrentWifiSsid(getApplicationContext());
        logger.debug("Wifi network change task completed; success={}. Current network is={}", result, ssid == null ? "(disconnected)" : ssid);

        if (callback != null) {
            callback.onWifiChangeComplete(result, ssid);
        }
    }

    @Override
    protected void onCancelled () {
        String ssid = PhoneWifiHelper.getCurrentWifiSsid(getApplicationContext());
        logger.debug("Wifi network change task cancelled");

        if (callback != null) {
            callback.onWifiChangeComplete(false, ssid);
        }
    }

    /**
     * Attempts to connect to the wifi network identified by networkId. Returns true if the connection
     * attempt was successful; false otherwise.
     *
     * @param networkId
     * @return
     */
    private boolean setCurrentWifiNetwork(int networkId) {
        final WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return false;
        }

        logger.debug("Changing wifi to network id {}, disconnecting from current network; success={}", networkId, wifiManager.disconnect());

        boolean success = wifiManager.enableNetwork(networkId, true);
        logger.debug("Request to connect to network id {}; success={}", networkId, success);

        return success;
    }

    /**
     * Attempts to connect to the previously known / previously connected network identified by
     * SSID. Returns true if the connection attempt was successful, false otherwise.
     *
     * Note that this method only works if the phone has been configured to use this network. If
     * this is a new network, use the {@link #setCurrentWifiNetwork(WifiConfiguration)}
     * method instead.
     *
     * @param ssid
     * @return
     */
    private boolean setCurrentWifiNetwork(String ssid) {
        logger.debug("Changing wifi to network SSID {}", ssid);

        // Need quotes around SSID in WifiConfiguration
        if (!ssid.startsWith("\"") && !ssid.endsWith("\"")) {
            ssid = "\"".concat(ssid).concat("\"");
        }

        if (PhoneWifiHelper.isNetworkConfigured(getApplicationContext(), ssid)) {
            WifiConfiguration config = PhoneWifiHelper.getConfiguredNetwork(getApplicationContext(), ssid);
            if (config != null) {
                int knownNetworkId = config.networkId;

                logger.debug("Found configured network with SSID {}; network ID {}", ssid, knownNetworkId);
                return setCurrentWifiNetwork(knownNetworkId);
            } else {
                return false;
            }
        }

        else {
            logger.error("No configured network found with SSID [{}]", ssid);
            return false;
        }
    }

    /**
     * Attempts to connect to the network defined by the given {@link WifiConfiguration}. Callers
     * should use the {@link WifiConfigurationBuilder} helper class to create valid
     * {@link WifiConfiguration} objects.
     *
     * @param network
     * @return True if the given network could be connected to; false otherwise
     */
    private boolean setCurrentWifiNetwork(WifiConfiguration network) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return false;
        }

        logger.debug("Changing wifi to network configuration SSID={} with password={}", network.SSID, network.preSharedKey);

        // If phone already knows how to connect to this network, then delete it!
        if (PhoneWifiHelper.isNetworkConfigured(getApplicationContext(), network.SSID)) {
            logger.debug("Network configuration already exists; deleting it.");

            if (!wifiManager.removeNetwork(network.networkId)) {
                logger.error("Attempt to delete existing network id={} failed. Ignoring.", network.networkId);
            }
        }

        // Try to add this network definition
        if (!PhoneWifiHelper.isNetworkConfigured(getApplicationContext(), network.SSID) && wifiManager.addNetwork(network) < 0) {
            logger.error("Failed to add WifiConfiguration: {}", network);
            return false;
        }

        // Provided we added the network successfully, try to connect to it
        return setCurrentWifiNetwork(network.SSID);
    }

    /**
     * Waits the for the WIFI network change to take affect and for the OS to be fully connected
     * to the network. Upon successful completion the app will be ready to use the newly selected
     * network.
     *
     * @return True to indicate the connection succeeded; false to indicate a timeout.
     */
    private boolean waitUntilConnected(String toSsid) {

        int timeout = CONNECTION_TIMEOUT_MS / CONNECTION_CHECK_INTERVAL_MS;

        do {

            if (isConnected(toSsid)) {
                try {
                    // Give it a buffer... Have seen this fail (pretty frequently) on reconnect to platform
                    // when we don't pause here for 2-5 seconds.
                    Thread.sleep(3_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }

            try {
                Thread.sleep(CONNECTION_CHECK_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.interrupted();
            }

        } while (--timeout > 0);

        return false;
    }

    /**
     * Determines if the phone is fully connected to a wifi network (i.e., connected, authenticated
     * and has an IP address).
     *
     * @return True if the phone is connected; false otherwise.
     */
    private boolean isConnected (String toSsid) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo network = connectivityManager.getActiveNetworkInfo();

            String currentSsid = PhoneWifiHelper.getCurrentWifiSsid(getApplicationContext());
            String unquotedToSsid = PhoneWifiHelper.getUnquotedSsid(toSsid);

            if (!StringUtils.isEmpty(toSsid) && !StringUtils.isEmpty(currentSsid) && !unquotedToSsid.equals(currentSsid)) {
                logger.warn("Current SSID '{}' does not yet match requested SSID '{}'.", currentSsid, unquotedToSsid);
                return false;
            }

            if (network != null) {
                logger.warn("Waiting for network to become available ({}) and connected ({})", network.isAvailable(), network.isConnected());
                return network.isAvailable() && network.isConnected();
            }
        }

        return false;
    }

    private Context getApplicationContext() {
        return ArcusApplication.getArcusApplication().getApplicationContext();
    }
}
