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
package arcus.presentation.pairing.hub.activation

import arcus.cornea.presenter.BasePresenterContract

data class DeviceActivationStatus(
    val needsCustomization: Int = 0,
    val needsActivation: Int = 0,
    val needsAttention: Int = 0,
    val inError: Int = 0
) {
    fun isOk(): Boolean =
        needsActivation == 0 &&
                needsAttention == 0 &&
                needsAttention == 0 &&
                inError == 0
}

interface KitActivationStatusView {
    /**
     * Called when we know if you can know the status of the kit devices.
     */
    fun onDeviceActivationStatusUpdate(status: DeviceActivationStatus)
}

interface BaseKitActivationStatusPresenter {
    /**
     * Attempts to get kit device activation status
     */
    fun getDeviceActivationStatus()
}

interface KitActivationStatusPresenter : BaseKitActivationStatusPresenter,
    BasePresenterContract<KitActivationStatusView>
