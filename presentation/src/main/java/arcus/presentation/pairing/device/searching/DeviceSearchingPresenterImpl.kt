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
package arcus.presentation.pairing.device.searching

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.provider.ProductModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.cornea.utils.Listeners
import arcus.presentation.pairing.CUSTOMIZATION_COMPLETE
import arcus.presentation.pairing.DEVICE_DETECTED
import arcus.presentation.pairing.UNNAMED_DEVICE
import arcus.presentation.pairing.device.steps.WebLink
import com.iris.client.bean.PairingCompletionStep
import com.iris.client.capability.PairingSubsystem
import com.iris.client.event.Futures
import com.iris.client.model.ModelChangedEvent
import org.slf4j.LoggerFactory

class DeviceSearchingPresenterImpl(
    private val controller: PairingSubsystemController = PairingSubsystemControllerImpl
) : KBasePresenter<DeviceSearchingView>(),
    DeviceSearchingPresenter {
    private val onErrorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.onError(error)
        }
    }

    private val changedListener = Listeners.runOnUiThread<ModelChangedEvent> {
        it.changedAttributes.forEach { entry ->
            when (entry.key) {
                PairingSubsystem.ATTR_SEARCHIDLE -> {
                    if (controller.hasPairedDevices()) {
                        logger.debug("Skipping search idle since there are devices paired.")
                    } else {
                        when {
                            controller.getPairingMode() == PairingSubsystem.PAIRINGMODE_IDLE -> {
                                logger.debug("We can't get help steps while we're idle!")
                            }
                            else -> // Can't get help steps if we're "IDLE"
                                getAndShowHelpSteps()
                        }
                    }
                }
                PairingSubsystem.ATTR_PAIRINGMODE -> {
                    if (entry.value == PairingSubsystem.PAIRINGMODE_IDLE) {
                        if (controller.hasPairedDevices()) {
                            val anyInError = PairingDeviceModelProvider
                                .instance()
                                .hasDevicesInErrorState()
                            onlyIfView { view ->
                                view.pairingTimedOutWithDevices(anyInError)
                            }
                        } else {
                            onlyIfView { view ->
                                view.pairingTimedOutWithoutDevices()
                            }
                        }
                    } else {
                        updateModels()
                    }
                }
                PairingSubsystem.ATTR_PAIRINGDEVICES, PairingSubsystem.ATTR_SEARCHIDLETIMEOUT -> {
                    updateModels()
                }
                else -> {
                    logger.warn("Received update for unmonitored key -> [${entry.key}]")
                }
            }
        }
    }

    private var controllerListener = Listeners.empty()
    private var storeListener = Listeners.empty()

    override fun dismissAll() {
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

    override fun startSearching(formInputs: Map<String, String>?) {
        when {
            controller.isSearching() -> {
                updateModels()
                logger.debug("Not calling search, we're already searching.")
            }
            else -> {
                controller
                    .searchFor(controller.getSearchContext(), formInputs)
                    .onFailure(onErrorListener)
            }
        }
    }

    override fun updatePairedDeviceList() {
        updateModels()
    }

    override fun isIdle() = controller.getPairingMode() == PairingSubsystem.PAIRINGMODE_IDLE

    override fun restartSearching(formInputs: Map<String, String>?) {
        controller
            .searchFor(controller.getPreviousSearchContext(), formInputs)
            .onFailure(onErrorListener)
    }

    override fun stopSearching() {
        controller.exitPairing()
    }

    override fun setView(view: DeviceSearchingView) {
        clearView()

        super.setView(view)
        controllerListener = controller.setChangedListenerFor(
            changedListener,
            PairingSubsystem.ATTR_SEARCHIDLE,
            PairingSubsystem.ATTR_PAIRINGMODE,
            PairingSubsystem.ATTR_PAIRINGDEVICES,
            PairingSubsystem.ATTR_SEARCHIDLETIMEOUT
        )
        storeListener = PairingDeviceModelProvider
            .instance()
            .store
            .addListener(ModelChangedEvent::class.java, Listeners.runOnUiThread {
                updateModels()
            })
    }

    private fun updateModels() {
        DeviceModelProvider
            .instance()
            .load()
            .chain {
                PairingDeviceModelProvider
                    .instance()
                    .load()
            }
            .chain { list ->
                if (list == null) {
                    Futures.failedFuture(RuntimeException("Received null response from provider."))
                } else {
                    Futures.succeededFuture(list.mapNotNull { pairingDeviceModel ->
                        try {
                            val isCustomized = pairingDeviceModel
                                .customizations
                                ?.contains(CUSTOMIZATION_COMPLETE) == true
                            val pairingPhase = DevicePairingPhase.valueOf(pairingDeviceModel.pairingPhase)
                            val pairingState = DevicePairingState.valueOf(pairingDeviceModel.pairingState)
                            val errorState = pairingPhase == DevicePairingPhase.FAILED
                            val model = if (pairingDeviceModel.deviceAddress.isNullOrBlank()) {
                                null
                            } else {
                                DeviceModelProvider
                                    .instance()
                                    .getModel(pairingDeviceModel.deviceAddress)
                                    .load()
                                    .get()
                            }
                            val description = when (pairingState) {
                                DevicePairingState.PAIRED -> model?.vendor ?: pairingPhase.canonicalName
                                DevicePairingState.MISCONFIGURED -> pairingState.canonicalName
                                DevicePairingState.MISPAIRED -> pairingState.canonicalName
                                else -> pairingPhase.canonicalName
                            }
                            val id = model?.id ?: ""
                            val productId: String = model?.productId ?: ""
                            val name = if (model != null) {
                                model.name ?: UNNAMED_DEVICE
                            } else {
                                DEVICE_DETECTED
                            }
                            val screen = when {
                                !pairingDeviceModel.productAddress.isNullOrBlank() ->
                                    ProductModelProvider
                                        .instance()
                                        .getModel(pairingDeviceModel.productAddress)
                                        .load()
                                        .get()
                                        .screen
                                        .sanitizeToLower()
                                else -> ""
                            }

                            DevicePairingData(
                                id,
                                productId,
                                screen,
                                pairingState,
                                description,
                                pairingDeviceModel.address,
                                name,
                                isCustomized,
                                errorState
                            )
                        } catch (e: Exception) {
                            logger.error("Could not convert to DevicePairingData model.", e)
                            null
                        }
                    }.toList())
                }
            }
            .onFailure(onErrorListener)
            .onSuccess(Listeners.runOnUiThread { devices ->
                onlyIfView { view ->
                    view.showPairedDevices(devices, controller.isInPairingMode())
                }
            })
    }

    private fun getAndShowHelpSteps() {
        controller
            .getHelpSteps()
            .onFailure(onErrorListener)
            .chain { helpItems ->
                if (helpItems == null) {
                    Futures.succeededFuture(emptyList())
                } else {
                    val viewHelpItems = helpItems.flatMap { item ->
                        listOf(
                            HelpStep(
                                item.id,
                                item.order,
                                HelpStepType.valueOf(
                                    item.action
                                ),
                                item.message ?: "",
                                if (item.linkText != null && item.linkUrl != null) {
                                    WebLink(
                                        item.linkText,
                                        item.linkUrl
                                    )
                                } else {
                                    null
                                }
                            )
                        )
                    }.sortedBy { it.order }

                    Futures.succeededFuture(viewHelpItems)
                }
            }
            .onSuccess(Listeners.runOnUiThread { viewHelpItems ->
                if (!controller.hasPairedDevices()) {
                    onlyIfView { view ->
                        view.searchTimedOut(viewHelpItems)
                    }
                }
            })
    }

    override fun allDevicesConfigured(): Boolean {
        val devices = PairingDeviceModelProvider.instance().filteredModels
        return devices.count() > 0 && devices.all { it.customizations.contains(CUSTOMIZATION_COMPLETE) }
    }

    override fun getMispairedOrMisconfigured() = PairingDeviceModelProvider.instance().hasDevicesInErrorState()

    override fun hasPairedDevices() = PairingDeviceModelProvider
        .instance()
        .filteredModels
        .filterNot { // Only consider devices that are not customized.
            it.customizations?.contains(CUSTOMIZATION_COMPLETE) == true
        }.count() > 0

    override fun getPairedDevicesCount() = controller.getsPairedDeviceAddresses().size

    override fun clearView() {
        super.clearView()
        Listeners.clear(controllerListener)
        Listeners.clear(storeListener)
    }

    private fun String?.sanitizeToLower(): String = this?.replace(MULTI_SPACES, "")?.toLowerCase() ?: ""

    companion object {
        private val logger = LoggerFactory.getLogger(DeviceSearchingPresenterImpl::class.java)
        private val MULTI_SPACES = "\\s+".toRegex()
    }
}
