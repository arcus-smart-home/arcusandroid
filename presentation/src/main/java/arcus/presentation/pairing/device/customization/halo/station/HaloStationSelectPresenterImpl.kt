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
package arcus.presentation.pairing.device.customization.halo.station

import androidx.annotation.VisibleForTesting
import arcus.cornea.device.smokeandco.halo.HaloRadioController
import arcus.cornea.device.smokeandco.halo.HaloRadioControllerImpl
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.event.Futures
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory

class HaloStationSelectPresenterImpl(
    @VisibleForTesting
    private val createController: (String?) -> HaloRadioController = { address ->
        HaloRadioControllerImpl(address ?: "DRIV:dev:")
    }
) : HaloStationSelectPresenter, KBasePresenter<HaloStationSelectView>() {
    private var haloController: HaloRadioController? = null
    private val scanInProgress = AtomicBoolean(false)

    override fun loadFromPairingDevice(address: String) {
        PairingDeviceModelProvider.instance()
            .getModel(address)
            .load()
            .onFailure {
                logger.error("Could not load device", it)
            }
            .onSuccess { pairingDevice ->
                haloController = createController(pairingDevice.deviceAddress)
            }
    }

    override fun loadFromDeviceAddress(address: String) {
        haloController = createController(address)
    }

    override fun loadRadioStations() {
        if (scanInProgress.compareAndSet(false, true)) {
            controllerOrLog { controller ->
                controller
                    .getAvailableStations()
                    .chain {
                        if (it == null) {
                            Futures.failedFuture(RuntimeException("Response was null."))
                        } else {
                            val selected = haloController?.getSelectedStation()
                            val stations = it
                                .map {
                                    RadioStation(
                                        it.first,
                                        "Station ${it.first}",
                                        it.second,
                                        it.third,
                                        it.first == selected
                                    )
                                }
                                .sortedWith(
                                    compareByDescending<RadioStation> { it.selected }
                                        .thenBy { it.rssi }
                                        .thenBy { it.name.toLowerCase() }
                                ) // Selected first, then rssi, then name (if they happen to have the same RSSI)
                            if (stations.size > 3) {
                                Futures.succeededFuture(
                                    Pair(
                                        stations.subList(0, 3),
                                        stations.subList(3, stations.size)
                                    )
                                )
                            } else {
                                Futures.succeededFuture(
                                    Pair(
                                        stations,
                                        emptyList()
                                    )
                                )
                            }
                        }
                    }
                    .onSuccess(Listeners.runOnUiThread { stations ->
                        onlyIfView { view ->
                            when {
                                stations.first.isEmpty() -> view.onNoStationsFound()
                                stations.second.isEmpty() -> view.onStationsFound(stations.first)
                                else -> view.onStationsFound(stations.first, stations.second)
                            }
                        }
                    })
                    .onFailure(Listeners.runOnUiThread<Throwable> { error ->
                        onlyIfView { view ->
                            logger.error("Could not scan stations", error)
                            view.onScanStationsFailed()
                        }
                    })
                    .onCompletion {
                        scanInProgress.set(false)
                    }
            }
        } else {
            logger.debug("Scan already in progress, not making new request.")
        }
    }

    override fun rescanForStations() {
        loadRadioStations()
    }

    override fun playStation(station: RadioStation, playDuration: Int /* = 10 */) {
        controllerOrLog { controller ->
            controller
                .playStation(station.id, playDuration)
                .onFailure(Listeners.runOnUiThread { error ->
                    logger.error("Failed playing station", error)
                    onlyIfView { view ->
                        view.onPlayStationFailed()
                    }
                })
        }
    }

    override fun stopPlayingStations() {
        controllerOrLog { controller ->
            controller
                .stopPlayingStation()
                .onFailure(Listeners.runOnUiThread { error ->
                    logger.error("Failed to stop the playing station.", error)
                    onlyIfView { view ->
                        view.onStopPlayingStationFailed()
                    }
                })
        }
    }

    override fun setSelectedStation(station: RadioStation) {
        controllerOrLog { controller ->
            controller
                .setSelectedStation(station.id)
                .onFailure(Listeners.runOnUiThread { error ->
                    logger.error("Could not set station to [${station.id}]", error)
                    onlyIfView { view ->
                        view.onSetSelectionFailed()
                    }
                })
        }
    }

    private fun controllerOrLog(action: (HaloRadioController) -> Unit) {
        haloController?.let {
            action(it)
        } ?: logger.debug("Controller was null. Did you call loadFromPairingDevice?")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HaloStationSelectPresenterImpl::class.java)
    }
}
