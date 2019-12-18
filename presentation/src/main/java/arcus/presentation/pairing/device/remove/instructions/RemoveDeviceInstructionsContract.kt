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
package arcus.presentation.pairing.device.remove.instructions

import arcus.cornea.presenter.BasePresenterContract

interface RemoveDeviceInstructionsView {

    /**
     * Called when the pairing device model is deleted from the cache
     */
    fun onDeviceRemoved()

    /**
     * Called when the removal fails
     * This is based on a manual trigger, using a 30 second timeout.
     */
    fun onDeviceRemoveFailed()
}

interface RemoveDeviceInstructionsPresenter : BasePresenterContract<RemoveDeviceInstructionsView> {
    /**
     * Used to load the pairing device to listen for it's deletion from the world
     */
    fun loadDeviceFromPairingAddress(pairingDeviceAddress: String)
}
