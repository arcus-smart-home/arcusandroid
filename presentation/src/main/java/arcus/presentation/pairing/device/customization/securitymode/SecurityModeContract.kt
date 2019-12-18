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
package arcus.presentation.pairing.device.customization.securitymode

import arcus.cornea.presenter.BasePresenterContract

interface SecurityModeView {

    /**
     * Called when we know the current configuration status of the device
     * Should default to the first selection
     *
     *  @param mode The security mode available
     */
    fun onConfigurationLoaded(mode: SecurityMode)

    /**
     * Called when a everything bad happens and we need a cookie
     *
     *  @param throwable Throwable Exception
     */
    fun showError(throwable: Throwable)
}

interface SecurityModePresenter : BasePresenterContract<SecurityModeView> {

    /**
     * Sets the Device Favorite state
     *
     *  @param pairingDeviceAddress the address of the pairing device.
     */
    fun loadFromPairingAddress(pairingDeviceAddress: String)

    /**
     * Loads the pairing device and gets the device name and Favorite state, then calls
     * the UI with this information
     *
     * @param mode the mode to set the device to
     */
    fun setMode(mode: SecurityMode)
}

enum class SecurityMode(val canonicalName: String) {
    ON("ON_ONLY"),
    PARTIAL("PARTIAL_ONLY"),
    ON_AND_PARTIAL("ON_AND_PARTIAL"),
    NOT_PARTICIPATING("NOT_PARTICIPATING")
}
