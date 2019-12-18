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

import android.os.Looper
import arcus.cornea.helpers.chainNonNull
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.ScheduledExecutor
import com.iris.client.capability.Contact
import java.util.concurrent.TimeUnit

class ContactTestPresenterImpl(
    private var scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!)
) : ContactTestPresenter, KBasePresenter<ContactTestView>() {

    private var listenerRegistration = Listeners.empty()
    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.onError(error)
        }
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
                val contactState = device as Contact
                val state = if (contactState.contact == Contact.CONTACT_OPENED) {
                    "open-off"
                } else {
                    "closed-off"
                }

                // Set the initial state of the contact sensor
                onMainWithView {
                    onContactStateUpdated(state)
                }

                listenerRegistration = device.addPropertyChangeListener {
                    var initialState = ""
                    var delayedState = ""

                    if (it.propertyName == Contact.ATTR_CONTACT) {
                        when (it.newValue) {
                            Contact.CONTACT_OPENED -> {
                                initialState = "open-on"
                                delayedState = "open-off"
                            }
                            Contact.CONTACT_CLOSED -> {
                                initialState = "closed-on"
                                delayedState = "closed-off"
                            }
                        }

                        onMainWithView {
                            onContactStateUpdated(initialState)
                            scheduledExecutor.executeDelayed(TimeUnit.SECONDS.toMillis(2)) {
                                onContactStateUpdated(delayedState)
                            }
                        }

                        scheduledExecutor.clearExecutor()
                    }
                }
            }
            .onFailure(errorListener)
    }
}
