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
package arcus.presentation.pairing.device.steps.bledevice.bleconnect

import arcus.presentation.pairing.device.steps.PairingStepInput
import arcus.presentation.pairing.device.steps.bledevice.BleConnectedPresenter
import arcus.presentation.pairing.device.steps.bledevice.BleConnectedView
import arcus.presentation.pairing.device.steps.bledevice.IpcdConnectionStatus
import arcus.presentation.pairing.device.steps.bledevice.WiFiConnectionStatus

interface BleConnectView : BleConnectedView {
    fun onWiFiStatusChange(status: WiFiConnectionStatus)
    fun onIpcdStatusChange(status: IpcdConnectionStatus)
    fun onHubPairEvent(hubId: String)
    fun onHubPairError(error: String, hubId: String)
    fun onHubPairTimeout()

    fun onWiFiSSIDNotUpdatedError()
    fun onWiFiSSIDNotUpdatedSuccess(updatedTo: String)
}

interface BleConnectPresenter<T> : BleConnectedPresenter<BleConnectView, T> {
    fun setDevicePrefix(prefix: String?)

    fun updateWiFiCredentials(pass: String, network: String, securityType: String, isReconnect: Boolean)

    fun registerIPCD(inputs: List<PairingStepInput>)

    fun registerHub()

    fun cancelIPCDRegistration()

    fun cancelHubRegistration()
}
