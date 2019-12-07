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
package arcus.app.pairing.device.searching.timeout

import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.utils.setColorSchemePurple
import arcus.app.common.utils.setColorSchemePurpleOutline
import arcus.app.common.utils.setColorSchemeWhiteRedText
import arcus.app.common.utils.setColorSchemeWhiteOutline
import arcus.app.common.view.ScleraTextView
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class ConfirmExitPairingPopupWithDevices : ModalBottomSheet() {
    override fun allowDragging() = false
    override fun getLayoutResourceId() = R.layout.generic_normal_popup_two_button_text_and_description
    private var topButtonListener : Reference<(() -> Unit)?> = WeakReference(null)
    private var goToDashboardButtonListener : Reference<(() -> Unit)?> = WeakReference(null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val title = view.findViewById<ScleraTextView>(R.id.title_text_view)
        title.text = getString(R.string.exit_pairing_plain)
        val body = view.findViewById<ScleraTextView>(R.id.description_text_view)
        body.text = arguments?.getString(ARG_DESCRIPTION)

        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        cancelButton.text = getString(R.string.go_to_dashboard)
        cancelButton.setOnClickListener {
            dismiss()
            goToDashboardButtonListener.get()?.invoke()
        }

        val okButton = view.findViewById<Button>(R.id.ok_button)
        okButton.text = arguments?.getString(ARG_TOP_BUTTON)
        okButton.setOnClickListener {
            dismiss()
            topButtonListener.get()?.invoke()
        }

        /* Update the color scheme as needed */
        val white = ContextCompat.getColor(view.context, R.color.white)
        if(arguments?.getBoolean(ARG_PAIRED_CORRECTLY) == false) {
            view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.sclera_alert))
            title.setTextColor(white)
            body.setTextColor(white)
            okButton.setColorSchemeWhiteRedText()
            cancelButton.setColorSchemeWhiteOutline()
        } else {
            val dark  = ContextCompat.getColor(view.context, R.color.sclera_text_color_dark)
            view.setBackgroundColor(white)
            title.setTextColor(dark)
            body.setTextColor(dark)
            okButton.setColorSchemePurple()
            cancelButton.setColorSchemePurpleOutline()
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

    override fun cleanUp() {
        super.cleanUp()
        topButtonListener.clear()
        goToDashboardButtonListener.clear()
    }

    fun setTopButtonListener(listener: (() -> Unit)?) {
        topButtonListener = WeakReference(listener)
    }

    fun setGoToDashboardButtonListener(listener: (() -> Unit)?) {
        goToDashboardButtonListener = WeakReference(listener)
    }

    companion object {

        const val ARG_PAIRED_CORRECTLY = "ARG_PAIRED_CORRECTLY"
        const val ARG_DESCRIPTION = "ARG_DESCRIPTION"
        const val ARG_TOP_BUTTON = "ARG_TOP_BUTTON"

        @JvmStatic
        fun newInstance(
                pairedCorrectly: Boolean,
                description: String,
                topButton: String
        ) : ConfirmExitPairingPopupWithDevices {
            val popup = ConfirmExitPairingPopupWithDevices()
            with(popup){
                val args = Bundle()
                args.putBoolean(ARG_PAIRED_CORRECTLY, pairedCorrectly)
                args.putString(ARG_DESCRIPTION, description)
                args.putString(ARG_TOP_BUTTON, topButton)

                popup.arguments = args
            }
            return popup
        }
    }
}
