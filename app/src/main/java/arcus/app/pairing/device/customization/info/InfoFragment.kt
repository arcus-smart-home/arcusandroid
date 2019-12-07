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
package arcus.app.pairing.device.customization.info

import android.content.Context
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
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import com.squareup.picasso.Picasso
import org.slf4j.LoggerFactory

class InfoFragment : Fragment(), TitledFragment {

    private lateinit var customizationStep: CustomizationStep
    private var cancelPresent: Boolean = false
    private var nextButtonText: Int = R.string.pairing_next

    private lateinit var infoImage: ImageView
    private lateinit var infoTitle: ScleraTextView
    private lateinit var infoDescription: ScleraTextView
    private lateinit var learnMoreLink: ScleraLinkView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button

    private lateinit var mCallback: CustomizationNavigationDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let{ bundle ->
            customizationStep = bundle.getParcelable(ARG_CUSTOMIZATION_STEP)!!
            cancelPresent = bundle.getBoolean(ARG_CANCEL_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pairing_customiztion_info, container, false)

        learnMoreLink = view.findViewById(R.id.learn_more_link)
        customizationStep.link?.let {
            learnMoreLink.visibility = View.VISIBLE
            learnMoreLink.setLinkTextAndTarget(it.text, it.url)
        }

        infoImage = view.findViewById(R.id.customization_info_image)
        infoTitle = view.findViewById(R.id.customization_info_title)
        infoDescription = view.findViewById(R.id.customization_info_desc)
        nextButton = view.findViewById(R.id.next_button)
        nextButton.setOnClickListener {
            mCallback.navigateForwardAndComplete(CustomizationType.INFO)
        }
        cancelButton = view.findViewById(R.id.cancel_button)
        cancelButton.setOnClickListener {
            mCallback.cancelCustomization()
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

        Picasso.with(context)
                .load(getImageUrl())
                .into(infoImage, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        infoImage.visibility = View.VISIBLE
                    }

                    override fun onError() {
                        infoImage.visibility = View.GONE
                    }
                })

        customizationStep.title?.let {
            infoTitle.text = it
        }

        if (customizationStep.description.isNotEmpty()) {
            infoDescription.text = customizationStep.description.joinToString(separator = "\n\n")
        }

        nextButton.text = getString(nextButtonText)

        if (cancelPresent) {
            cancelButton.visibility = View.VISIBLE
        } else {
            cancelButton.visibility = View.GONE
        }
    }

    override fun getTitle() = customizationStep.header ?: getString(R.string.customization_info_header)

    private fun getImageUrl() : String {
        val stepType = StringUtils.lowerCase(customizationStep.id)
        val screenDensity = ImageUtils.screenDensity
        return CUSTOMIZATION_URL_FORMAT.format(SessionController.instance().staticResourceBaseUrl, stepType, screenDensity)
    }


    companion object {
        const val ARG_CUSTOMIZATION_STEP = "ARG_CUSTOMIZATION_STEP"
        const val ARG_CANCEL_PRESENT = "ARG_CANCEL_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"
        private const val CUSTOMIZATION_URL_FORMAT = "%s/o/%s-and-%s.png"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(InfoFragment::class.java)

        @JvmStatic
        fun newInstance(step: CustomizationStep, cancelPresent: Boolean, nextButtonText: Int) : InfoFragment {
            val fragment = InfoFragment()
            with (fragment) {
                val args = Bundle()
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
