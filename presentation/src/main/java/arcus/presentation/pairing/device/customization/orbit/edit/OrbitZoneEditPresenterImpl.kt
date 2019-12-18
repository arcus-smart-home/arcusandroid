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
package arcus.presentation.pairing.device.customization.orbit.edit

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.ClientEvent
import com.iris.client.EmptyEvent
import com.iris.client.capability.IrrigationZone
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import org.slf4j.LoggerFactory

class OrbitZoneEditPresenterImpl : OrbitZoneEditPresenter, KBasePresenter<OrbitZoneEditView>() {
    override fun loadFromPairingDevice(address: String, zone: String) {
        PairingDeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .transform {
                it?.deviceAddress ?: throw RuntimeException("Model/Address was null.")
            }
            .onFailureMain {
                onlyIfView { view ->
                    view.onZoneLoadingFailure()
                }

                logger.error("Could not load zone info from pairing device model.", it)
            }
            .onSuccess {
                loadFromDeviceAddress(it, zone)
            }
    }

    override fun loadFromDeviceAddress(address: String, zone: String) {
        DeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .transform {
                it?.let {
                    val name = it[zoneNameAttr(zone)] as String? ?: ""
                    val duration = it[durationAttr(zone)] as Number?
                    IrrigationZoneDetails(
                        name,
                        duration?.toInt() ?: 1
                    )
                } ?: throw RuntimeException("Device Model was null.")
            }
            .onSuccessMain { details ->
                onlyIfView { view ->
                    view.onZoneLoaded(details)
                }
            }
            .onFailureMain {
                onlyIfView { view ->
                    view.onZoneLoadingFailure()
                }

                logger.error("Could not load zone info from device model.", it)
            }
    }

    override fun saveZoneInformationToPairingDevice(
        details: IrrigationZoneDetails,
        zone: String,
        address: String
    ) {
        PairingDeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .transform {
                it?.deviceAddress ?: throw RuntimeException("Pairing device model / address was null.")
            }
            .onSuccess {
                saveZoneInformationToDevice(details, zone, it)
            }
            .onFailureMain {
                onlyIfView { view ->
                    view.onZoneLoadingFailure()
                }

                logger.error("Could not save zone info to pairing model.", it)
            }
    }

    override fun saveZoneInformationToDevice(
        details: IrrigationZoneDetails,
        zone: String,
        address: String
    ) {
        DeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .chain {
                it?.let {
                    it.clearChanges()
                    val currentName = it[zoneNameAttr(zone)] as String?
                    val requestedName = details.zoneName.trim()
                    val currentDuration = it[durationAttr(zone)] as Number? ?: 1

                    if (currentName != requestedName) {
                        it[zoneNameAttr(zone)] = details.zoneName
                    }

                    if (currentDuration != details.minutes) {
                        it[durationAttr(zone)] = details.minutes
                    }

                    if (it.isDirty) {
                        it.commit()
                    } else {
                        Futures.succeededFuture(ClientEvent(EmptyEvent.NAME, it.address))
                    }
                } ?: Futures.failedFuture(RuntimeException("Device Model was null."))
            }
            .onSuccessMain {
                onlyIfView { view ->
                    view.onZoneSaveSuccess()
                }
            }
            .onFailureMain {
                onlyIfView { view ->
                    view.onZoneSaveFailure()
                }

                logger.error("Could not save zone info to device model.", it)
            }
    }

    private fun durationAttr(instance: String) = "${IrrigationZone.ATTR_DEFAULTDURATION}:$instance"
    private fun zoneNameAttr(instance: String) = "${IrrigationZone.ATTR_ZONENAME}:$instance"

    private inline fun <T> ClientFuture<T>.onSuccessMain(crossinline handler: (T) -> Unit): ClientFuture<T> {
        return this.onSuccess(Listeners.runOnUiThread {
            handler(it)
        })
    }

    private inline fun <T> ClientFuture<T>.onFailureMain(crossinline handler: (Throwable) -> Unit): ClientFuture<T> {
        return this.onFailure(Listeners.runOnUiThread {
            handler(it)
        })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OrbitZoneEditPresenterImpl::class.java)
    }
}
