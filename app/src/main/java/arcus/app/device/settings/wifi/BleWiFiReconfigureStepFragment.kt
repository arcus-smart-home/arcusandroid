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
package arcus.app.device.settings.wifi

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.steps.BleWiFiReconfigureStep

class BleWiFiReconfigureStepFragment : Fragment() {
    private lateinit var customImage: ImageView
    private lateinit var stepInstruction: ScleraTextView
    private lateinit var instructionLink: ScleraLinkView

    private var step: BleWiFiReconfigureStep? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        step = arguments?.getParcelable(ARG_RECONNECT_STEP)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_reconnect_step)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customImage = view.findViewById(R.id.image)
        stepInstruction = view.findViewById(R.id.step_instructions)
        instructionLink = view.findViewById(R.id.instructions_link)
    }

    override fun onStart() {
        super.onStart()
        val reconnectStep = step ?: return

        ImageManager
            .with(activity)
            .putReconnectStepImage(
                reconnectStep.productId,
                reconnectStep.stepNumber.toString()
            )
            .into(customImage)
            .execute()

        stepInstruction.text = reconnectStep
            .instructions
            .joinToString("\n")

        reconnectStep
            .link
            ?.let { webLink ->
                instructionLink.visibility = View.VISIBLE
                instructionLink.text = webLink.text
                instructionLink.setOnClickListener {
                    ActivityUtils.launchUrl(Uri.parse(webLink.url))
                }
            }
    }

    companion object {
        private const val ARG_RECONNECT_STEP = "ARG_RECONNECT_STEP"

        @JvmStatic
        fun newInstance(
            reconnectStep: BleWiFiReconfigureStep
        ): BleWiFiReconfigureStepFragment = BleWiFiReconfigureStepFragment().also {
            it.apply {
                val args = Bundle()
                args.putParcelable(ARG_RECONNECT_STEP, reconnectStep)
                arguments = args
                retainInstance = true
            }
        }
    }
}
