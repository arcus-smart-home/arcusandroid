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
package arcus.presentation.hub.wifi.scan

import android.os.Looper
import arcus.cornea.CorneaClientFactory
import arcus.cornea.helpers.chainNonNull
import arcus.cornea.helpers.getProductAddress
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.HubModelProvider
import arcus.cornea.provider.ProductModelProvider
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.ScheduledExecutor
import com.iris.client.IrisClient
import com.iris.client.capability.Hub
import com.iris.client.capability.HubWiFi
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import com.iris.client.model.HubModel
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

class HubWiFiScanPresenterImpl(
    private val client: IrisClient = CorneaClientFactory.getClient(),
    private val hubLoader: () -> ClientFuture<HubModel?> = {
        val hubModel = HubModelProvider.instance().hubModel
        if (hubModel != null) {
            Futures.succeededFuture(hubModel)
        } else {
            HubModelProvider.instance().reload().transformNonNull { it.first() }
        }
    },
    private val scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!),
    private val scanNetworksScheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!),
    private val hubWiFiScanConfig: HubWiFiScanConfig = HubWiFiScanConfig()
) : HubWiFiScanPresenter, KBasePresenter<HubWiFiScanView>() {
    private val logger = LoggerFactory.getLogger(HubWiFiScanPresenter::class.java)
    private var eventListener = Listeners.empty()
    private var hubStateEventListener = Listeners.empty()
    private val networksFound = ConcurrentHashMap<String, HubAvailableWiFiNetwork>(25)
    private val Xs_AND_Ohs = ".*\\\\x00.*".toRegex()

    override fun scanNetworks() {
        cleanUp()

        hubLoader
            .invoke()
            .transformNonNull { hub ->
                if (hub.get(Hub.ATTR_STATE) == Hub.STATE_DOWN) {
                    onMainWithView {
                        onHubDisconnected()
                    }
                    error("Unable to scan for networks.")
                } else {
                    hub
                }
            }
            .transform {
                it as? HubWiFi? ?: error("Hub does not have WiFi capability.")
            }
            .chainNonNull { hubWiFi ->
                onMainWithView {
                    onSearching()
                }

                hubWiFi.wiFiStartScan(hubWiFiScanConfig.scanTimeoutInSeconds)
            }
            .onCompletion { result ->
                scheduledExecutor.executeDelayed(hubWiFiScanConfig.rescanDelayInSeconds, TimeUnit.SECONDS) {
                    scanNetworks()
                }

                if (result.isError && result.error !is IllegalStateException) {
                    onMainWithView {
                        onDoneSearching()
                    }
                    logger.error("Could not scan for networks.", result.error)
                }
            }
    }

    override fun stopScanningNetworks() {
        cleanUp()
        hubLoader
            .invoke()
            .transform {
                it as? HubWiFi ?: error("Hub does not have WiFi capability.")
            }
            .chainNonNull { hubWiFi ->
                hubWiFi.wiFiEndScan()
            }
            .onCompletion { result ->
                onMainWithView {
                    onDoneSearching()
                }

                if (result.isError) {
                    logger.error("Could not stop scanning for networks.", result.error)
                }
            }
    }

    override fun sortNetworksAndSetSelected(
        inputList: List<HubAvailableWiFiNetwork>,
        deviceConnectedTo: String?,
        listSelection: HubAvailableWiFiNetwork?
    ): List<HubAvailableWiFiNetwork> = inputList
        .asSequence()
        .filterNot { wifiNetwork ->
            wifiNetwork.ssid.isBlank()
        }
        .filterNot { wifiNetwork ->
            wifiNetwork.ssid.matches(Xs_AND_Ohs)
        }
        .distinctBy { wifiNetwork ->
            wifiNetwork.ssid
        }
        .map { network ->
            when {
                // Only copy when needed (to to reduce object creation)
                network.ssid == listSelection?.ssid || network.ssid == deviceConnectedTo -> {
                    network.copy(selected = true)
                }
                else -> network
            }
        }
        .sortedWith(hubWiFiScanConfig.networkSorter)
        .toList()
        .also { networks ->
            if (networks.isNotEmpty() && networks.first().selected) {
                onMainWithView {
                    selectedNetwork = networks.first()
                }
            }
        }
        .let { networkList ->
            val otherNetworkSelected = listSelection?.isOtherNetwork == true
            val otherNetwork = HubAvailableWiFiNetwork.OTHER_NETWORK.copy(selected = otherNetworkSelected)
            if (otherNetworkSelected) {
                onMainWithView {
                    selectedNetwork = otherNetwork
                }
            }

            networkList + otherNetwork
        }

    override fun setView(view: HubWiFiScanView) {
        super.setView(view)
        view.showCantFindDevice = false

        scanNetworksScheduledExecutor
            .executeDelayed(
                hubWiFiScanConfig.secondsBeforeShowCantFindNetwork,
                TimeUnit.SECONDS
            ) {
                onMainWithView {
                    showCantFindDevice = true
                }
            }

        eventListener = client.addMessageListener { message ->
            val event = message.event
            if (event != null && HubWiFi.WiFiScanResultsEvent.NAME == event.type) {
                HubWiFi
                    .WiFiScanResultsEvent(event)
                    .scanResults
                    .forEach {
                        val network = HubAvailableWiFiNetwork.fromResponse(it)
                        networksFound[network.ssid] = network
                    }

                val foundNetworks = networksFound.values.toList()
                onMainWithView {
                    onDoneSearching()
                    onNetworksFound(foundNetworks)
                }
            }
        }

        hubLoader
            .invoke()
            .onSuccessMain { hubModel ->
                hubModel?.run {
                    hubStateEventListener = addPropertyChangeListener { pce ->
                        if (pce.propertyName == Hub.ATTR_STATE) {
                            onMainWithView {
                                if (pce.newValue == Hub.STATE_DOWN) {
                                    onHubDisconnected()
                                } else {
                                    onHubConnected()
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun getHubShortName() {
        hubLoader
            .invoke()
            .chainNonNull {
                ProductModelProvider
                    .instance()
                    .getModel(it.getProductAddress())
                    .load()
            }
            .onSuccess { product ->
                onMainWithView {
                    onHubShortNameFound(product.shortName ?: "Smart Hub")
                }
            }
            .onFailure { error ->
                onMainWithView {
                    onHubShortNameFound("Smart Hub")
                }
                logger.error("Error getting short name for the hub...", error)
            }
    }

    override fun clearView() {
        super.clearView()
        eventListener = Listeners.clear(eventListener)
        hubStateEventListener = Listeners.clear(hubStateEventListener)
        scanNetworksScheduledExecutor.clearExecutor()
        networksFound.clear()
        cleanUp()
    }

    private fun cleanUp() {
        scheduledExecutor.clearExecutor()
    }
}
