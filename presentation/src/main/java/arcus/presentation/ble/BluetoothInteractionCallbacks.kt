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
package arcus.presentation.ble

import java.util.UUID

abstract class BluetoothInteractionCallbacks {
    abstract fun onConnected()

    abstract fun onDisconnected(previouslyConnected: Boolean)

    open fun onReadSuccess(uuid: UUID, value: String) {
        // No-Op
    }

    open fun onReadFailure(uuid: UUID) {
        // No-Op
    }

    open fun onWriteSuccess(uuid: UUID, value: String) {
        // No-Op
    }

    open fun onWriteFailure(uuid: UUID) {
        // No-Op
    }
}
