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
package arcus.presentation.pairing.device.steps

import arcus.cornea.error.ConnectivityException
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.ProductModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.cornea.subsystem.pairing.StartPairingResponse
import arcus.cornea.utils.Listeners
import arcus.presentation.pairing.BLE_GS_INDOOR_PLUG_PRODUCT_ID
import arcus.presentation.pairing.BLE_GS_OUTDOOR_PLUG_PRODUCT_ID
import arcus.presentation.pairing.BLE_SWANN_CAMERA_PRODUCT_ID
import arcus.presentation.pairing.V03_HUB_PRODUCT_ADDRESS
import arcus.presentation.pairing.VOICE_ASST_CATEGORY
import arcus.presentation.pairing.WIFI_SMART_SWITCH_PRODUCT_ID
import com.iris.client.bean.PairingApplication
import com.iris.client.bean.PairingInput
import com.iris.client.bean.PairingStep
import com.iris.client.capability.PairingSubsystem
import com.iris.client.event.Futures
import com.iris.client.event.Listener
import com.iris.client.model.ModelChangedEvent
import com.iris.client.model.ProductModel

/**
 * Pairing Steps Presenter
 *
 * Hooks into the PairingSubsystemController to call appropriate methods and transform the results
 * for the [PairingStepsView] if it's present.
 */
