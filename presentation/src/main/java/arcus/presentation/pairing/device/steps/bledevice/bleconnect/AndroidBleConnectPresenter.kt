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
package arcus.presentation.pairing.device.steps.bledevice.bleconnect

import android.content.Context
import android.os.Looper
import arcus.cornea.CorneaClientFactory
import arcus.cornea.SessionController
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.ScheduledExecutor
import arcus.presentation.ble.BleConnector
import arcus.presentation.ble.BluetoothInteractionCallbacks
import arcus.presentation.ble.GattCharacteristic
import arcus.presentation.pairing.device.steps.PairingStepInput
import arcus.presentation.pairing.device.steps.bledevice.BleConnectionStatus
import arcus.presentation.pairing.device.steps.bledevice.IpcdAlreadyAdded
import arcus.presentation.pairing.device.steps.bledevice.IpcdAlreadyClaimed
import arcus.presentation.pairing.device.steps.bledevice.IpcdConnected
import arcus.presentation.pairing.device.steps.bledevice.IpcdNotFound
import arcus.presentation.pairing.device.steps.bledevice.IpcdTimedOut
import arcus.presentation.pairing.device.steps.bledevice.WiFiConnectionStatus
import com.iris.client.ClientRequest
import com.iris.client.IrisClient
import com.iris.client.capability.Capability
import com.iris.client.capability.HubWiFi
import com.iris.client.capability.PairingDevice
import com.iris.client.capability.Place
import com.iris.client.capability.WiFi
import com.iris.client.exception.ErrorResponseException
import com.iris.client.service.BridgeService
import java.util.Timer
import java.util.TimerTask
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.slf4j.LoggerFactory

