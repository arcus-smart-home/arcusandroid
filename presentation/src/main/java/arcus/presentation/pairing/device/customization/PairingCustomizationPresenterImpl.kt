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
package arcus.presentation.pairing.device.customization

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import arcus.presentation.pairing.device.steps.WebLink
import com.iris.client.bean.PairingCustomizationStep
import com.iris.client.event.Futures
import java.util.concurrent.atomic.AtomicReference

class PairingCustomizationPresenterImpl : CustomizationPresenter, KBasePresenter<CustomizationView>() {
    private val deviceAddress = AtomicReference<String>("_UNKNOWN_")
    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }

    override fun loadDevice(pairingDeviceAddress: String) {
        deviceAddress.set(pairingDeviceAddress)
        PairingDeviceModelProvider
            .instance()
            .getModel(pairingDeviceAddress)
            .load()
            .chain { model ->
                if (model == null) {
                    Futures.failedFuture(RuntimeException("Model was null. Cannot continue"))
                } else {
                    model.customize()
                }
            }
            .chain { response ->
                if (response == null) {
                    Futures.failedFuture(RuntimeException("Response was null. Cannot continue"))
                } else {
                    val items = response
                        .steps
                        .map { PairingCustomizationStep(it) }
                        .mapIndexedNotNull { index, platformStep ->
                            val customizationType =
                                CustomizationType.fromPlatformType(
                                    platformStep.action
                                )
                            if (customizationType == CustomizationType.UNKNOWN ||
                                customizationType == CustomizationType.CUSTOMIZATION_COMPLETE
                            ) {
                                null
                            } else {
                                CustomizationStep(
                                    platformStep.id,
                                    platformStep.order ?: index,
                                    customizationType,
                                    platformStep.header,
                                    platformStep.title,
                                    platformStep.description ?: emptyList(),
                                    platformStep.info,
                                    if (platformStep.linkText != null && platformStep.linkUrl != null) {
                                        WebLink(
                                            platformStep.linkText,
                                            platformStep.linkUrl
                                        )
                                    } else {
                                        null
                                    },
                                    platformStep.choices ?: emptyList()
                                )
                            }
                        }
                        .toList()
                        .sortedBy { it.order }

                    Futures.succeededFuture(items)
                }
            }
            .onSuccess(Listeners.runOnUiThread { items ->
                onlyIfView { view ->
                    view.customizationSteps(items)
                }
            })
            .onFailure(errorListener)
    }

    override fun completeCustomization(type: CustomizationType) {
        PairingDeviceModelProvider
            .instance()
            .getModel(deviceAddress.get())
            .load()
            .chain { model ->
                if (model == null) {
                    Futures.failedFuture(RuntimeException("Model was null. Cannot continue"))
                } else {
                    val future = model.addCustomization(type.name)
                    future
                }
            }
            .onFailure(errorListener)
    }
}
