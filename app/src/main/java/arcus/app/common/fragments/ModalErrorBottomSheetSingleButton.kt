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
package arcus.app.common.fragments

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.LayoutRes
import arcus.app.R

class ModalErrorBottomSheetSingleButton : ModalBottomSheet() {
    private lateinit var errorTitle: String
    private lateinit var errorDescription: String
    private lateinit var buttonText: String

    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.modal_sclera_error_popup_single_button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            errorTitle = it.getString(ARG_TITLE).orEmpty()
            errorDescription = it.getString(ARG_DESCRIPTION).orEmpty()
            buttonText = it.getString(BUTTON_TEXT).orEmpty()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.modal_error_title).text = errorTitle
        view.findViewById<TextView>(R.id.modal_error_description).text = errorDescription
        view.findViewById<Button>(R.id.modal_error_dismiss_button).text = buttonText
        view.findViewById<Button>(R.id.modal_error_dismiss_button).setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_DESCRIPTION = "ARG_DESCRIPTION"
        private const val BUTTON_TEXT = "BUTTON_TEXT"

        @JvmStatic
        fun newInstance(
            title: String,
            description: String,
            buttonText: String
        ): ModalErrorBottomSheetSingleButton = ModalErrorBottomSheetSingleButton().apply {
            arguments = with(Bundle()) {
                putString(ARG_TITLE, title)
                putString(ARG_DESCRIPTION, description)
                putString(BUTTON_TEXT, buttonText)
                this
            }
        }
    }
}
