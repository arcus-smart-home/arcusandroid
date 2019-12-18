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
package arcus.presentation.pairing.device.productcatalog

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.cornea.utils.Listeners
import com.iris.client.bean.PairingCompletionStep
import com.iris.client.capability.PairingSubsystem
import org.slf4j.LoggerFactory

/**
 * Product Catalog Presenter
 *
 * Uses the PairingDeviceModelProvider's store to listen for added/removed PairingDeviceModel events,
 * and updates the [ProductCatalogView] when devices are added, if the view exits
 */
class ProductCatalogPresenterImpl(
    private val controller: PairingSubsystemController = PairingSubsystemControllerImpl
) : KBasePresenter<ProductCatalogView>(), ProductCatalogPresenter {
    private var listenerRegistration = Listeners.empty()

    override fun getMisparedDevicesCount() {
        PairingDeviceModelProvider
            .instance()
            .load() // Make sure it's loaded before we check.
            .onSuccess(Listeners.runOnUiThread {
                if (getMispairedOrMisconfigured()) {
                    PairingDeviceModelProvider
                        .instance()
                        .getDevicesInErrorStateCount().let { mispairedCount ->
                            onlyIfView { viewRef ->
                                viewRef.displayMispairedDeviceCount(mispairedCount)
                            }
                        }
                } else {
                    onlyIfView { viewRef ->
                        viewRef.displayMispairedDeviceCount(0)
                    }
                }
            })
    }

    override fun getMispairedOrMisconfigured() = controller.hasDevicesInErrorState()
    override fun hasPairedDevices() = controller.hasPairedDevices()

    override fun dismissAll() {
        controller.dismissAll()
    }

    override fun exitPairing() {
        controller.dismissAll()
                .onSuccess(Listeners.runOnUiThread {
                    val needsRebuild = it.any { pairingCompletionStep ->
                        pairingCompletionStep.action == PairingCompletionStep.ACTION_ZWAVE_REBUILD
                    }
                    if (needsRebuild) {
                        onlyIfView { view ->
                            view.dismissWithZwaveRebuild()
                        }
                    } else {
                        onlyIfView { view ->
                            view.dismissNormally()
                        }
                    }
                })
                .onFailure(Listeners.runOnUiThread {
                    onlyIfView { view ->
                        view.dismissNormally()
                    }
                })
    }

    override fun stopPairing() {
        controller.exitPairing()
    }

    override fun setView(view: ProductCatalogView) {
        super.setView(view)
        listenerRegistration = controller.setChangedListenerFor(Listeners.runOnUiThread {
            getMisparedDevicesCount()
        }, PairingSubsystem.ATTR_PAIRINGDEVICES)
    }

    override fun clearView() {
        super.clearView()
        Listeners.clear(listenerRegistration)
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ProductCatalogPresenterImpl::class.java)
    }
}
