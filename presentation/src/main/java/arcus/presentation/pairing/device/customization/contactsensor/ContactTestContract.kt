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
package arcus.presentation.pairing.device.customization.contactsensor

import arcus.cornea.presenter.BasePresenterContract

interface ContactTestView {
    /**
     * Called to update the view with the state of the contact sensor (Open/Closed)
     *
     * @param state The (Open/Closed) state of the sensor
     */
    fun onContactStateUpdated(state: String)

    /**
     * Called when an Exception is thrown by the View to handle
     *
     * @param error Throwable Exception
     */
    fun onError(error: Throwable)
}

interface ContactTestPresenter : BasePresenterContract<ContactTestView> {
    /**
     * Loads the pairing device and gets contact type, then calls
     * the UI with this information
     *
     * @param pairingDeviceAddress address of the device to be customized.
     */
    fun loadFromPairingDevice(pairedDeviceAddress: String)
}
