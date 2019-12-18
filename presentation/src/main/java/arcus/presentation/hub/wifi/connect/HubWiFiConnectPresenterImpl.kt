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
package arcus.presentation.hub.wifi.connect

import android.os.Looper
import arcus.cornea.CorneaClientFactory
import arcus.cornea.helpers.chainNonNull
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.HubModelProvider
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.ScheduledExecutor
import arcus.presentation.common.wifi.DeviceWiFiNetworkSecurity
import com.iris.client.IrisClient
import com.iris.client.capability.Hub
import com.iris.client.capability.HubWiFi
import com.iris.client.event.ClientFuture
import com.iris.client.model.HubModel
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

class HubWiFiConnectPresenterImpl(
    private val hubLoader: () -> ClientFuture<HubModel?> = {
        HubModelProvider.instance().load().transformNonNull { it.first() }
    },
    private val client: IrisClient = CorneaClientFactory.getClient(),
    private val scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!)
) : HubWiFiConnectPresenter, KBasePresenter<HubWiFiConnectView>() {
    private val logger = LoggerFactory.getLogger(HubWiFiConnectPresenter::class.java)
    private var eventListenerReg = Listeners.empty()
    private var hubStateEventListener = Listeners.empty()

    override fun setView(view: HubWiFiConnectView) {
        super.setView(view)
        hubLoader
            .invoke()
            .onSuccessMain { hubModel ->
                hubModel?.run {
                    hubStateEventListener = addPropertyChangeListener { pce ->
                        if (pce.propertyName == Hub.ATTR_STATE) {
                            onMainWithView {
                                hubWiFiConnectState = if (pce.newValue == Hub.STATE_DOWN) {
                                    HubWiFiConnectState.HUB_OFFLINE
                                } else {
                                    HubWiFiConnectState.INITIAL
                                }
                            }
                        }
                    }
                }
            }
    }

    override fun clearView() {
        super.clearView()
        clearExecutor()
        clearEventListener()
        hubStateEventListener = Listeners.empty()
    }

    override fun connectToWiFi(credentials: HubWiFiCredentials) {
        val currentState = credentials.getErrorState()
        setHubWiFiConnectionState(currentState)

        if (currentState == HubWiFiConnectState.CONNECTING) {
            doConnectToWiFi(credentials)
        }
    }

    private fun doConnectToWiFi(credentials: HubWiFiCredentials) {
        setupTimeout()
        setupEventListener()

        hubLoader
            .invoke()
            .transform { hub ->
                hub as? HubWiFi?
            }
            .chainNonNull { hubWiFi ->
                hubWiFi.wiFiConnect(
                    credentials.ssid,
                    null,
                    credentials.security.platformName,
                    String(credentials.pass)
                )
            }
            .onSuccess { response ->
                if (response.status != HubWiFi.WiFiConnectResponse.STATUS_CONNECTING) {
                    setHubWiFiConnectionState(HubWiFiConnectState.CONNECTION_FAILED)
                }
            }
            .onFailure { error ->
                setHubWiFiConnectionState(HubWiFiConnectState.CONNECTION_FAILED)
                logger.error("Could not connect Hub to WiFi.", error)
            }
    }

    private fun clearExecutor() {
        scheduledExecutor.clearExecutor()
    }

    private fun clearEventListener() {
        eventListenerReg = Listeners.clear(eventListenerReg)
    }

    private fun setHubWiFiConnectionState(state: HubWiFiConnectState) {
        clearExecutor()
        clearEventListener()
        onMainWithView {
            hubWiFiConnectState = state
        }
    }

    private fun setupTimeout() {
        scheduledExecutor.executeDelayed(
            MAX_SECONDS_TO_WAIT_FOR_CONNECTION,
            TimeUnit.SECONDS
        ) {
            setHubWiFiConnectionState(HubWiFiConnectState.CONNECTION_FAILED)
        }
    }

    private fun setupEventListener() {
        eventListenerReg = client.addMessageListener { message ->
            val event = message?.event
            if (event?.type == HubWiFi.WiFiConnectResultEvent.NAME) {
                val response = HubWiFi.WiFiConnectResultEvent(event)
                setHubWiFiConnectionState(if (response.status == CONNECT_SUCCESS) {
                    HubWiFiConnectState.CONNECTED
                } else {
                    HubWiFiConnectState.CONNECTION_FAILED
                })
            }
        }
    }

    override fun getWiFiSecuritySelectionChoices() = DeviceWiFiNetworkSecurity.getAvailableChoices()

    companion object {
        private const val MAX_SECONDS_TO_WAIT_FOR_CONNECTION = 45L
        private const val CONNECT_SUCCESS = HubWiFi.WiFiConnectResultEvent.STATUS_OK
    }
}
