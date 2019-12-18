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
package arcus.presentation.pairing.hub.activation

import arcus.cornea.helpers.chainNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import com.iris.client.capability.DeviceAdvanced
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures

abstract class BaseKitDevicePresenter<T>(
    protected val pairingSubsystemController: PairingSubsystemController,
    protected val pairingDeviceModelProvider: PairingDeviceModelProvider,
    protected val deviceModelProvider: DeviceModelProvider
) : KBasePresenter<T>() {
    /**
     * Gets the kitted devices from the platform if there is nothing in the cache.
     *
     * @return Future of: Map<ProtocolAddress, ProductAddress>
     */
    protected fun getKittedDevices(): ClientFuture<Map<ProtocolAddress, ProductAddress>?> {
        return pairingSubsystemController
            .getKitInformation()
            .transform {
                if (it == null) {
                    throw RuntimeException("Kit map was null - should never happen.")
                }
                it
            }
    }

    /**
     * Loads the additonal models needed when looking for kitted items except the hub model.
     *
     * @param initial the kit items returned from [getKittedDevices]
     * @return Future of UNMAPPED MetaDevices
     */
    protected fun getAdditionalMetaDevices(
        initial: Map<String, String>
    ): ClientFuture<InitialMetaDevices?> {
        return pairingDeviceModelProvider
            .loadUnfiltered()
            .transform { load ->
                val modelsFromLoad = load ?: emptyList()
                val pairingDeviceModels = modelsFromLoad.filter { it.isKitDevice() }
                Pair(initial, pairingDeviceModels)
            }
            .chainNonNull { (kittedDevices, pairingDevices) ->
                if (deviceModelProvider.isLoaded) {
                    // Don't use the loadRef if we're loaded. We won't get the right device set
                    Futures.succeededFuture(
                        Triple(kittedDevices, pairingDevices, deviceModelProvider.store.values().toList())
                    )
                } else {
                    deviceModelProvider.load().transform { deviceModels ->
                        Triple(kittedDevices, pairingDevices, deviceModels ?: emptyList())
                    }
                }
            }
    }

    /**
     * Parses the list of kit items, device models, and otherwise into a Triple of:
     *
     * 1) missingDevices
     *    Any protocol address from getKitInfo we couldn't find a pairdev or device model for
     *
     * 2) pairingDevices
     *    Any protocol address from getKitInfo we could find a pairdev for which is
     *    Either Paired + Customized, Paired but needs Customization OR Needs to be Activated
     *
     * 3) deviceModels
     *    Any protocol address from getKitInfo we could only find a device model for
     *    Probably paired during another session and was then dismissed
     *
     * @param initial UNMAPPED MetaDevices (typically) from [getAdditionalMetaDevices]
     * @return MAPPED MetaDevices
     */
    protected fun mapMetaDevicesToStartingGridDevices(
        initial: InitialMetaDevices
    ): ParsedMetaDevices? {
        val (kittedDevices, pairingDevices, deviceModels) = initial
        val kitDevices = kittedDevices.toMutableMap()
        val knownDevices = kittedDevices.mapNotNull { kitDevice ->
            pairingDevices
                .firstOrNull {
                    it.protocolAddress == kitDevice.key
                }
                ?.let {
                    kitDevices.remove(it.protocolAddress)
                    kitDevice.value to it
                }
        }

        val pairedDevices = kitDevices
            .toMutableMap() // Iterate through the remaining devices
            .mapNotNull { kitDevice ->
                deviceModels
                    .firstOrNull {
                        val protoId = it[DeviceAdvanced.ATTR_PROTOCOLID] as String? ?: "_UNKNOWN_"
                        kitDevice.key.contains(protoId, true)
                    }
                    ?.let {
                        kitDevices.remove(kitDevice.key)
                        it
                    }
            }

        return Triple(kitDevices.toMap(), knownDevices, pairedDevices)
    }
}
