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
package arcus.presentation.pairing.device.customization.contactsensor

import arcus.cornea.helpers.chainNonNull
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.capability.Contact
import com.iris.client.model.DeviceModel

class ContactTypePresenterImpl : ContactTypePresenter, KBasePresenter<ContactTypeView>() {

    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }
    private var deviceModel: DeviceModel? = null

    override fun setMode(type: ContactSensorAssignment) {
        deviceModel?.set(Contact.ATTR_USEHINT, type.name)
        deviceModel?.commit()?.onFailure(errorListener)
    }

    override fun loadFromPairingDevice(pairedDeviceAddress: String) {
        PairingDeviceModelProvider
            .instance()
            .getModel(pairedDeviceAddress)
            .load()
            .chainNonNull {
                DeviceModelProvider
                    .instance()
                    .getModel(it.deviceAddress ?: "DRIV:dev:")
                    .load()
            }
            .onSuccessMain { device ->
                deviceModel = device
                onlyIfView { presentedView ->
                    val hint = device[Contact.ATTR_USEHINT] as String? ?: ""
                    when (hint) {
                        Contact.USEHINT_DOOR -> {
                            presentedView.onContactTypeLoaded(ContactSensorAssignment.DOOR)
                        }
                        Contact.USEHINT_WINDOW -> {
                            presentedView.onContactTypeLoaded(ContactSensorAssignment.WINDOW)
                        }
                        Contact.USEHINT_OTHER -> {
                            presentedView.onContactTypeLoaded(ContactSensorAssignment.OTHER)
                        }
                        Contact.USEHINT_UNKNOWN -> {
                            presentedView.onContactTypeLoaded(ContactSensorAssignment.DOOR)
                        }
                    }
                }
            }
            .onFailure(errorListener)
    }
}
