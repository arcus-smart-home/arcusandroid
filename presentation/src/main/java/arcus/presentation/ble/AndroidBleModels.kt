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
import com.google.gson.annotations.SerializedName

data class BleWiFiNetwork(
    val name: String,
    val security: String,
    val rssi: Int,
    val isOtherNetwork: Boolean = false,
    var isSelected: Boolean = false
) {
    fun isSecure() = security != "None" && !security.contains("open", true)
}

data class BleDevice(
    val bluetoothDevice: BluetoothDevice,
    var isConnected: Boolean = false
) {
    val address: String
        get() = bluetoothDevice.address ?: "00:00:00:00:00:00"

    val name: String
        get() = bluetoothDevice.name ?: "Unknown"

    val type: Int
        get() = bluetoothDevice.type
}

data class BLEScanResults(
    @SerializedName("scanresults")
    var networks: List<BleNetworkScanResult> = emptyList(),

    @SerializedName("more")
    var hasMore: String = "false"
)

data class BleNetworkScanResult(
    @SerializedName("ssid")
    var ssid: String = "No SSID Found",

    @SerializedName("security")
    var security: String = "None",

    @SerializedName("channel")
    var channel: Int = 1,

    @SerializedName("signal")
    var signal: Double = 0.0
) {
    fun signalAsInt(): Int = signal.toInt()
}
