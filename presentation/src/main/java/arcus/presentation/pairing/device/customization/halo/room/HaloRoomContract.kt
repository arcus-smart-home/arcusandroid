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

import arcus.cornea.presenter.BasePresenterContract

class HaloRoomContract private constructor() {
    interface View {
        /**
         * Called when the list of rooms is available
         *
         * @param rooms is a list of rooms to which Halo can be assigned
         */
        fun onRoomsLoaded(rooms: List<HaloRoom>)

        /**
         * Called when an error occurs for the view to handle
         *
         * @param throwable the error
         */
        fun onError(throwable: Throwable)
    }

    interface Presenter : BasePresenterContract<View> {

        /**
         * Fetches the device based on the pairing devices device address attribute
         * @param pairedDeviceAddress address of the paired device
         */
        fun loadFromPairingDevice(pairedDeviceAddress: String)

        /**
         * Gets a list of room options for the view to show
         */
        fun getRooms()

        /**
         * Sets the room for the device
         *
         * @param room the room to which Halo should be assigned
         */
        fun setRoom(room: HaloRoom)

        /**
         * Gets the currently selected room, if any
         */
        fun getCurrentRoomSelection(): HaloRoom
    }
}

data class HaloRoom(val platformKey: String, val displayName: String)
