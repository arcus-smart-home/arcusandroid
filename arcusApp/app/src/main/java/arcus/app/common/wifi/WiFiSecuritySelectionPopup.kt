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
package arcus.app.common.wifi

import android.os.Bundle
import arcus.app.R
import arcus.app.common.view.ScleraSelectionPopup
import arcus.presentation.common.wifi.DeviceWiFiNetworkSecurity


class WiFiSecuritySelectionPopup : ScleraSelectionPopup<DeviceWiFiNetworkSecurity>() {
    private val choices = DeviceWiFiNetworkSecurity.getAvailableChoices().toTypedArray()

    override fun loadChoices(): Array<DeviceWiFiNetworkSecurity> = choices

    override fun getDisplayName(
        itemIndex: Int,
        item: DeviceWiFiNetworkSecurity
    ): String = choices[itemIndex].friendlyName

    override fun getTitle(): String = getString(R.string.wifi_security_setting_title)

    override fun getInitialSelectionValue(): Int = choices
        .indexOf(
            DeviceWiFiNetworkSecurity.fromStringRepresentation(arguments?.getString(ARG_CURRENT_SELECTION))
        )

    companion object {
        private const val ARG_CURRENT_SELECTION = "ARG_CURRENT_SELECTION"

        @JvmStatic
        fun newInstance(
            currentSelection: String
        ) = WiFiSecuritySelectionPopup().also {
            it.arguments = Bundle().apply {
                putString(ARG_CURRENT_SELECTION, currentSelection)
            }
            it.isCancelable = false
        }
    }
}
