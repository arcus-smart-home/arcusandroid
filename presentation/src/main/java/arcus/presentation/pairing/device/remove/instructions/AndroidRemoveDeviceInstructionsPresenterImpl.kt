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
package arcus.presentation.pairing.device.remove.instructions

import android.os.Handler
import android.os.Looper
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.cornea.utils.Listeners
import com.iris.client.model.ModelDeletedEvent
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

class AndroidRemoveDeviceInstructionsPresenterImpl(
    private val controller: PairingSubsystemController = PairingSubsystemControllerImpl
) : RemoveDeviceInstructionsPresenter,
        KBasePresenter<RemoveDeviceInstructionsView>() {
    private var storeListener = Listeners.empty()
    private val waitingOnDeviceRemoval = AtomicBoolean(false)
    private val handler = Handler(Looper.myLooper())
    private val removeDeviceTimeout = {
        if (waitingOnDeviceRemoval.compareAndSet(true, false)) {
            onlyIfView {
                it.onDeviceRemoveFailed()
            }
        }
    }

    override fun loadDeviceFromPairingAddress(pairingDeviceAddress: String) {
        if (!controller.getsPairedDeviceAddresses().contains(pairingDeviceAddress)) {
            onlyIfView {
                it.onDeviceRemoved()
            }
        } else {
            waitingOnDeviceRemoval.set(true)
            handler.postDelayed(removeDeviceTimeout, REMOVE_DEVICE_TIMEOUT)
            Listeners.clear(storeListener)
            storeListener = PairingDeviceModelProvider
                    .instance()
                    .store
                    .addListener(ModelDeletedEvent::class.java, Listeners.runOnUiThread {
                        if (it.model.address == pairingDeviceAddress) {
                            waitingOnDeviceRemoval.set(false)
                            handler.removeCallbacksAndMessages(null)
                            onlyIfView {
                                it.onDeviceRemoved()
                            }
                        }
                    })
        }
    }

    override fun clearView() {
        super.clearView()
        handler.removeCallbacksAndMessages(null)
        Listeners.clear(storeListener)
    }

    companion object {
        private val REMOVE_DEVICE_TIMEOUT = TimeUnit.MINUTES.toMillis(10)
    }
}
