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
package arcus.presentation.pairing.device.customization.orbit.list

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.capability.Capability
import com.iris.client.capability.IrrigationZone as PlatformIrrigationZone
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel

class OrbitZonePresenterImpl : OrbitZonePresenter, KBasePresenter<OrbitZoneView>() {
    private var deviceModel: DeviceModel? = null

    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }

    override fun loadFromPairingDevice(pairedDeviceAddress: String) {
        PairingDeviceModelProvider
                .instance()
                .getModel(pairedDeviceAddress)
                .load()
                .chain { pairingDeviceModel ->
                    pairingDeviceModel?.deviceAddress?.let {
                        DeviceModelProvider
                                .instance()
                                .getModel(it)
                                .load()
                    } ?: Futures.failedFuture(RuntimeException("Cannot load null model. Failed."))
                }
                .onSuccess {
                    deviceModel = it
                }
                .onFailure(errorListener)
    }

    override fun loadZones() {
        deviceModel?.let { model ->
            val instances = model[Capability.ATTR_INSTANCES] as? Map<*, *>? ?: emptyMap<Any, Any>()
            val zones = instances
                .keys
                .mapNotNull {
                    it as? String?
                }
                .map { instance ->
                    val duration = model["${PlatformIrrigationZone.ATTR_DEFAULTDURATION}:$instance"] as Number?

                    val zoneNumber = model["${PlatformIrrigationZone.ATTR_ZONENUM}:$instance"] as Number?
                    val zoneString = "Zone ${zoneNumber?.toInt() ?: 1}"

                    val name = model["${PlatformIrrigationZone.ATTR_ZONENAME}:$instance"] as String? ?: ""

                    IrrigationZone(
                        instance,
                        name,
                        zoneString,
                        duration?.toInt() ?: 1,
                        zoneNumber?.toInt() ?: 1
                    )
                }
                .sortedBy {
                    it.zoneNumber
                }

            onlyIfView { view ->
                view.onZonesLoaded(zones)
            }
        }
    }
}
