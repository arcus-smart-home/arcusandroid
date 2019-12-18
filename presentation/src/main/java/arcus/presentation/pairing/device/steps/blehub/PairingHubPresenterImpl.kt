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
package arcus.presentation.pairing.device.steps.blehub

import android.os.Looper
import arcus.cornea.helpers.chainNonNull
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.PlaceModelProvider
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.ModelSource
import arcus.cornea.utils.ScheduledExecutor
import com.iris.client.IrisClientFactory
import com.iris.client.capability.Place
import com.iris.client.exception.ErrorResponseException
import com.iris.client.model.ModelCache
import com.iris.client.model.PlaceModel
import java.util.concurrent.TimeUnit

class PairingHubPresenterImpl(
    private val scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!),
    private val currentPlace: () -> ModelSource<PlaceModel> = {
        PlaceModelProvider.getCurrentPlace()
    },
    private val modelCache: ModelCache = IrisClientFactory.getModelCache()
) : PairingHubPresenter, KBasePresenter<PairingHubView>() {
    @Volatile
    private var hubRegistrationInProgress = false

    override fun registerHub(hubId: String) {
        if (!hubRegistrationInProgress) {
            // Start hub pairing timeout
            scheduledExecutor.executeDelayed(MN_MAX_TIMEOUT) {
                cancelHubRegistration()
                onMainWithView {
                    onHubPairTimeout()
                }
            }

            doRegisterHub(hubId)
        }
    }

    private fun doRegisterHub(hubId: String) {
        hubRegistrationInProgress = true

        currentPlace()
            .load()
            .chainNonNull {
                it.registerHubV2(hubId)
            }
            .onSuccess { event ->
                when (event.state) {
                    Place.RegisterHubV2Response.STATE_REGISTERED,
                    Place.RegisterHubV2Response.STATE_ONLINE -> {
                        cancelHubRegistration()

                        modelCache.addOrUpdate(event.hub)

                        // Update the UI
                        onMainWithView {
                            onHubPairEvent()
                        }
                    }

                    // Clear pairing timeout once we start downloading / applying firmware
                    // If not we could tell the view there is an error, when really it's just taking a while to upgrade the hub
                    Place.RegisterHubV2Response.STATE_DOWNLOADING -> {
                        scheduledExecutor.clearExecutor()
                        onMainWithView {
                            onHubFirmwareStatusChange(HubFirmwareStatus.DOWNLOADING, event.progress)
                        }
                    }

                    Place.RegisterHubV2Response.STATE_APPLYING -> {
                        scheduledExecutor.clearExecutor()
                        onMainWithView {
                            onHubFirmwareStatusChange(HubFirmwareStatus.APPLYING, 0)
                        }
                    }
                }

                if (hubRegistrationInProgress) {
                    scheduledExecutor.executeDelayed(MS_POLLING_INTERVAL) {
                        doRegisterHub(hubId)
                    }
                }
            }
            .onFailure { throwable ->
                cancelHubRegistration()
                val error = if (throwable is RuntimeException) {
                    throwable.message ?: ""
                } else {
                    val exception = throwable as ErrorResponseException
                    exception.code
                }

                onMainWithView {
                    cancelHubRegistration()
                    onHubPairError(error)
                }
            }
    }

    override fun cancelHubRegistration() {
        hubRegistrationInProgress = false
        scheduledExecutor.clearExecutor()
    }

    companion object {
        private val MS_POLLING_INTERVAL = TimeUnit.SECONDS.toMillis(2)
        private val MN_MAX_TIMEOUT = TimeUnit.MINUTES.toMillis(5)
    }
}
