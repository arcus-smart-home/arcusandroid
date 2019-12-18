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
package arcus.presentation.ble

import android.bluetooth.BluetoothDevice
import java.util.concurrent.TimeUnit

interface BleConnector<in T> {
    /**
     * The callback to notify when something interesting happens during the bluetooth conection
     */
    var interactionCallback: BluetoothInteractionCallbacks?

    /**
     * The serial number of the device which is read from [GattCharacteristic.MANUFACTURER]
     */
    var serialNumber: String

    /**
     * Connect to the device using the specified object [with]
     *
     * @param with the object to use to help connect the [device]
     * @param device the device to connect to
     * @param autoConnect if the gatt should auto connect if it becomes disconnected, not recommended to use anything but 'false'
     */
    fun connect(with: T?, device: BluetoothDevice, autoConnect: Boolean = false)

    /**
     * Disconnect from, and close the connection to, the currently connected device (if any).
     */
    fun disconnectAndClose()

    /**
     * Checks to see if we have an active connection to a device.
     */
    fun isConnected(): Boolean

    /**
     * Gets the currently connected device from the Gatt object.
     *
     * @return the ble device if connected - null if not connected
     */
    fun getConnectedDevice(): BluetoothDevice?

    /**
     * Attempts to reconnect to the last device connected to.
     *
     * If there's no device we were previously connected to, or if the device was null when
     * we tried to caputre a reference to it - we won't be able to attempt to connect to anything.
     *
     * @return true if an attempt will be made, flase if no attempt will be made
     */
    fun reconnect(with: T?, autoConnect: Boolean = false): Boolean

    /**
     * Scans for wifi networks using the [GattService.WIFI_CONFIG] services characteristic:
     * [GattCharacteristic.SCAN_RESULTS]
     *
     * @return true if was able to request a scan, false if not.
     */
    fun scanForWiFiNetworks(): Boolean

    /**
     * Writes the wifi information to the device.
     *
     * @param pass the password for the [network]
     * @param network the network SSID to connect to
     * @param securityType the security type of the network that is being connected to
     */
    fun writeWiFiConfiguration(pass: String, network: String, securityType: String): Boolean

    /**
     * Starts monitoring network connection status.
     *
     * @param delayBetweenReads the delay between reads
     * @param unit the time unit the value is given in
     *
     * @return true if was able to start the monitor, false if not
     */
    fun startMonitoringNetworkStatus(delayBetweenReads: Long = 2, unit: TimeUnit = TimeUnit.SECONDS): Boolean

    /**
     * Stops monitoring network connection status.
     */
    fun stopMonitoringNetworkStatus()
}
