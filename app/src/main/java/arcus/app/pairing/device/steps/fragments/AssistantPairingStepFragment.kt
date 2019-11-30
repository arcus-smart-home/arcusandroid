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
package arcus.app.pairing.device.steps.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.steps.StepsNavigationDelegate
import arcus.presentation.pairing.device.steps.AssistantPairingStep
import org.slf4j.LoggerFactory

class AssistantPairingStepFragment : Fragment() {

    private lateinit var mCallback: StepsNavigationDelegate
    private lateinit var stepInstruction: ScleraTextView
    private lateinit var downloadLink: ScleraLinkView
    private lateinit var step: AssistantPairingStep
    private lateinit var appName: String
    private lateinit var appUrl: Uri
    private lateinit var instructionsUrl: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            step = bundle.getParcelable(ARG_ASSISTANT_PAIRING_STEP)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_pairing_step, container, false)

        stepInstruction = view.findViewById(R.id.step_instructions)
        downloadLink = view.findViewById(R.id.download_link)

        /* Set up the assistant name */
        if(step.manufacturer == "Google") {
            appName = resources.getString(R.string.assistant_app)
            instructionsUrl = Uri.parse(GlobalSetting.GOOGLE_PAIRING_INSTRUCTIONS_URL)
        } else {
            appName = resources.getString(R.string.alexa_app)
            instructionsUrl = Uri.parse(GlobalSetting.ALEXA_PAIRING_INSTRUCTIONS_URL)
        }

        /* display the step instructions */
        stepInstruction.text = step.instructions ?: resources.getString(R.string.pairing_assistant_step_instructions, step.name, appName)

        /* display the link to download the assistant app */
        appUrl = Uri.parse(step.appUrl)
        downloadLink.text = resources.getString(R.string.download_assistant_app, appName)
        downloadLink.setOnClickListener({
            ActivityUtils.launchUrl(appUrl)
        })

        return view
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        activity?.let {
            try {
                mCallback = it as StepsNavigationDelegate
            } catch (exception: ClassCastException){
                logger.debug(it.toString() +
                        " must implement StepsNavigationDelegate: \n" +
                        exception.message)
                throw (exception)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        mCallback.displaySecondButton(
                resources.getString(R.string.setup_instructions),
                instructionsUrl
        )
    }

    companion object {
        const val ARG_ASSISTANT_PAIRING_STEP = "ARG_ASSISTANT_PAIRING_STEP"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(AssistantPairingStepFragment::class.java)

        @JvmStatic
        fun newInstance(
                pairingStep: AssistantPairingStep
        ): AssistantPairingStepFragment {
            val fragment = AssistantPairingStepFragment()

            with (fragment) {
                val args = Bundle()
                args.putParcelable(ARG_ASSISTANT_PAIRING_STEP, pairingStep)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}
