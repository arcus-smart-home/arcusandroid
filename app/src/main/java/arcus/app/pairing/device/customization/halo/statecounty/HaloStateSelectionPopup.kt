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
package arcus.app.pairing.device.customization.halo.statecounty

import android.os.Bundle
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.view.ScleraNumberPicker
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.customization.halo.statecounty.HaloStateAndCode
import kotlin.properties.Delegates

class HaloStateSelectionPopup : ModalBottomSheet() {
    override fun allowDragging() = false
    override fun getLayoutResourceId() = R.layout.generic_popup_spinner_layout

    private var spinnerOptions by Delegates.notNull<List<HaloStateAndCode>>()
    private var initialState : HaloStateAndCode? = null
    private var callback : ((HaloStateAndCode) -> Unit)? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = view.findViewById<ScleraTextView>(R.id.spinner_title)
        val spinner = view.findViewById<ScleraNumberPicker>(R.id.spinning_picker)
        val okButton = view.findViewById<Button>(R.id.ok_button)
        val cancelButton = view.findViewById<Button>(R.id.cancel_button)

        arguments?.let {
            initialState = it.getParcelable(ARG_CURRENT_STATE) as HaloStateAndCode?
            spinnerOptions = it.getSerializable(ARG_SPINNER_OPTIONS) as List<HaloStateAndCode>
            title.text = it.getString(ARG_SPINNER_TITLE) ?: resources.getString(R.string.halo_choose_state)
            okButton.text = it.getString(ARG_TOP_BUTTON)
        }

        val states = spinnerOptions.map {
            if(it.state.length > 20) {
                it.state.substring(0, 17) + "..."
            } else {
                it.state
            }
        }.toTypedArray()

        spinner.wrapSelectorWheel = true
        spinner.minValue = 0
        spinner.maxValue = spinnerOptions.lastIndex
        spinner.displayedValues = states
        spinner.value = initialState?.let {
            Math.max(0, states.indexOf(it.state))
            } ?: 0

        okButton.setOnClickListener {
            callback?.let {
                it(
                    spinnerOptions[spinner.value]
                )
                dismiss()
            }
        }

        cancelButton.text = getString(R.string.cancel_text)
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

    override fun cleanUp() {
        super.cleanUp()
        callback = null
    }

    fun setSelectionCallback(selectionCallback: (HaloStateAndCode) -> Unit) = apply {
        callback = selectionCallback
    }

    companion object {

        const val ARG_CURRENT_STATE = "ARG_CURRENT_STATE"
        const val ARG_SPINNER_TITLE = "ARG_SPINNER_TITLE"
        const val ARG_SPINNER_OPTIONS = "ARG_SPINNER_OPTIONS"
        const val ARG_TOP_BUTTON = "ARG_TOP_BUTTON"

        @JvmStatic
        fun newInstance(
            currentState: HaloStateAndCode?,
            spinnerTitle: String,
            options: List<HaloStateAndCode>,
            topButton: String
        ) = HaloStateSelectionPopup().also { fragment ->
            fragment.arguments =
                    createArgumentBundle(
                        currentState,
                        spinnerTitle,
                        options,
                        topButton
                    )
            fragment.retainInstance = true
        }

        @JvmStatic
        private fun createArgumentBundle(
            currentState: HaloStateAndCode?,
            spinnerTitle: String,
            options: List<HaloStateAndCode>,
            topButton: String
        ) = Bundle().also { args->

            val spinnerOptions : ArrayList<HaloStateAndCode> =
                ArrayList(options)
            args.putParcelable(ARG_CURRENT_STATE, currentState)
            args.getString(ARG_SPINNER_TITLE, spinnerTitle)
            args.putSerializable(ARG_SPINNER_OPTIONS, spinnerOptions)
            args.putString(ARG_TOP_BUTTON, topButton)
        }
    }
}

