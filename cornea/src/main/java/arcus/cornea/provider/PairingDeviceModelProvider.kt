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
package arcus.cornea.provider

import arcus.cornea.CorneaClientFactory
import arcus.cornea.subsystem.SubsystemController
import com.iris.client.IrisClient
import com.iris.client.capability.PairingDevice
import com.iris.client.capability.PairingSubsystem
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import com.iris.client.model.ModelCache
import com.iris.client.model.PairingDeviceModel
import com.iris.client.model.Store

/**
 * Pairing Device Model Provider
 *
 * - Since Kotlin objects can't have constructors - need to create a more typical Java singleton
 * so we can test this easier later.
 */
class PairingDeviceModelProvider @JvmOverloads internal constructor(
    irisClient: IrisClient = CorneaClientFactory.getClient(),
    modelCache: ModelCache = CorneaClientFactory.getModelCache(),
    modelStore: Store<PairingDeviceModel> = CorneaClientFactory.getStore(PairingDeviceModel::class.java)
) : BaseModelProvider<PairingDeviceModel>(irisClient, modelCache, modelStore) {
    val filteredModels : List<PairingDeviceModel>
        get() {
            val subsystem = SubsystemController
                .instance()
                .getSubsystemModel(PairingSubsystem.NAMESPACE)
            val current = if (subsystem.isLoaded) {
                (subsystem.get() as PairingSubsystem).pairingDevices ?: emptyList()
            } else {
                emptyList()
            }

            return store.values().filter {
                it.isNotKitDevice() && current.remove(it.address)
                        || (it.isMispaired() || it.isMisConfigured())
            }
        }

    override fun doLoad(placeId: String): ClientFuture<List<PairingDeviceModel>> {
        return SubsystemController
            .instance()
            .getSubsystemModel(PairingSubsystem.NAMESPACE)
            .load()
            .chain { subsystem ->
                if (subsystem == null || subsystem !is PairingSubsystem) {
                    Futures.failedFuture(RuntimeException("Unable to load subsystem (null), cannot load devices."))
                } else {
                    subsystem
                        .listPairingDevices() // Load from platform
                        .transform { response ->
                            if (response == null) {
                                throw RuntimeException("Response was null. Shouldn't happen")
                            } else {
                                // Convert to response
                                val parsedResponse = PairingSubsystem.ListPairingDevicesResponse(response)
                                @Suppress("UNCHECKED_CAST")
                                cache.retainAll(PairingDevice.NAMESPACE, parsedResponse.devices) as List<PairingDeviceModel>
                            }
                        }
                }
            }
    }

    fun loadUnfiltered() : ClientFuture<List<PairingDeviceModel>> = if (isLoaded) {
        Futures.succeededFuture(store.values().toList())
    } else {
        reload()
    }

    override fun load(): ClientFuture<List<PairingDeviceModel>> = if (isLoaded) {
        Futures.succeededFuture(filteredModels)
    } else {
        reload()
    }

    fun hasDevicesInErrorState() : Boolean = getDevicesInErrorStateCount() > 0

    fun hasPairedDevices() : Boolean {
        return if (isLoaded) {
            filteredModels.isNotEmpty() && !hasPairedKitDevices()
        } else {
            false
        }
    }

    fun hasPairedKitDevices() : Boolean {
        return if (isLoaded) {
            filteredModels
                .any { model ->
                    model.isKitDevice()
                }
        } else {
            false
        }
    }

    fun getDevicesInErrorStateCount() : Int {
        return if (isLoaded) {
            filteredModels
                .filter { model ->
                    model.isMispaired()
                }
                .count()
        } else {
            0
        }
    }

    private fun PairingDeviceModel.isNotKitDevice() : Boolean = !isKitDevice()


    private fun PairingDeviceModel.isKitDevice() : Boolean = tags
            ?.any {
                KIT_TAG.equals(it, true)
            } == true
            &&
            pairingPhase == PairingDeviceModel.PAIRINGPHASE_JOIN

    private fun PairingDeviceModel.isMispaired() : Boolean {
        return pairingState == PairingDevice.PAIRINGSTATE_MISPAIRED || isMisConfigured()
    }

    // Eventually the plan is to make this a different state from mispaired but drivers don't support it currently
    private fun PairingDeviceModel.isMisConfigured() : Boolean {
        return pairingState == PairingDevice.PAIRINGSTATE_MISCONFIGURED
    }

    companion object {
        private val INSTANCE = PairingDeviceModelProvider()
        private const val KIT_TAG = "KIT"

        @JvmStatic
        fun instance() = INSTANCE
    }
}