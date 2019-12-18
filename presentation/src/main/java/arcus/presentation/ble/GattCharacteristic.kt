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

import java.util.UUID

enum class GattCharacteristic(val uuid: UUID, val canonicalName: String) {
    // Generic Access
    DEVICE_NAME(
        UUID.fromString("00002A00-0000-1000-8000-00805F9B34FB"),
        "Device Name"
    ),
    // WiFi Config Service
    STATUS(
        UUID.fromString("9DAB269A-000A-4C87-805F-BC42474D3C0B"),
        "WiFi Config Status"
    ),
    SCAN_RESULTS(
        UUID.fromString("9DAB269A-0001-4C87-805F-BC42474D3C0B"),
        "WiFi Scan Results"
    ),
    SSID(
        UUID.fromString("9DAB269A-0006-4C87-805F-BC42474D3C0B"),
        "WiFi Config SSID"
    ),
    AUTH(
        UUID.fromString("9DAB269A-0007-4C87-805F-BC42474D3C0B"),
        "WiFi Config Auth"
    ),
    PASS(
        UUID.fromString("9DAB269A-0009-4C87-805F-BC42474D3C0B"),
        "WiFi Config Pass"
    ),
    ENCRYPT(
        UUID.fromString("9DAB269A-0008-4C87-805F-BC42474D3C0B"),
        "WiFi Encrypt"
    ),

    // Device Information Service
    MODEL_NUMBER(
        UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb"),
        "Model Number"
    ),
    SERIAL_NUMBER(
        UUID.fromString("00002a25-0000-1000-8000-00805f9b34fb"),
        "Serial Number"
    ),
    FIRMWARE_REVISION(
        UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb"),
        "Firmware Rev."
    ),
    HARDWARE_REVISION(
        UUID.fromString("00002a27-0000-1000-8000-00805f9b34fb"),
        "Hardware Rev."
    ),
    MANUFACTURER(
        UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb"),
        "Manufacturer"
    ),

    // These appear unused
    WIFI_CFG_SUP_MODES(
        UUID.fromString("9DAB269A-0002-4C87-805F-BC42474D3C0B"),
        "WiFi Supported Modes"
    ),
    WIFI_CFG_SUP_FREQS(
        UUID.fromString("9DAB269A-0003-4C87-805F-BC42474D3C0B"),
        "WiFi Supported Frequencies"
    ),
    WIFI_CFG_MODE(
        UUID.fromString("9DAB269A-0004-4C87-805F-BC42474D3C0B"),
        "WiFi Mode"
    ), // Would want byteArrayOf('G'.toByte()) ??
    WIFI_CFG_FREQ(
        UUID.fromString("9DAB269A-0005-4C87-805F-BC42474D3C0B"),
        "WiFi Frequency"
    ), // Would want "2.4".toByte()

    UNKNOWN(
        UUID(0, 0),
        "Unknown Characteristic"
    )
    ;

    companion object {
        @JvmStatic
        fun fromUuid(uuid: UUID?) = values().firstOrNull { it.uuid == uuid } ?: UNKNOWN
    }
}
