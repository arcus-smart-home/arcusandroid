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
package arcus.presentation.pairing.hub.paired

import arcus.cornea.presenter.BasePresenterContract

enum class ConnectionType {
    WIFI,
    ETHERNET,
    CELLULAR,
    UNKNOWN
    ;
}

class HubConnectionInfo(
    val connectionUp: Boolean,
    val wifiNetworkName: String?,
    val connectionType: ConnectionType
) {
    fun hasWiFiSetup(): Boolean = !wifiNetworkName.isNullOrBlank()

    companion object {
        @JvmField
        val EMPTY = HubConnectionInfo(
            false,
            null,
            ConnectionType.UNKNOWN
        )
    }
}

interface HubPairedSuccessView {
    /**
     * Provides the type of connection and the connection status
     */
    var hubConnectionInfo: HubConnectionInfo
}

interface HubPairedSuccessPresenter : BasePresenterContract<HubPairedSuccessView> {
    /**
     * Get hub connection information
     */
    fun getHubKitInfo()

    /**
     * Gets the hub wifi name for some copy...
     */
    fun getHubWiFiNetworkName(): String
}
