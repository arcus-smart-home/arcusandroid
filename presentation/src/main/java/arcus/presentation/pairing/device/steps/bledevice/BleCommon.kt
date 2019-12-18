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
package arcus.presentation.pairing.device.steps.bledevice

import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.ble.BleConnector

enum class BleConnectionStatus {
    BLE_DISCONNECTED,
    BLE_CONNECTED,
    BLE_CONNECT_FAILURE
    ;
}

enum class WiFiConnectionStatus {
    WIFI_CONNECTED,
    WIFI_FAILED_TO_CONNECT,
    WIFI_ERROR_IN_WRITING
    ;
}

sealed class IpcdConnectionStatus
object IpcdConnected : IpcdConnectionStatus()
object IpcdAlreadyClaimed : IpcdConnectionStatus()
object IpcdNotFound : IpcdConnectionStatus()
object IpcdTimedOut : IpcdConnectionStatus()
data class IpcdAlreadyAdded(val deviceName: String) : IpcdConnectionStatus()

interface BleConnectedView {
    /**
     * Notifies of BLE status changes such as connected, disconnected, etc.
     */
    fun onBleStatusChange(status: BleConnectionStatus)
}

interface BleConnectedPresenter<V : BleConnectedView, T> : BasePresenterContract<V> {
    /**
     * Sets the BLE Connector to use.
     */
    fun setBleConnector(connector: BleConnector<T>)

    /**
     * Sets the appropriate interaction callback on the connector.
     */
    fun initializeBleInteractionCallback()

    /**
     * Tries to reconnect to the BLE device usig [with] (I.e. in Android, Context)
     */
    fun reconnectBle(with: T?)
}
