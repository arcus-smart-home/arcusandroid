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
package arcus.cornea.device.smokeandco.halo

import arcus.cornea.CorneaClientFactory
import arcus.cornea.helpers.laterAs
import arcus.cornea.helpers.nonNullChain
import arcus.cornea.helpers.nowAs
import arcus.cornea.provider.DeviceModelProvider
import com.iris.client.ClientEvent
import com.iris.client.bean.SameState
import com.iris.client.capability.Halo
import com.iris.client.capability.WeatherRadio
import com.iris.client.event.ClientFuture
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel
import com.iris.client.model.ModelCache
import com.iris.client.service.NwsSameCodeService
import kotlin.properties.Delegates

abstract class HaloBaseController(
    deviceAddress : String,
    modelCache : ModelCache = CorneaClientFactory.getModelCache(),
    deviceModelProvider : DeviceModelProvider = DeviceModelProvider.instance()
) {
    internal var deviceModel by Delegates.notNull<DeviceModel>()
    init {
        val model = modelCache[deviceAddress]
        if (model != null) {
            deviceModel = model as DeviceModel
        } else {
            deviceModelProvider
                .getModel(deviceAddress)
                .load()
                .onSuccess {
                    deviceModel = it
                }
        }
    }

    fun <T> DeviceModel.laterAsHalo(action: (Halo) -> ClientFuture<T>) = laterAs(Halo::class.java, action)

    fun <T> DeviceModel.laterAsWeatherRadio(action: (WeatherRadio) -> ClientFuture<T>) = laterAs(WeatherRadio::class.java, action)

    fun <T> DeviceModel.nowAsHalo(action: (Halo) -> T?) = nowAs(Halo::class.java, action)
}


/**
 * Handles radio selection and playback for a halo device.
 */
interface HaloRadioController {
    /**
     * Gets the playing state of the radio. If the value is null we assume, and return,
     * [WeatherRadio.PLAYINGSTATE_QUIET]
     */
    fun getRadioState(): String

    /**
     * Gets the selected (currently set) station for the radio
     */
    fun getSelectedStation(): Int

    /**
     * Sets the selected radio station to [station] and commits that value to the platform
     */
    fun setSelectedStation(station: Int): ClientFuture<ClientEvent>

    /**
     * Sends the appropriate command to scan a list of the radio stations and parses the results
     * into a list of Triple<Int, String, Double> where:
     * [Triple.first] = id or index (in the response) if that was null / not castable
     * [Triple.second] = Frequency (Mhz) of the station or "" if it was null / not castable
     * [Triple.third] = RSSI of the station or 0.0 if it was null / not castable
     *
     * @return ClientFuture<List<Triple<Int, String, Double>>>
     */
    fun getAvailableStations(): ClientFuture<List<Triple<Int, String, Double>>>

    /**
     * Sends the command to start playing radio station [station] for [seconds].
     *
     * @param station station index to play
     * @param seconds duration to play the station, default value is 30 seconds
     */
    fun playStation(station: Int = getSelectedStation(), seconds: Int = 30): ClientFuture<WeatherRadio.PlayStationResponse>

    /**
     * Sends the command to stop playing a radio station.
     */
    fun stopPlayingStation(): ClientFuture<WeatherRadio.StopPlayingStationResponse>
}


class HaloRadioControllerImpl(
    deviceAddress : String
) : HaloBaseController(deviceAddress), HaloRadioController {
    override fun getRadioState() = deviceModel[WeatherRadio.ATTR_PLAYINGSTATE] as String? ?: WeatherRadio.PLAYINGSTATE_QUIET

    override fun getSelectedStation() = (deviceModel[WeatherRadio.ATTR_STATIONSELECTED] as Number? ?: 0).toInt()

    override fun setSelectedStation(station: Int) = run {
        deviceModel[WeatherRadio.ATTR_STATIONSELECTED] = station
        deviceModel.commit()
    }

    override fun getAvailableStations() = deviceModel.laterAsWeatherRadio { radio ->
        radio
            .scanStations()
            .nonNullChain { response ->
                val stations = response.stations.mapIndexed { index, bag ->
                    Triple( // Question all the things!
                        (bag["id"] as? Number?)?.toInt() ?: index,
                        bag["freq"] as? String? ?: "",
                        bag["rssi"] as? Double? ?: 0.0
                    )
                }.toList()

                Futures.succeededFuture(stations)
            }
    }

    override fun playStation(station: Int, seconds: Int) = deviceModel.laterAsWeatherRadio {
        it.playStation(station, seconds)
    }

    override fun stopPlayingStation() = deviceModel.laterAsWeatherRadio {
        it.stopPlayingStation()
    }
}



