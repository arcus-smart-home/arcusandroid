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
package arcus.presentation.pairing.device.customization.ota

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.LooperExecutor
import com.iris.client.capability.DeviceOta
import kotlin.math.roundToInt
import org.slf4j.LoggerFactory

class OTAUpgradePresenterImpl : OTAUpgradePresenter, KBasePresenter<OTAUpgradeView>() {
    private var listenerReg = Listeners.empty()

    override fun loadFromPairingDeviceAddress(address: String) {
        PairingDeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .transform {
                it?.deviceAddress ?: throw RuntimeException("PairingDevice (or address) was null.")
            }
            .onFailure {
                logger.error("Boom goes the dynamite.", it)
            }
            .onSuccess {
                loadFromDeviceAddress(it)
            }
    }

    override fun loadFromDeviceAddress(address: String) {
        DeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .onSuccess {
                cleanUpListeners()
                listenerReg = it.addPropertyChangeListener(Listeners.filter(
                    DeviceOta.ATTR_PROGRESSPERCENT,
                    {
                        val newValue = (it.newValue as Number? ?: 0).toDouble().roundToInt()
                        handleProgressUpdate(newValue)
                    }
                ))

                val current = (it[DeviceOta.ATTR_PROGRESSPERCENT] as Number? ?: 0).toDouble().roundToInt()
                handleProgressUpdate(current)
            }
    }

    private fun handleProgressUpdate(newValue: Int) {
        onlyIfView { view ->
            LooperExecutor
                .getMainExecutor()
                .execute {
                    view.onProgressUpdate(newValue)
                }
        }
    }

    override fun clearView() {
        super.clearView()
        cleanUpListeners()
    }

    private fun cleanUpListeners() {
        Listeners.clear(listenerReg)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(OTAUpgradePresenterImpl::class.java)
    }
}
