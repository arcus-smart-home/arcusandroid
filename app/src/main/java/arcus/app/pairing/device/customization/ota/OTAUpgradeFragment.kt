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
package arcus.app.pairing.device.customization.ota

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import arcus.app.R
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.ota.OTAUpgradePresenter
import arcus.presentation.pairing.device.customization.ota.OTAUpgradePresenterImpl
import arcus.presentation.pairing.device.customization.ota.OTAUpgradeView
import kotlin.properties.Delegates

class OTAUpgradeFragment : Fragment(),
    TitledFragment, OTAUpgradeView {

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep

    private lateinit var pageHeaderTitle: ScleraTextView
    private lateinit var updateProgressBar: ProgressBar
    private lateinit var percentCompleteText: ScleraTextView
    private lateinit var pageDescription: ScleraTextView
    private lateinit var pageInfo: ScleraTextView

    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button

    private var callback by Delegates.notNull<CustomizationNavigationDelegate>()

    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next

    private val presenter: OTAUpgradePresenter =
        OTAUpgradePresenterImpl()

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
        return inflater.inflate(R.layout.fragment_ota_upgrade, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pageHeaderTitle = view.findViewById(R.id.header_title)
        updateProgressBar = view.findViewById(R.id.firmware_update_progress)
        percentCompleteText = view.findViewById(R.id.percent_complete_text)
        pageDescription = view.findViewById(R.id.description)
        pageInfo = view.findViewById(R.id.info)

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            callback.navigateForwardAndComplete(CustomizationType.OTA_UPGRADE)
        }

        cancelButton = view.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener { callback.cancelCustomization() }
        cancelButton.visibility = if (cancelPresent) View.VISIBLE else View.GONE
    }

    override fun onStart() {
        super.onStart()

        pageHeaderTitle.text = customizationStep.title ?: getString(R.string.firmware_upgrade_needed)
        pageDescription.text = if (customizationStep.description.isEmpty()) {
            getString(R.string.firmware_upgrade_desc_fallback)
        } else {
            customizationStep.description.joinToString("\n\n")
        }
        pageInfo.text = customizationStep.info ?: getString(R.string.firmware_upgrade_info_fallback)
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.loadFromPairingDeviceAddress(pairingDeviceAddress)
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

    override fun onProgressUpdate(percentComplete: Int) {
        updateProgressBar.progress = Math.min(updateProgressBar.max, percentComplete)
        percentCompleteText.text = "%s %%".format(percentComplete)
    }

    override fun getTitle(): String = customizationStep.header ?: resources.getString(R.string.attention)

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
        ) = OTAUpgradeFragment().also { fragment ->
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
