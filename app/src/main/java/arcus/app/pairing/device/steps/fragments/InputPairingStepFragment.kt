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
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.utils.ActivityUtils
import arcus.app.pairing.device.steps.StepsNavigationDelegate
import arcus.presentation.pairing.device.steps.InputPairingStep
import arcus.presentation.pairing.device.steps.PairingStepInputType
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.slf4j.LoggerFactory

class InputPairingStepFragment : Fragment(), DataFragment {

    private lateinit var step: InputPairingStep
    private lateinit var mCallback: StepsNavigationDelegate

    private lateinit var productImage: ImageView
    private lateinit var stepInstruction: TextView
    private lateinit var instructionLink: TextView
    private lateinit var inputContainer: LinearLayout

    private var inputTextSize: Float = 0F
    private var inputMap = hashMapOf<String, String>()
    override val formValues : HashMap<String, String>
        get() = HashMap(inputMap)
    private val inputFieldsValid = mutableMapOf<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let{ bundle ->
            step = bundle.getParcelable(ARG_INPUT_PAIRING_STEP)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view =inflater.inflate(R.layout.fragment_input_pairing_step, container, false)

        productImage = view.findViewById(R.id.product_image)
        stepInstruction = view.findViewById(R.id.step_instructions)
        instructionLink = view.findViewById(R.id.instructions_link)
        inputContainer = view.findViewById(R.id.input_container)

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inputTextSize =
                if(savedInstanceState != null && savedInstanceState.containsKey(ARG_INPUT_TEXT_SIZE)){
                    savedInstanceState.getFloat(ARG_INPUT_TEXT_SIZE)
                } else {
                    16F
                }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if(inputTextSize != 0F) {
            outState.putFloat(ARG_INPUT_TEXT_SIZE, inputTextSize)
        }
    }

    override fun onResume() {
        super.onResume()

        updateUI()
    }

    private fun updateUI() {
        ImageManager
                .with(activity)
                .putPairingStepImage(step.productId, (step.stepNumber).toString())
                .into(productImage)
                .execute()

        /* display the link step instructions */
        step.instructions.let {
            stepInstruction.visibility = View.VISIBLE
            stepInstruction.text = it.joinToString("\n")
        }

        /* display the link for manufacturer's instructions */
        step.link?.let {
            instructionLink.visibility = View.VISIBLE
            instructionLink.text = it.text
            val url = Uri.parse(it.url)
            instructionLink.setOnClickListener {
                ActivityUtils.launchUrl(url)
            }
        }

        // Add input fields
        inputContainer.removeAllViews()
        step.inputs.forEachIndexed { index, input ->
            when(input.inputType){
                PairingStepInputType.HIDDEN -> {
                    input.value?.let {
                        inputMap[input.keyName] = it
                    }
                }

                PairingStepInputType.TEXT -> {
                    // Set up the input fields
                    inputFieldsValid[input.keyName] = false

                    val textInputLayoutContainer = TextInputLayout(context!!).apply {
                        hint = input.label
                        isCounterEnabled = true
                        counterMaxLength = input.maxLength
                    }

                    val editText = TextInputEditText(context)
                    editText.textSize = inputTextSize
                    editText.hint = input.label
                    editText.setSingleLine()
                    editText.maxLines = 1
                    editText.filters = arrayOf(InputFilter.LengthFilter(input.maxLength))
                    if (index == step.inputs.lastIndex) {
                        editText.imeOptions = EditorInfo.IME_ACTION_DONE
                    } else {
                        editText.imeOptions = EditorInfo.IME_ACTION_NEXT
                    }
                    editText.addTextChangedListener(object : TextWatcher {
                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                            // No op
                        }

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                            // No op
                        }

                        override fun afterTextChanged(s: Editable?) {
                            val currentValue = (s?.toString() ?: "").trimEnd()
                            inputMap[input.keyName] = currentValue
                            inputFieldsValid[input.keyName] = (currentValue.length >= input.minLength) && (currentValue.length <= input.maxLength)
                            enableNextButton(shouldEnableContinueButton())
                        }
                    })


                    inputMap[input.keyName]?.let {
                        editText.setText(it)
                    }

                    textInputLayoutContainer.addView(editText)
                    inputContainer.addView(textInputLayoutContainer)
                }
            }
        }
    }

    // Since these are booleans we're parsing, all { it } is the same as saying all { it == true }
    override fun shouldEnableContinueButton() = inputFieldsValid.values.all { it }

    private fun enableNextButton(enable: Boolean) {
        if (enable) {
            mCallback.enableContinue()
        } else {
            mCallback.disableContinue()
        }
    }

    companion object {
        const val ARG_INPUT_PAIRING_STEP = "ARG_INPUT_PAIRING_STEP"
        const val ARG_INPUT_TEXT_SIZE = "ARG_INPUT_TEXT_SIZE"

        @JvmStatic
        private val logger = LoggerFactory.getLogger(InputPairingStepFragment::class.java)

        @JvmStatic
        fun newInstance(
                        pairingStep: InputPairingStep
        ): InputPairingStepFragment {
                val fragment = InputPairingStepFragment()

                with (fragment) {
                        val args = Bundle()
                        args.putParcelable(ARG_INPUT_PAIRING_STEP, pairingStep)
                        arguments = args
                        retainInstance = true
                    }
                return fragment
            }
    }
}
