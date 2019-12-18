/*
 * API2 camera based on Camera2Basic:  https://github.com/googlesamples/android-Camera2Basic
 * Deprecated camera based on https://developer.android.com/training/camera/photobasics
 *
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package arcus.presentation.camera

import arcus.cornea.presenter.BasePresenterContract
import java.io.File

// Some values for the presenters
/**
 * Max preview width and height guaranteed by Camera2 API (used for both APIs, for consistency)
 */
const val MAX_PREVIEW_WIDTH = 1920
const val MAX_PREVIEW_HEIGHT = 1080

const val CAMERA_FACING_FRONT = 0
const val CAMERA_FACING_BACK = 1

const val FLASH_ON = 0
const val FLASH_OFF = 1

interface CameraView {
    /**
     * If the camera fails to open for some reason, tell the view
     */
    fun onCameraFailedToOpen()

    /**
     *
     * If the camera fails to release for some reason, tell the view
     */
    fun onCameraFailedToRelease()

    /**
     *
     * Called when we fail to start the preview
     */
    fun onFailedToStartPreview()

    /**
     * Called once the camera has successfully taken the picture
     */
    fun onPictureSaveSuccess(file: File)

    /**
     * Called when we have toggled the flash state
     */
    fun onFlashToggled(state: Int)
}

interface CameraPresenter : BasePresenterContract<CameraView> {
    /**
     * Open the camera
     */
    fun openCamera()

    /**
     * Release the camera
     */
    fun releaseCamera()

    /**
     * Toggle the camera
     */
    fun toggleCamera()

    /**
     * Get the ID of the camera in use (Front/Back)
     */
    fun flipCamera()

    /**
     * Take a picture
     */
    fun takePicture()

    /**
     * Toggle the flash state (ON/OFF)
     */
    fun toggleFlashState()

    /**
     * Restart the camera preview because the user wants take a different photo
     */
    fun retakePicture()
}

interface Camera2Presenter : BasePresenterContract<CameraView> {
    /**
     * Open the camera
     */
    fun openCamera()

    fun configureTransform(viewWidth: Int, viewHeight: Int)

    /**
     * Release the camera
     */
    fun releaseCamera()

    /**
     * Toggle the camera
     */
    fun toggleCamera()

    /**
     * Get the ID of the camera in use (Front/Back)
     */
    fun flipCamera()

    /**
     * Take a picture
     */
    fun takePicture()

    /**
     * Toggle the flash state (ON/OFF)
     */
    fun toggleFlashState()

    /**
     * Restart the camera preview because the user wants take a different photo
     */
    fun retakePicture()
}
