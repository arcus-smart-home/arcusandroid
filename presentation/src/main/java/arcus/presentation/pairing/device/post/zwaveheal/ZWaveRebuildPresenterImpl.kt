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
package arcus.presentation.pairing.device.post.zwaveheal

import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.provider.HubModelProvider
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.LooperExecutor
import com.iris.client.capability.HubZwave
import com.iris.client.event.ListenerRegistration
import com.iris.client.model.HubModel
import com.iris.client.model.HubZwaveModel
import kotlin.math.roundToInt
import org.slf4j.LoggerFactory

class ZWaveRebuildPresenterImpl : ZWaveRebuildPresenter, KBasePresenter<ZWaveRebuildView>() {
    private var hubListener: ListenerRegistration = Listeners.empty()

    override fun startRebuild() {
        getHubZWave()
                ?.heal(false, null)
                ?.onFailure(Listeners.runOnUiThread { throwable ->
                    logger.error("Could not start rebuilding.", throwable)
                    onlyIfView {
                        it.onUnhandledError()
                    }
                })
                ?.onSuccess(Listeners.runOnUiThread {
                    getCurrentStatus()
                })
    }

    override fun getCurrentStatus() {
        getHubZWave()?.let { hubZWave ->
            if (hubZWave.healInProgress || hubZWave.healRecommended) {
                LooperExecutor.getMainExecutor().execute {
                    onlyIfView { view ->
                        val healPercent = (hubZWave.healPercent ?: 0.0) * 100
                        view.onProgressUpdated(healPercent.roundToInt())
                    }
                }
            } else {
                onlyIfView {
                    it.onProgressUpdated(100) // We're done - oh oh oh it's magic!
                }
            }
        } ?: logger.debug("Hub Z-Wave returned null. Cannot proceed.")
    }

    override fun cancelRebuild() {
        getHubZWave()
                ?.cancelHeal()
                ?.onFailure(Listeners.runOnUiThread { throwable ->
                    logger.error("Could not cancel Z-Wave Rebuild.", throwable)
                    onlyIfView {
                        it.onUnhandledError()
                    }
                })
                ?.onSuccess(Listeners.runOnUiThread {
                    onlyIfView {
                        logger.info("Successfully canceled Z-Wave Rebuild.")
                    }
                })
    }

    override fun setView(view: ZWaveRebuildView) {
        super.setView(view)
        Listeners.clear(hubListener)
        HubModelProvider.instance().hubModel?.let { nnHubModel ->
            hubListener = nnHubModel.addPropertyChangeListener { event ->
                if (UPDATE_ON.contains(event.propertyName)) {
                    getCurrentStatus()
                }
            }
        }
    }

    override fun clearView() {
        super.clearView()
        Listeners.clear(hubListener)
    }

    private fun getHub(): HubModel? = HubModelProvider.instance().hubModel
    private fun getHubZWave(): HubZwave? = getHub() as? HubZwave?

    companion object {
        private val logger = LoggerFactory.getLogger(ZWaveRebuildPresenterImpl::class.java)

        private val UPDATE_ON = setOf(
                HubZwaveModel.ATTR_HEALPERCENT,
                HubZwaveModel.ATTR_HEALCOMPLETED
        )
    }
}
