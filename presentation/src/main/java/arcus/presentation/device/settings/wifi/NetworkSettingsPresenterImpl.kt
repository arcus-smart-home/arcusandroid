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
package arcus.presentation.device.settings.wifi

import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.utils.Listeners
import com.iris.capability.util.Addresses
import com.iris.client.capability.Product
import com.iris.client.capability.WiFi
import com.iris.client.event.ListenerRegistration

class NetworkSettingsPresenterImpl : NetworkSettingsPresenter, KBasePresenter<NetworkSettingsView>() {

    private var modelListener: ListenerRegistration = Listeners.empty()

    override fun clearView() {
        super.clearView()
        Listeners.clear(modelListener)
    }

    override fun loadFromDeviceAddress(address: String) {
        DeviceModelProvider
            .instance()
            .getModel(address)
            .load()
            .onSuccessMain { model ->
                onlyIfView { view ->
                    val networkName = model[WiFi.ATTR_SSID] as String? ?: ""
                    val rssi = model[WiFi.ATTR_RSSI] as Number? ?: 20
                    val signalLevel = when (rssi.toInt()) {
                        in 0..20 -> 0
                        in 21..40 -> 1
                        in 41..60 -> 2
                        in 61..80 -> 3
                        else -> 4
                    }

                    view.onLoaded(WiFiNetwork(
                        networkName,
                        signalLevel,
                        model.address,
                        Addresses.toServiceAddress(Product.NAMESPACE) + model.productId.orEmpty()
                    ))

                    modelListener = model.addPropertyChangeListener { event ->
                        if (event.propertyName == WiFi.ATTR_SSID) {
                            view.onLoaded(
                                WiFiNetwork(
                                    event.newValue.toString(),
                                    signalLevel,
                                    model.address,
                                    Addresses.toServiceAddress(Product.NAMESPACE) + model.productId.orEmpty()
                                )
                            )
                        }
                    }
                }
            }
    }
}
