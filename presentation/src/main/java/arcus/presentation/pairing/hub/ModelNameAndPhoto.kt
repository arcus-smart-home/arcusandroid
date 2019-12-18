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

import arcus.cornea.presenter.BasePresenterContract

interface ModelNameAndPhotoView {
    /**
     * Called when we have loaded the device model and know its name
     *
     * @param name is the name of the device
     */
    fun showName(name: String, placeId: String, deviceId: String)

    /**
     * Called when the name was saved successfully
     */
    fun saveSuccessful()

    /**
     * Called when the world burns and we should know that the save was not successful.
     */
    fun saveUnsuccessful()
}

interface ModelNameAndPhotoPresenter : BasePresenterContract<ModelNameAndPhotoView> {
    /**
     * Loads the device name for [address]
     */
    fun loadDeviceNameForAddress(address: String)

    /**
     * Loads the hub name for the current place
     */
    fun loadHubName()

    /**
     * Sets the name of the device for the device at [address]
     *
     * @param name is the name of the device
     * @param address the device address
     */
    fun setNameForDeviceAddress(name: String, address: String)

    /**
     * Sets the name of the device for the hub, if present on the place.
     *
     * @param name is the name of the device
     */
    fun setNameForHub(name: String)

    // TODO: Add a method that checks for the existance of a custom image
    // The above is not availble now since the image checking utilies are in the app
    // and listing those here would create a circular dependency.
    // Once this is done, the view's [showName] method won't need place/device id's
}
