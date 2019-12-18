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

enum class GattDescriptor(val uuid: UUID, val canonicalName: String) {
    CLIENT_CONFIGURATION(
        UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"),
        "Client Configuration"
    ),

    UNKNOWN(
        UUID(0, 0),
        "Unknown Descriptor"
    )
    ;

    companion object {
        @JvmStatic
        fun fromUuid(uuid: UUID?): GattDescriptor = values().firstOrNull { it.uuid == uuid } ?: UNKNOWN
    }
}
