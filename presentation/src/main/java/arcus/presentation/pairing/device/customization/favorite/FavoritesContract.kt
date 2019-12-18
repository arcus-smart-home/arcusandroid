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
package arcus.presentation.pairing.device.customization.favorite

import arcus.cornea.presenter.BasePresenterContract

interface FavoritesView {

    /**
     * Called to display the View with the Favorite status
     *
     *  @param isFavorite The Favorite status of the Device
     */
    fun showDevice(isFavorite: Boolean)

    /**
     * Called when a Exception is thrown by the Presenter for the View to handle
     *
     *  @param throwable Throwable Exception
     */
    fun showError(throwable: Throwable)
}

interface FavoritesPresenter : BasePresenterContract<FavoritesView> {

    /**
     * Sets the Device Favorite state
     *
     *  @param isFavorite
     */
    fun favorite(isFavorite: Boolean): Unit

    /**
     * Loads the pairing device and gets the device name and Favorite state, then calls
     * the UI with this information
     *
     * @param pairingDeviceAddress address of the device to be customized.
     */
    fun loadFromPairingDevice(pairedDeviceAddress: String): Unit
}
