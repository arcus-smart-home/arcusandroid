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
package arcus.app.pairing.device.customization.waterheater

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.waterheater.WaterHeaterModelSerialPresenter
import arcus.presentation.pairing.device.customization.waterheater.WaterHeaterModelSerialPresenterImpl
import arcus.presentation.pairing.device.customization.waterheater.WaterHeaterModelSerialView
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.Delegates

class WaterHeaterModelSerialNumberFragment : Fragment(),
    TitledFragment,
    WaterHeaterModelSerialView {

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep

    private lateinit var pageHeaderTitle: TextView
    private lateinit var pageDescription: TextView
    private lateinit var modelNumber: EditText
    private lateinit var modelNumberContainer: TextInputLayout
    private lateinit var serialNumber: EditText
    private lateinit var serialNumberContainer: TextInputLayout

    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button

    private var callback by Delegates.notNull<CustomizationNavigationDelegate>()

    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next

    private val presenter: WaterHeaterModelSerialPresenter =
        WaterHeaterModelSerialPresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            pairingDeviceAddress = getString(ARG_PAIRING_DEVICE_ADDRESS)!!
            customizationStep = getParcelable(ARG_CUSTOMIZATION_STEP)!!
            cancelPresent = getBoolean(ARG_CANCEL_PRESENT)
            nextButtonText = getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_water_heater_model_serial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageHeaderTitle = view.findViewById(R.id.header_title)
        pageDescription = view.findViewById(R.id.description)
        modelNumber = view.findViewById(R.id.model_number)
        modelNumberContainer = view.findViewById(R.id.model_number_container)
        serialNumber = view.findViewById(R.id.serial_number)
        serialNumberContainer = view.findViewById(R.id.serial_number_container)

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            presenter.saveModelAndSerialNumbersToPairingDevice(
                pairingDeviceAddress,
                modelNumber.text,
                serialNumber.text
            )

            callback.navigateForwardAndComplete(CustomizationType.WATER_HEATER)
        }

        cancelButton = view.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener { callback.cancelCustomization() }
        cancelButton.visibility = if (cancelPresent) View.VISIBLE else View.GONE
    }

    override fun onStart() {
        super.onStart()

        pageHeaderTitle.text = customizationStep.title ?: getString(R.string.water_heater_assistance_title)
        pageDescription.text = if (customizationStep.description.isEmpty()) {
            getString(R.string.water_heater_assistance_desc)
        } else {
            customizationStep.description.joinToString("\n\n")
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            callback = it as CustomizationNavigationDelegate
        }
    }

    override fun onSaveSuccess() {
        /* No-Op */
    }

    override fun onUnhandledError() {
        /* No-Op */
    }

    override fun onSaveError() {
        /* No-Op */
    }

    override fun getTitle(): String = customizationStep.header ?: resources.getString(R.string.water_heater_assistance_header)

    companion object {
        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        private const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        private const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String,
                        step: CustomizationStep,
                        cancelPresent: Boolean = false,
                        nextButtonText: Int = R.string.pairing_next
        ) = WaterHeaterModelSerialNumberFragment().also { fragment ->
            fragment.arguments = createArgumentBundle(
                pairingDeviceAddress,
                step,
                cancelPresent,
                nextButtonText)
            fragment.retainInstance = true
        }

        @JvmStatic
        fun createArgumentBundle(
            pairingDeviceAddress: String,
            step: CustomizationStep,
            cancelPresent: Boolean = false,
            nextButtonText: Int = R.string.pairing_next
        ) = Bundle().also { args ->
            args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
            args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
            args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
            args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
        }
    }
}
