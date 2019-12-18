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
package arcus.presentation.pairing.device.steps.bledevice.selectwifi

import arcus.presentation.ble.BleWiFiNetwork
import arcus.presentation.pairing.device.steps.bledevice.BleConnectedPresenter
import arcus.presentation.pairing.device.steps.bledevice.BleConnectedView

interface BleWiFiSelectView : BleConnectedView {
    fun onNetworksFound(networks: List<BleWiFiNetwork>)

    fun onNoNetworksFound()

    fun onUnhandledError()

    fun onScanningForNetworksActive(active: Boolean)

    fun currentNetworkSet(network: BleWiFiNetwork)
}

interface BleWiFiSelectPresenter<T> : BleConnectedPresenter<BleWiFiSelectView, T> {
    fun setCurrentWiFiConnection(currentWifi: String?)

    fun setSelectedNetwork(currentSelection: BleWiFiNetwork? = null)

    fun scanForNetworks(currentSelection: BleWiFiNetwork? = null)

    fun stopScanningForNetworks()
}
