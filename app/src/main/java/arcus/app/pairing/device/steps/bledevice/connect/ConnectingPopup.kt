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
package arcus.app.pairing.device.steps.bledevice.connect

import android.os.Bundle
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.view.ScleraTextView


class ConnectingPopup : ModalBottomSheet() {
    private lateinit var titleText: String
    private lateinit var descriptionText: String

    override fun allowDragging() = false
    override fun getLayoutResourceId() = R.layout.popup_ble_ipcd_connecting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            titleText = args.getString(ARG_TITLE_TEXT)
            descriptionText = args.getString(ARG_DESCRIPTION_TEXT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<ScleraTextView>(R.id.title).text = titleText
        view.findViewById<ScleraTextView>(R.id.description).text = descriptionText
    }

    fun updateText(description : String) {
        view?.findViewById<ScleraTextView>(R.id.description)?.text = description
    }

    companion object {
        private const val ARG_TITLE_TEXT = "ARG_TITLE_TEXT"
        private const val ARG_DESCRIPTION_TEXT = "ARG_DESCRIPTION_TEXT"
        @JvmStatic
        fun newInstance(title: String,
                        description: String) = ConnectingPopup().also {
            with(Bundle()) {
                putString(ARG_TITLE_TEXT, title)
                putString(ARG_DESCRIPTION_TEXT, description)
                it.arguments = this
            }
            it.isCancelable = false
        }
    }
}
