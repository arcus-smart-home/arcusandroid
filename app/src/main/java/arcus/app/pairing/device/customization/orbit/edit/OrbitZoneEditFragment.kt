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
package arcus.app.pairing.device.customization.orbit.edit

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.StringUtils
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import arcus.presentation.pairing.device.customization.orbit.edit.IrrigationZoneDetails
import arcus.presentation.pairing.device.customization.orbit.edit.OrbitZoneEditPresenter
import arcus.presentation.pairing.device.customization.orbit.edit.OrbitZoneEditPresenterImpl
import arcus.presentation.pairing.device.customization.orbit.edit.OrbitZoneEditView
import com.squareup.picasso.Picasso

class OrbitZoneEditFragment : Fragment(),
    TitledFragment,
    OrbitZoneEditView {

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep
    private lateinit var stepImage: ImageView
    private lateinit var zoneName: EditText
    private lateinit var zoneNameContainer: EditText
    private lateinit var zoneDuration: TextView
    private lateinit var zoneInformationContainer: View
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button

    private var callback : CustomizationNavigationDelegate? = null

    private var isPopup : Boolean = true
    private lateinit var zoneInstance : String

    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next

    private val presenter: OrbitZoneEditPresenter =
        OrbitZoneEditPresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            pairingDeviceAddress = getString(ARG_PAIRING_DEVICE_ADDRESS)!!
            customizationStep = getParcelable(ARG_CUSTOMIZATION_STEP)!!
            cancelPresent = getBoolean(ARG_CANCEL_PRESENT)
            nextButtonText = getInt(ARG_NEXT_BUTTON_TEXT)
            isPopup = getBoolean(ARG_IS_POPUP)
            zoneInstance = getString(
                ARG_ZONE_INSTANCE,
                ZONE_INSTANCE_DEFAULT
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_orbit_edit_zone, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        zoneName = view.findViewById(R.id.zone_name)
        zoneNameContainer = view.findViewById(R.id.zone_name_container)

        val zoneHint = getString(R.string.zone_name_hint_format, zoneInstance.replace("z", ""))
        zoneNameContainer.hint = zoneHint
        zoneDuration = view.findViewById(R.id.duration_slot)
        zoneInformationContainer = view.findViewById(R.id.zone_information)

        stepImage = view.findViewById(R.id.customization_info_image)
        Picasso
            .with(activity)
            .load(getImageUrl())
            .into(stepImage, object : com.squareup.picasso.Callback {
                override fun onSuccess() {
                    stepImage.visibility = View.VISIBLE
                }
                override fun onError() {
                    stepImage.visibility = View.GONE
                }
            })

        with (view.findViewById<TextView>(R.id.customization_info_title)) {
            text = customizationStep.title ?: getString(R.string.edit_zone_defaults)
        }

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            val details =
                IrrigationZoneDetails(
                    zoneName.text.toString(),
                    zoneDuration.tag as? Int? ?: 1
                )
            presenter.saveZoneInformationToPairingDevice(details, zoneInstance, pairingDeviceAddress)
        }

        cancelButton = view.findViewById(R.id.cancel_button)
        if (cancelPresent) {
            cancelButton.visibility = View.VISIBLE
        } else {
            cancelButton.visibility = View.GONE
        }
        cancelButton.setOnClickListener {
            if (isPopup) {
                activity?.finish()
            } else {
                callback?.cancelCustomization()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.loadFromPairingDevice(pairingDeviceAddress, zoneInstance)

        if (isPopup) {
            activity?.title = getTitle()
        }
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            callback = it as? CustomizationNavigationDelegate
        }
    }

    override fun onZoneLoaded(details: IrrigationZoneDetails) {
        zoneName.setText(details.zoneName, TextView.BufferType.EDITABLE)
        setZoneDurationText(details)

        zoneInformationContainer.setOnClickListener { _ ->
            val currentSelection = zoneDuration.tag as? Int? ?: 1
            val popup = WateringDurationPopup.newInstance(
                currentSelection
            ).setSelectionCallback {
                setZoneDurationText(
                    IrrigationZoneDetails(
                        "",
                        it
                    )
                )
            }

            popup.show(fragmentManager)
        }
    }

    private fun setZoneDurationText(details: IrrigationZoneDetails) {
        if (details.minutes < 60) {
            zoneDuration.text = resources.getQuantityString(R.plurals.mins, details.minutes, details.minutes)
        } else {
            val minutes = details.minutesToHours()
            zoneDuration.text = resources.getQuantityString(R.plurals.hrs, minutes, minutes)
        }

        zoneDuration.tag = details.minutes
    }

    override fun onZoneLoadingFailure() {
        /* no - op */
    }

    override fun onZoneSaveFailure() {
        /* no - op */
    }

    override fun onZoneSaveSuccess() {
        if (isPopup) {
            activity?.finish()
        } else {
            callback?.navigateForwardAndComplete(CustomizationType.IRRIGATION_ZONE)
        }
    }

    override fun getTitle(): String = customizationStep.header ?: resources.getString(R.string.customize_zone)

    private fun getImageUrl() : String {
        val stepType = StringUtils.lowerCase(customizationStep.id)
        val screenDensity = ImageUtils.screenDensity
        return CUSTOMIZATION_URL_FORMAT.format(SessionController.instance().staticResourceBaseUrl, stepType, screenDensity)
    }

    companion object {
        private const val ZONE_INSTANCE_DEFAULT = "z1"
        private const val CUSTOMIZATION_URL_FORMAT = "%s/o/%s-and-%s.png"

        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        private const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        private const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"
        private const val ARG_IS_POPUP = "ARG_IS_POPUP"
        private const val ARG_ZONE_INSTANCE = "ARG_ZONE_INSTANCE"

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String,
                        step: CustomizationStep,
                        cancelPresent: Boolean = false,
                        nextButtonText: Int = R.string.pairing_next,
                        isPopup: Boolean = true,
                        zoneInstance: String = ZONE_INSTANCE_DEFAULT
        ) = OrbitZoneEditFragment().also { fragment ->
            fragment.arguments =
                    createArgumentBundle(
                        pairingDeviceAddress,
                        step,
                        cancelPresent,
                        nextButtonText,
                        isPopup,
                        zoneInstance
                    )
            fragment.retainInstance = true
        }

        @JvmStatic
        fun createArgumentBundle(
            pairingDeviceAddress: String,
            step: CustomizationStep,
            cancelPresent: Boolean = false,
            nextButtonText: Int = R.string.pairing_next,
            isPopup: Boolean = true,
            zoneInstance: String = ZONE_INSTANCE_DEFAULT
        ) = Bundle().also { args ->
            args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
            args.putParcelable(ARG_CUSTOMIZATION_STEP, step)
            args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
            args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
            args.putBoolean(ARG_IS_POPUP, isPopup)
            args.putString(ARG_ZONE_INSTANCE, zoneInstance)
        }
    }
}
