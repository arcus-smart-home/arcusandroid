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
package arcus.presentation.camera

import android.app.Activity
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.Point
import android.hardware.Camera
import android.os.Build
import android.os.Environment
import android.view.Display
import android.view.Surface
import arcus.cornea.presenter.KBasePresenter
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import org.slf4j.LoggerFactory

@Suppress("DEPRECATION")
class Camera1PresenterImpl(
    val activity: Activity,
    private val textureView: AutoFitTextureView
) : CameraPresenter, KBasePresenter<CameraView>() {
    private var cameraRotate = 0
    private var cameraFacing = CAMERA_FACING_BACK
    private var flashState = FLASH_OFF
    private var camera: Camera? = null
    private var params: Camera.Parameters? = null
    private var orientation: Int = 0
    private var defaultDisplay: Display

    init {
        orientation = activity.resources.configuration.orientation
        defaultDisplay = activity.windowManager.defaultDisplay
    }

    private val pictureCallback = Camera.PictureCallback { data, _ ->
        val pictureFile = getOutputMediaFile()

        if (pictureFile == null) {
            logger.debug("Error creating media file, check storage permissions.")
            return@PictureCallback
        }
        try {
            FileOutputStream(pictureFile).use {
                it.write(data)
            }
            onlyIfView { view ->
                view.onPictureSaveSuccess(pictureFile)
            }
        } catch (e: FileNotFoundException) {
            logger.debug("File not found: " + e.message + "\n\n" + pictureFile.toString())
        } catch (e: IOException) {
            logger.debug("Error accessing file: " + e.message + "\n\n" + pictureFile.toString())
        }
    }

    override fun openCamera() {
        synchronized(CAMERA_LOCK) {
            camera?.let { cam ->
                try {
                    cam.release()
                    camera = null
                } catch (e: Exception) {
                    logger.error("Failed to release camera: {}", e)
                }
            }

            try {
                val cameraFacingId = getCameraId()
                camera = Camera.open(cameraFacingId)
                params = camera?.parameters

                setFlashState()

                setCameraPreview(textureView.width, textureView.height)
                setCameraOrientation()

                val pictureSize = Collections.max(params?.supportedPictureSizes, CompareSizesByArea())
                params?.setPictureSize(pictureSize.width, pictureSize.height)

                camera?.parameters = params
            } catch (e: Exception) {
                onlyIfView { view ->
                    view.onCameraFailedToOpen()
                }
            }

            try {
                camera?.setPreviewTexture(textureView.surfaceTexture)
                camera?.startPreview()
            } catch (e: IOException) {
                logger.error(e.toString())
                onlyIfView { view ->
                    view.onFailedToStartPreview()
                }
            }
        }
    }

    override fun releaseCamera() {
        synchronized(CAMERA_LOCK) {
            try {
                camera?.setPreviewCallback(null)
                camera?.stopPreview()
            } catch (e: Exception) {
            }

            try {
                camera?.release()
            } catch (e: Exception) {
                logger.error(e.toString())
                onlyIfView { view ->
                    view.onCameraFailedToRelease()
                }
            }
        }
    }

    override fun toggleCamera() {
        synchronized(CAMERA_LOCK) {
            releaseCamera()
            openCamera()
        }
    }

    override fun flipCamera() {
        cameraFacing = if (cameraFacing == CAMERA_FACING_BACK) {
            CAMERA_FACING_FRONT
        } else {
            CAMERA_FACING_BACK
        }

        releaseCamera()
        openCamera()
    }

    override fun takePicture() {
        params?.flashMode = setFlashState()
        params?.pictureFormat = ImageFormat.JPEG
        params?.jpegQuality = 85
        camera?.parameters = params
        camera?.takePicture(null, null, pictureCallback)
    }

    override fun retakePicture() {
        camera?.startPreview()
    }

    override fun toggleFlashState() {
        flashState = if (flashState == FLASH_ON) {
            FLASH_OFF
        } else {
            FLASH_ON
        }

        onlyIfView {
            it.onFlashToggled(flashState)
        }
    }

    private fun setFlashState(): String {
        return when (flashState) {
            FLASH_ON -> Camera.Parameters.FLASH_MODE_ON
            else -> Camera.Parameters.FLASH_MODE_OFF
        }
    }

    /** Create a File for saving an image or video */
    private fun getOutputMediaFile(): File? {
        // Check that the SDCard is mounted
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {

            // Specify the directory
            val mediaStorageDir = File(Environment.getExternalStorageDirectory(), "Arcus")

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    logger.debug("Failed to create directory {} : $mediaStorageDir")
                }
            }

            // Create a media file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            return File(
                mediaStorageDir.path + File.separator +
                        "IMG_" + timeStamp + ".jpg"
            )
        }
        return null
    }

    private fun getCameraId(): Int {
        return if (cameraFacing == CAMERA_FACING_BACK) {
            Camera.CameraInfo.CAMERA_FACING_BACK
        } else {
            Camera.CameraInfo.CAMERA_FACING_FRONT
        }
    }

    private fun setCameraPreview(width: Int, height: Int) {
        // set autoFocus automatically
        params?.let { params ->
            if (params.supportedFocusModes.contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
                )
            ) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            } else if (params.supportedFocusModes.contains(
                    Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
                )
            ) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            }

            val displaySize = Point()
            defaultDisplay.getSize(displaySize)
            var rotatedPreviewWidth = width
            var rotatedPreviewHeight = height
            var maxPreviewWidth = displaySize.x
            var maxPreviewHeight = displaySize.y

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                rotatedPreviewWidth = height
                rotatedPreviewHeight = width
                maxPreviewWidth = displaySize.y
                maxPreviewHeight = displaySize.x
            }

            if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                maxPreviewWidth = MAX_PREVIEW_WIDTH
            }

            if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                maxPreviewHeight = MAX_PREVIEW_HEIGHT
            }

            val sizeList = params.supportedPictureSizes
            val previewSize = chooseOptimalSize(
                sizeList,
                rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                maxPreviewHeight, Collections.max(sizeList, CompareSizesByArea())
            )

            // Set the aspect ratio of TextureView to the size of preview.
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                textureView.setAspectRatio(
                    previewSize.width, previewSize.height
                )
            } else {
                textureView.setAspectRatio(
                    previewSize.height, previewSize.width
                )
            }

