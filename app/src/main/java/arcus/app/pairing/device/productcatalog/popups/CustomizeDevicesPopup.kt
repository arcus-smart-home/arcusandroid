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
package arcus.app.pairing.device.productcatalog.popups

import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.utils.setColorSchemeWhiteRedText
import arcus.app.common.utils.setColorSchemePurpleOutline
import arcus.app.common.utils.setColorSchemeWhiteOutline
import arcus.app.common.utils.setColorSchemePurple
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.searching.DeviceSearchingActivity

class CustomizeDevicesPopup : ModalBottomSheet() {
    private var pairedCorrectly: Boolean = true
    private lateinit var description: String
    private var exitPairingListener : (() -> Unit)? = null

    override fun allowDragging(): Boolean = false

    override fun getLayoutResourceId(): Int = R.layout.fragment_customize_devices_popup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            pairedCorrectly = it.getBoolean(ARG_PAIRED_CORRECTLY)
            description = it.getString(ARG_DESCRIPTION)!!

        }
        val title = view.findViewById<ScleraTextView>(R.id.required_title)

        val body = view.findViewById<ScleraTextView>(R.id.required_body)
        body.text = description

        val topButton: Button = view.findViewById(R.id.view_devices_button)
        topButton.setOnClickListener {
            activity?.let {
                val intent = Intent(it, DeviceSearchingActivity::class.java)
                intent.putExtra(DeviceSearchingActivity.ARG_START_SEARCHING_BOOL, false)
                startActivity(intent)

                dismiss()
            }
        }

        val bottomButton: Button = view.findViewById(R.id.exit_pairing_button)
         bottomButton.setOnClickListener {
             dismiss()
             exitPairingListener?.invoke()
        }

        /* Update the color scheme as needed */
        val white = ContextCompat.getColor(view.context, R.color.white)
        if(!pairedCorrectly) {
            view.setBackgroundColor(ContextCompat.getColor(view.context, R.color.sclera_alert))
            title.setTextColor(white)
            body.setTextColor(white)
            topButton.setColorSchemeWhiteRedText()
            bottomButton.setColorSchemeWhiteOutline()
        } else {
            val dark  = ContextCompat.getColor(view.context, R.color.sclera_text_color_dark)
            view.setBackgroundColor(white)
            title.setTextColor(dark)
            body.setTextColor(dark)
            topButton.setColorSchemePurple()
            bottomButton.setColorSchemePurpleOutline()
        }

    }

    fun setExitPairingListener(listener: (() -> Unit)?) {
        exitPairingListener = listener
    }

    companion object {

        const val ARG_PAIRED_CORRECTLY = "ARG_PAIRED_CORRECTLY"
        const val ARG_DESCRIPTION = "ARG_DESCRIPTION"

        @JvmStatic
        fun newInstance(
                pairedCorrectly: Boolean,
                description: String
        ) : CustomizeDevicesPopup {
            val popup = CustomizeDevicesPopup()
            with(popup){
                val args = Bundle()
                args.putBoolean(ARG_PAIRED_CORRECTLY, pairedCorrectly)
                args.putString(ARG_DESCRIPTION, description)

                popup.arguments = args
            }
            return popup
        }
    }
}
