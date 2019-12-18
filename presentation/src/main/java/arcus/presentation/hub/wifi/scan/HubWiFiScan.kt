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
package arcus.presentation.hub.wifi.scan

import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.common.wifi.DeviceWiFiNetworkSecurity
import kotlinx.android.parcel.Parcelize

/**
 * @param scanTimeoutInSeconds The amount of time the device should scan for networks.
 *                             Invariant: scanTimeout > 0
 * @param _rescanDelayInSeconds The amount of time before requesting another scan.
 *                              Invariant: rescanDealy > scanTimeout + 1
 * @param networkSorter How to sort the hubs available network list
 * @param secondsBeforeShowCantFindNetwork How long to wait before prompting "can't find network"
 */
class HubWiFiScanConfig(
    val scanTimeoutInSeconds: Int = 10,
    _rescanDelayInSeconds: Long = 10,
    val networkSorter: Comparator<HubAvailableWiFiNetwork> = compareByDescending<HubAvailableWiFiNetwork> {
        it.selected
    }.thenBy(String.CASE_INSENSITIVE_ORDER) {
        it.ssid
    },
    val secondsBeforeShowCantFindNetwork: Long = 30
) {
    val rescanDelayInSeconds = _rescanDelayInSeconds.coerceAtLeast(scanTimeoutInSeconds.toLong() + 1)
}

@Parcelize
data class HubAvailableWiFiNetwork(
    val ssid: String,
    val security: DeviceWiFiNetworkSecurity,
    val signal: Int,
    val channel: Int,
    val selected: Boolean = false,
    val isOtherNetwork: Boolean = false
) : Parcelable {
    fun isSecure(): Boolean = security != DeviceWiFiNetworkSecurity.NONE &&
            security != DeviceWiFiNetworkSecurity.UNKNOWN

    companion object {
        @JvmField
        val EMPTY = HubAvailableWiFiNetwork("None", DeviceWiFiNetworkSecurity.NONE, 0, 0)

        @JvmField
        val OTHER_NETWORK = HubAvailableWiFiNetwork(
            ssid = "Other Network",
            security = DeviceWiFiNetworkSecurity.UNKNOWN,
            signal = 0,
            channel = 0,
            selected = false,
            isOtherNetwork = true
        )

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun fromResponse(attributes: Map<String, Any>): HubAvailableWiFiNetwork {
            val firstSecurity = (attributes["security"] as? List<String>?)?.firstOrNull()

            val ssid = attributes["ssid"].toString()
            val security = DeviceWiFiNetworkSecurity.fromStringRepresentation(firstSecurity)
            val signal = attributes["signal"] as? Number ?: 0
            val channel = attributes["channel"] as? Number ?: 0

            return HubAvailableWiFiNetwork(ssid, security, signal.toInt(), channel.toInt())
        }
    }
}

interface HubWiFiScanView {
    var selectedNetwork: HubAvailableWiFiNetwork
    var showCantFindDevice: Boolean

    /**
     * The hub's short name...
     */
    fun onHubShortNameFound(name: String)

    /**
     * Called when searching for networks.
     */
    fun onSearching()

    /**
     * Called when done (for now) searching for networks.
     */
    fun onDoneSearching()

    /**
     * Called when networks have been found.
     *
     * @param networks the filtered list of networks found (no duplicates, sorted, etc)
     */
    fun onNetworksFound(networks: List<HubAvailableWiFiNetwork>)

    /**
     * Called when we are notified the hub has been disconnected.
     */
    fun onHubDisconnected()

    /**
     * Called when we are notified the hub has been reconnected.
     */
    fun onHubConnected()
}

interface HubWiFiScanPresenter : BasePresenterContract<HubWiFiScanView> {
    /**
     * Gets the hubs short name from the product catalog.
     */
    fun getHubShortName()

    /**
     * Requests to start scanning for networks.
     */
    fun scanNetworks()

    /**
     * Requests to stop scanning for networks.
     */
    fun stopScanningNetworks()

    /**
     * Sorts the list of networks provided
     *
     * @param inputList the list of networks to sort
     * @param deviceConnectedTo the wifi network the device is connected to (optional)
     * @param listSelection the selection the user has chosen from a list of options (optional)
     */
    fun sortNetworksAndSetSelected(
        inputList: List<HubAvailableWiFiNetwork>,
        deviceConnectedTo: String? = null,
        listSelection: HubAvailableWiFiNetwork? = null
    ): List<HubAvailableWiFiNetwork>
}
