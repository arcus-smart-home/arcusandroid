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

import arcus.cornea.presenter.BasePresenterContract

sealed class KitDevice {
    abstract val name: String
    abstract val sortName: String
    abstract val protocolId: String
    abstract val productAddress: String
    abstract val pairingDeviceAddress: String
}

data class HubDeviceCustomized(
    override val name: String,
    override val productAddress: String,
    val imageUrl: String,
    val allConfigured: Boolean,
    override val sortName: String = "HUB_1",
    override val protocolId: String = "HUB_1",
    override val pairingDeviceAddress: String = "HUB_1"
) : KitDevice()

data class KitDeviceCustomized(
    override val name: String,
    override val sortName: String,
    override val protocolId: String,
    override val productAddress: String,
    override val pairingDeviceAddress: String,
    val imageUrl: String
) : KitDevice()

data class KitDeviceNotCustomized(
    override val name: String,
    override val sortName: String,
    override val protocolId: String,
    override val productAddress: String,
    override val pairingDeviceAddress: String,
    val description: String,
    val imageUrl: String
) : KitDevice()

data class KitDeviceNotActivated(
    override val name: String,
    override val sortName: String,
    override val protocolId: String,
    override val productAddress: String,
    override val pairingDeviceAddress: String,
    val description: String,
    val imageUrl: String
) : KitDevice()

data class KitDeviceInError(
    override val name: String,
    override val sortName: String,
    override val protocolId: String,
    override val productAddress: String,
    override val pairingDeviceAddress: String,
    val description: String = "Improperly Paired"
) : KitDevice()

interface KitDeviceActivationView : KitActivationStatusView {
    /**
     * Called when the initial item list is loaded
     */
    fun onKitItemsLoaded(items: List<KitDevice>, allConfigured: Boolean)
}

interface KitDeviceActivationPresenter :
    BaseKitActivationStatusPresenter, BasePresenterContract<KitDeviceActivationView> {
    /**
     * Dismisses any configured kit items.
     */
    fun dismissActivatedKitItems()

    /**
     * Loads the kit items for this hub.
     */
    fun loadKitItems()
}
