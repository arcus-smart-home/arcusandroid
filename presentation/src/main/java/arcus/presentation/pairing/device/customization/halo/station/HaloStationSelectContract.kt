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
package arcus.presentation.pairing.device.customization.halo.station

import android.os.Parcel
import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract

data class RadioStation(
    val id: Int,
    val name: String,
    val frequency: String,
    val rssi: Double,
    val selected: Boolean
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString(),
        source.readString(),
        source.readDouble(),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(id)
        writeString(name)
        writeString(frequency)
        writeDouble(rssi)
        writeInt((if (selected) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<RadioStation> = object : Parcelable.Creator<RadioStation> {
            override fun createFromParcel(source: Parcel): RadioStation = RadioStation(source)
            override fun newArray(size: Int): Array<RadioStation?> = arrayOfNulls(size)
        }
    }
}

interface HaloStationSelectView {
    /**
     * Called when a list of stations is ready and there are <= 3 results
     *
     * @param initialStations list of stations - max elements is 3
     */
    fun onStationsFound(initialStations: List<RadioStation>)

    /**
     * Called when a list of stations is ready and there is more than 3 results
     *
     * @param initialStations list of stations - max elements is 3
     * @param additionalStations list of additional stations that can be shown
     */
    fun onStationsFound(initialStations: List<RadioStation>, additionalStations: List<RadioStation>)

    /**
     * Called when no stations are found or an empty list of stations is returned by the device
     */
    fun onNoStationsFound()

    /**
     * Called when the request to scan stations fails
     */
    fun onScanStationsFailed()

    /**
     * Called when the call to set the selected radio station fails
     */
    fun onSetSelectionFailed()

    /**
     * Called when the request to play a station fails so that any "playing" icons can be changed
     * back to "stopped" icons
     */
    fun onPlayStationFailed()

    /**
     * Called when the request to stop the playing station failed
     */
    fun onStopPlayingStationFailed()
}

interface HaloStationSelectPresenter : BasePresenterContract<HaloStationSelectView> {
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
     * Loads the available stations for the device and calls view's method with the stations
     * *Note: If a scan is currently taking place, a new scan request should not start*
     *
     * Stations should be sorted in the following manner:
     *  Selected station first
     *  RSSI signal strength - smallest to largest
     *
     * If no station is currently selected, the first station (strongest signal) will be picked for that default selection
     */
    fun loadRadioStations()

    /**
     * Start a new scan for stations, provided one is not currently in progress, calling the
     * appropriate view method(s) and using the same sorting logic as loadRadioStations()
     */
    fun rescanForStations()

    /**
     * Attempts to set the selected station to [station]
     *
     * @param station the station to set as the selected station
     */
    fun setSelectedStation(station: RadioStation)

    /**
     * Requests that the halo device play the requested song (Dedicated to you Halo...)
     *
     * @param station the Radio Station you'd like to play
     * @param playDuration the duration of the play time, uses 10 seconds if not specified
     */
    fun playStation(station: RadioStation, playDuration: Int = 10)

    /**
     * Requests to stop playing any current stations
     */
    fun stopPlayingStations()
}