/**
 * Handles room selection and retrieval for a halo device.
 */
class HaloRoomController(deviceAddress : String) : HaloBaseController(deviceAddress) {
    /**
     * Gets the currently selected room or [Halo.ROOM_NONE] if one has not been selected.
     */
    fun getSelectedRoom() : String = deviceModel.nowAsHalo { it.room } ?: Halo.ROOM_NONE

    /**
     * Gets the normal mapping of rooms where it's:
     * platform key => display name
     *
     * @see [getRoomNamesInverted] for the inversion of these
     */
    fun getRoomNames() : Map<String, String> = deviceModel.nowAsHalo { it.roomNames } ?: emptyMap()

    /**
     * Gets the inverted mapping of rooms where it's:
     * display name => platform key
     *
     * @see [getRoomNames] for the normal retrieval method of these
     */
    fun getRoomNamesInverted() : Map<String, String> = deviceModel.nowAsHalo {
        it.roomNames?.map { it.value to it.key }?.toMap()
    } ?: emptyMap()

    /**
     * Sets the room to the specified [room] if it's one of the supported room types and commits
     * that value to the platform.
     *
     * If it's not one of the supported types this will return an immediate [Futures.failedFuture]
     */
    fun setRoom(room: String) : ClientFuture<ClientEvent> = deviceModel.laterAsHalo {
        if (it.roomNames.containsValue(room)) {
            deviceModel[Halo.ATTR_ROOM] = getRoomNamesInverted()[room]
            deviceModel.commit()
        } else {
            Futures.failedFuture(RuntimeException("Invalid room selection."))
        }
    }
}


/**
 * Handles radio selection and playback for a halo device.
 */
interface HaloLocationController {
    /**
     * Attempts to get the list of state names as a list of [SameState] beans.
     */
    fun getStateNames(): ClientFuture<List<SameState>>

    /**
     * Gets the list of county names based on the passed in [stateCode].
     */
    fun getCountiesFor(stateCode: String): ClientFuture<List<String>>

    /**
     * Sets the SameCode for the given [stateCode] and [county] on the [WeatherRadio] device.
     *
     * Flow:
     * Get Same code from platform ->
     *  If fail (or response was null)
     *   return failed future
     *  else
     *   set value (from response) on model
     *   return future from commit on device model
     */
    fun setLocationUsing(stateCode: String, county: String): ClientFuture<ClientEvent>
}

class HaloLocationControllerImpl(
    deviceAddress : String
) : HaloBaseController(deviceAddress), HaloLocationController {
    override fun getStateNames() = CorneaClientFactory
        .getService(NwsSameCodeService::class.java)
        .listSameStates()
        .nonNullChain { response ->
            val states = response.sameStates?.map { SameState(it) }?.toList() ?: emptyList()
            Futures.succeededFuture(states)
        }

    override fun getCountiesFor(stateCode: String) = CorneaClientFactory
        .getService(NwsSameCodeService::class.java)
        .listSameCounties(stateCode)
        .nonNullChain { response ->
            Futures.succeededFuture(response.counties ?: emptyList())
        }

    override fun setLocationUsing(stateCode: String, county: String) = CorneaClientFactory
        .getService(NwsSameCodeService::class.java)
        .getSameCode(stateCode, county)
        .nonNullChain { response ->
            deviceModel[WeatherRadio.ATTR_LOCATION] = response.code
            deviceModel.commit()
        }
}