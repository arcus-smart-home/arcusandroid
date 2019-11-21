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
import arcus.cornea.SessionController
import arcus.cornea.device.DeviceController
import arcus.cornea.error.Errors
import arcus.cornea.helpers.onFailureMain
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PagedRecordingModelProvider
import arcus.cornea.subsystem.SubsystemController
import arcus.cornea.subsystem.cameras.CameraPreviewGetter
import arcus.cornea.subsystem.cameras.model.CameraModel
import arcus.cornea.subsystem.cameras.model.PlaybackModel
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.ModelSource
import arcus.cornea.utils.ScheduledExecutor
import com.iris.capability.util.Addresses
import com.iris.client.IrisClient
import com.iris.client.capability.*
import com.iris.client.event.ListenerRegistration
import com.iris.client.model.DeviceModel
import com.iris.client.model.ModelChangedEvent
import com.iris.client.model.RecordingModel
import com.iris.client.model.SubsystemModel
import com.iris.client.service.VideoService
import okhttp3.Call
import okhttp3.Callback as OkHttpCallback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class CameraPlaybackPresenterImpl internal constructor(
    private val client: IrisClient,
    source: ModelSource<DeviceModel>,
    deviceID: String,
    private val cellBackupSubsystem: ModelSource<SubsystemModel>,
    private val videoService: VideoService,
    private val scheduledExecutor: ScheduledExecutor,
    private val mainExecutor: ScheduledExecutor
) : CameraPlaybackPresenter<PlaybackView>, DeviceController<CameraModel>(source) {
    @Volatile
    private var currentStreamID : String? = null
    private val pollingStatus = AtomicReference(PollingStatus.IDLE)

    private var recordingsListener: ListenerRegistration = Listeners.empty()
    private var changedListener: ListenerRegistration = Listeners.empty()

    private val okHttpClient = OkHttpClient
            .Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()

    private val onError : (Throwable) -> Unit = { throwable -> showError(Exception(throwable)) }

    private val playbackView : PlaybackView?
        get() = callback as? PlaybackView?

    internal val isLoaded: Boolean
        get() = device != null && PagedRecordingModelProvider.instance().isLoaded

    private val accountID: String
        get() = SessionController.instance().place?.account ?: ""

    private val sortedRecordings: List<RecordingModel>
        get() {
            return PagedRecordingModelProvider
                .instance()
                .store
                .values()
                .filter { input ->
                    input != null
                            && input.cameraid == device.id
                            && input.deleted == false
                }
                .sortedBy { it.timestamp }
        }

    private// Can't have stream & recording going at the same time; so head should be most current.
    val currentRecording: RecordingModel?
        get() {
            return sortedRecordings
                .firstOrNull {
                    it.completed == false
                            && it.deleted == false
                            && CameraModel.isLessThanTwentyMinutesOld(it.timestamp)
                }
        }


    init {
        this.recordingsListener = PagedRecordingModelProvider.instance().addStoreLoadListener { _ ->
            updateViewOnMain()

            changedListener = PagedRecordingModelProvider.instance().store.addListener { event ->
                val model = event.model as RecordingModel?
                val checkedDevice = device
                if (model != null // Sanity Checks
                    && checkedDevice != null // Sanity Checks
                    && model.timestamp != null // Sanity Checks
                    && model.cameraid.equals(checkedDevice.id, ignoreCase = true)
                    && CameraModel.isLessThanTwentyMinutesOld(model.timestamp)
                ) {
                    updateViewOnMain()
                }
            }
        }

        PagedRecordingModelProvider.instance().load()
        this.cellBackupSubsystem.load()
        this.cellBackupSubsystem.addModelListener(
            { _ -> updateViewOnMain() },
            ModelChangedEvent::class.java
        )

        listenForProperties(DeviceConnection.ATTR_STATE, DeviceOta.ATTR_STATUS)

        CameraPreviewGetter.instance().addCallback(deviceID) {
            this.updateViewOnMain()
        }
    }

    override fun setView(callback: PlaybackView) {
        super.setCallback(callback)
    }

    override fun clearView() {
        super.clearCallback()
        val device = device
        if (device != null) {
            // The callback is wrapped in a weak reference in the CameraPreviewGetter
            // so this should not continue to hold resources if we don't clear here.
            CameraPreviewGetter.instance().clearCallbacks(device.id)
        }
        Listeners.clear(changedListener)
        Listeners.clear(recordingsListener)
    }

    override fun startStream() {
        doPlayCameraAsStream(true)
    }

    override fun cancelRecordOrStreamAttempt() {
        pollingStatus.set(PollingStatus.IDLE)
    }

    override fun stopStreaming() {
        doStopStream()
    }

    override fun startRecording() {
        doPlayCameraAsStream(false)
    }

    override fun updateView() {
        // Only update the view while we're idle
        if (isLoaded && pollingStatus.get() == PollingStatus.IDLE) {
            super.updateView()
        }
    }

    override fun update(model: DeviceModel): CameraModel {
        val subsystemModel = cellBackupSubsystem.get()
        val onCellular =
            subsystemModel != null && CellBackupSubsystem.STATUS_ACTIVE == subsystemModel.get(
                CellBackupSubsystem.ATTR_STATUS
            )

        return CameraModel(model, onCellular)
    }

    private fun pollForData(
        url: String,
        callback: PlaybackView?,
        playbackModel: PlaybackModel,
        timeoutTime: Long,
        startPollingTime: Long
    ) {
        // TODO: Add metrics around start/stop of polling to determine time to stream?
        if (pollingStatus.get() == PollingStatus.ACTIVE) {
            scheduledExecutor.execute(PollServerForVideo(url, callback, playbackModel, timeoutTime, startPollingTime))
        } else {
            logger.debug("Polling went idle, not continuing.")
        }
    }

    private fun doStopStream() {
        videoService.stopRecording(client.activePlace.toString(), currentStreamID)
    }

    /**
     * if requesting stream (asStream == true) and...
     * currently streaming -> piggyback
     * currently recording -> piggyback
     * currently idle      -> new
     *
     * if requesting record (asStream == false) and...
     * currently streaming -> new
     * currently recording -> piggyback
     * currently idle      -> new
     */
    private fun doPlayCameraAsStream(asStream: Boolean) {
        if (!pollingStatus.compareAndSet(
                PollingStatus.IDLE,
                PollingStatus.ACTIVE
            )) {
            // If we didn't set this to ACTIVE because it wasn't IDLE, just return;
            return
        }

        playbackView?.showLoading()

        val model = currentRecording
        if (model != null && (asStream || RecordingModel.TYPE_RECORDING == model.type)) {
            // Requesting stream and not Idle OR Requesting record, and currently recording
            logger.debug("Piggybacking on [${model.id}] with TYPE [${model.type}]")
            model.view().onSuccess(Listeners.runOnUiThread<Recording.ViewResponse> { viewResponse ->
                try {
                    currentStreamID = getRecordingIDFromURL(viewResponse.hls)

                    val playbackModel = PlaybackModel()
                    playbackModel.deviceAddress = device?.address
                    playbackModel.setIsNewStream(false)
                    playbackModel.setIsStreaming(RecordingModel.TYPE_STREAM == model.type)
                    playbackModel.recordingID = currentStreamID
                    playbackModel.url = viewResponse.hls

                    playbackView?.playbackReady(playbackModel)
                } finally {
                    pollingStatus.set(PollingStatus.IDLE)
                }
            }).onFailureMain(onError)
        } else {
            // Currently Idle OR Streaming and want to record NEW STREAM
            // asStream == true ? Stream ?: Recording
            client.activePlace?.toString()?.let { activePlace ->
                videoService
                    .startRecording(activePlace, accountID, device.address, asStream, null)
                    .onFailureMain(onError)
                    .onSuccess { response ->
                        val playbackModel = PlaybackModel()
                        playbackModel.deviceAddress = device?.address
                        playbackModel.setIsStreaming(asStream)
                        playbackModel.setIsNewStream(true)

                        val now = System.currentTimeMillis()
                        val giveUpTime = now + DELAY_UNTIL_GIVE_UP_LOOKING_MS
                        pollForData(
                            response.hls,
                            callback as PlaybackView,
                            playbackModel,
                            giveUpTime,
                            now
                        )
                    }
            }
        }
    }

    internal fun showError(ex: Exception) {
        pollingStatus.set(PollingStatus.IDLE)
        playbackView?.onError(Errors.translate(ex))
    }

    internal fun getRecordingIDFromURL(url: String): String = try {
        val recordingID = url.substring(0, url.lastIndexOf("/"))
        val parsedRecordingID = recordingID.substring(recordingID.lastIndexOf("/") + 1, recordingID.length)
        logger.debug("Recording ID[{}]", parsedRecordingID)

        parsedRecordingID
    } catch (ex: Exception) {
        logger.debug("Could not parse string [{}] for recording ID", url)
        ""
    }

    internal fun updateViewOnMain() { // Check if this is generating a Synthetic method
        mainExecutor.execute { this.updateView() }
    }


    internal inner class PollServerForVideo internal constructor(
        private val url: String,
        private val callback: PlaybackView?,
        private val playbackModel: PlaybackModel,
        private val timeoutTime: Long,
        private val startPollingTime: Long
    ) : Runnable {

        override fun run() {
            if (System.currentTimeMillis() > timeoutTime) {
                showErrorOnLooper(RuntimeException("Timed out getting a 200 for streaming request."))
                return
            }

            okHttpClient.newCall(
                Request
                    .Builder()
                    .url(url)
                    .get()
                    .build()
            ).enqueue(object : OkHttpCallback {
                override fun onFailure(call: Call, e: IOException) {
                    showErrorOnLooper(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    try {
                        when (response.code) {
                            200 -> {
                                val totalDuration = System.currentTimeMillis() - startPollingTime
                                val durationPollingInSeconds = TimeUnit.MILLISECONDS.toSeconds(totalDuration)
                                logger.debug("Took [$durationPollingInSeconds] seconds to get a 200 -> [$totalDuration MS]")

                                if (pollingStatus.compareAndSet(
                                        PollingStatus.ACTIVE,
                                        PollingStatus.IDLE
                                    )) {
                                    logger.debug("Received a 200, waiting a second or two, then posting.")

                                    currentStreamID = getRecordingIDFromURL(url)
                                    playbackModel.recordingID = currentStreamID
                                    playbackModel.url = url

                                    // TODO: Can we remove this 2 second delay??
                                    mainExecutor.executeDelayed(DELAY_AFTER_VIDEO_READY_MS) {
                                        try {
                                            callback?.playbackReady(playbackModel)
                                        } catch (ex: Exception) {
                                            logger.debug("Could not dispatch success - view destroyed?", ex)
                                        }
                                    }
                                } else {
                                    logger.debug("We received a 200 but the request has been cancelled. Not posting anything.")
                                }
                            }

                            404 -> {
                                logger.debug("Re-polling for video... received 404 - not ready yet.")
                                scheduledExecutor.executeDelayed(DELAY_IN_POLLING_MS) {
                                    pollForData(
                                        url,
                                        callback,
                                        playbackModel,
                                        timeoutTime,
                                        startPollingTime
                                    )
                                }
                            }

                            else -> showErrorOnLooper(RuntimeException("Did not receive a 404 or 200. Was [${response.code}]"))
                        }
                    } finally {
                        response.body?.close()
                    }
                }
            })
        }

        private fun showErrorOnLooper(ex: Exception) {
            logger.error("Posting [Error]", ex)

            if (pollingStatus.compareAndSet(
                    PollingStatus.ACTIVE,
                    PollingStatus.IDLE
                )) {
                mainExecutor.execute { showError(ex) }
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CameraPlaybackPresenterImpl::class.java)

        private val DELAY_IN_POLLING_MS = TimeUnit.SECONDS.toMillis(1)
        private val DELAY_AFTER_VIDEO_READY_MS = TimeUnit.SECONDS.toMillis(2)
        private val DELAY_UNTIL_GIVE_UP_LOOKING_MS = TimeUnit.SECONDS.toMillis(40)

        @JvmStatic
        fun newController(deviceId: String): CameraPlaybackPresenterImpl {
            val address = Addresses.toObjectAddress(Device.NAMESPACE, deviceId)
            val source = DeviceModelProvider.instance().getModel(address)

            return CameraPlaybackPresenterImpl(
                CorneaClientFactory.getClient(),
                source,
                deviceId,
                SubsystemController.instance().getSubsystemModel(CellBackupSubsystem.NAMESPACE),
                CorneaClientFactory.getService(VideoService::class.java),
                AndroidExecutor(Looper.myLooper()!!),
                AndroidExecutor.mainExecutor
            )
        }
    }
}