//                    params.setPreviewSize(previewSize.width,previewSize.height);
        }
    }

    private fun setCameraOrientation() {
        val cameraInfo = Camera.CameraInfo()
        val cameraFacingId = getCameraId()
        Camera.getCameraInfo(cameraFacingId, cameraInfo)
        val rotation = defaultDisplay.rotation

        var degrees = 0
        when (rotation) {
            Surface.ROTATION_0 -> degrees = 0
            Surface.ROTATION_90 -> degrees = 90
            Surface.ROTATION_180 -> degrees = 180
            Surface.ROTATION_270 -> degrees = 270
        }

        if (cameraFacing == CAMERA_FACING_FRONT) {
            cameraRotate = (cameraInfo.orientation + degrees) % 360
            cameraRotate = (360 - cameraRotate) % 360 // compensate the mirror
        } else { // back-facing
            cameraRotate = (cameraInfo.orientation - degrees + 360) % 360
        }
        params?.setRotation(cameraRotate)

        if ("Nexus 5X" == Build.MODEL) {
            params?.setRotation(270)
        }

        camera?.setDisplayOrientation(cameraRotate)
    }

    /**
     * Given `choices` of `Size`s supported by a camera, choose the smallest one that
     * is at least as large as the respective texture view size, and that is at most as large as the
     * respective max size, and whose aspect ratio matches with the specified value. If such size
     * doesn't exist, choose the largest one that is at most as large as the respective max size,
     * and whose aspect ratio matches with the specified value.
     *
     * @param choices The list of sizes that the camera supports for the intended output
     * class
     * @param textureViewWidth The width of the texture view relative to sensor coordinate
     * @param textureViewHeight The height of the texture view relative to sensor coordinate
     * @param maxWidth The maximum width that can be chosen
     * @param maxHeight The maximum height that can be chosen
     * @param aspectRatio The aspect ratio
     * @return The optimal `Size`, or an arbitrary one if none were big enough
     */
    private fun chooseOptimalSize(
        choices: List<Camera.Size>,
        textureViewWidth: Int,
        textureViewHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
        aspectRatio: Camera.Size
    ): Camera.Size {

        // Collect the supported resolutions that are at least as big as the preview Surface
        val bigEnough = ArrayList<Camera.Size>()
        // Collect the supported resolutions that are smaller than the preview Surface
        val notBigEnough = ArrayList<Camera.Size>()
        val w = aspectRatio.width
        val h = aspectRatio.height
        for (option in choices) {
            if (option.width <= maxWidth &&
                option.height <= maxHeight &&
                option.height == option.width * h / w
            ) {
                if (option.width >= textureViewWidth && option.height >= textureViewHeight) {
                    bigEnough.add(option)
                } else {
                    notBigEnough.add(option)
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        return when {
            bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
            notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
            else -> {
                logger.error("Couldn't find any suitable preview size")
                choices[0]
            }
        }
    }

    companion object {
        private val CAMERA_LOCK = Any()

        @JvmStatic
        private val logger = LoggerFactory.getLogger(Camera1PresenterImpl::class.java)
    }

    /**
     * Compares two `Size`s based on their areas.
     */
    internal class CompareSizesByArea : Comparator<Camera.Size> {

        override fun compare(lhs: Camera.Size, rhs: Camera.Size): Int {
            // We cast here to ensure the multiplications won't overflow
            return java.lang.Long.signum(lhs.width.toLong() * lhs.height - rhs.width.toLong() * rhs.height)
        }
    }
}
