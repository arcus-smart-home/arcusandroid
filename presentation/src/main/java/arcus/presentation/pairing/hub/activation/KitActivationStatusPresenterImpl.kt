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

import arcus.cornea.helpers.chainNonNull
import arcus.cornea.helpers.transformNonNull
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import arcus.presentation.pairing.device.searching.DevicePairingState
import com.iris.client.model.PairingDeviceModel
import java.util.concurrent.atomic.AtomicBoolean

class KitActivationStatusPresenterImpl(
    pairingSubsystemController: PairingSubsystemController = PairingSubsystemControllerImpl,
    pairingDeviceModelProvider: PairingDeviceModelProvider = PairingDeviceModelProvider.instance(),
    deviceModelProvider: DeviceModelProvider = DeviceModelProvider.instance()
) : BaseKitDevicePresenter<KitActivationStatusView>(
    pairingSubsystemController,
    pairingDeviceModelProvider,
    deviceModelProvider
),
    KitActivationStatusPresenter {
    private val checkingExitStatus = AtomicBoolean(false)

    override fun getDeviceActivationStatus() {
        // This check helps block spamming of exit button
        if (checkingExitStatus.compareAndSet(false, true)) {
            getKittedDevices() // Get the items the platform says are in a kit
                .chainNonNull {
                    getAdditionalMetaDevices(it) // And all the devices it needs to do some mapping
                }
                .transformNonNull {
                    mapMetaDevicesToStartingGridDevices(it) // put the devices into the right buckets
                }
                .transformNonNull { (missing, pairDevPairs, _) ->
                    val groupings = pairDevPairs.groupingBy { (_, pairDev) ->
                        getActivationStatus(pairDev)
                    }.eachCount()

                    DeviceActivationStatus(
                        groupings[NEEDS_CUSTOMIZATION] ?: 0,
                        groupings[NEEDS_ACTIVATION] ?: 0,
                        groupings[NEEDS_ATTENTION] ?: 0,
                        missing.size
                    )
                }
                .onCompletion {
                    checkingExitStatus.set(false)
                }
                .onFailure {
                    onMainWithView {
                        onDeviceActivationStatusUpdate(DeviceActivationStatus()) /* Don't block exit if we fail */
                    }
                }
                .onSuccess { exitStatus ->
                    onMainWithView {
                        onDeviceActivationStatusUpdate(exitStatus)
                    }
                }
        }
    }

    private fun getActivationStatus(pairDev: PairingDeviceModel): String {
        val pairingState = DevicePairingState.valueOf(pairDev.pairingState)

        return when (pairingState) {
            // We're mispaired/configured, need to flag it.
            DevicePairingState.MISCONFIGURED,
            DevicePairingState.MISPAIRED -> {
                NEEDS_ATTENTION
            }
            DevicePairingState.PAIRED -> {
                // We can't check, just keep going with what we have...
                if (pairDev.isCustomized()) {
                    OK
                } else {
                    NEEDS_CUSTOMIZATION
                }
            }
            else -> NEEDS_ACTIVATION
        }
    }

    companion object {
        private const val OK = "OK"
        private const val NEEDS_ATTENTION = "NEEDS_ATTENTION"
        private const val NEEDS_CUSTOMIZATION = "NEEDS_CUSTOMIZATION"
        private const val NEEDS_ACTIVATION = "NEEDS_ACTIVATION"
    }
}
