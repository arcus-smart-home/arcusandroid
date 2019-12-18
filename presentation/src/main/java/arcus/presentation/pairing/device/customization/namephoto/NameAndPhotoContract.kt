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
package arcus.presentation.pairing.device.customization.namephoto

import arcus.cornea.presenter.BasePresenterContract
import com.iris.client.model.DeviceModel

interface NameAndPhotoView {
    /**
     * Called when we have loaded the device model and know its name
     *
     * @param name is the name of the device
     */
    fun showDevice(name: String, model: DeviceModel, address: String)

    /**
     * Called when the world burns and we should know
     *
     * @param throwable the error
     */
    fun showError(throwable: Throwable)
}

interface NameAndPhotoPresenter : BasePresenterContract<NameAndPhotoView> {
    /**
     * Sets the name of the device. This is called on every character update
     *
     * @param name is the name of the device
     */
    fun setName(name: String)

    /**
     * Loads the paired device and tells the presenter its address
     *
     * @param pairingDeviceAddress address of the paired device to be named
     */
    fun loadDeviceFrom(pairedDeviceAddress: String)
}
