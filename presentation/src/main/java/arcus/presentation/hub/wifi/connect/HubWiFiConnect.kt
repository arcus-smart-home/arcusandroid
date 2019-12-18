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
package arcus.presentation.hub.wifi.connect

import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.common.wifi.DeviceWiFiNetworkSecurity

class HubWiFiCredentials(
    val ssid: String,
    _pass: String,
    val security: DeviceWiFiNetworkSecurity
) {
    val pass = _pass.toCharArray()

    fun clear() {
        pass.fill('\u0000')
    }

    fun getErrorState(): HubWiFiConnectState = when (security) {
        DeviceWiFiNetworkSecurity.WEP,
        DeviceWiFiNetworkSecurity.WPA_PSK,
        DeviceWiFiNetworkSecurity.WPA2_PSK,
        DeviceWiFiNetworkSecurity.WPA_ENTERPRISE,
        DeviceWiFiNetworkSecurity.WPA2_ENTERPRISE -> {
            val ssidPresent = ssid.isNotBlank()
            val passPresent = pass.isNotEmpty() // Min length etc?

            if (ssidPresent && passPresent) { // both ok
                HubWiFiConnectState.CONNECTING
            } else if (!ssidPresent && !passPresent) { // both bad
                HubWiFiConnectState.MISSING_OR_INVALID_SSID_AND_PASS
            } else if (!ssidPresent) { // ssid bad
                HubWiFiConnectState.MISSING_OR_INVALID_SSID
            } else { // pass bad
                HubWiFiConnectState.MISSING_OR_INVALID_PASS
            }
        }

        DeviceWiFiNetworkSecurity.UNKNOWN,
        DeviceWiFiNetworkSecurity.NONE -> {
            if (ssid.isBlank()) {
                HubWiFiConnectState.MISSING_OR_INVALID_SSID
            } else {
                HubWiFiConnectState.CONNECTING
            }
        }
    }

    override fun toString(): String {
        return "HubWiFiCredentials(ssid='$ssid', security=$security, pass=${"*".repeat(pass.size + 1)})"
    }
}

enum class HubWiFiConnectState {
    INITIAL,
    CONNECTING,
    CONNECTED,
    CONNECTION_FAILED,
    HUB_OFFLINE,
    MISSING_OR_INVALID_SSID,
    MISSING_OR_INVALID_PASS,
    MISSING_OR_INVALID_SSID_AND_PASS
    ;
}

interface HubWiFiConnectView {
    var hubWiFiConnectState: HubWiFiConnectState
}

interface HubWiFiConnectPresenter : BasePresenterContract<HubWiFiConnectView> {
    /**
     * Requests to connect to the wifi network with the [credentials] provided.
     *
     * @param credentials credentials to connect to the network provided
     */
    fun connectToWiFi(credentials: HubWiFiCredentials)

    /**
     * Gets a list of the available wifi security selection choices for the hub
     */
    fun getWiFiSecuritySelectionChoices(): List<DeviceWiFiNetworkSecurity>
}
