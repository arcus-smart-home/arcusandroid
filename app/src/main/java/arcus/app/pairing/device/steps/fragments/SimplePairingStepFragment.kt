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

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.app.R
import arcus.app.activities.FullscreenFragmentActivity
import arcus.app.common.image.ImageManager
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.steps.SimplePairingStep

class SimplePairingStepFragment : Fragment() {

    private lateinit var productImage: ImageView
    private lateinit var stepInstruction: ScleraTextView
    private lateinit var instructionLink: ScleraLinkView

    lateinit var step: SimplePairingStep
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let { bundle ->
            step = bundle.getParcelable(ARG_SIMPLE_PAIRING_STEP)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_simple_pairing_step, container, false)

        productImage = view.findViewById(R.id.product_image)
        stepInstruction = view.findViewById(R.id.step_instructions)
        instructionLink = view.findViewById(R.id.instructions_link)

        return view
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        /* Load the step image */
        ImageManager.with(activity)
                .putPairingStepImage(step.productId, step.stepNumber.toString())
                .into(productImage)
                .execute()

        /* display the link step instructions */
        stepInstruction.text = step.instructions.joinToString("\n")

        /* display the link for manufacturer's instructions */
        step.link?.let {
            instructionLink.visibility = View.VISIBLE
            instructionLink.text = it.text
            val url = it.url
            instructionLink.setOnClickListener {
                val bundle = Bundle(1)
                bundle.putString(PairingStepInstructionsPopup.INSTRUCTIONS_URL, url)
                FullscreenFragmentActivity.launch(this.activity as Activity,
                    PairingStepInstructionsPopup::class.java,
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
                    bundle)
            }
        }
    }

    companion object {
        const val ARG_SIMPLE_PAIRING_STEP = "ARG_SIMPLE_PAIRING_STEP"

        @JvmStatic
        fun newInstance(
                pairingStep: SimplePairingStep
        ): SimplePairingStepFragment {
            val fragment = SimplePairingStepFragment()

            with (fragment) {
                val args = Bundle()
                args.putParcelable(ARG_SIMPLE_PAIRING_STEP, pairingStep)
                arguments = args
                retainInstance = true
            }
            return fragment
        }
    }
}
