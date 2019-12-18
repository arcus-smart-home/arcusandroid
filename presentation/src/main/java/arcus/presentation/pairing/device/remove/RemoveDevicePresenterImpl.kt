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
package arcus.presentation.pairing.device.remove

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.provider.ProductModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.bean.PairingStep
import com.iris.client.event.Futures
import org.slf4j.LoggerFactory

class RemoveDevicePresenterImpl : RemoveDevicePresenter, KBasePresenter<RemoveDeviceView>() {
    override fun removePairingDevice(pairingDeviceAddress: String) {
        PairingDeviceModelProvider
                .instance()
                .getModel(pairingDeviceAddress)
                .load()
                .chain {
                    it?.remove() ?: Futures.failedFuture(RuntimeException("Device was null!"))
                }
                .transform { response ->
                    response?.steps?.map {
                        DeviceRemovalStep(
                                PairingStep(it).id ?: "",
                                PairingStep(it).instructions ?: emptyList(),
                                PairingStep(it).order ?: 1,
                                PairingStep(it).title
                        )
                    } ?: emptyList()
                }
                .onSuccess(Listeners.runOnUiThread { steps ->
                    onlyIfView { view ->
                        view.onRemovalStepsLoaded(steps)
                    }
                })
                .onFailure(Listeners.runOnUiThread {
                    logger.error("Failed to remove device", it)
                    onlyIfView { view ->
                        view.onRemoveFailed()
                    }
                })
        }

    override fun checkForMispairedHue(pairingDeviceAddress: String) {
        PairingDeviceModelProvider
            .instance()
            .getModel(pairingDeviceAddress)
            .load()
            .onSuccess(Listeners.runOnUiThread { model ->
                if (model.productAddress.contains("dead15", true)) {
                    onlyIfView { view ->
                        val productModel = ProductModelProvider.instance().getByProductIDOrNull(model.id)
                        view.onHueDeviceMispaired(productModel?.shortName ?: "Light Bulb")
                    }
                }
            })
            .onFailure(Listeners.runOnUiThread {
                logger.error("Failed to determine if this is a Hue device", it)
            })
    }

    companion object {
        private val logger = LoggerFactory.getLogger(RemoveDevicePresenterImpl::class.java)
    }
}
