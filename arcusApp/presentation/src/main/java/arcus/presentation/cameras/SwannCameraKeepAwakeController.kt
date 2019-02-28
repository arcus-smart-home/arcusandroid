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
package arcus.presentation.cameras

import android.os.Looper
import arcus.cornea.CorneaClientFactory
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.ScheduledExecutor
import com.iris.client.IrisClient
import com.iris.client.capability.SwannBatteryCamera
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

class SwannCameraKeepAwakeController
@JvmOverloads
constructor(
    private val deviceAddress: String,
    private val client: IrisClient = CorneaClientFactory.getClient(),
    private val messageInterval: Long = KEEP_AWAKE_INTERVAL,
    private val messageIntervalUnit: TimeUnit = TimeUnit.SECONDS,
    private val keepAwakePerRequest: Int = KEEP_AWAKE_PER_REQUEST,
    private val scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper())
) : KeepAwakeController {
    @Volatile
    private var isCancelled = false

    override fun startKeepAwake() {
        isCancelled = false
        scheduledExecutor.clearExecutor()

        DeviceModelProvider
            .instance()
            .getModel(deviceAddress)
            .load()
            .onSuccess { model ->
                if (model?.caps?.contains(SwannBatteryCamera.NAMESPACE) == true) {
                    scheduledExecutor.execute(this::sendKeepAwake)
                } else {
                    logger.debug("Not sending keep awake - did not find [${SwannBatteryCamera.NAMESPACE}] in caps: ${model?.caps}")
                }
            }
    }

    override fun stopKeepAwake() {
        isCancelled = true
        scheduledExecutor.clearExecutor()
    }

    internal fun sendKeepAwake() {
        val r = SwannBatteryCamera.KeepAwakeRequest()
        r.seconds = keepAwakePerRequest
        r.address = deviceAddress

        try {
            client
                .request(r)
                .onSuccess {
                    scheduledExecutor.executeDelayed(
                        messageIntervalUnit.toMillis(messageInterval)) {
                        if (!isCancelled) {
                            sendKeepAwake()
                        }
                        }
                }
        } catch (e: Exception) {
            logger.error("Not requesting keep awake again. Failed with exception to send request.", e)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SwannCameraKeepAwakeController::class.java)

        private const val KEEP_AWAKE_INTERVAL = 20L
        private const val KEEP_AWAKE_PER_REQUEST = 30
    }
}
