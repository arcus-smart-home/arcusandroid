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
package arcus.presentation.pairing.device.customization.securitymode

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.subsystem.BaseSubsystemController
import arcus.cornea.subsystem.SubsystemController
import arcus.cornea.subsystem.security.SecurityDeviceConfigController
import arcus.cornea.utils.Listeners
import com.iris.client.capability.SecurityAlarmMode
import com.iris.client.capability.SecuritySubsystem
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel
import com.iris.client.model.SubsystemModel

class SecurityModePresenterImpl(
    val controller: SecurityDeviceConfigController = SecurityDeviceConfigController.instance()
) : SecurityModePresenter, KBasePresenter<SecurityModeView>(), SecurityDeviceConfigController.SelectedDeviceCallback {

    private lateinit var securitySubsystem: SubsystemModel
    private var deviceModel: DeviceModel? = null
    private var listenerRegistration = Listeners.empty()
    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }

    override fun loadFromPairingAddress(pairingDeviceAddress: String) {
        PairingDeviceModelProvider
            .instance()
            .getModel(pairingDeviceAddress)
            .load()
            .chain { pairingDeviceModel ->
                pairingDeviceModel?.deviceAddress?.let {
                    DeviceModelProvider
                        .instance()
                        .getModel(it)
                        .load()
                } ?: Futures.failedFuture(RuntimeException("Cannot load null model. Failed."))
            }
            .onSuccess(Listeners.runOnUiThread {
                deviceModel = it
                securitySubsystem = SubsystemController
                    .instance()
                    .getSubsystemModel(SecuritySubsystem.NAMESPACE)
                    .get()

                val address: String = it.address
                val isSecurityDevice = controller.securitySubsystem?.securityDevices?.contains(address) == true
                val isPartial = BaseSubsystemController
                    .set(securitySubsystem.get(ATTR_PARTIAL_DEVICES) as Collection<*>)
                    .contains(address)
                val isOn = BaseSubsystemController
                    .set(securitySubsystem.get(ATTR_ON_DEVICES) as Collection<*>)
                    .contains(address)

                val mode = if (isSecurityDevice) {
                    if (isPartial && isOn) {
                        SecurityMode.ON_AND_PARTIAL
                    } else if (isPartial) {
                        SecurityMode.PARTIAL
                    } else if (isOn) {
                        SecurityMode.ON
                    } else {
                        SecurityMode.NOT_PARTICIPATING
                    }
                } else {
                    SecurityMode.ON_AND_PARTIAL
                }
                onlyIfView { view ->
                    view.onConfigurationLoaded(mode)
                }
            })
            .onFailure(errorListener)
    }

    override fun setMode(mode: SecurityMode) {
        val securityMode = SecurityDeviceConfigController.Mode.values()[mode.ordinal]

        deviceModel?.run {
            controller.setMode(this.id, securityMode)
            commit()
        }
    }

    override fun updateSelected(name: String?, mode: SecurityDeviceConfigController.Mode?) {
        var securityMode =
            SecurityMode.ON_AND_PARTIAL
        mode?.let {
            securityMode = SecurityMode.values()[mode.ordinal]
        }
        onlyIfView { viewRef ->
            viewRef.onConfigurationLoaded(securityMode)
        }
    }

    override fun setView(view: SecurityModeView) {
        super.setView(view)
        deviceModel?.let {
            listenerRegistration = controller.setSelectedDeviceCallback(it.id, this)
        }
    }

    override fun clearView() {
        super.clearView()
        Listeners.clear(listenerRegistration)
    }

    companion object {
        const val ATTR_ON_DEVICES = SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON
        const val ATTR_PARTIAL_DEVICES = SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL
    }
}
