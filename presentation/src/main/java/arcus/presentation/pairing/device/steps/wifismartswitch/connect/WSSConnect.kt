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
package arcus.presentation.pairing.device.steps.wifismartswitch.connect

import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.pairing.device.steps.PairingStepInput

typealias SwannAPName = String

data class WiFiConnectInformation(
    val ssid: String,
    val password: String,
    val isSecure: Boolean,
    val inputs: List<PairingStepInput>
)

interface WSSConnectView {
    fun onDeviceTakenError()

    fun onResetDeviceError()

    fun onInvalidCredentialsError()

    fun onNoApsFoundError()

    fun onSuccess()
}

interface WSSConnectPresenter<in T> : BasePresenterContract<WSSConnectView> {
    fun parseScanResultsForSwannAPs(scanResults: List<T>?): List<SwannAPName>

    fun startPairing(
        swannAPName: SwannAPName,
        wifiConnectInformation: WiFiConnectInformation
    )
}
