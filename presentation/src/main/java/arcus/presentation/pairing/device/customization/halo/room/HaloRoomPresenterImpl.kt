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
package arcus.presentation.pairing.device.customization.halo.room

import arcus.cornea.device.smokeandco.halo.HaloRoomController
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.event.Futures

class HaloRoomPresenterImpl : HaloRoomContract.Presenter, KBasePresenter<HaloRoomContract.View>() {

    private lateinit var controller: HaloRoomController
    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.onError(error)
        }
    }

    override fun loadFromPairingDevice(pairedDeviceAddress: String) {
        PairingDeviceModelProvider
                .instance()
                .getModel(pairedDeviceAddress)
                .load()
                .chain { pairingDeviceModel ->
                    pairingDeviceModel?.deviceAddress?.let {
                        DeviceModelProvider
                                .instance()
                                .getModel(it)
                                .load()
                    } ?: Futures.failedFuture(RuntimeException("Cannot load null model. Failed."))
                }
                .onSuccess(Listeners.runOnUiThread {
                    controller = HaloRoomController(it.address)
                    onlyIfView { view ->
                        view.onRoomsLoaded(getRoomModels(controller.getRoomNames()))
                    }
                })
                .onFailure(errorListener)
    }

    private fun getRoomModels(rooms: Map<String, String>): List<HaloRoom> {
        return rooms.map {
            HaloRoom(
                it.key,
                it.value
            )
        }.sortedWith(
            compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayName }
        )
    }

    override fun getRooms() {
        val rooms = getRoomModels(controller.getRoomNames())
        onlyIfView { view ->
            view.onRoomsLoaded(rooms)
        }
    }

    override fun setRoom(room: HaloRoom) {
        controller.setRoom(room.displayName)
    }

    override fun getCurrentRoomSelection(): HaloRoom {
        var room = HaloRoom(
            "NONE",
            "None"
        )
        val roomKey = controller.getSelectedRoom()
        val roomValue = controller.getRoomNames()[roomKey]
        roomValue?.let {
            room = HaloRoom(
                roomKey,
                roomValue
            )
        }
        return room
    }
}
