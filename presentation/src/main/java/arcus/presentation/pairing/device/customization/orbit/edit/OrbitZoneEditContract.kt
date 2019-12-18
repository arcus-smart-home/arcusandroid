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
package arcus.presentation.pairing.device.customization.orbit.edit

import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import java.util.concurrent.TimeUnit
import kotlinx.android.parcel.Parcelize

@Parcelize
data class IrrigationZoneDetails(
    val zoneName: String,
    val minutes: Int
) : Parcelable {
    fun minutesToHours() = TimeUnit.MINUTES.toHours(minutes.toLong()).toInt()
}

interface OrbitZoneEditView {
    /**
     * Called when then zone information for the current zone is known
     */
    fun onZoneLoaded(details: IrrigationZoneDetails)

    /**
     * Called when the zone save operation fails
     */
    fun onZoneSaveFailure()

    /**
     * Called when the zone save operation fails
     */
    fun onZoneSaveSuccess()

    /**
     * Called when the zone details load operation fails
     */
    fun onZoneLoadingFailure()
}

interface OrbitZoneEditPresenter : BasePresenterContract<OrbitZoneEditView> {
    /**
     * Loads the device based on the pairing devices device address attribute
     *
     * @param address Pairing device address
     * @param zone the zone instance to load details from
     */
    fun loadFromPairingDevice(address: String, zone: String)

    /**
     * Loads the device based on the device address
     *
     * @param address Device address
     * @param zone the zone instance to load details from
     */
    fun loadFromDeviceAddress(address: String, zone: String)

    /**
     * Attempts to save the zone information provided.
     *
     * @param details the irrigation zone information
     * @param zone the zone to save to
     * @param address the pairing device address
     */
    fun saveZoneInformationToPairingDevice(details: IrrigationZoneDetails, zone: String, address: String)

    /**
     * Attempts to save the zone information provided.
     *
     * @param details the irrigation zone information
     * @param zone the zone to save to
     * @param address the device address
     */
    fun saveZoneInformationToDevice(details: IrrigationZoneDetails, zone: String, address: String)
}