class AndroidBleConnectPresenter(
    private val pairingSubsystemController: PairingSubsystemController = PairingSubsystemControllerImpl,
    private val scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!),
    private val client: IrisClient = CorneaClientFactory.getClient()
) : BleConnectPresenter<Context>, KBasePresenter<BleConnectView>() {
    @Volatile
    private var ipcdRegistrationInProgress = false
    @Volatile
    private var suppressBleDisconnect = false
    @Volatile
    private var hubRegistrationInProgress = false

    private var isForReconnect = false
    private var expectedNetwork = ""

    private var hubRegistrationPollingTimer = Timer("HUB_REG_TIMER")
    private var devicePrefix: String? = null
    private val maxWaitForWiFiConnection = {
        onMainWithView {
            onWiFiStatusChange(WiFiConnectionStatus.WIFI_FAILED_TO_CONNECT)
        }
    }

    @Volatile
    private var isInitialAttempt = false

    private val hubPairTimeoutRunnable = {
        cancelHubRegistration()
        onMainWithView {
            onHubPairTimeout()
        }
    }

    private lateinit var bleConnector: BleConnector<Context>
    private val timeoutRunnable = {
        ipcdRegistrationInProgress = false
        cleanUp()

        onMainWithView {
            onIpcdStatusChange(IpcdTimedOut)
        }
    }
    private var listenerRegistration = Listeners.empty()
    private var messageListener = Listeners.empty()

    override fun setBleConnector(connector: BleConnector<Context>) {
        bleConnector = connector
    }

    override fun setDevicePrefix(prefix: String?) {
        devicePrefix = prefix
    }

    override fun initializeBleInteractionCallback() {
        bleConnector.interactionCallback = object : BluetoothInteractionCallbacks() {
            override fun onConnected() {
                onMainWithView {
                    onBleStatusChange(BleConnectionStatus.BLE_CONNECTED)
                }
            }

            override fun onDisconnected(previouslyConnected: Boolean) {
                if (!ipcdRegistrationInProgress && !hubRegistrationInProgress && !suppressBleDisconnect) {
                    if (previouslyConnected) {
                        onMainWithView {
                            onBleStatusChange(BleConnectionStatus.BLE_DISCONNECTED)
                        }
                    } else {
                        onMainWithView {
                            onBleStatusChange(BleConnectionStatus.BLE_CONNECT_FAILURE)
                        }
                    }
                }
            }

            override fun onReadSuccess(uuid: UUID, value: String) {
                logger.debug("onReadSuccess; Value[$value]")
                when (uuid) {
                    GattCharacteristic.STATUS.uuid -> {
                        // Flip this back so the despite the hub/device starting
                        // with "DISCONNECTED" once we go to switch to something else we start to
                        // listen for the change appropriately again.
                        // Since some devices also use DISCONNECTED to mean it failed to
                        // connect to the new network :)
                        if (isInitialAttempt) {
                            isInitialAttempt = DISCONNECTED_WIFI_CFG_STATUS.equals(value, true)
                        }
                        logger.debug("Initial Attempt Flag Status: -> [$isInitialAttempt].")

                        when {
                            isConnectedWifiConfigStatus(value) -> {
                                bleConnector.stopMonitoringNetworkStatus()
                                scheduledExecutor.clearExecutor()

                                // If we're reconnecting we'll need to (typically) suppress
                                // the ble disconnected messages as well as wait for the SSID
                                // value change to come in.  Since this is generic we'll need
                                // to inspect all the incoming messages for either hub or device
                                // wifi ssid changes, and see if they match what we expect.
                                if (isForReconnect) {
                                    suppressBleDisconnect = true
                                    scheduledExecutor.executeDelayed(MS_MAX_WAIT_FOR_SSID_UPDATE) {
                                        messageListener = Listeners.clear(messageListener)
                                        suppressBleDisconnect = false
                                        onMainWithView {
                                            onWiFiSSIDNotUpdatedError()
                                        }
                                    }

                                    messageListener = client.addMessageListener {
                                        val attributes = it?.event?.attributes ?: emptyMap()
                                        if (attributes[HubWiFi.ATTR_WIFISSID] == expectedNetwork ||
                                            attributes[WiFi.ATTR_SSID] == expectedNetwork
                                        ) {
                                            onMainWithView {
                                                onWiFiSSIDNotUpdatedSuccess(expectedNetwork)
                                            }
                                            messageListener = Listeners.clear(messageListener)
                                        }
                                    }
                                } else {
                                    onMainWithView {
                                        onWiFiStatusChange(WiFiConnectionStatus.WIFI_CONNECTED)
                                    }
                                }
                            }
                            isNotConnectedWifiConfigStatus(value) -> {
                                bleConnector.stopMonitoringNetworkStatus()
                                scheduledExecutor.clearCommand(maxWaitForWiFiConnection)
                                onMainWithView {
                                    onWiFiStatusChange(WiFiConnectionStatus.WIFI_FAILED_TO_CONNECT)
                                }
                            }
                            else -> {
                                logger.warn("Ignoring Status of [$value]")
                            }
                        }
                    }
                    else -> {
                        logger.debug("Ingoring update for [$uuid] -> [$value].")
                    }
                }
            }

            private fun isConnectedWifiConfigStatus(value: String): Boolean {
                return value.equals(CONNECTED_WIFI_CFG_STATUS, true)
            }

            private fun isNotConnectedWifiConfigStatus(value: String): Boolean {
                return value.equals(NO_INTERNET_WIFI_CFG_STATUS, true) ||
                        value.equals(DISCONNECTED_WIFI_CFG_STATUS, true) && !isPlug() && !isInitialAttempt ||
                        value.equals(
                            NO_SERVER,
                            true
                        ) && !isInitialAttempt || // The hub seems to get stuck on this one when we get it...
                        value.equals(BAD_SSID, true) ||
                        value.equals(BAD_PASSWORD, true) ||
                        value.equals(BAD_PASS, true) ||
                        value.equals(FAILED, true)
            }

            private fun isPlug(): Boolean = devicePrefix?.contains("plug", true) ?: false
        }
    }

    override fun reconnectBle(with: Context?) {
        if (!bleConnector.reconnect(with)) {
            onlyIfView {
                it.onBleStatusChange(BleConnectionStatus.BLE_CONNECT_FAILURE)
            }
        }
    }

    override fun updateWiFiCredentials(pass: String, network: String, securityType: String, isReconnect: Boolean) {
        isForReconnect = isReconnect
        expectedNetwork = network

        if (bleConnector.isConnected()) {
            val didWrite = bleConnector.writeWiFiConfiguration(pass, network, securityType)

            if (didWrite) {
                isInitialAttempt = true
                bleConnector.startMonitoringNetworkStatus()
                scheduledExecutor.executeDelayed(MS_MAX_WAIT_FOR_WIFI_CONNECTION, command = maxWaitForWiFiConnection)
            } else {
                onlyIfView {
                    it.onWiFiStatusChange(WiFiConnectionStatus.WIFI_ERROR_IN_WRITING)
                }
            }
        } else {
            onlyIfView {
                it.onBleStatusChange(BleConnectionStatus.BLE_DISCONNECTED)
            }
        }
    }

    override fun cancelIPCDRegistration() {
        ipcdRegistrationInProgress = false
        cleanUp()
    }

    override fun registerIPCD(inputs: List<PairingStepInput>) {
        ipcdRegistrationInProgress = true
        val request = BridgeService.RegisterDeviceRequest()
        request.address = IPCD_ADDRESS
        request.attrs = inputs
            .map {
                it.keyName to it.value
            }
            .plus(IPCD_SN to bleConnector.serialNumber)
            .toMap()

        cleanUp() // One can never be too careful ;)
        listenerRegistration = client
            .addMessageListener {
                val event = it.event

                // Wait for the pairing device to come in - ohhhh yeah. It's like the Koolaid man! ooooooh yeaaaahhh!
                if (event is Capability.AddedEvent &&
                    event.attributes[Capability.ATTR_TYPE] == PairingDevice.NAMESPACE
                ) {
                    logger.debug("Got Added Event -> Guess this is our stop! Posing notification to the view.")
                    cleanUp()
                    pairingSubsystemController.exitPairing()
                    onMainWithView {
                        onIpcdStatusChange(IpcdConnected)
                    }
                }
            }

        // Max overall time we will sit here waiting for something to happen.
        scheduledExecutor.executeDelayed(MN_MAX_TIMEOUT, command = timeoutRunnable)

        // Start the train a moving.
        // Chug a chug a... chug a chug a --- CHOO CHOO!
        doRegisterIPCD(1, request)
    }

    private fun doRegisterIPCD(attemptNumber: Int, request: ClientRequest) {
        client
            .request(request)
            .onSuccess {
                val response = BridgeService.RegisterDeviceResponse(it)
                val isAlreadyAdded = response.alreadyAddedAtPlace
                if (isAlreadyAdded != null && isAlreadyAdded) {
                    cleanUp()
                    // TODO: Get the device name???
                    onMainWithView {
                        onIpcdStatusChange(IpcdAlreadyAdded("Your device"))
                    }
                } else {
                    logger.debug("Device is not already paired at place. Should see new device added soon.")
                }
            }
            .onFailure {
                if (ipcdRegistrationInProgress) {
                    if (it is ErrorResponseException && it.code == REQUEST_INVALID) {
                        cleanUp()
                        onMainWithView {
                            onIpcdStatusChange(IpcdAlreadyClaimed)
                        }
                    } else {
                        val nextAttempt = attemptNumber + 1
                        if (nextAttempt > MAX_ATTEMPTS) {
                            // Oh, woe are we that we can not find the device. So sad :(
                            cleanUp()
                            onMainWithView {
                                onIpcdStatusChange(IpcdNotFound)
                            }
                        } else {
                            // Keep searching - la la la.
                            logger.warn("Received error. Not found yet - will try again!! " +
                                    "[$attemptNumber/$MAX_ATTEMPTS] ${it.message}")
                            scheduledExecutor.executeDelayed(MS_DELAY_BETWEEN_ATTEMPTS) {
                                doRegisterIPCD(nextAttempt, request)
                            }
                        }
                    }
                }
            }
    }

    override fun registerHub() {
        cleanUp()
        // Start hub pairing timeout
        scheduledExecutor.executeDelayed(MN_MAX_TIMEOUT, command = hubPairTimeoutRunnable)

        // Make sure the timer is not running
        hubRegistrationPollingTimer.cancel()
        hubRegistrationPollingTimer.purge()

        hubRegistrationPollingTimer = Timer("HUB_REG_TIMER")
        hubRegistrationPollingTimer.schedule(object : TimerTask() {
            override fun run() {
                if (!hubRegistrationInProgress) {
                    return
                }
                doRegisterHub()
            }
        }, 0, MS_POLLING_INTERVAL)

        hubRegistrationInProgress = true
    }

    private fun doRegisterHub() {
        val hubId = bleConnector.serialNumber

        try {
            SessionController.instance().place?.let { placeModel ->
                placeModel.registerHubV2(hubId)
                    .onSuccess { event ->
                        if (event.state == Place.RegisterHubV2Response.STATE_DOWNLOADING ||
                            event.state == Place.RegisterHubV2Response.STATE_APPLYING ||
                            event.state == Place.RegisterHubV2Response.STATE_REGISTERED
                        ) {
                            cancelHubRegistration()

                            // Update the UI
                            onMainWithView {
                                onHubPairEvent(hubId)
                            }
                        }
                    }
                    .onFailure { throwable ->
                        cancelHubRegistration()
                        var error = ""
                        if (throwable is RuntimeException) run {
                            throwable.message?.let {
                                error = it
                            }
                        } else {
                            val exception = throwable as ErrorResponseException
                            error = exception.code
                            onMainWithView {
                                onHubPairError(error, hubId)
                            }
                        }

                        onMainWithView {
                            cancelHubRegistration()
                            onHubPairError(error, hubId)
                        }
                    }
            }
        } catch (ex: Exception) {
            logger.error(ex.toString())
            cancelHubRegistration()
        }
    }

    override fun cancelHubRegistration() {
        hubRegistrationInProgress = false
        hubRegistrationPollingTimer.cancel()
        hubRegistrationPollingTimer.purge()
        scheduledExecutor.clearExecutor()
    }

    internal fun cleanUp() {
        scheduledExecutor.clearExecutor()
        messageListener = Listeners.clear(messageListener)
        Listeners.clear(listenerRegistration)
        isForReconnect = false
        expectedNetwork = ""
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AndroidBleConnectPresenter::class.java)

        private const val MAX_ATTEMPTS = 30
        private const val MS_DELAY_BETWEEN_ATTEMPTS = 2_000L
        private val MN_MAX_TIMEOUT = TimeUnit.MINUTES.toMillis(10)
        private val MS_POLLING_INTERVAL = TimeUnit.SECONDS.toMillis(2)
        private val MS_MAX_WAIT_FOR_WIFI_CONNECTION = TimeUnit.MINUTES.toMillis(1)
        private val MS_MAX_WAIT_FOR_SSID_UPDATE = TimeUnit.MINUTES.toMillis(2)

        private const val NO_INTERNET_WIFI_CFG_STATUS = "no_internet"
        private const val CONNECTED_WIFI_CFG_STATUS = "connected"
        private const val DISCONNECTED_WIFI_CFG_STATUS = "disconnected"
        private const val NO_SERVER = "no_server"
        private const val BAD_SSID = "bad_ssid"
        private const val BAD_PASSWORD = "bad_password"
        private const val BAD_PASS = "bad_pass"
        private const val FAILED = "failed"

        private const val IPCD_ADDRESS = "BRDG::IPCD"
        private const val IPCD_SN = "IPCD:sn"
        private const val REQUEST_INVALID = "request.invalid" // "already taken"
    }
}
