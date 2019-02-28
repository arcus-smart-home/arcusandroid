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
package arcus.app.pairing.device.steps.bledevice

import android.content.Context
import arcus.presentation.ble.BleConnector

/**
 * Used by screens that require a BLE connection whos functionality may be split
 * across multiple screens.  I.e. one screen connects while the next screen gets WiFi
 * information while one screen scans for devices (etc!)
 *
 * This way they all can share the same Connector and know when it's appropriate to start/stop
 * scanning.
 */
interface BleConnected {
    fun setBleConnector(bleConnector: BleConnector<Context>)
}