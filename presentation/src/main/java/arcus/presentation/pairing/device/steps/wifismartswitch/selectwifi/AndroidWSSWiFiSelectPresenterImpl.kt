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
package arcus.presentation.pairing.device.steps.wifismartswitch.selectwifi

import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager

class AndroidWSSWiFiSelectPresenterImpl : WSSWiFiSelectPresenter<ScanResult, WifiInfo> {
    override fun parseScanResults(
        scanResults: List<ScanResult>?,
        currentlySelected: WiFiNetwork?,
        currentlyConnectedTo: WifiInfo?,
        otherWifiNetworkName: String
    ): Pair<List<WiFiNetwork>, SelectedNetwork?> {
        val selectedNetwork = (currentlySelected?.name ?: currentlyConnectedTo?.ssid ?: "")
            .replace("\"", "")
        val isOtherNetworkSelected = selectedNetwork == otherWifiNetworkName

        val results = (scanResults ?: emptyList())
            .filter {
                it.frequency in FREQUENCY_RANGE
            }
            .filterNot {
                it.SSID.isNullOrEmpty()
            }
            .filterNot {
                it.SSID.matches(SWANN_AP_FILTER)
            }
            .groupBy {
                it.SSID
            }
            .map {
                it.value.reduce { acc, scanResult ->
                    val firstLevel = WifiManager.calculateSignalLevel(acc.level, RSSI_LEVELS)
                    val secondLevel = WifiManager.calculateSignalLevel(scanResult.level, RSSI_LEVELS)
                    if (firstLevel >= secondLevel) {
                        acc
                    } else {
                        scanResult
                    }
                }
            }
            .map {
                WiFiNetwork(
                    it.SSID,
                    !isOtherNetworkSelected && selectedNetwork == it.SSID,
                    false,
                    it.capabilities.contains(WPA_OR_WEP),
                    SignalStrength.values()[WifiManager.calculateSignalLevel(it.level, RSSI_LEVELS)]
                )
            }
            .sortedWith(
                compareByDescending<WiFiNetwork> {
                    it.isSelected
                }.thenBy(String.CASE_INSENSITIVE_ORDER) {
                    it.name
                }
            ).plus(WiFiNetwork( // Even if this is selected it should aways be at the bottom
                otherWifiNetworkName,
                isOtherNetworkSelected,
                true,
                true,
                SignalStrength.LEVEL_3
            ))

        return results to results.firstOrNull { it.isSelected }
    }

    companion object {
        private const val RSSI_LEVELS = 5
        private val FREQUENCY_RANGE = 2400..2499
        private val WPA_OR_WEP = ".*WPA.*|.*WEP.*".toRegex()
        private val SWANN_AP_FILTER = ".*Smart.*Plug.[a-zA-Z0-9]{4}".toRegex()
    }
}
