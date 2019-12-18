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
package arcus.presentation.pairing.device.customization.waterheater

import arcus.cornea.presenter.BasePresenterContract

interface WaterHeaterModelSerialView {
    /**
     * Called when the save operation was successful
     */
    fun onSaveSuccess()

    /**
     * Called for any errors that are not handled
     */
    fun onUnhandledError()

    /**
     * Called when there was an error saving the attributes.
     */
    fun onSaveError()
}

interface WaterHeaterModelSerialPresenter : BasePresenterContract<WaterHeaterModelSerialView> {
    /**
     * Saves the [model] and [serial] number to the water heater properties for the specified
     * pairing device.
     *
     * @param adress The pairing device address
     * @param model The optional model number
     * @param serial The optional serial number
     */
    fun saveModelAndSerialNumbersToPairingDevice(
        address: String,
        model: CharSequence?,
        serial: CharSequence?
    )

    /**
     * Saves the [model] and [serial] number to the water heater properties for the specified
     * device.
     *
     * @param address The device address
     * @param model The optional model number
     * @param serial The optional serial number
     */
    fun saveModelAndSerialNumbersToDevice(
        address: String,
        model: CharSequence?,
        serial: CharSequence?
    )
}
