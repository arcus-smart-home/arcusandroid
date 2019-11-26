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
package arcus.app.pairing.device.steps.wifismartswitch.connect

import android.net.wifi.ScanResult
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.subsystem.pairing.PairingSubsystemController
import arcus.cornea.subsystem.pairing.PairingSubsystemControllerImpl
import com.iris.client.model.DeviceModel
import arcus.app.common.machine.State
import arcus.app.pairing.device.steps.wifismartswitch.connect.swannwifi.controller.SwannProvisioningController
import arcus.presentation.pairing.device.steps.wifismartswitch.connect.SwannAPName
import arcus.presentation.pairing.device.steps.wifismartswitch.connect.WSSConnectPresenter
import arcus.presentation.pairing.device.steps.wifismartswitch.connect.WSSConnectView
import arcus.presentation.pairing.device.steps.wifismartswitch.connect.WiFiConnectInformation
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

class AndroidWSSConnectPresenterImpl(
    val controller : PairingSubsystemController = PairingSubsystemControllerImpl
) : WSSConnectPresenter<ScanResult>, KBasePresenter<WSSConnectView>() {
    private val pairedDevice = AtomicBoolean(false)

    private val provisioningListener : SwannProvisioningController.ProvisioningControllerListener =
        object : SwannProvisioningController.ProvisioningControllerListener {
            override fun onStateChange(lastState: State?, currentState: State?) {
                logger.debug("Last State: $lastState, Current State: $currentState")
            }

            override fun onError(state: State?, e: Throwable?) {
                logger.debug("Received error", e)
                if (state is SwannProvisioningController.TerminalFailedState) {
                    when (state.cause) {
                        SwannProvisioningController.TerminalFailedState.Cause.DEVICE_TAKEN -> {
                            onlyIfView { it.onDeviceTakenError() }
                        }
                        SwannProvisioningController.TerminalFailedState.Cause.NOT_FOUND -> {
                            onlyIfView { it.onInvalidCredentialsError() }
                        }
                        else -> {
                            onlyIfView { it.onResetDeviceError() }
                        }
                    }
                } else {
                    onlyIfView { it.onResetDeviceError() }
                }
            }

            override fun onSuccess(deviceModel: DeviceModel?) {
                pairedDevice.set(true)
                callSuccessIfDevicePaired()
            }
        }

    override fun parseScanResultsForSwannAPs(scanResults: List<ScanResult>?): List<SwannAPName> {
        return (scanResults ?: emptyList())
            .filterNot {
                it.SSID.isNullOrEmpty()
            }
            .filter {
                it.SSID.matches(SWANN_AP_FILTER)
            }
            .map {
                it.SSID
            }
    }

    override fun startPairing(
        swannAPName: SwannAPName,
        wifiConnectInformation: WiFiConnectInformation
    ) {
        SwannProvisioningController.setRequestParams(
            wifiConnectInformation
                .inputs
                .map { stepInput -> stepInput.keyName to stepInput.value }
                .toMap()
        )

        SwannProvisioningController.provisionSmartPlug(
            provisioningListener,
            swannAPName,
            wifiConnectInformation.ssid,
            wifiConnectInformation.password
        )
    }

    override fun setView(view: WSSConnectView) {
        super.setView(view)
        callSuccessIfDevicePaired()
    }

    private fun callSuccessIfDevicePaired() {
        if (pairedDevice.get()) {
            onlyIfView { view ->
                controller.exitPairing()
                view.onSuccess()
            }
        }
    }

    companion object {
        private val SWANN_AP_FILTER = ".*Smart.*Plug.[a-zA-Z0-9]{4}".toRegex()
        private val logger = LoggerFactory.getLogger(AndroidWSSConnectPresenterImpl::class.java)
    }
}