class PairingStepsPresenterImpl(
    private val tutorialUrl: String,
    private val subsystemController: PairingSubsystemController = PairingSubsystemControllerImpl
) : PairingStepsPresenter, KBasePresenter<PairingStepsView>() {
    private val errorHandler = Listeners.runOnUiThread<Throwable> { error ->
        if (error !is ConnectivityException) {
            onlyIfView {
                it.errorReceived(error)
            }
        }
    }

    private val propertyChangeListener = Listeners.runOnUiThread(Listener<ModelChangedEvent> { mce ->
        val changedKeys = mce.changedAttributes.keys
        changedKeys.forEach {
            when (it) {
                PairingSubsystem.ATTR_PAIRINGDEVICES -> {
                    onlyIfView { viewRef ->
                        val devicesPaired = mce.changedAttributes[it] as? Collection<*>
                        val newPairedSize = devicesPaired?.size ?: 0
                        if (newPairedSize > pairedDevicesCount) {
                            viewRef.devicesPaired(newPairedSize)
                        }

                        pairedDevicesCount = newPairedSize
                    }
                }
                PairingSubsystem.ATTR_PAIRINGMODE -> {
                    checkTimeout()
                }
            }
        }
    })
    private var listenerRegistration = Listeners.empty()

    /**
     * We're tracking the number of devices paired to make sure that we only invoke
     * the call to the view if the number of devices has *increased*
     *
     * I was seeing in testing that when I removed a paired device - and went from 3 paired deices
     * to 2 paired devices - that caused the subsystem to update the [PairingSubsystem.ATTR_PAIRINGDEVICES]
     * number - which caused the view to be told about it - which I don't believe is the ideal scenario.
     *
     * I don't think we'll ever see this in production so this is really just a testing safeguard
     */
    private var pairedDevicesCount = 0

    override fun startPairing(productAddress: String, isForReconnect: Boolean) {
        when {
            isForReconnect -> loadReconnectSteps(productAddress)
            productAddress == V03_HUB_PRODUCT_ADDRESS -> loadHubPairingSteps(productAddress) // If pairing a hub
            else -> loadDevicePairingSteps(productAddress)
        }
    }

    private fun loadReconnectSteps(productAddress: String) {
        ProductModelProvider
            .instance()
            .getModel(productAddress)
            .load()
            .transform { product ->
                if (product == null) {
                    error("Product was null, unable to proceed.")
                }

                val steps = product.reconnect ?: emptyList()
                val title = product.shortName.orEmpty()
                val prodId = product.id

                val reconnectInitialSteps: List<ParsedPairingStep> = steps.mapIndexed { index, itemMap ->
                    val instructions = (itemMap["text"] as? String?)?.let { textItem ->
                        listOf(textItem)
                    } ?: emptyList()
                    val linkUrl = itemMap["linkUrl"] as? String?
                    val linkText = itemMap["linkText"] as? String?
                    val webLink = linkUrl?.let { url ->
                        WebLink(linkText.orEmpty(), url)
                    }

                    BleWiFiReconfigureStep(
                        prodId,
                        instructions,
                        title,
                        itemMap["info"] as? String?,
                        webLink,
                        index + 1
                    )
                }

                Triple(prodId, title, reconnectInitialSteps)
            }
            .transformNonNull { (prodId, shortName, initialSteps) ->
                val prefix = when (prodId) {
                    BLE_SWANN_CAMERA_PRODUCT_ID -> CAMERA_NAME_PREFIX_FILTER
                    BLE_GS_INDOOR_PLUG_PRODUCT_ID,
                    BLE_GS_OUTDOOR_PLUG_PRODUCT_ID -> PLUG_NAME_PREFIX_FILTER
                    else -> HUB_NAME_PREFIX_FILTER
                }

                val allSteps = initialSteps
                    .toMutableList()
                    .also {
                        repeat(6) { stepOrder ->
                            it.add(BleGenericPairingStep(
                                stepOrder + 2,
                                prodId,
                                shortName,
                                prefix,
                                isForReconnect = true
                            ))
                        }
                    }

                shortName to allSteps
            }
            .onSuccessMain { (shortName, steps) ->
                onlyIfView { view ->
                    view.updateView(shortName, steps)
                }
            }
    }

    private fun loadHubPairingSteps(productAddress: String) {
        ProductModelProvider
            .instance()
            .getModel(productAddress)
            .load()
            .onSuccessMain { model ->
                val steps = getGenericBleDevicePairingSteps(
                    null,
                    V03_HUB_PRODUCT_ADDRESS.substringAfterLast(":"),
                    HUB_NAME_PREFIX_FILTER,
                    PairingModeType.HUB)

                // TODO: once the URL is active, revert to conditional view
                onlyIfView { view ->
                    view.updateView(model.shortName, steps, tutorialUrl)
                }
            }
    }

    private fun loadDevicePairingSteps(productAddress: String) {
        listenerRegistration = Listeners.clear(listenerRegistration)
        listenerRegistration = subsystemController.setChangedListenerFor(
            propertyChangeListener,
            PairingSubsystem.ATTR_PAIRINGDEVICES,
            PairingSubsystem.ATTR_PAIRINGMODE
        )

        val productModelSource = ProductModelProvider.instance().getModel(productAddress)
        productModelSource.load()
        subsystemController
            .startPairingFor(productAddress)
            .chain { startResponse ->
                if (startResponse == null) {
                    Futures.failedFuture(RuntimeException("StartPairingResponse was null."))
                } else {
                    val productId = productAddress.substringAfterLast(':')
                    val pairingModeType = PairingModeType.valueOf(startResponse.pairingMode.mode)
                    val steps = when (productId) {
                        BLE_SWANN_CAMERA_PRODUCT_ID -> {
                            getGenericBleDevicePairingSteps(
                                startResponse,
                                productId,
                                CAMERA_NAME_PREFIX_FILTER,
                                pairingModeType)
                        }
                        BLE_GS_INDOOR_PLUG_PRODUCT_ID,
                        BLE_GS_OUTDOOR_PLUG_PRODUCT_ID -> {
                            getGenericBleDevicePairingSteps(
                                startResponse,
                                productId,
                                PLUG_NAME_PREFIX_FILTER,
                                pairingModeType)
                        }
                        WIFI_SMART_SWITCH_PRODUCT_ID -> {
                            getWiFiSmartSwitchPairingSteps(startResponse, productId, pairingModeType)
                        }
                        else -> {
                            val productModel = productModelSource.get()
                            getPairingSteps(startResponse, productId, pairingModeType, productModel)
                        }
                    }

                    steps.sortBy { it.stepNumber }

                    Futures.succeededFuture(Pair(startResponse.videoUrl, steps.toList()))
                }
            }
            .onFailure(errorHandler)
            .onSuccess(Listeners.runOnUiThread { pair ->
                onlyIfView { view ->
                    val title = if (productModelSource.isLoaded) {
                        productModelSource.get().shortName
                    } else {
                        ""
                    }
                    val videoUrl = pair.first
                    if (videoUrl == null) {
                        view.updateView(title, pair.second)
                    } else {
                        view.updateView(title, pair.second, videoUrl)
                    }
                }
            })
    }

    private fun getPairingSteps(
        pairingResponse: StartPairingResponse,
        productId: String,
        pairingModeType: PairingModeType,
        productModel: ProductModel
    ): MutableList<ParsedPairingStep> {
        val steps = pairingResponse.steps.mapIndexed { index, item ->
            val webLink = getWebLink(item)

            if (index == pairingResponse.steps.lastIndex && pairingResponse.inputs.isNotEmpty()) {
                val inputs = getInputList(pairingResponse.inputs)

                InputPairingStep(
                        productId,
                        item.instructions,
                        inputs,
                        pairingModeType,
                        item.title,
                        item.info,
                        webLink,
                        item.order,
                        item.id
                )
            } else {
                val oAuthDetails =
                        if (index == pairingResponse.steps.lastIndex && PairingModeType.OAUTH == pairingModeType) {
                            OAuthDetails(pairingResponse.oAuthUrl, pairingResponse.oAuthStyle)
                        } else {
                            null
                        }

                SimplePairingStep(
                        productId,
                        item.instructions,
                        pairingModeType,
                        item.title,
                        item.info,
                        webLink,
                        item.order,
                        item.id,
                        oAuthDetails
                )
            }
        }.toMutableList()

        if (productModel.categories.contains(VOICE_ASST_CATEGORY)) {
            // For assistant devices: get step instructions and app download URL
            steps.add(getAssistantPairingStep(pairingResponse.steps, productModel, pairingModeType))
        }

        return steps
    }

    private fun getAssistantPairingStep(
        steps: List<PairingStep>,
        productModel: ProductModel,
        pairingModeType: PairingModeType
    ): AssistantPairingStep {
        val externalAppStep = steps.firstOrNull { it.externalApps?.isNotEmpty() == true }
        val appUrl: String = externalAppStep?.externalApps
                ?.firstOrNull {
                    it[PairingApplication.ATTR_PLATFORM] == PairingApplication.PLATFORM_ANDROID
                }
                ?.let {
                    PairingApplication(it)
                }
                ?.appUrl ?: ""

        return AssistantPairingStep(
                productModel.id ?: "",
                pairingModeType,
                productModel.manufacturer ?: "",
                productModel.name ?: "",
                steps.size + 1,
                null,
                appUrl
        )
    }

    private fun getWiFiSmartSwitchPairingSteps(
        pairingResponse: StartPairingResponse,
        productId: String,
        pairingModeType: PairingModeType
    ): MutableList<ParsedPairingStep> {
        val tmp = mutableListOf<ParsedPairingStep>()
        val item = pairingResponse.steps[0]
        val webLink = getWebLink(item)
        tmp.add(SimplePairingStep(
                productId,
                item.instructions,
                pairingModeType,
                item.title,
                item.info,
                webLink,
                item.order,
                item.id
        ))

        for (stepOrder in 2..5) {
            tmp.add(WiFiSmartSwitchPairingStep(stepOrder))
        }
        tmp.add(WiFiSmartSwitchPairingStep(6, getInputList(pairingResponse.inputs)))

        return tmp
    }

    private fun getGenericBleDevicePairingSteps(
        pairingResponse: StartPairingResponse?,
        productId: String,
        prefix: String,
        pairingModeType: PairingModeType
    ): MutableList<ParsedPairingStep> {
        val productModel = ProductModelProvider.instance().getByProductIDOrNull(productId)
        val tmp = mutableListOf<ParsedPairingStep>()

        pairingResponse?.let {
            val item = it.steps[0]

            val webLink = getWebLink(item)
            tmp.add(SimplePairingStep(
                    productId,
                    item.instructions,
                    pairingModeType,
                    item.title,
                    item.info,
                    webLink,
                    item.order,
                    item.id
            ))
        }

        for (stepOrder in 2..6) {
            // If this is a hub, skip step 4 (enable Bluetooth on the camera step)
            if (productId == V03_HUB_PRODUCT_ADDRESS.substringAfterLast(":") &&
                stepOrder == 4) {
                continue
            }
            tmp.add(BleGenericPairingStep(stepOrder,
                    productId,
                    productModel?.shortName ?: "",
                    prefix))
        }
        tmp.add(BleGenericPairingStep(
                7,
                productId,
                productModel?.shortName ?: "",
                prefix,
                getInputList(pairingResponse?.inputs ?: emptyList()))
        )
        return tmp
    }

    private fun getWebLink(item: PairingStep) = if (!item.linkUrl.isNullOrBlank() && !item.linkText.isNullOrBlank()) {
        WebLink(item.linkText, item.linkUrl)
    } else {
        null
    }

    private fun getInputList(inputs: List<PairingInput>) = inputs.map {
        PairingStepInput(
            PairingStepInputType.valueOf(it.type),
            it.name,
            it.minlen ?: 0,
            it.maxlen ?: 128,
            it.label ?: "_NONE_", // Hidden inputs don't have a label.
            it.value
        )
    }

    override fun stopPairing() {
        subsystemController.exitPairing() // Do we need to wait for confirmation of this?
    }

    @Suppress("ProtectedInFinal") // Don't want to expose to clients, don't want synthetics created
    protected fun checkTimeout() {
        if (subsystemController.getPairingMode() == PairingSubsystem.PAIRINGMODE_IDLE) {
            onlyIfView {
                it.pairingTimedOut(subsystemController.hasPairedDevices())
            }
        }
    }

    override fun clearView() {
        super.clearView()
        listenerRegistration = Listeners.clear(listenerRegistration)
    }

    companion object {
        /**
         * The prefix of smart plug devices.
         */
        const val PLUG_NAME_PREFIX_FILTER = "Iris_Plug"

        /**
         * The prefix of camera devices.
         */
        const val CAMERA_NAME_PREFIX_FILTER = "Iris_Cam"

        /**
         * The prefix of hub devices.
         */
        const val HUB_NAME_PREFIX_FILTER = "Iris_Hub"
    }
}
