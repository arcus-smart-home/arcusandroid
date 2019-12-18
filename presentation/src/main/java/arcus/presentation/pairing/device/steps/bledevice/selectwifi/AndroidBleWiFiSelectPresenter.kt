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
package arcus.presentation.pairing.device.steps.bledevice.selectwifi

import android.content.Context
import android.os.Looper
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.ScheduledExecutor
import arcus.presentation.ble.BLEScanResults
import arcus.presentation.ble.BleConnector
import arcus.presentation.ble.BleNetworkScanResult
import arcus.presentation.ble.BleWiFiNetwork
import arcus.presentation.ble.BluetoothInteractionCallbacks
import arcus.presentation.ble.GattCharacteristic
import arcus.presentation.pairing.device.steps.bledevice.BleConnectionStatus
import com.google.gson.Gson
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
import org.slf4j.LoggerFactory

class AndroidBleWiFiSelectPresenter(
    private val executor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!)
) :
    BleWiFiSelectPresenter<Context>,
    KBasePresenter<BleWiFiSelectView>() {
    private var bluetoothConnector by Delegates.notNull<BleConnector<Context>>()
    private val gson = Gson()
    private var networks = mutableListOf<BleWiFiNetwork>()
    private var currentWiFiConnectionName: String? = null
    private var selectedWiFiNetwork: BleWiFiNetwork? = null
    private val interactionCallback = object : BluetoothInteractionCallbacks() {
        override fun onConnected() {
            onMainWithView {
                onBleStatusChange(BleConnectionStatus.BLE_CONNECTED)
            }
        }

        override fun onDisconnected(previouslyConnected: Boolean) {
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

        override fun onReadSuccess(uuid: UUID, value: String) {
            when (uuid) {
                GattCharacteristic.SCAN_RESULTS.uuid -> {
                    val scanResults = parseScanResultsFrom(value)
                    addAllNetworksToCurrent(scanResults.networks)

                    if ("true".equals(scanResults.hasMore, true)) {
                        bluetoothConnector.scanForWiFiNetworks()
                    } else {
                        addSelectedWiFiIfNotInList(selectedWiFiNetwork)
                        val networksCopy = filterNetworks(networks.toList())
                        networks.clear()

                        onMainWithView {
                            if (networksCopy.isEmpty()) {
                                onNoNetworksFound()
                            } else {
                                selectedWiFiNetwork?.let {
                                    currentNetworkSet(it)
                                }
                                onNetworksFound(networksCopy)
                            }

                            onScanningForNetworksActive(false)
                        }

                        // Schedule another scan after a short intermission.
                        executor.executeDelayed(LOOP_INTERVAL_TIME) {
                            doScanForNetworks()
                        }
                    }
                }
                else -> logger.debug("Ignoring read update [$uuid] -> [$value]")
            }
        }
    }

    internal fun parseScanResultsFrom(value: String): BLEScanResults {
        return try {
            val realString = if (NA_TEXT.equals(value, true)) {
                // Le sigh. BLE Camera does this between reads :(
                SCAN_MORE_JSON
            } else {
                value
            }

            gson.fromJson(realString, BLEScanResults::class.java)
        } catch (ex: Exception) {
            // Send back a "false" should scan more object so we post what we have.
            logger.debug("Could not parse JSON string [$value]", ex)
            return BLEScanResults()
        }
    }

    internal fun addAllNetworksToCurrent(scanResults: List<BleNetworkScanResult>): List<BleWiFiNetwork> {
        networks.addAll(scanResults
            .map {
                BleWiFiNetwork(
                    it.ssid,
                    it.security,
                    it.signalAsInt(),
                    isSelected = it.ssid == selectedWiFiNetwork?.name
                )
            })

        return networks
    }

    internal fun addSelectedWiFiIfNotInList(selected: BleWiFiNetwork?) {
        if (selected != null && selected.name != OTHER_NETWORK_NAME) {
            networks
                .firstOrNull {
                    it.name == selected.name
                }
                .let {
                    if (it == null) {
                        networks.add(0, selected.copy(isSelected = true))
                    }
                }
        }
    }

    internal fun filterNetworks(networks: List<BleWiFiNetwork>) = networks
        .filterNot {
            // Git rid of cruft that sometimes shows up.
            it.name.matches(Xs_AND_Ohs)
        }
        .filterNot {
            // Exclude blank names
            it.name.isBlank()
        }
        .groupBy {
            it.name
        }
        .map {
            // Remove duplicates leaving the result with the strongest signal.
            it.value
                .reduce { acc, scanResult ->
                    val firstLevel = acc.rssi
                    val secondLevel = scanResult.rssi

                    if (firstLevel > secondLevel) acc else scanResult
                }
        }
        .map {
            if (!it.isSelected && selectedWiFiNetwork == null && currentWiFiConnectionName == it.name) {
                selectedWiFiNetwork = it
                it.copy(isSelected = true)
            } else {
                it
            }
        }
        .sortedWith(
            // Selected First - Then by name.
            compareByDescending<BleWiFiNetwork> {
                it.isSelected
            }.thenBy(String.CASE_INSENSITIVE_ORDER) {
                it.name
            }
        )
        .plus(
            BleWiFiNetwork(
                OTHER_NETWORK_NAME,
                NO_SECURITY_LABEL,
                50,
                true,
                selectedWiFiNetwork?.isOtherNetwork == true
            )
        )

    override fun setBleConnector(connector: BleConnector<Context>) {
        bluetoothConnector = connector
    }

    override fun initializeBleInteractionCallback() {
        bluetoothConnector.interactionCallback = interactionCallback
    }

    override fun setCurrentWiFiConnection(currentWifi: String?) {
        currentWiFiConnectionName = currentWifi?.replace("\"", "")
    }

    override fun setSelectedNetwork(currentSelection: BleWiFiNetwork?) {
        selectedWiFiNetwork = currentSelection
    }

    override fun reconnectBle(with: Context?) {
        if (!bluetoothConnector.reconnect(with)) {
            onlyIfView {
                it.onBleStatusChange(BleConnectionStatus.BLE_CONNECT_FAILURE)
            }
        }
    }

    override fun scanForNetworks(currentSelection: BleWiFiNetwork?) {
        setSelectedNetwork(currentSelection)
        networks.clear()
        doScanForNetworks()
    }

    internal fun doScanForNetworks() {
        bluetoothConnector.scanForWiFiNetworks()

        onlyIfView {
            it.onScanningForNetworksActive(true)
        }
    }

    override fun stopScanningForNetworks() {
        executor.clearExecutor()
        onlyIfView {
            it.onScanningForNetworksActive(false)
        }
    }

    companion object {
        /**
         * Interval we scan for networks at.  Once every _ seconds
         */
        private val LOOP_INTERVAL_TIME = TimeUnit.SECONDS.toMillis(20)

        private const val OTHER_NETWORK_NAME = "OTHER"
        private const val NO_SECURITY_LABEL = "None"
        private const val NA_TEXT = "N/A"
        private const val SCAN_MORE_JSON = "{\"scanResults\": [], \"more\":\"true\"}"
        private val Xs_AND_Ohs = ".*\\\\x00.*".toRegex()

        private val logger = LoggerFactory.getLogger(AndroidBleWiFiSelectPresenter::class.java)
    }
}
