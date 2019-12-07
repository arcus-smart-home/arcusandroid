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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import arcus.app.R
import arcus.app.common.image.UGCImageIntentResultHandler
import arcus.presentation.camera.AutoFitTextureView
import arcus.presentation.camera.FLASH_ON
import java.io.File

open class CameraFragment : BaseCameraFragment() {

    private lateinit var autoFitTextureView: AutoFitTextureView
    private lateinit var flashButton : ImageButton
    private lateinit var flipCameraButton : ImageButton
    private lateinit var takePictureButton : ImageButton
    private lateinit var savePictureButton : ImageButton
    private lateinit var  cancelButton : Button
    private lateinit var  retakeButton : Button
    private lateinit var pictureFile : File

    private val handler = UGCImageIntentResultHandler.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_sclera_camera, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        autoFitTextureView = view.findViewById(R.id.texture)
        savePictureButton = view.findViewById(R.id.crop)
        takePictureButton = view.findViewById(R.id.picture)
        flashButton = view.findViewById(R.id.flash)
        flipCameraButton = view.findViewById(R.id.flip)
        cancelButton = view.findViewById(R.id.cancel_button)
        retakeButton = view.findViewById(R.id.retake_button)

        savePictureButton.setOnClickListener {
            activity?.let {
                handler.onCameraImageCapture(it, pictureFile)
                it.finish()
            }
        }
        takePictureButton.setOnClickListener {
            takePictureButton.isEnabled = false
            takePicture()
        }
        flashButton.setOnClickListener {
            toggleFlash()
        }
        flipCameraButton.setOnClickListener {
            flipCamera()
        }
        retakeButton.setOnClickListener {
            showRetake()
            toggleCamera()
        }
        cancelButton.setOnClickListener {
            activity?.finish()
        }
    }

    override fun onPause() {
        super.onPause()
        showRetake()
        toggleCamera()
    }

    override fun getTextureView(): AutoFitTextureView {
        return autoFitTextureView
    }

    override fun setFlashIcon(state: Int) {
        activity?.runOnUiThread({
            setIcon(state)
      })
    }

    override fun showSaveView(file: File) {
        activity?.runOnUiThread({
            showSave(file)
        })
    }

    override fun showRetakeView() {
        activity?.runOnUiThread({
            showRetake()
        })
    }

    override fun hideFlipCamera(multiple: Boolean) {
        activity?.runOnUiThread({
            hideCamera(multiple)
        })
    }

    private fun hideCamera(multiple: Boolean) {
        if(multiple) {
            flipCameraButton.visibility = View.VISIBLE
        } else {
            flipCameraButton.visibility = View.GONE
        }
    }


    private fun setIcon(state: Int) {
        if(state == FLASH_ON) {
            flashButton.setImageResource(R.drawable.flash_12x20)
        } else {
            flashButton.setImageResource(R.drawable.flash_off_14x22)
        }
    }

    private fun showSave(file: File) {
        pictureFile = file
        takePictureButton.visibility = View.GONE
        flashButton.visibility = View.GONE
        flipCameraButton.visibility = View.GONE
        cancelButton.visibility = View.GONE

        savePictureButton.visibility = View.VISIBLE
        retakeButton.visibility = View.VISIBLE
    }

    private fun showRetake() {
        takePictureButton.isEnabled = true
        takePictureButton.visibility = View.VISIBLE
        flashButton.visibility = View.VISIBLE
        flipCameraButton.visibility = View.VISIBLE
        cancelButton.visibility = View.VISIBLE

        savePictureButton.visibility = View.GONE
        retakeButton.visibility = View.GONE
    }
}
