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
package arcus.presentation.common.wifi

import com.iris.client.capability.HubWiFi

enum class DeviceWiFiNetworkSecurity(
    val friendlyName: String,
    val platformName: String = "",
    val blePlatformName: String = "",
    private val alternateRepresentations: List<String> = emptyList()
) {
    NONE(
        "None",
        HubWiFi.WIFISECURITY_NONE,
        "None",
        listOf(
            "None",
            "NONE",
            "OPEN"
        )
    ),
    WEP(
        "WEP",
        HubWiFi.WIFISECURITY_WEP,
        "WEP"
    ),
    WPA_PSK(
        "WPA PSK",
        HubWiFi.WIFISECURITY_WPA_PSK,
        "WPA-PSK",
        listOf(
            "WPA-PSK",
            "WPA PSK"
        )
    ),
    WPA2_PSK(
        "WPA2 PSK",
        HubWiFi.WIFISECURITY_WPA2_PSK,
        "WPA2-PSK",
        listOf(
            "WPA2-PSK",
            "WPA2 PSK"
        )
    ),
    WPA_ENTERPRISE(
        "WPA Enterprise",
        HubWiFi.WIFISECURITY_WPA_ENTERPRISE,
        alternateRepresentations = listOf(
            "WPA-ENTERPRISE",
            "WPA Enterprise"
        )
    ),
    WPA2_ENTERPRISE(
        "WPA2 Enterprise",
        HubWiFi.WIFISECURITY_WPA2_ENTERPRISE,
        alternateRepresentations = listOf(
            "WPA2-ENTERPRISE",
            "WPA2 Enterprise"
        )
    ),
    UNKNOWN(
        "UNKNOWN"
    )
    ;

    companion object {
        private val NOT_A_VALID_CHAR = "[^A-Za-z0-9_\\- ]".toRegex()

        /**
         * Convert a possibly null string into the Security representation we believe it belongs to
         */
        @JvmStatic
        fun fromStringRepresentation(value: String?): DeviceWiFiNetworkSecurity {
            if (value == null) {
                return UNKNOWN
            }

            val trimmedName = value.replace(NOT_A_VALID_CHAR, "")
            return values().firstOrNull {
                it.name == trimmedName || it.alternateRepresentations.contains(trimmedName)
            } ?: UNKNOWN
        }

        /**
         * Gets a list of choices we can allow the UI to see.
         */
        @JvmStatic
        fun getAvailableChoices() = listOf(
            NONE,
            WEP,
            WPA_PSK,
            WPA2_PSK,
            WPA_ENTERPRISE,
            WPA2_ENTERPRISE
        )

        /**
         * Gets a list of choices we can allow the UI to see for BLE devices.
         */
        @JvmStatic
        fun getAvailableBleChoices() = listOf(
            NONE,
            WEP,
            WPA_PSK,
            WPA2_PSK
        )
    }
}
