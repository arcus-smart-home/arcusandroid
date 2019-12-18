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
package arcus.presentation.pairing.hub

import arcus.cornea.CorneaClientFactory
import arcus.cornea.helpers.chainNonNull
import arcus.cornea.helpers.onFailureMain
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.HubModelProvider
import com.iris.client.ClientRequest
import com.iris.client.IrisClient
import com.iris.client.capability.Capability
import com.iris.client.capability.Device
import com.iris.client.capability.Hub
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import org.slf4j.LoggerFactory

class ModelNameAndPhotoPresenterImpl(
    private val client: IrisClient = CorneaClientFactory.getClient()
) : ModelNameAndPhotoPresenter, KBasePresenter<ModelNameAndPhotoView>() {
    override fun loadDeviceNameForAddress(address: String) {
        DeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .onSuccessMain { device ->
                onlyIfView { view ->
                    val place = device.place ?: ""
                    val id = device.id ?: ""
                    view.showName(device.name, place, id)
                }
            }
            .onFailure {
                logger.error("Failed getting device name.", it)
            }
    }

    override fun loadHubName() {
        HubModelProvider
            .instance()
            .load()
            .chain {
                Futures.succeededFuture(HubModelProvider.instance().hubModel)
            }
            .transformNonNull {
                it
            }
            .onSuccessMain { hub ->
                onlyIfView { view ->
                    val place = hub.place ?: ""
                    val id = hub.id ?: ""
                    view.showName(hub.name, place, id)
                }
            }
            .onFailure {
                logger.error("Failed getting hub name.", it)
            }
    }

    override fun setNameForDeviceAddress(name: String, address: String) {
        processRequest(DeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .chainNonNull {
                client.request(
                    getSetAttributesRequest(
                        it.address,
                        mapOf(Device.ATTR_NAME to name
                        )
                    )
                )
            })
    }

    override fun setNameForHub(name: String) {
        processRequest(HubModelProvider
            .instance()
            .load()
            .transform {
                val hubModel = HubModelProvider.instance().hubModel
                hubModel ?: throw RuntimeException("No hub model found. Cannot load hub.")
            }
            .chainNonNull {
                client.request(
                    getSetAttributesRequest(
                        it.address,
                        mapOf(Hub.ATTR_NAME to name
                        )
                    )
                )
            })
    }

    private fun <T> processRequest(request: ClientFuture<T?>) {
        request
            .onSuccessMain {
                onlyIfView {
                    it.saveSuccessful()
                }
            }
            .onFailureMain { throwable ->
                onlyIfView { view ->
                    view.saveUnsuccessful()
                    logger.error("Unable to save the device/hub name", throwable)
                }
            }
    }

    @Suppress("MemberVisibilityCanBePrivate") // No Synths!
    internal fun getSetAttributesRequest(
        address: String,
        attributes: Map<String, Any>
    ) = ClientRequest().also {
        it.command = Capability.CMD_SET_ATTRIBUTES
        it.address = address
        it.attributes = attributes
        it.timeoutMs = 10_000
        it.isRestfulRequest = false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ModelNameAndPhotoPresenterImpl::class.java)
    }
}
