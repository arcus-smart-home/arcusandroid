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
package arcus.app.pairing.device.customization.orbit.list

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.StringUtils
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import org.slf4j.LoggerFactory
import android.view.*
import android.widget.ImageView
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.customization.orbit.list.IrrigationZone
import arcus.presentation.pairing.device.customization.orbit.list.OrbitZonePresenter
import arcus.presentation.pairing.device.customization.orbit.list.OrbitZonePresenterImpl
import arcus.presentation.pairing.device.customization.orbit.list.OrbitZoneView
import com.squareup.picasso.Picasso

class OrbitZoneFragment : Fragment(),
    TitledFragment,
    OrbitZoneView {

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep
    private lateinit var stepImage: ImageView
    private lateinit var stepTitle: ScleraTextView
    private lateinit var stepDescription: ScleraTextView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button
    private lateinit var mCallback: CustomizationNavigationDelegate
    private lateinit var recyclerView : RecyclerView

    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next

    private val presenter: OrbitZonePresenter =
        OrbitZonePresenterImpl()

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
        val view = inflater.inflate(R.layout.fragment_orbit_zone, container, false)

        stepImage = view.findViewById(R.id.customization_info_image)
        stepTitle = view.findViewById(R.id.customization_info_title)
        stepDescription = view.findViewById(R.id.customization_info_desc)

        recyclerView = view.findViewById(R.id.orbit_zone_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(false)
        ViewCompat.setNestedScrollingEnabled(recyclerView, false)

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            mCallback.navigateForwardAndComplete(CustomizationType.MULTI_IRRIGATION_ZONE)
        }

        cancelButton = view.findViewById(R.id.cancel_button)
        if (cancelPresent) {
            cancelButton.visibility = View.VISIBLE
        } else {
            cancelButton.visibility = View.GONE
        }
        cancelButton.setOnClickListener {
            mCallback.cancelCustomization()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)

        pairingDeviceAddress?.let {
            presenter.loadFromPairingDevice(it)
        }

        presenter.loadZones()
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


    override fun getTitle(): String = customizationStep.header ?: resources.getString(R.string.orbit_zone_header_default)

    override fun onZonesLoaded(zones: List<IrrigationZone>) {
        stepTitle.text = customizationStep.title ?: getString(R.string.orbit_zone_title_default)
        stepDescription.text = if(customizationStep.description.isNotEmpty()) {
            customizationStep.description.joinToString("\n")
        } else {
            resources.getString(R.string.orbit_zone_description_default)
        }

        val adapter = recyclerView.adapter
        var newAdapter : OrbitZonesAdapter? = null

        activity?.let { activity->
            newAdapter =
                    OrbitZonesAdapter(
                        pairingDeviceAddress,
                        customizationStep.id,
                        activity,
                        zones
                    )

            Picasso.with(activity)
                    .load(getImageUrl())
                    .into(stepImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            stepImage.visibility = View.VISIBLE
                        }
                        override fun onError() {
                            stepImage.visibility = View.GONE
                        }
                    })
        }

        if (adapter != null && newAdapter != null) {
            recyclerView.swapAdapter(newAdapter, true)
        } else {
            recyclerView.adapter = newAdapter
        }
    }

    override fun showError(throwable: Throwable) {
        logger.error(
            TAG, "Error received", throwable)
    }

    private fun getImageUrl() : String {
        val stepType = StringUtils.lowerCase(customizationStep.id)
        val screenDensity = ImageUtils.screenDensity
        return CUSTOMIZATION_URL_FORMAT.format(SessionController.instance().staticResourceBaseUrl, stepType, screenDensity)
    }

    companion object {
        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        private const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        private const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"
        const val TAG = "OrbitZoneFragment"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(OrbitZoneFragment::class.java)
        private const val CUSTOMIZATION_URL_FORMAT = "%s/o/%s-and-%s.png"

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String,
                        step: CustomizationStep,
                        cancelPresent: Boolean = false,
                        nextButtonText: Int = R.string.pairing_next
        ): OrbitZoneFragment {
            val fragment =
                OrbitZoneFragment()
            with (fragment) {
                val args = Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
                args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}
