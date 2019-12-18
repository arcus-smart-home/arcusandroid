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
package arcus.presentation.pairing.device.customization.halo.statecounty

import androidx.annotation.VisibleForTesting
import arcus.cornea.SessionController
import arcus.cornea.device.smokeandco.halo.HaloLocationController
import arcus.cornea.device.smokeandco.halo.HaloLocationControllerImpl
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import arcus.presentation.pairing.device.customization.halo.station.HaloStationSelectPresenterImpl
import com.iris.client.event.Futures
import java.util.concurrent.atomic.AtomicBoolean
import org.slf4j.LoggerFactory

class HaloStateCountySelectPresenterImpl(
    @VisibleForTesting
    private val createController: (String?) -> HaloLocationController = { address ->
        HaloLocationControllerImpl(address ?: "DRIV:dev:")
    },
    @VisibleForTesting
    private val getPersonState: () -> String = {
        SessionController.instance().place?.stateProv ?: ""
    },
    @VisibleForTesting
    private val getPersonCounty: () -> String = {
        SessionController.instance().place?.addrCounty ?: ""
    }
) : HaloStateCountySelectPresenter, KBasePresenter<HaloStateCountySelectView>() {
    private var haloController: HaloLocationController? = null
    private val savingSelection = AtomicBoolean(false)

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

    override fun loadStates() {
        controllerOrLog { controller ->
            controller
                .getStateNames()
                .chain { listItems ->
                    if (listItems == null || listItems.isEmpty()) {
                        Futures.failedFuture(RuntimeException("List was null / empty - cannot process"))
                    } else {
                        val personState = getPersonState()
                        Futures.succeededFuture(
                            listItems
                                .map {
                                    HaloStateAndCode(
                                        it.state,
                                        it.stateCode,
                                        personState == it.stateCode
                                    )
                                }
                        )
                    }
                }
                .onSuccess(Listeners.runOnUiThread { codes ->
                    onlyIfView { view ->
                        view.onStatesLoaded(codes)
                    }
                })
                .onFailure(Listeners.runOnUiThread { error ->
                    logger.error("Failed to load states.", error)
                    onlyIfView { view ->
                        view.onStatesFailedToLoad()
                    }
                })
        }
    }

    override fun loadCounties(state: HaloStateAndCode) {
        controllerOrLog { controller ->
            controller
                .getCountiesFor(state.sameCode)
                    .chain { listItems ->
                        if (listItems == null || listItems.isEmpty()) {
                            Futures.failedFuture(RuntimeException("List was null / empty - cannot process"))
                        } else {
                            val personCounty = getPersonCounty()
                            Futures.succeededFuture(
                                    listItems
                                    .map {
                                        HaloCounty(
                                                it,
                                                it == personCounty && state.isPersonsPlace
                                        )
                                    }
                            )
                        }
                    }
                .onSuccess(Listeners.runOnUiThread { counties ->
                    onlyIfView { view ->
                        view.onCountiesLoaded(counties)
                    }
                })
                .onFailure(Listeners.runOnUiThread { error ->
                    logger.error("Failed to load counties.", error)
                    onlyIfView { view ->
                        view.onCountiesFailedToLoad()
                    }
                })
        }
    }

    override fun setSelectedStateAndCounty(state: HaloStateAndCode, county: String) {
        if (savingSelection.compareAndSet(false, true)) {
            controllerOrLog { controller ->
                controller
                    .setLocationUsing(state.sameCode, county)
                    .onSuccess(Listeners.runOnUiThread {
                        onlyIfView { view ->
                            view.onSelectionSaved()
                        }
                    })
                    .onFailure(Listeners.runOnUiThread { error ->
                        logger.error("Failed to save selection.", error)
                        onlyIfView { view ->
                            view.onSelectionSaveFailed()
                        }
                    })
                    .onCompletion {
                        savingSelection.set(false)
                    }
            }
        } else {
            logger.debug("Currently saving selection - waiting for current to finish.")
        }
    }

    private fun controllerOrLog(action: (HaloLocationController) -> Unit) {
        haloController?.let {
            action(it)
        } ?: logger.debug("Controller was null. Did you call loadFromPairingDevice?")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HaloStationSelectPresenterImpl::class.java)
    }
}
