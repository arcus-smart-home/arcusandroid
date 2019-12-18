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

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel
import java.util.concurrent.atomic.AtomicReference

class NameAndPhotoPresenterImpl : NameAndPhotoPresenter, KBasePresenter<NameAndPhotoView>() {
    private val deviceAddress = AtomicReference<String>("_UNKNOWN_")
    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }
    private var deviceModel: DeviceModel? = null

    override fun setName(name: String) {
        deviceModel?.run {
            setName(name)
            commit()
        }
    }

    override fun loadDeviceFrom(pairedDeviceAddress: String) {
        deviceAddress.set(pairedDeviceAddress)
        PairingDeviceModelProvider
                .instance()
                .getModel(pairedDeviceAddress)
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
                    onlyIfView { view ->
                        view.showDevice(it.name, it, it.address)
                    }
                })
                .onFailure(errorListener)
    }
}
