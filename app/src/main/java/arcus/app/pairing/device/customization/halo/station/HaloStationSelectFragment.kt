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
package arcus.app.pairing.device.customization.halo.station

import android.content.Context
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.GlobalSetting
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.halo.station.HaloStationSelectPresenter
import arcus.presentation.pairing.device.customization.halo.station.HaloStationSelectPresenterImpl
import arcus.presentation.pairing.device.customization.halo.station.HaloStationSelectView
import arcus.presentation.pairing.device.customization.halo.station.RadioStation
import org.slf4j.LoggerFactory

class HaloStationSelectFragment : Fragment(),
    TitledFragment,
    HaloStationSelectView,
    HaloStationAdapter.HaloStationAdapterCallback {

    private lateinit var pairingDeviceAddress : String
    private lateinit var customizationStep: CustomizationStep
    private lateinit var progressBar : ConstraintLayout
    private lateinit var stepTitle : ScleraTextView
    private lateinit var stepDescription : ScleraTextView
    private lateinit var stepInfo : ScleraTextView
    private lateinit var moreStations : ScleraTextView
    private lateinit var divider : View
    private lateinit var nextButton : Button
    private lateinit var cancelButton : Button
    private lateinit var mCallback : CustomizationNavigationDelegate
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapterCallback: HaloStationAdapter.HaloStationAdapterCallback
    private lateinit var adapter : HaloStationAdapter

    private val presenter : HaloStationSelectPresenter =
        HaloStationSelectPresenterImpl()
    private var nextButtonText : Int = R.string.pairing_next
    private var cancelPresent : Boolean = false
    private var radioStations = mutableListOf<RadioStation>()

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
        return inflater.inflate(R.layout.fragment_halo_station_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        progressBar = view.findViewById(R.id.indeterminate_progress)
        stepTitle = view.findViewById(R.id.step_title)
        stepDescription = view.findViewById(R.id.step_description)
        recyclerView = view.findViewById(R.id.recycler_view)
        moreStations = view.findViewById(R.id.more_stations)
        stepInfo = view.findViewById(R.id.step_info)
        divider = view.findViewById(R.id.bottom_divider)
        nextButton = view.findViewById(R.id.next_button)
        cancelButton = view.findViewById(R.id.cancel_button)

        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(false)
        ViewCompat.setNestedScrollingEnabled(recyclerView, false)

        setupDisplay()
        progressBar.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.loadFromPairingDevice(pairingDeviceAddress)
        presenter.loadRadioStations()

        adapter = HaloStationAdapter(
            0,
            radioStations,
            adapterCallback
        )
        recyclerView.adapter = adapter
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        adapterCallback = this
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

    override fun getTitle() = customizationStep.header ?: getString(R.string.halo_weather_radio_header_default)

    override fun onStationsFound(initialStations: List<RadioStation>) {
        setupDisplay()

        recyclerView.adapter =
                HaloStationAdapter(
                    0,
                    initialStations,
                    adapterCallback
                )
    }

    override fun onStationsFound(initialStations: List<RadioStation>, additionalStations: List<RadioStation>) {
        radioStations.clear()
        setupDisplay()

        // Add the first set of stations and notify
        radioStations.addAll(0, initialStations)
        recyclerView.adapter?.notifyItemRangeInserted(
                0,
                radioStations.size)

        // Add the second set of stations when "More Stations" is clicked
        if(additionalStations.isNotEmpty() && radioStations.size<(initialStations.size + additionalStations.size)) {
            moreStations.visibility = View.VISIBLE
            moreStations.setOnClickListener {
                radioStations.addAll(radioStations.size, additionalStations)
                recyclerView.adapter?.notifyItemRangeInserted(
                        initialStations.size,
                        additionalStations.size)
                moreStations.visibility = View.GONE
            }
        }
    }

    override fun onSelectionChanged() {
        presenter.setSelectedStation(radioStations[adapter.lastSelection])
    }

    override fun onPlaybackChanged(playing: Int?) {
        presenter.stopPlayingStations()

        playing?.let {
            val stationPlaying = radioStations[it]
            presenter.playStation(stationPlaying, 10)
        }
    }

    override fun onNoStationsFound() {
        progressBar.visibility = View.GONE
        divider.visibility = View.GONE
        stepInfo.visibility = View.GONE
        stepTitle.text = getString(R.string.halo_weather_radio_no_stations_title)
        stepDescription.text = Html.fromHtml(String.format(getString(R.string.weather_radio_no_results), GlobalSetting.NOAA_WEATHER_RADIO_COVERAGE_URI.toString()))
        stepDescription.movementMethod = LinkMovementMethod.getInstance()

        nextButton.text = getString(R.string.rescan)
        nextButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            presenter.rescanForStations()
        }

        cancelButton.visibility = View.VISIBLE
        cancelButton.text = getString(R.string.weather_radio_skip_setup)
        cancelButton.setOnClickListener {
            mCallback.navigateForwardAndComplete(CustomizationType.WEATHER_RADIO_STATION)
        }
    }

    override fun onScanStationsFailed() {
        logger.error("Failed to scan for radio stations")
    }

    override fun onSetSelectionFailed() {
        logger.error("Failed to set selected station")
    }

    override fun onPlayStationFailed() {
        logger.error("Failed to play selected station")
    }

    override fun onStopPlayingStationFailed() {
        logger.error("Failed to stop playing selected station")
    }

    private fun setupDisplay(){
        progressBar.visibility = View.GONE
        divider.visibility = View.VISIBLE
        moreStations.visibility = View.GONE
        stepInfo.visibility = View.VISIBLE

        stepTitle.text = customizationStep.title ?: getString(R.string.halo_weather_radio_title_default)

        if(customizationStep.description.isNotEmpty()) {
            stepDescription.text = customizationStep.description.joinToString(separator = "\n\n")
        } else {
            stepDescription.text = getString(R.string.halo_weather_radio_description_default)
        }

        stepInfo.text = customizationStep.info ?: getString(R.string.halo_weather_radio_info_default)

        moreStations.text = getString(R.string.halo_weather_radio_more_stations)

        nextButton.text = getString(nextButtonText)
        nextButton.setOnClickListener {
            presenter.stopPlayingStations()
            mCallback.navigateForwardAndComplete(CustomizationType.WEATHER_RADIO_STATION)
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

    companion object {

        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        private const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        private const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(HaloStationSelectFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String,
                        step: CustomizationStep,
                        cancelPresent: Boolean = false,
                        nextButtonText: Int = R.string.more_stations
        ) = HaloStationSelectFragment().also { fragment ->
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
        ) = Bundle().also { args ->
            args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
            args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
            args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
            args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
        }
    }


}

