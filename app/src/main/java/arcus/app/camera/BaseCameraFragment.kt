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

import androidx.fragment.app.Fragment
import arcus.presentation.camera.AutoFitTextureView
import java.io.File

interface CameraInterface {
    fun takePicture()

    fun toggleFlash()

    fun toggleCamera()

    fun flipCamera()
}

abstract class BaseCameraFragment : Fragment(), CameraInterface {
    override fun takePicture() {
        // No - op
    }

    override fun toggleFlash() {
        // No - op
    }

    override fun toggleCamera() {
        // No - op
    }

    override fun flipCamera() {
        // No - op
    }

    protected abstract fun getTextureView(): AutoFitTextureView

    protected abstract fun setFlashIcon(state : Int)

    protected abstract fun showSaveView(file: File)

    protected abstract fun showRetakeView()

    protected abstract fun hideFlipCamera(multiple : Boolean)
}
