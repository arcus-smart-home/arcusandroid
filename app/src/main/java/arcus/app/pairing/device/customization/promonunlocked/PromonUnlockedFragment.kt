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
package arcus.app.pairing.device.customization.promonunlocked

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.StringUtils
import arcus.app.common.utils.inflate
import android.widget.Button
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.CustomizationNavigationDelegate
import arcus.app.common.fragment.TitledFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType
import com.squareup.picasso.Picasso
import org.slf4j.LoggerFactory

class PromonUnlockedFragment : Fragment(),
    TitledFragment {

    private var nextButtonText: Int = R.string.pairing_next
    private var showCancelButton: Boolean = false

    private lateinit var step: CustomizationStep
    private lateinit var mCallback: CustomizationNavigationDelegate
    private lateinit var promonImage: ImageView
    private lateinit var promonTitle: ScleraTextView
    private lateinit var promonDescription: ScleraTextView
    private lateinit var promonLink: ScleraLinkView
    private lateinit var nextButton: Button
    private lateinit var cancelButton: Button
    private lateinit var alarmsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let{ bundle ->
            step = bundle.getParcelable(ARG_PROMON_UNLOCKED_STEP)!!
            showCancelButton = bundle.getBoolean(ARG_CANCEL_BUTTON_PRESENT)
            nextButtonText = bundle.getInt(ARG_NEXT_BUTTON_TEXT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_promon_unlocked, container, false)

        alarmsContainer = view.findViewById(R.id.alarms)
        promonImage =  view.findViewById(R.id.promon_image)
        promonTitle = view.findViewById(R.id.promon_title)
        promonDescription = view.findViewById(R.id.promon_description)
        promonLink = view.findViewById(R.id.promon_link)
        nextButton = view.findViewById(R.id.next_btn)
        cancelButton = view.findViewById(R.id.cancel_btn)

        nextButton.text = resources.getString(nextButtonText)
        nextButton.setOnClickListener {
            mCallback.navigateForwardAndComplete(CustomizationType.PROMON_ALARM)
        }

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
        context?.run {
            Picasso.with(this)
                    .load(getImageUrl())
                    .placeholder(R.drawable.image_placeholder)
                    .into(promonImage, object : com.squareup.picasso.Callback {
                        override fun onSuccess() {
                            // No Op
                        }
                        override fun onError() {
                            promonImage.setImageResource(R.drawable.promon_badge_large)
                        }
                    })
        }
        promonTitle.text = step.title ?: getString(R.string.promon_unlokced_title)

        if(step.description.isNotEmpty()) {
            promonDescription.text = step.description.joinToString(separator = "\n\n")
        } else {
            promonDescription.text = getString(R.string.promon_unlocked_description)
        }

        step.link?.let {
            if (it.url.isNotEmpty() && it.text.isNotEmpty()) {
                val url = it.url
                promonLink.visibility = View.VISIBLE
                promonLink.text = it.text ?: resources.getString(R.string.promon_unlocked_link)
                promonLink.setOnClickListener {
                    ActivityUtils.launchUrl(Uri.parse(url))
                }
            }
        }

        alarmsContainer.removeAllViews()
        step.choices?.let {  choices ->
            alarmsContainer.visibility = View.VISIBLE
            for(choice in choices){
                val child = (alarmsContainer.inflate(R.layout.unlocked_promon_alarm) as ScleraTextView)
                child.text = choice.decapitalize().capitalize()
                alarmsContainer.addView(child)
        }
        }
    }
    override fun getTitle(): String {
        return step.header ?: getString(R.string.promon_unlocked_header)
    }

    private fun getImageUrl() : String {
        val stepType = StringUtils.lowerCase(step?.id)
        val screenDensity = ImageUtils.screenDensity
        return CUSTOMIZATION_URL_FORMAT.format(SessionController.instance().staticResourceBaseUrl, stepType, screenDensity)
    }

    companion object {
        const val ARG_PROMON_UNLOCKED_STEP = "ARG_PROMON_UNLOCKED_STEP"
        const val ARG_CANCEL_BUTTON_PRESENT = "ARG_CANCEL_BUTTON_PRESENT"
        const val ARG_NEXT_BUTTON_TEXT = "ARG_NEXT_BUTTON_TEXT"
        private const val CUSTOMIZATION_URL_FORMAT = "%s/o/%s-and-%s.png"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(PromonUnlockedFragment::class.java)

        fun newInstance(
            step: CustomizationStep,
            cancelPresent: Boolean = false,
            nextButtonText: Int = R.string.pairing_next
        ): PromonUnlockedFragment {
            val fragment =
                PromonUnlockedFragment()

            with(fragment) {
                val args = Bundle()
                args.putParcelable(ARG_PROMON_UNLOCKED_STEP, step)
                args.putBoolean(ARG_CANCEL_BUTTON_PRESENT, cancelPresent)
                args.putInt(ARG_NEXT_BUTTON_TEXT, nextButtonText)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}
