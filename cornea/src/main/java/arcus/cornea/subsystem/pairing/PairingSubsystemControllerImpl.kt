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
package arcus.cornea.subsystem.pairing

import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.subsystem.KBaseSubsystemControllerImpl
import com.iris.capability.util.Addresses
import com.iris.client.bean.KitDeviceId
import com.iris.client.bean.PairingCompletionStep
import com.iris.client.bean.PairingHelpStep
import com.iris.client.bean.PairingInput
import com.iris.client.bean.PairingStep
import com.iris.client.capability.PairingSubsystem
import com.iris.client.capability.Product
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import com.iris.client.model.PairingDeviceModel
import com.iris.client.session.SessionActivePlaceSetEvent
import com.iris.client.session.SessionExpiredEvent
import java.util.Date
import java.util.concurrent.atomic.AtomicReference

object PairingSubsystemControllerImpl :
    KBaseSubsystemControllerImpl<PairingSubsystem>(PairingSubsystem.NAMESPACE),
    PairingSubsystemController {
    private val cachedKittedDevices : AtomicReference<Map<String, String>> = AtomicReference<Map<String, String>>(null)
    private var lastKnownSearchContext : String? = null
    init {
        irisClient.addSessionListener {
            if (it is SessionActivePlaceSetEvent || it is SessionExpiredEvent) {
                cachedKittedDevices.set(null)
            }
        }
    }

    override fun startPairingFor(productAddress: String) : ClientFuture<StartPairingResponse> {
        return ifLoadedDoRequest { subsystem ->
            lastKnownSearchContext = productAddress
            subsystem.startPairing(productAddress, false /* not a mock */).transform { response ->
                if (response == null) {
                    throw RuntimeException("Response was null - should never happen.")
                } else {
                    val videoUrl = if (!response.video.isNullOrBlank()) {
                        response.video
                    } else {
                        null
                    }
                    val steps = response.steps.map { PairingStep(it) }.toList()
                    val mode = PairingMode(response.mode)
                    val inputs = response.form.map { PairingInput(it) }.toList()
                    val oAuthUrl = response.oauthUrl
                    val oAuthStyle = response.oauthStyle

                    StartPairingResponse(videoUrl, steps, mode, inputs, oAuthUrl, oAuthStyle)
                }
            }
        }
    }

    override fun exitPairing() : ClientFuture<*> {
        return ifLoadedDoRequest {
            it.stopSearching()
        }
    }

    override fun searchFor(productAddress: String?, withAttributes: Map<String, String>?) : ClientFuture<PairingMode> {
        return ifLoadedDoRequest {
            it.search(productAddress, withAttributes).transform { response ->
                val mode = response?.mode
                if (mode == null) {
                    throw RuntimeException("Mode, in the response, was null and should always have a value.")
                } else {
                    PairingMode(mode)
                }
            }
        }
    }

    override fun dismissAll() : ClientFuture<List<PairingCompletionStep>> {
        return ifLoadedDoRequest {
            it.dismissAll().transform { response ->
                response?.actions?.map { PairingCompletionStep(it) }?.toList() ?: emptyList()
            }
        }
    }

    override fun getHelpSteps() : ClientFuture<List<PairingHelpStep>> {
        return ifLoadedDoRequest {
            it.listHelpSteps().transform { response ->
                response?.steps?.map { PairingHelpStep(it) }?.toList() ?: emptyList()
            }
        }
    }

    override fun hasPairedDevices() = PairingDeviceModelProvider.instance().hasPairedDevices()

    override fun hasDevicesInErrorState() = PairingDeviceModelProvider.instance().hasDevicesInErrorState()

    override fun getPairedDevices() : ClientFuture<List<PairingDeviceModel>> = PairingDeviceModelProvider.instance().load()

    override fun refreshPairedDevices(): ClientFuture<List<PairingDeviceModel>> = PairingDeviceModelProvider.instance().reload()

    override fun getsPairedDeviceAddresses() : List<String> = getTypedModel()?.pairingDevices ?: emptyList()

    override fun getPairingMode() = getTypedModel()?.pairingMode ?: "UNKNOWN"

    override fun isSearching() = getTypedModel()?.searchIdleTimeout?.after(Date()) == true

    override fun isInPairingMode() = getTypedModel()?.pairingMode != PairingSubsystem.PAIRINGMODE_IDLE

    override fun getSearchContext() = getTypedModel()?.searchProductAddress

    override fun getPreviousSearchContext() = lastKnownSearchContext

    override fun getFactoryResetSteps() : ClientFuture<FactoryResetSteps> {
        return ifLoadedDoRequest { subsystem ->
            subsystem.factoryReset().transform { response ->
                val responseSteps = response?.steps

                if (responseSteps == null) {
                    FactoryResetSteps(null, emptyList())
                } else {
                    val videoUrl = response.video
                    val steps = responseSteps.map { PairingStep(it) }.toList()
                    FactoryResetSteps(videoUrl, steps)
                }
            }
        }
    }

    override fun getKitInformation() : ClientFuture<Map<String, String>> {
        val cache = cachedKittedDevices.get()
        return if (cache != null) {
            Futures.succeededFuture(cache)
        } else {
            ifLoadedDoRequest { subsystem ->
                subsystem
                    .kitInformation
                    .transform { response ->
                        if (response == null || response.kitInfo == null || response.kitInfo.isEmpty()) {
                            emptyMap()
                        } else {
                            val updateCacheWith = response
                                .kitInfo
                                .map { kitItem ->
                                    val currentId = kitItem[KitDeviceId.ATTR_PRODUCTID] as String? ?: "Uknown"
                                    val productAddress = "${Addresses.toServiceAddress(Product.NAMESPACE)}$currentId"
                                    val protocolAddress = kitItem[KitDeviceId.ATTR_PROTOCOLADDRESS] as String? ?: "I dunno"

                                    protocolAddress to productAddress
                                }
                                .toMap()
                            cachedKittedDevices.set(updateCacheWith)
                            updateCacheWith
                        }
                    }
            }
        }
    }
}