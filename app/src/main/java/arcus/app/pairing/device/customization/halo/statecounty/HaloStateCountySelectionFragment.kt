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
package arcus.app.pairing.device.customization.halo.statecounty

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
import arcus.presentation.pairing.device.customization.halo.statecounty.*
import org.slf4j.LoggerFactory

class HaloStateCountySelectionFragment : Fragment(),
    TitledFragment,
    HaloStateCountySelectView {

    private lateinit var pairingDeviceAddress : String
    private lateinit var customizationStep: CustomizationStep
    private lateinit var alertBanner: ScleraTextView
    private lateinit var stepTitle : ScleraTextView
    private lateinit var stateSelection : ScleraTextView
    private lateinit var countySelection : ScleraTextView
    private lateinit var nextButton : Button
    private lateinit var cancelButton : Button
    private lateinit var mCallback : CustomizationNavigationDelegate
    private lateinit var stateProgressBar: ProgressBar
    private lateinit var countyProgressBar: ProgressBar

    private val presenter : HaloStateCountySelectPresenter =
        HaloStateCountySelectPresenterImpl()
    private var nextButtonText : Int = R.string.pairing_next
    private var cancelPresent : Boolean = false

    private var currentState : HaloStateAndCode? = null
    private var currentCounty : HaloCounty?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ bundle ->
            pairingDeviceAddress = bundle.getString(ARG_PAIRING_DEVICE_ADDRESS)!!
            customizationStep = bundle.getParcelable(ARG_CUSTOMIZATION_STEP)!!
            cancelPresent = bundle.getBoolean(ARG_CANCEL_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_halo_state_county_selection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertBanner = view.findViewById(R.id. alert_banner)
        stateProgressBar = view.findViewById(R.id.state_progress_bar)
        countyProgressBar = view.findViewById(R.id.county_progress_bar)
        stepTitle = view.findViewById(R.id.step_title)
        stateSelection = view.findViewById(R.id.selected_state)
        countySelection = view.findViewById(R.id.selected_county)
        nextButton = view.findViewById(R.id.next_btn)
        cancelButton = view.findViewById(R.id.cancel_btn)

        nextButton.text = getString(nextButtonText)
        nextButton.setOnClickListener {
            handleNextButtonClick()
        }

        if (cancelPresent) {
            cancelButton.visibility = View.VISIBLE
            cancelButton.setOnClickListener {
                mCallback.cancelCustomization()
            }
        } else {
            cancelButton.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.loadFromPairingDevice(pairingDeviceAddress)
        presenter.loadStates()

        stepTitle.text = customizationStep.title ?: getString(R.string.halo_state_county_title_default)
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
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

    override fun onStatesLoaded(states: List<HaloStateAndCode>) {
        alertBanner.visibility = View.GONE
        // Try pre-populate the fields using the place information
        currentState = states.singleOrNull {
            it.isPersonsPlace
        }
        currentState?.let {
            stateProgressBar.visibility = View.GONE
            currentState = it
            stateSelection.text = it.sameCode
            presenter.loadCounties(it)
        }

        stateSelection.setOnClickListener{ _ ->

            val popup = HaloStateSelectionPopup.newInstance(
                currentState,
                resources.getString(R.string.halo_choose_state),
                states,
                "Save"
            ).setSelectionCallback {
                alertBanner.visibility = View.GONE
                stateProgressBar.visibility = View.GONE
                clearCountySetState(it)
            }
            popup.show(fragmentManager)
        }

    }

    private fun clearCountySetState(selectedState: HaloStateAndCode){
        currentCounty = null
        countySelection.text = ""
        stateSelection.setTextColor(resources.getColor(R.color.sclera_text_color_dark))
        countySelection.setTextColor(resources.getColor(R.color.sclera_text_color_dark))
        currentState = selectedState
        stateSelection.text = selectedState.sameCode
        presenter.loadCounties(selectedState)
    }

    override fun onCountiesLoaded(counties: List<HaloCounty>) {
        countyProgressBar.visibility = View.GONE
        currentCounty = counties.singleOrNull {
            it.isPersonsCounty
        }
        currentCounty?.let {
            countySelection.text = it.county
        }

        countySelection.setOnClickListener{ _ ->
            val popup = HaloCountySelectionPopup.newInstance(
                currentCounty,
                resources.getString(R.string.halo_choose_county),
                counties,
                resources.getString(R.string.generic_save_text)
            ).setSelectionCallback {
                alertBanner.visibility = View.GONE
                currentCounty = it
                countySelection.setTextColor(resources.getColor(R.color.sclera_text_color_dark))
                countySelection.text = it.county
            }
            popup.show(fragmentManager)
        }
    }

    override fun onSelectionSaved() {
        mCallback.navigateForwardAndComplete(CustomizationType.STATE_COUNTY_SELECT)
    }

    override fun onStatesFailedToLoad() {
        alertBanner.visibility = View.VISIBLE
    }

    override fun onCountiesFailedToLoad() {
        alertBanner.visibility = View.VISIBLE
    }

    override fun onSelectionSaveFailed() {
        alertBanner.visibility = View.VISIBLE
    }

    override fun getTitle() = customizationStep.header ?: getString(R.string.halo_state_county_header_default)


    private fun handleNextButtonClick(){
        stateProgressBar.visibility = View.GONE
        countyProgressBar.visibility = View.GONE
        if(countySelection.text.isNullOrEmpty()) {
            alertBanner.visibility = View.VISIBLE
            countySelection.text = getString(R.string.halo_county_missing)
            countySelection.setTextColor(resources.getColor(R.color.sclera_alert))
        }
        if(stateSelection.text.isNullOrEmpty()) {
            alertBanner.visibility = View.VISIBLE
            stateSelection.text = getString(R.string.halo_state_missing)
            stateSelection.setTextColor(resources.getColor(R.color.sclera_alert))
        }
        else {
            currentState?.let { state ->
                currentCounty?.let {
                    alertBanner.visibility = View.GONE
                    presenter.setSelectedStateAndCounty(state, it.county)
                }
            }
        }
    }

    companion object {

        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        private const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        private const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(HaloStateCountySelectionFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String,
                        step: CustomizationStep,
                        cancelPresent: Boolean = false,
                        nextButtonText: Int = R.string.setup_weather_radio
        ) = HaloStateCountySelectionFragment().also { fragment ->
            fragment.arguments =
                    createArgumentBundle(
                        pairingDeviceAddress,
                        step,
                        cancelPresent,
                        nextButtonText
                    )
            fragment.retainInstance = true
        }

        @JvmStatic
        private fun createArgumentBundle(
            pairingDeviceAddress: String,
            step: CustomizationStep,
            cancelPresent: Boolean,
            nextButtonText: Int
        ) = Bundle().also {args ->
            args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
            args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
            args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
            args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
        }
    }
}
