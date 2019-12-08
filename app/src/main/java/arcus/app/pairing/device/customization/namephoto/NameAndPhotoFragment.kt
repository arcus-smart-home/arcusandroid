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
package arcus.app.pairing.device.customization.namephoto

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatImageView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.cornea.SessionController
import com.iris.client.model.DeviceModel
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.common.image.picasso.transformation.Invert
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.namephoto.NameAndPhotoPresenter
import arcus.presentation.pairing.device.customization.namephoto.NameAndPhotoPresenterImpl
import arcus.presentation.pairing.device.customization.namephoto.NameAndPhotoView
import com.google.android.material.textfield.TextInputLayout
import org.slf4j.LoggerFactory

open class NameAndPhotoFragment : Fragment(),
    TitledFragment,
    NameAndPhotoView {
    private val presenter : NameAndPhotoPresenter =
        NameAndPhotoPresenterImpl()

    private var nextButtonText: Int = R.string.pairing_next
    private var showCancelButton: Boolean = false

    private lateinit var step: CustomizationStep
    private lateinit var mCallback: CustomizationNavigationDelegate
    private lateinit var deviceImage: ImageView
    private lateinit var cameraImage: ImageView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button
    private lateinit var stepTitle: TextView
    private lateinit var stepInstruction: TextView
    private lateinit var inputField: EditText
    private lateinit var inputFieldContainer: TextInputLayout
    private lateinit var deviceAddress: String
    private lateinit var deviceName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ bundle ->
            deviceAddress = bundle.getString(ARG_PAIRING_DEVICE_ADDRESS)!!
            step = bundle.getParcelable(ARG_NAME_AND_PHOTO_STEP)!!
            showCancelButton = bundle.getBoolean(ARG_CANCEL_BUTTON_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_name_and_photo, container, false)

        deviceImage =  view.findViewById(R.id.product_image)
        cameraImage =  view.findViewById(R.id.camera_image)
        stepTitle = view.findViewById(R.id.step_title)
        stepInstruction = view.findViewById(R.id.step_instructions)
        inputField = view.findViewById(R.id.input_field)
        inputFieldContainer = view.findViewById(R.id.input_field_container)
        nextButton = view.findViewById(R.id.next_btn)
        cancelButton = view.findViewById(R.id.cancel_btn)

        stepTitle.text = step.title ?: getString(R.string.give_device_a_name_generic)

        nextButton.text = resources.getString(nextButtonText)
        if(showCancelButton){
            cancelButton.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                mCallback.cancelCustomization()
            }
        } else {
            cancelButton.visibility = View.GONE
        }

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            try {
                mCallback = it as CustomizationNavigationDelegate
            } catch (exception: ClassCastException){
                logger.debug(it.toString() +
                        " must implement CustomizationNavigationDelegate: \n" +
                        exception.message)
                throw (exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        presenter.loadDeviceFrom(deviceAddress)
    }

    override fun getTitle(): String {
        return step.header ?: getString(R.string.name_your_device_generic)
    }


    override fun showDevice(name: String, model: DeviceModel, address: String) {

        ImageManager.with(activity)
                .putLargeDeviceImage(model)
                .withTransformForStockImages(BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .withTransformForUgcImages(CropCircleTransformation())
                .into(deviceImage)
                .execute()

        // Add the user image, if selected
        cameraImage.setOnClickListener {
            ImageManager.with(activity)
                .putUserGeneratedDeviceImage(SessionController.instance().placeIdOrEmpty, address)
                .fromCameraOrGallery()
                .withTransform(CropCircleTransformation())
                .into(deviceImage)
                .execute()
        }

        // Show the step instructions
        stepInstruction.text = step.info ?: resources.getString(R.string.name_your_device_assistant_generic)

        // Set up the input field
        inputField.setText(name)
        inputField.hint = name
        deviceName = inputField.text.toString()
        inputField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                deviceName = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No op
            }

            override fun afterTextChanged(s: Editable?) {
                deviceName = s.toString()
                if(deviceName.isEmpty() || deviceName == ""){
                    presenter.setName(name)
                }
            }
        })

        nextButton.setOnClickListener {
            if(deviceName.isEmpty() || deviceName == ""){
                inputFieldContainer.error = getString(R.string.missing_device_name)
            } else {
                inputFieldContainer.error = null
                presenter.setName(deviceName)
                mCallback.navigateForwardAndComplete(CustomizationType.NAME)
            }
        }
    }

    override fun showError(throwable: Throwable) {
        logger.error("Name and Photo Customization", "Received error: ", throwable)
    }

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        const val ARG_NAME_AND_PHOTO_STEP = "ARG_NAME_AND_PHOTO_STEP"
        const val ARG_CANCEL_BUTTON_PRESENT = "ARG_CANCEL_BUTTON_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(NameAndPhotoFragment::class.java)

        fun newInstance(
            pairingDeviceAddress: String,
            step: CustomizationStep,
            cancelPresent: Boolean = false,
            nextButtonText: Int = R.string.pairing_next
        ): NameAndPhotoFragment {
            val fragment =
                NameAndPhotoFragment()

            with (fragment) {
                val args = Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                args.putParcelable(ARG_NAME_AND_PHOTO_STEP, step)
                args.putBoolean(ARG_CANCEL_BUTTON_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}
