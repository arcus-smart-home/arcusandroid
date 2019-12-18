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

import android.os.Looper
import arcus.cornea.CorneaClientFactory
import arcus.cornea.helpers.chainNonNull
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.HubModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.provider.ProductModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.ScheduledExecutor
import arcus.presentation.pairing.CUSTOMIZATION_COMPLETE
import arcus.presentation.pairing.device.searching.DevicePairingState
import com.iris.capability.util.Addresses
import com.iris.client.capability.DeviceAdvanced
import com.iris.client.capability.PairingDevice
import com.iris.client.capability.Product
import com.iris.client.event.ClientFuture
import com.iris.client.model.DeviceModel
import com.iris.client.model.HubModel
import com.iris.client.model.ModelDeletedEvent
import com.iris.client.model.PairingDeviceModel
import java.util.UUID

class KitDeviceActivationPresenterImpl(
    private val screenDensity: String,
    private val srsBaseUrl: String = CorneaClientFactory.getClient().sessionInfo?.staticResourceBaseUrl ?: "",
    private val productModelProvider: ProductModelProvider = ProductModelProvider.instance(),
    private val hubModelProvider: HubModelProvider = HubModelProvider.instance(),
    private val scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!),
    pairingSubsystemController: PairingSubsystemController = PairingSubsystemControllerImpl,
    pairingDeviceModelProvider: PairingDeviceModelProvider = PairingDeviceModelProvider.instance(),
    deviceModelProvider: DeviceModelProvider = DeviceModelProvider.instance()
) : BaseKitDevicePresenter<KitDeviceActivationView>(
    pairingSubsystemController,
    pairingDeviceModelProvider,
    deviceModelProvider
),
    KitDeviceActivationPresenter {
    private var pairingStoreListener = Listeners.empty()
    private val kitActivationStatusPresenter = KitActivationStatusPresenterImpl()

    override fun setView(view: KitDeviceActivationView) {
        super.setView(view)
        kitActivationStatusPresenter.setView(view)
        pairingStoreListener = pairingDeviceModelProvider
            .store
            .addListener {
                if (it !is ModelDeletedEvent) {
                    // This method gets spammed on updates, so we'll debounce these a touch
                    scheduledExecutor.clearExecutor()
                    scheduledExecutor.executeDelayed(250) {
                        loadKitItems()
                    }
                }
            }
    }

    override fun clearView() {
        super.clearView()
        kitActivationStatusPresenter.clearView()
        Listeners.clear(pairingStoreListener)
    }

    override fun getDeviceActivationStatus() {
        kitActivationStatusPresenter.getDeviceActivationStatus()
    }

    override fun dismissActivatedKitItems() {
        getKittedDevices() // Get the items the platform says are in a kit
            .chainNonNull {
                getAdditionalMetaDevices(it) // And all the devices it needs to do some mapping
            }
            .onSuccess { metaDevices ->
                metaDevices
                    ?.second
                    ?.forEach { pairingDevice ->
                        if (PairingDevice.PAIRINGPHASE_PAIRED == pairingDevice.pairingPhase) {
                            pairingDevice.dismiss()
                        }
                    }
            }
    }

    /**
     * Easy Bake Oven Instructions:
     *      Load Product Catalog
     *      Get Kit Items from Pairing Subsystem if not loaded
     *      Get Pairing Device Models from Pairing Subsystem if not loaded
     *      Get Device Models from Platform if not loaded
     *
     *      Map the above into: Missing, Paired (unknown state - pairdevs), Paired + Customized (no pair dev only device model) by:
     *      for each kit item
     *       for each pairing device
     *           if proto addresses match
     *           put into unknown pairdev models bucket (with product address from get kit info....)
     *           continue
     *
     *      for kit items not found in above
     *       for all devices
     *           if proto address contains devices proto ID
     *           put into customized device models bucket
     *           continue
     *
     *      missing = any devices not found in above blocks
     *
     *      Get the original list of kit items (for sorting)
     *
     *      ... sometime in the future ...
     *
     *      create HubKitDeviceModel
     *
     *      for each unknown pairdev model
     *       if has device address
     *           if customized already
     *               create CustomizedKitDevice
     *           else if device is in error
     *               create KitDeviceInError
     *           else
     *               create KitDeviceNeedsCustomization
     *        else
     *           create needs activating device
     *
     *      for each customized device model
     *       create CustomizedKitDevice
     *
     *      for each missing device
     *       create KitDeviceInError
     *
     *      (always) sort list based on the order from the getKitInfo
     *
     *      if all are customized
     *       tell view to show success
     *      else
     *       give the models created above to view for rendering
     */
    override fun loadKitItems() {
        productModelProvider
            .load() // Make sure this guy is loaded as we'll need to do .get()'s on it later.
            .chain { _ ->
                getKittedDevices()
            }
            .chainNonNull { kittedDevices ->
                getAdditionalMetaDevices(kittedDevices)
            }
            .transformNonNull { metaDevices ->
                mapMetaDevicesToStartingGridDevices(metaDevices)
            }
            .chain { metaDevices ->
                if (metaDevices == null) {
                    throw RuntimeException("Meta Devices were null - cannot continue.")
                } else {
                    getHubModelWithMetaDevices(metaDevices)
                }
            }
            .chainNonNull { parsedAndHubPair ->
                getKittedDevices()
                    .transform { kittedDevices ->
                        val kitDevicesOriginal = kittedDevices ?: emptyMap()
                        Triple(parsedAndHubPair.first, parsedAndHubPair.second, kitDevicesOriginal)
                    }
            }
            .transformNonNull { (metaDevices, hubModel, originalKitDevices) ->
                val (missingDevices, pairingDevices, deviceModels) = metaDevices

                // Combine all devices into one list
                val unsortedDevices = mapPairingDevicesToKitDevices(pairingDevices)
                    .plus(mapDevicesToCustomizedKitDevices(deviceModels))
                    .plus(mapMissingDevicesToErrors(missingDevices))
                    .associateBy(
                        { it.protocolId },
                        { it }
                    )
                val allDevices = originalKitDevices.keys.mapNotNull { potoAddr ->
                    unsortedDevices[potoAddr.substringAfterLast(":")]
                }

                val rainConfetti = allDevices.all { it is KitDeviceCustomized }
                val showSearching = allDevices.any { it is KitDeviceNotActivated }
                val hubKitDevice = HubDeviceCustomized(
                    hubModel.name ?: SMART_HUB,
                    "", // We're faking it - doesn't matter :)
                    generateUrlForScreen(HUB_SCREEN, true),
                    showSearching
                )

                // take the hub first, then add all devices to a new list for returning.
                val devices = listOf(hubKitDevice).plus(allDevices)
                devices to rainConfetti
            }
            .onSuccess { devicesAndAllActivated ->
                onMainWithView {
                    onKitItemsLoaded(devicesAndAllActivated.first, devicesAndAllActivated.second)
                }
            }
    }

    /**
     * Loads the hub model (so we can get the name of it....) and returns that with the passed in
     * [initial] meta devices.
     *
     * @param initial MAPPED MetaDevices from [mapMetaDevicesToStartingGridDevices]
     * @return Future of [Pair]<MetaDevices, HubModel>
     */
    private fun getHubModelWithMetaDevices(
        initial: ParsedMetaDevices
    ): ClientFuture<Pair<ParsedMetaDevices, HubModel>>? = hubModelProvider
        .load()
        .transform {
            val hubModel = hubModelProvider.hubModel
            if (hubModel == null) {
                throw RuntimeException("Hub Model was unable to be loaded.")
            } else {
                Pair(initial, hubModel)
            }
        }

    /**
     * Takes a list of pairing devices and converts them into a list of kit devices which could be
     * one of:
     *
     * KitDeviceInError
     * KitDeviceCustomized
     * KitDeviceNotCustomized
     *
     * Depending on the [PairingDeviceModel.getPairingState] and if the
     * [PairingDeviceModel.getCustomizations] contains the [CUSTOMIZATION_COMPLETE] tag.
     *
     * If there is a device model for the pairing dev - the attributes (where appropriate) from
     * the device model will be used instead of any default values from the pairing device / product
     * model.
     *
     * The returning list is UNSORTED
     */
    private fun mapPairingDevicesToKitDevices(
        pairingDevices: List<PairDevPair>
    ): List<KitDevice> = pairingDevices
        .map { (productAddress, pairingDevice) ->
            val product = productModelProvider
                .getModel(productAddress)
                .load()
                .get()
            val sortName = product.shortName ?: UNKNOWN
            val protocolId = pairingDevice.protocolAddress.protocolId()

            if (pairingDevice.deviceAddress != null) {
                val pairingDeviceAddress = pairingDevice.address ?: UNKNOWN
                val isMispaired = when (DevicePairingState.valueOf(pairingDevice.pairingState)) {
                    DevicePairingState.MISCONFIGURED,
                    DevicePairingState.MISPAIRED -> true
                    else -> false
                }

                if (isMispaired) {
                    KitDeviceInError(
                        product.shortName,
                        sortName,
                        protocolId,
                        product.address,
                        pairingDeviceAddress
                    )
                } else {
                    // Get the device models name
                    // If that's null, get the product.shortName
                    // If that's null!! set to "UNKNOWN"
                    val deviceName = deviceModelProvider
                        .getModel(pairingDevice.deviceAddress)
                        .load()
                        .get()
                        .name ?: product.shortName ?: UNKNOWN

                    if (pairingDevice.isCustomized()) {
                        // The device is customized.
                        KitDeviceCustomized(
                            deviceName,
                            sortName,
                            protocolId,
                            product.address,
                            pairingDeviceAddress,
                            generateUrlForScreen(product.screen, true)
                        )
                    } else {
                        // Get to doing customizations!
                        KitDeviceNotCustomized(
                            deviceName,
                            sortName,
                            protocolId,
                            product.address,
                            pairingDeviceAddress,
                            CUSTOMIZE,
                            generateUrlForScreen(product.screen, true)
                        )
                    }
                }
            } else { // No device model address - Still needs activating
                KitDeviceNotActivated(
                    product.shortName ?: UNKNOWN,
                    sortName,
                    protocolId,
                    product.address,
                    pairingDevice.protocolAddress,
                    getPlugItInOrPullTabFromScreen(product.screen),
                    generateUrlForScreen(product.screen, false)
                )
            }
        }

    private fun mapDevicesToCustomizedKitDevices(
        deviceModels: List<DeviceModel>
    ): List<KitDevice> = deviceModels.map {
        val productAddress = Addresses.toServiceAddress(Product.NAMESPACE) + it.productId
        val product = productModelProvider
            .getModel(productAddress)
            .load()
            .get()

        KitDeviceCustomized(
            it.name ?: product.shortName ?: UNKNOWN,
            product.shortName ?: UNKNOWN,
            (it[DeviceAdvanced.ATTR_PROTOCOLID] as String?).protocolId(),
            product.address,
            it.address,
            generateUrlForScreen(product.screen, true)
        )
    }

    private fun mapMissingDevicesToErrors(
        missing: Map<ProtocolAddress, ProductAddress>
    ) = missing.map {
        val product = productModelProvider
            .getModel(it.value)
            .load()
            .get()

        KitDeviceInError(
            product.shortName ?: ERROR_DESCRIPTION,
            product.shortName ?: ERROR_DESCRIPTION,
            it.key.protocolId(),
            it.value,
            UUID.randomUUID().toString(), // Let's fake it
            ERROR_DESCRIPTION
        )
    }

    private fun getPlugItInOrPullTabFromScreen(screen: String?): String {
        val deviceScreen = screen ?: "unknown"

        return if (deviceScreen.equals(PLUG_IT_IN_SCREEN, true)) {
            PLUG_IT_IN
        } else {
            PULL_TAB
        }
    }

    private fun generateUrlForScreen(screen: String?, activated: Boolean): String {
        val safeScreen = screen?.toLowerCase()?.replace(MULTI_SPACES, "") ?: "unknown"
        val prefix = if (activated) "" else "un"
        return URL_FORMAT.format(srsBaseUrl, safeScreen, prefix, screenDensity)
    }

    private fun String?.protocolId(): String = this?.substringAfterLast(":") ?: "_NULL_"

    companion object {
        private val MULTI_SPACES = "\\s+".toRegex()

        private const val PLUG_IT_IN_SCREEN = "switch"

        private const val HUB_SCREEN = "hub"
        private const val SMART_HUB = "Smart Hub"
        private const val PLUG_IT_IN = "Plug it in"
        private const val PULL_TAB = "Pull Tab"
        private const val CUSTOMIZE = "Customize"
        private const val UNKNOWN = "Unknown"
        private const val ERROR_DESCRIPTION = "Error"

        private const val URL_FORMAT = "%s/o/dtypes/%s/%sactivated_small-and-%s.png"
    }
}
