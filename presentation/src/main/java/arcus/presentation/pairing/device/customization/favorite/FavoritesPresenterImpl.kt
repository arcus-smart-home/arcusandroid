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
package arcus.presentation.pairing.device.customization.favorite

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.cornea.utils.Listeners
import arcus.presentation.pairing.FAVORITE_TAG
import com.iris.client.event.Futures
import com.iris.client.model.DeviceModel

class FavoritesPresenterImpl : FavoritesPresenter, KBasePresenter<FavoritesView>() {

    private val errorListener = Listeners.runOnUiThread<Throwable> { error ->
        onlyIfView { view ->
            view.showError(error)
        }
    }

    private var deviceModel: DeviceModel? = null

    override fun favorite(isFavorite: Boolean) {
        deviceModel?.run {
            if (isFavorite) {
                addTags(FAVORITE_SET).onFailure(errorListener)
            } else {
                removeTags(FAVORITE_SET).onFailure(errorListener)
            }
        }
    }

    override fun loadFromPairingDevice(pairedDeviceAddress: String) {
        PairingDeviceModelProvider
            .instance()
            .getModel(pairedDeviceAddress)
            .load()
            .chain { model ->
                model?.deviceAddress?.let { address ->
                    DeviceModelProvider
                        .instance()
                        .getModel(address)
                        .load()
                    } ?: Futures.failedFuture(RuntimeException("Cannot load null pairing/device model."))
                }
            .onSuccess(Listeners.runOnUiThread { device ->
                deviceModel = device
                onlyIfView { presentedView ->
                    presentedView.showDevice(
                        device.tags?.intersect(FAVORITE_SET)?.isNotEmpty() ?: false
                    )
                }
            })
            .onFailure(errorListener)
    }

    companion object {
        @JvmStatic
        private val FAVORITE_SET = setOf(FAVORITE_TAG)
    }
}
