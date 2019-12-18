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
package arcus.presentation.pairing.device.customization.waterheater

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.capability.AOSmithWaterHeaterController
import com.iris.client.event.Futures

class WaterHeaterModelSerialPresenterImpl :
    WaterHeaterModelSerialPresenter, KBasePresenter<WaterHeaterModelSerialView>() {
    override fun saveModelAndSerialNumbersToPairingDevice(
        address: String,
        model: CharSequence?,
        serial: CharSequence?
    ) {
        PairingDeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .transform {
                it?.deviceAddress ?: throw RuntimeException("Model/Address was null cannot load.")
            }
            .onSuccess { deviceAddress ->
                saveModelAndSerialNumbersToDevice(deviceAddress, model, serial)
            }
            .onFailure(Listeners.runOnUiThread {
                onlyIfView { view ->
                    view.onUnhandledError()
                }
            })
    }

    override fun saveModelAndSerialNumbersToDevice(
        address: String,
        model: CharSequence?,
        serial: CharSequence?
    ) {
        DeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .chain {
                if (it == null) {
                    Futures.failedFuture(RuntimeException("Model was null, cannot save attributes."))
                } else {
                    if (model != null) {
                        it[AOSmithWaterHeaterController.ATTR_MODELNUMBER] = model.toString()
                    }
                    if (serial != null) {
                        it[AOSmithWaterHeaterController.ATTR_SERIALNUMBER] = serial.toString()
                    }

                    it.commit()
                }
            }
            .onSuccess(Listeners.runOnUiThread {
                onlyIfView { view ->
                    view.onSaveSuccess()
                }
            })
            .onFailure(Listeners.runOnUiThread {
                onlyIfView { view ->
                    view.onSaveError()
                }
            })
    }
}
