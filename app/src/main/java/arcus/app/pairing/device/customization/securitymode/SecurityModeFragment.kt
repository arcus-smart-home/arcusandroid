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
package arcus.app.pairing.device.customization.securitymode

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.StringUtils.lowerCase
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import arcus.app.pairing.device.customization.favorite.FavoritesFragment
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.securitymode.SecurityMode
import arcus.presentation.pairing.device.customization.securitymode.SecurityModePresenter
import arcus.presentation.pairing.device.customization.securitymode.SecurityModePresenterImpl
import arcus.presentation.pairing.device.customization.securitymode.SecurityModeView
import com.squareup.picasso.Picasso
import org.slf4j.LoggerFactory

class SecurityModeFragment : Fragment(),
    TitledFragment,
    SecurityModeView {

    private lateinit var pairingDeviceAddress: String
    private lateinit var customizationStep: CustomizationStep
    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next

    private lateinit var securityImage: ImageView
    private lateinit var securityTitle: ScleraTextView
    private lateinit var securityDescription: ScleraTextView
    private lateinit var securityInfo: ScleraTextView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button
    private lateinit var mCallback: CustomizationNavigationDelegate
    private lateinit var securityModesList: RadioGroup
    private lateinit var onPartialMode: RadioButton
    private lateinit var onMode: RadioButton
    private lateinit var partialMode: RadioButton
    private lateinit var notParticipatingMode: RadioButton
    private val presenter: SecurityModePresenter =
        SecurityModePresenterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let{ bundle ->
            pairingDeviceAddress = bundle.getString(FavoritesFragment.ARG_PAIRING_DEVICE_ADDRESS)!!
            customizationStep = bundle.getParcelable(FavoritesFragment.ARG_CUSTOMIZATION_STEP)!!
            cancelPresent = bundle.getBoolean(FavoritesFragment.ARG_CANCEL_PRESENT)
            nextButtonText = bundle.getInt(FavoritesFragment.ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_security_mode, container, false)

        securityImage = view.findViewById(R.id.security_image)
        securityTitle = view.findViewById(R.id.security_title)
        securityDescription = view.findViewById(R.id.security_description)
        securityInfo = view.findViewById(R.id.security_info)

        securityModesList = view.findViewById(R.id.security_modes_list)
        onPartialMode = view.findViewById(R.id.on_partial_mode)
        partialMode = view.findViewById(R.id.partial_mode)
        onMode = view.findViewById(R.id.on_mode)
        notParticipatingMode = view.findViewById(R.id.not_participating_mode)

        onPartialMode.text = Html.fromHtml(getString(R.string.security_on_partial_description))
        partialMode.text = Html.fromHtml(getString(R.string.security_partial_description))
        onMode.text = Html.fromHtml(getString(R.string.security_on_description))
        notParticipatingMode.text = Html.fromHtml(getString(R.string.security_not_participating_description))

        nextButton = view.findViewById(R.id.next_btn)
        nextButton.text = resources.getString(nextButtonText)
        cancelButton = view.findViewById(R.id.cancel_btn)
        if (cancelPresent) {
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

    override fun onStart() {
        super.onStart()

        securityTitle.text = customizationStep.title ?: resources.getString(R.string.paired_a_security_device)
        securityDescription.text = if(customizationStep.description.isNotEmpty()) {
            customizationStep.description.joinToString("\n")
        } else {
            resources.getString(R.string.review_default_security_settings)
        }
        securityInfo.text = customizationStep.info ?: resources.getString(R.string.security_changes_take_efect_next_time)
    }

    override fun onResume() {
        super.onResume()
        context?.run {
            Picasso.with(context)
                .load(getImageUrl())
                .into(securityImage, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        securityImage.visibility = View.VISIBLE
                    }

                    override fun onError() {
                        securityImage.visibility = View.GONE
                    }
                })
        }
        presenter.setView(this)
        presenter.loadFromPairingAddress(pairingDeviceAddress)
    }

    override fun getTitle() = customizationStep.header ?: getString(R.string.security_title_default)

    override fun onConfigurationLoaded(mode: SecurityMode) {
        when(mode) {
            SecurityMode.ON_AND_PARTIAL -> securityModesList.check(R.id.on_partial_mode)
            SecurityMode.ON -> securityModesList.check(R.id.on_mode)
            SecurityMode.PARTIAL -> securityModesList.check(R.id.partial_mode)
            SecurityMode.NOT_PARTICIPATING -> securityModesList.check(R.id.not_participating_mode)
        }

        nextButton.setOnClickListener {
            val selectedMode = when(securityModesList.checkedRadioButtonId) {
                R.id.on_partial_mode -> SecurityMode.ON_AND_PARTIAL
                R.id.on_mode -> SecurityMode.ON
                R.id.partial_mode -> SecurityMode.PARTIAL
                R.id.not_participating_mode -> SecurityMode.NOT_PARTICIPATING
                else -> SecurityMode.ON_AND_PARTIAL
            }

            presenter.setMode(selectedMode)
            mCallback.navigateForwardAndComplete(CustomizationType.SECURITY_MODE)
        }
    }

    private fun getImageUrl() : String {
        val stepType = lowerCase(customizationStep.id)
        val screenDensity = ImageUtils.screenDensity
        return CUSTOMIZATION_URL_FORMAT.format(SessionController.instance().staticResourceBaseUrl, stepType, screenDensity)
    }

    override fun showError(throwable: Throwable) {
        logger.error("Security Participation Customization", "Error Received", throwable)
    }

    companion object {
        private const val ARG_PAIRING_DEVICE_ADDRESS = "ARG_PAIRING_DEVICE_ADDRESS"
        private const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        private const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        private const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(FavoritesFragment::class.java)
        private const val CUSTOMIZATION_URL_FORMAT = "%s/o/%s-and-%s.png"

        @JvmStatic
        fun newInstance(pairingDeviceAddress: String,
                        step: CustomizationStep,
                        cancelPresent: Boolean = false,
                        nextButtonText: Int = R.string.pairing_next
        ): SecurityModeFragment {
            val fragment =
                SecurityModeFragment()
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
