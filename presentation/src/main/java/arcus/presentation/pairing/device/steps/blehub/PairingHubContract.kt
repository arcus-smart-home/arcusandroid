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
package arcus.presentation.pairing.device.steps.blehub

import arcus.cornea.presenter.BasePresenterContract

enum class HubFirmwareStatus {
    DOWNLOADING, // We know the status complete
    APPLYING // We fake the status complete
    ;
}

interface PairingHubView {
    /**
     * Called when the hub is paired to the place (Registered / Online)
     */
    fun onHubPairEvent()

    /**
     * Called when the hubs firmware status changes
     *
     * @param status The type of status received
     * @param percentComplete the current percent complete, for APPLYING, we fake it so this will always be 0
     */
    fun onHubFirmwareStatusChange(status: HubFirmwareStatus, percentComplete: Int = 0)

    /**
     * Called when the hub fails to pair.
     *
     * @param error the error string?
     */
    fun onHubPairError(error: String)

    /**
     * Called when we have failed to find a hub
     */
    fun onHubPairTimeout()
}

interface PairingHubPresenter : BasePresenterContract<PairingHubView> {
    /**
     * Call to start the hub registration process.
     *
     * @param hubId the Hub Id (ABC-1234) to register
     */
    fun registerHub(hubId: String)

    /**
     * Stop any in progress hub registration calls
     */
    fun cancelHubRegistration()
}
