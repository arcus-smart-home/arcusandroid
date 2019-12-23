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
import arcus.app.R

class GenericInformationPopup : ModalBottomSheet() {
    private var topButtonListener: (() -> Unit)? = null
    private var bottomButtonListener: (() -> Unit)? = null

    override fun allowDragging(): Boolean = false

    override fun getLayoutResourceId(): Int = R.layout.popup_generic_info_two_button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = arguments ?: return

        view.findViewById<TextView>(R.id.title).text = args.getString(ARG_TITLE_TEXT)
        view.findViewById<TextView>(R.id.bodyText).text = args.getString(ARG_BODY_TEXT)

        val topButton: Button = view.findViewById(R.id.topButton)
        topButton.text = args.getString(ARG_TOP_BUTTON_TEXT)
        topButton.setOnClickListener {
            dismiss()
            topButtonListener?.invoke()
        }

        args.getString(ARG_BOTTOM_BUTTON_TEXT, null)?.let {
            val bottomButton: Button = view.findViewById(R.id.bottomButton)
            bottomButton.text = it
            bottomButton.setOnClickListener {
                dismiss()
                bottomButtonListener?.invoke()
            }
        }
    }

    fun setTopButtonListener(listener: (() -> Unit)?) = apply {
        topButtonListener = listener
    }

    fun setBottomButtonListener(listener: (() -> Unit)?) = apply {
        bottomButtonListener = listener
    }

    companion object {
        private const val ARG_TITLE_TEXT = "ARG_TITLE_TEXT"
        private const val ARG_BODY_TEXT = "ARG_BODY_TEXT"
        private const val ARG_TOP_BUTTON_TEXT = "ARG_TOP_BUTTON_TEXT"
        private const val ARG_BOTTOM_BUTTON_TEXT = "ARG_BOTTOM_BUTTON_TEXT"

        @JvmStatic
        fun newInstance(
            title: String,
            body: String,
            topButtonText: String,
            bottomButtonText: String? = null
        ): GenericInformationPopup = GenericInformationPopup().apply {
            arguments = with(Bundle(4)) {
                putString(ARG_TITLE_TEXT, title)
                putString(ARG_BODY_TEXT, body)
                putString(ARG_TOP_BUTTON_TEXT, topButtonText)
                bottomButtonText?.let { bottomButton ->
                    putString(ARG_BOTTOM_BUTTON_TEXT, bottomButton)
                }
                this
            }
        }
    }
}
