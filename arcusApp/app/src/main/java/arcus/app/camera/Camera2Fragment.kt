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
package arcus.app.camera

import android.annotation.TargetApi
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.view.TextureView
import android.view.View
import arcus.presentation.camera.Camera2PresenterImpl
import arcus.presentation.camera.CameraView
import java.io.File

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class Camera2Fragment : CameraFragment(), CameraView {
    private lateinit var presenter : Camera2PresenterImpl
    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
            presenter.openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {
            presenter.configureTransform(width, height)
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {
            // No-Op
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean {
            return true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let {
            presenter = Camera2PresenterImpl(it, getTextureView())
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        /**
         * When the screen is turned off > on, the surfaceTexture is available, and "onSurfaceTextureAvailable"
         * will not be called. In that case, we can open a camera and star the preview from here
         */
        if (getTextureView().isAvailable) {
            presenter.openCamera()
        } else {
            getTextureView().surfaceTextureListener = surfaceTextureListener
        }
    }

    override fun onPause() {
        presenter.releaseCamera()
        super.onPause()
        presenter.clearView()
    }

    override fun takePicture() {
        presenter.takePicture()
    }

    override fun toggleFlash() {
        super.toggleFlash()
        presenter.toggleFlashState()
    }

    override fun toggleCamera() {
        super.toggleCamera()
        presenter.toggleCamera()
    }

    override fun flipCamera() {
        super.flipCamera()
        presenter.flipCamera()
    }

    override fun onCameraFailedToOpen() {
        // No-op
    }

    override fun onCameraFailedToRelease() {
        // No-op
    }

    override fun onFailedToStartPreview() {
        // No-op
    }

    override fun onPictureSaveSuccess(file: File) {
        showSaveView(file)
    }

    override fun onFlashToggled(state: Int) {
        setFlashIcon(state)
    }

    companion object {
        @JvmStatic
        fun newInstance() = Camera2Fragment()
    }
}