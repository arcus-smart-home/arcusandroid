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

import arcus.cornea.helpers.onFailureMain
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.HubModelProvider
import com.iris.client.capability.HubConnection
import com.iris.client.capability.HubNetwork
import com.iris.client.capability.HubWiFi
import com.iris.client.event.ClientFuture
import com.iris.client.model.HubModel

class HubPairedSuccessPresenterImpl(
    private val reloadHub: () -> ClientFuture<List<HubModel>> = {
        HubModelProvider.instance().reload()
    },
    private val currentHubModel: () -> HubModel? = {
        HubModelProvider.instance().hubModel
    }
) : HubPairedSuccessPresenter, KBasePresenter<HubPairedSuccessView>() {
    override fun getHubKitInfo() {
        reloadHub
            .invoke()
            .onFailureMain {
                onMainWithView {
                    hubConnectionInfo = HubConnectionInfo.EMPTY
                }
            }
            .onSuccessMain { values ->
                values.firstOrNull()?.let { hub ->
                    updateViewFromHubModel(hub)
                }
            }
    }

    override fun getHubWiFiNetworkName(): String {
        val wifiName = currentHubModel.invoke()?.get(HubWiFi.ATTR_WIFISSID) as String?
        return wifiName.orEmpty()
    }

    private fun updateViewFromHubModel(hubModel: HubModel) {
        onMainWithView {
            val connectionIsUp = HubConnection.STATE_DOWN != hubModel[HubConnection.ATTR_STATE]
            hubConnectionInfo = HubConnectionInfo(
                connectionIsUp,
                hubModel[HubWiFi.ATTR_WIFISSID] as? String?,
                when (hubModel[HubNetwork.ATTR_TYPE]) {
                    HubNetwork.TYPE_ETH -> ConnectionType.ETHERNET
                    HubNetwork.TYPE_WIFI -> ConnectionType.WIFI
                    HubNetwork.TYPE_3G -> ConnectionType.CELLULAR
                    else -> ConnectionType.UNKNOWN
                }
            )
        }
    }
}
