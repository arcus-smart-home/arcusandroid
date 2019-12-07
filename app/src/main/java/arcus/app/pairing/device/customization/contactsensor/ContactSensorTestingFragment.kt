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
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.StringUtils
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.view.ScleraLinkView
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.presentation.pairing.device.customization.contactsensor.*
import com.squareup.picasso.Picasso
import org.slf4j.LoggerFactory

class ContactSensorTestingFragment : Fragment(),
    TitledFragment,
    ContactTestView {

    private var nextButtonText: Int = R.string.pairing_next
    private var showCancelButton: Boolean = false

    private lateinit var contactTestImage: ImageView
    private lateinit var contactTestTitle: ScleraTextView
    private lateinit var contactTestDescription: ScleraTextView
    private lateinit var contactTestLink: ScleraLinkView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep

    private lateinit var mCallback: CustomizationNavigationDelegate
    private val presenter : ContactTestPresenter =
        ContactTestPresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            pairingDeviceAddress = getString(ARG_PAIRING_DEVICE_ADDRESS)!!
            customizationStep = getParcelable(ARG_CONTACT_TYPE_STEP)!!
            showCancelButton = getBoolean(ARG_CANCEL_PRESENT)
            nextButtonText = getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_contact_sensor_testing, container, false)

        contactTestImage = view.findViewById(R.id.customization_contact_type_image)
        contactTestTitle = view.findViewById(R.id.customization_contact_type_title)
        contactTestDescription = view.findViewById(R.id.customization_contact_type_description)
        contactTestLink = view.findViewById(R.id.customization_contact_type_link)
        nextButton = view.findViewById(R.id.next_button)
        nextButton.setText(nextButtonText)
        nextButton.setOnClickListener {
            mCallback.navigateForwardAndComplete(CustomizationType.CONTACT_TYPE)
        }
        cancelButton = view.findViewById(R.id.cancel_button)
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

    override fun onResume() {
        super.onResume()

        presenter.setView(this)
        presenter.loadFromPairingDevice(pairingDeviceAddress)

        contactTestTitle.text = customizationStep.title

        if(customizationStep.description.isNotEmpty()){
            contactTestDescription.text = customizationStep.description.joinToString("\n\n")
        }

        customizationStep.link?.let { link ->
            if(link.url.isNotEmpty() && link.text.isNotEmpty()){
                contactTestLink.text = link.text
                contactTestLink.setOnClickListener{
                    ActivityUtils.launchUrl(Uri.parse(link.url))
                }
            }
        }
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


    override fun getTitle() = customizationStep.header ?: getString(R.string.customization_contact_type_header)

    override fun onContactStateUpdated(state: String) {
        Picasso
            .with(activity)
            .load(getImageUrl(state))
            .into(contactTestImage)
    }

    override fun onError(error: Throwable) {
        logger.error("Contact Type Testing", "Received error: ", error)
    }

    private fun getImageUrl(state: String) : String {
        val stepType = StringUtils.lowerCase(customizationStep.id)
        val screenDensity = ImageUtils.screenDensity
        return CUSTOMIZATION_URL_FORMAT.format(SessionController.instance().staticResourceBaseUrl, stepType, state, screenDensity)
    }

    companion object {
        const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        const val ARG_CONTACT_TYPE_STEP = "ARG_CONTACT_TYPE_STEP"
        const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"
        private const val CUSTOMIZATION_URL_FORMAT = "%s/o/%s-%s-and-%s.png"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(ContactSensorTestingFragment::class.java)

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String, step: CustomizationStep, cancelPresent: Boolean, nextButtonText: Int) : ContactSensorTestingFragment {
            val fragment =
                ContactSensorTestingFragment()
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
