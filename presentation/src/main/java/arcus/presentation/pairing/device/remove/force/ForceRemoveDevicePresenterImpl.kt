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
package arcus.presentation.pairing.device.remove.force

import arcus.cornea.helpers.onFailureMain
import arcus.cornea.helpers.onSuccessMain
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PairingDeviceModelProvider
import arcus.presentation.pairing.device.remove.DeviceRemovalStep
import com.iris.client.bean.PairingStep
import com.iris.client.event.Futures
import org.slf4j.LoggerFactory

class ForceRemoveDevicePresenterImpl : ForceRemoveDevicePresenter, KBasePresenter<ForceRemoveDeviceView>() {
    override fun retryRemove(pairingDeviceAddress: String) {
        PairingDeviceModelProvider
                .instance()
                .getModel(pairingDeviceAddress)
                .load()
                .chain { model ->
                    if (model == null) {
                        Futures.failedFuture(RuntimeException("Model was null, cannot perform action"))
                    } else {
                        model.remove()
                    }
                }.transform { response ->
                    response?.steps?.map {
                        DeviceRemovalStep(
                                PairingStep(it).id ?: "",
                                PairingStep(it).instructions ?: emptyList(),
                                PairingStep(it).order ?: 1,
                                PairingStep(it).title
                        )
                    } ?: emptyList() }
                .onSuccessMain {
                    onlyIfView { view ->
                        view.onRetryRemoveSuccess(it)
                        logger.info("Successfully retried to remove the device.", it)
                    }
                }
                .onFailureMain {
                    onlyIfView { view ->
                        view.onRetryRemoveFailed()
                        logger.error("Could not load the removal steps.", it)
                    }
                }
        }

    override fun forceRemove(pairingDeviceAddress: String) {
        PairingDeviceModelProvider
                .instance()
                .getModel(pairingDeviceAddress)
                .load()
                .chain { model ->
                    if (model == null) {
                        Futures.failedFuture(RuntimeException("Model was null, cannot perform action"))
                    } else {
                        model.forceRemove()
                    }
                }
                .onSuccessMain {
                        onlyIfView { view ->
                            view.onForceRemoveSuccess()
                            logger.info("Successfully force removed the device.", it)
                        }
                    }
                .onFailureMain {
                    onlyIfView { view ->
                        view.onForceRemoveFailed()
                        logger.error("Could not force remove the device.", it)
                    }
                }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ForceRemoveDevicePresenterImpl::class.java)
    }
}
