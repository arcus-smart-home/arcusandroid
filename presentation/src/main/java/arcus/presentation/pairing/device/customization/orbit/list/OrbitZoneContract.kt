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
package arcus.presentation.pairing.device.customization.orbit.list

import android.os.Parcel
import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract

interface OrbitZoneView {

    /**
     * Called when we know the list of zones available for the devices
     */
    fun onZonesLoaded(zones: List<IrrigationZone>)


    /**
     * Called when a Exception is thrown by the Presenter for the View to handle
     *
     *  @param throwable Throwable Exception
     */
    fun showError(throwable: Throwable)
}

interface OrbitZonePresenter : BasePresenterContract<OrbitZoneView> {
    /**
     * Loads the pairing device from the cache
     *
     * @param pairingDeviceAddress address of the device to be customized.
     */
    fun loadFromPairingDevice(pairedDeviceAddress: String)

    /**
     * Loads the zones for this device calling the appropriate view callback when finished
     */
    fun loadZones()
}

/**
 * @param zoneInstanceId Zone instance such as z1, z3, etc.
 * @param name The custom name a user has set for the zone
 * @param zone The "Zone X" string such as "Zone 1", "Zone 3" etc.
 * @param wateringTimeInMinutes The watering time in minutes or 1 (if value is not set)
 * @param zoneNumber The zone number as an integer
 */
data class IrrigationZone(
        val zoneInstanceId: String = "z1",
        val name: String = "",
        val zone: String = "Zone 1",
        val wateringTimeInMinutes: Int = 1,
        val zoneNumber: Int = 1) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(zoneInstanceId)
        writeString(name)
        writeString(zone)
        writeInt(wateringTimeInMinutes)
        writeInt(zoneNumber)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<IrrigationZone> = object : Parcelable.Creator<IrrigationZone> {
            override fun createFromParcel(source: Parcel): IrrigationZone =
                IrrigationZone(
                    source
                )
            override fun newArray(size: Int): Array<IrrigationZone?> = arrayOfNulls(size)
        }
    }
}