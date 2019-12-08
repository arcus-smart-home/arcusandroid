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
package arcus.app.pairing.hub.ethernet

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import arcus.app.R
import arcus.app.common.fragment.FragmentVisibilityListener
import arcus.app.common.fragment.ValidationFragment
import arcus.app.common.steps.StepFragment
import arcus.app.pairing.hub.HubIdInputTextWatcher
import arcus.app.pairing.hub.hubIdInputFilers
import com.google.android.material.textfield.TextInputLayout


class V3HubPairingInputStepFragment : StepFragment<V3HubPairingStepContainer>(),
    FragmentVisibilityListener,
    ValidationFragment {
    private lateinit var hubIdInput : EditText
    private lateinit var hubIdInputContainer : TextInputLayout
    private val hubIdWatcher = HubIdInputTextWatcher()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_v3_hub_pairing_input_step, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        hubIdInput = view.findViewById(R.id.hub_id_entry)
        hubIdInputContainer = view.findViewById(R.id.hub_id_entry_container)
        hubIdInput.addTextChangedListener(hubIdWatcher)
        hubIdInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Enable the button as soon as the user starts typing (to be able to give feedback)
                stepContainer.enableStepForward(!s.isNullOrEmpty())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Nop */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* Nop */ }
        })
        hubIdInput.filters = hubIdInputFilers

        hubIdInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                stepContainer.navigateForward()
                true
            } else {
                false
            }
        }
    }

    override fun inValidState(): Boolean = if (hubIdWatcher.isValid) {
        hubIdInputContainer.error = null
        stepContainer.hubId = hubIdInput.text.toString()
        hideKeyboard()
        true
    } else {
        hubIdInputContainer.error = getString(hubIdWatcher.errorRes)
        false
    }

    override fun onFragmentVisible() {
        stepContainer.enableStepForward(hubIdWatcher.isValid || !hubIdInput.text.isNullOrEmpty())
        stepContainer.setTitle(getString(R.string.pairing_hub))
    }

    override fun onFragmentNotVisible() {
        stepContainer.enableStepForward(true)
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val imm = hubIdInput.context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(hubIdInput.windowToken, 0)
    }

    companion object {
        @JvmStatic
        fun newInstance() = V3HubPairingInputStepFragment()
    }
}
