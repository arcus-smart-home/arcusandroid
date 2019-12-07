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
package arcus.app.pairing.device.customization.contactsensor

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.StringUtils
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.pairing.device.customization.favorite.FavoritesFragment.Companion.ARG_CANCEL_PRESENT
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.contactsensor.ContactSensorAssignment
import arcus.presentation.pairing.device.customization.contactsensor.ContactTypePresenter
import arcus.presentation.pairing.device.customization.contactsensor.ContactTypePresenterImpl
import arcus.presentation.pairing.device.customization.contactsensor.ContactTypeView
import com.squareup.picasso.Picasso
import org.slf4j.LoggerFactory

class ContactTypeFragment : Fragment(),
    TitledFragment,
    ContactTypeView {

    private var pairingDeviceAddress: String? = null
    private var customizationStep: CustomizationStep? = null
    private var nextButtonText: Int = R.string.pairing_next
    private var showCancelButton: Boolean = false

    private lateinit var contactTypeImage: ImageView
    private lateinit var contactTypeTitle: ScleraTextView
    private lateinit var contactTypeRadioGroup: RadioGroup
    private lateinit var doorRadioButton: RadioButton
    private lateinit var windowRadioButton: RadioButton
    private lateinit var otherRadioButton: RadioButton
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button

    private lateinit var mCallback: CustomizationNavigationDelegate
    private val presenter : ContactTypePresenter =
        ContactTypePresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ bundle ->
            pairingDeviceAddress = bundle.getString(ARG_PAIRING_DEVICE_ADDRESS)
            customizationStep = bundle.getParcelable(ARG_CONTACT_TYPE_STEP)
            showCancelButton = bundle.getBoolean(ARG_CANCEL_BUTTON_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_customization_contact_type, container, false)

        contactTypeImage = view.findViewById(R.id.customization_contact_type_image)
        contactTypeTitle = view.findViewById(R.id.customization_contact_type_title)
        contactTypeRadioGroup = view.findViewById(R.id.customization_contact_type_radio_group)
        doorRadioButton = view.findViewById(R.id.door_radio_button)
        windowRadioButton = view.findViewById(R.id.window_radio_button)
        otherRadioButton = view.findViewById(R.id.other_radio_button)
        contactTypeRadioGroup.check(doorRadioButton.id)
        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            val checkedRadioButtonId = contactTypeRadioGroup.checkedRadioButtonId
            when (checkedRadioButtonId) {
                R.id.door_radio_button -> { presenter.setMode(ContactSensorAssignment.DOOR) }
                R.id.window_radio_button -> { presenter.setMode(ContactSensorAssignment.WINDOW) }
                R.id.other_radio_button -> { presenter.setMode(ContactSensorAssignment.OTHER) }
            }
            mCallback.navigateForwardAndComplete(CustomizationType.CONTACT_TYPE)
        }
        cancelButton = view.findViewById(R.id.cancel_button)
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
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCallback = context as CustomizationNavigationDelegate
    }

    override fun getTitle() = customizationStep?.header ?: getString(R.string.customization_contact_type_header)

    override fun onContactTypeLoaded(type: ContactSensorAssignment) {
        context?.run {
            Picasso.with(this)
                    .load(getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(contactTypeImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            contactTypeImage.visibility = View.VISIBLE
                        }
                        override fun onError() {
                            contactTypeImage.visibility = View.GONE
                        }
                    })
        }

        contactTypeTitle.text = customizationStep?.title ?: getString(R.string.customization_contact_type_title)

        when (type) {
            ContactSensorAssignment.DOOR -> {
                doorRadioButton.isChecked = true
            }
            ContactSensorAssignment.WINDOW -> {
                windowRadioButton.isChecked = true
            }
            ContactSensorAssignment.OTHER -> {
                otherRadioButton.isChecked = true
            }
        }
    }

    override fun showError(throwable: Throwable) {
        logger.error("Contact Type Customization", "Received error: ", throwable)
    }

    private fun getImageUrl() : String {
        val stepType = StringUtils.lowerCase(customizationStep?.id)
        val screenDensity = ImageUtils.screenDensity
        return CUSTOMIZATION_URL_FORMAT.format(SessionController.instance().staticResourceBaseUrl, stepType, screenDensity)
    }

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        const val ARG_CONTACT_TYPE_STEP = "ARG_CONTACT_TYPE_STEP"
        const val ARG_CANCEL_BUTTON_PRESENT = "ARG_CANCEL_BUTTON_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"
        private const val CUSTOMIZATION_URL_FORMAT = "%s/o/%s-and-%s.png"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(ContactTypeFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String, step: CustomizationStep, cancelPresent: Boolean, nextButtonText: Int) : ContactTypeFragment {
            val fragment =
                ContactTypeFragment()
            with (fragment) {
                val args = Bundle()
                args.putString(ARG_PAIRING_DEVICE_ADDRESS, pairingDeviceAddress)
                args.putParcelable(ARG_CONTACT_TYPE_STEP, step)
                args.putBoolean(ARG_CANCEL_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }

}
