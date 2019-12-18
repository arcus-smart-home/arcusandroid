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
package arcus.presentation.pairing.device.customization.halo.statecounty

import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HaloStateAndCode(
    val state: String,
    val sameCode: String,
    val isPersonsPlace: Boolean = false
) : Parcelable

@Parcelize
data class HaloCounty(
    val county: String,
    val isPersonsCounty: Boolean = false
) : Parcelable

interface HaloStateCountySelectView {
    /**
     * Called when the list of available states is ready
     *
     * @param states list of states with same code
     */
    fun onStatesLoaded(states: List<HaloStateAndCode>)

    /**
     * Called when the states fail to load.
     */
    fun onStatesFailedToLoad()

    /**
     * Called when the list of available counties is ready
     *
     * @param counties County list for the selected state
     */
    fun onCountiesLoaded(counties: List<HaloCounty>)

    /**
     * Called when the counties failed to load.
     */
    fun onCountiesFailedToLoad()

    /**
     * Called when the selection was saved successfully
     */
    fun onSelectionSaved()

    /**
     * Called when the selection cannot be saved
     */
    fun onSelectionSaveFailed()
}

interface HaloStateCountySelectPresenter : BasePresenterContract<HaloStateCountySelectView> {
    /**
     * Loads the device based on the pairing devices device address attribute
     *
     * @param address Pairing device address
     */
    fun loadFromPairingDevice(address: String)

    /**
     * Loads the device based on the device address
     *
     * @param address Device address
     */
    fun loadFromDeviceAddress(address: String)

    /**
     * Loads the states (using SAME codes) for the UI to render
     */
    fun loadStates()

    /**
     * Loads the counties for the selected state same code provided
     *
     * @param state The state to load counties for
     */
    fun loadCounties(state: HaloStateAndCode)

    /**
     * Calls NWS service to get the SAME code & sets that value on the device.
     */
    fun setSelectedStateAndCounty(state: HaloStateAndCode, county: String)
}
