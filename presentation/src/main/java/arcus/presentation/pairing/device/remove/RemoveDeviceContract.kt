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
package arcus.presentation.pairing.device.remove

import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import kotlinx.android.parcel.Parcelize

interface RemoveDeviceView {

    /**
     * Called when removal failed
     */
    fun onRemoveFailed()

    /**
     * Called when we have received the removal steps from the platform and can proceed to show
     * the user how to remove the device
     */
    fun onRemovalStepsLoaded(steps: List<DeviceRemovalStep>)

    /**
     * Called when we the mispaired device is a Hue device so we can update the view with
     * the appropriate text
     */
    fun onHueDeviceMispaired(shortName: String)
}

interface RemoveDevicePresenter : BasePresenterContract<RemoveDeviceView> {
    /**
     * Used to determine if the device is a Hue device
     * Returns 'true' when the PairingSubsystemController tells us the device is Hue
     */
    fun checkForMispairedHue(pairingDeviceAddress: String)

    /**
     * Used to load the pairing device and gets the removal steps
     * Invokes the onRemovalStepsLoaded() method when the steps have been fetched and parsed
     */
    fun removePairingDevice(pairingDeviceAddress: String)
}

@Parcelize
data class DeviceRemovalStep(
    val id: String,
    val instructions: List<String> = emptyList(),
    val order: Int = 1,
    val title: String? = null
) : Parcelable
