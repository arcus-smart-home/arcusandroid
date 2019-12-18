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
package arcus.presentation.pairing.device.remove.force

import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.pairing.device.remove.DeviceRemovalStep

interface ForceRemoveDeviceView {
    /**
     * Called when force removal of the given device succeeds
     */
    fun onForceRemoveSuccess()

    /**
     * Called when force removal of the given device fails
     */
    fun onForceRemoveFailed()

    /**
     * Called when loading the removal steps has failed
     */
    fun onRetryRemoveFailed()

    /**
     * Called when the removal steps have been successfully received, and we can transition to the removal screen
     */
    fun onRetryRemoveSuccess(steps: List<DeviceRemovalStep>)
}

interface ForceRemoveDevicePresenter : BasePresenterContract<ForceRemoveDeviceView> {
    /**
     * Attempts a retry to remove the device
     *
     * @param pairingDeviceAddress address of the device to be removed
      */
    fun retryRemove(pairingDeviceAddress: String)

    /**
     * Attempts to force remove the device
     *
     * @param pairingDeviceAddress address of the device to be force removed
      */
    fun forceRemove(pairingDeviceAddress: String)
}
