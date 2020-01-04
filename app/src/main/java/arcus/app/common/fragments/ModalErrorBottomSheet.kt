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
import androidx.annotation.LayoutRes
import android.view.View
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible

class ModalErrorBottomSheet : ModalBottomSheet() {

    private lateinit var errorTitle : String
    private lateinit var errorDescription : String
    private var topButtonText : String? = null
    private var bottomButtonText : String? = null
    private var actionListener : () -> Unit = {
        ActivityUtils.launchSupport()
    }

    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.modal_sclera_error_popup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            errorTitle = it.getString(ARG_TITLE, "")
            errorDescription = it.getString(ARG_DESCRIPTION, "")
            topButtonText = it.getString(ARG_TOP_BUTTON_TEXT)
            bottomButtonText = it.getString(ARG_BOTTOM_BUTTON_TEXT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val errorTitleTextView = view.findViewById<TextView>(R.id.modal_error_title)
        errorTitleTextView?.text = errorTitle

        val errorDescriptionTextView = view.findViewById<TextView>(R.id.modal_error_description)
        errorDescriptionTextView?.text = errorDescription

        val supportButton = view.findViewById<Button>(R.id.modal_error_support_button)
        if (topButtonText == null) {
            supportButton.isVisible = false
        } else {
            supportButton.text = topButtonText
            supportButton.setOnClickListener {
                actionListener.invoke()
                dismiss()
            }
        }

        val dismissButton = view.findViewById<Button>(R.id.modal_error_dismiss_button)
        if (bottomButtonText == null) {
            dismissButton.isVisible = false
        } else {
            dismissButton.text = bottomButtonText
            dismissButton.setOnClickListener {
                dismiss()
            }
        }
    }

    /**
     * Sets the action to perform; then the dialog is dismissed
     */
    fun setGetSupportAction(action: () -> Unit) {
        actionListener = action
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

    override fun cleanUp() {
        super.cleanUp()
        actionListener = {}
    }

    companion object {
        private const val ARG_TITLE = "ARG_TITLE"
        private const val ARG_DESCRIPTION = "ARG_DESCRIPTION"
        private const val ARG_TOP_BUTTON_TEXT = "ARG_TOP_BUTTON_TEXT"
        private const val ARG_BOTTOM_BUTTON_TEXT = "ARG_BOTTOM_BUTTON_TEXT"

        @JvmStatic
        fun newInstance(
            title: String,
            description: String,
            topButtonText: String? = null,
            bottomButtonText: String? = null
        ) : ModalErrorBottomSheet = ModalErrorBottomSheet().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_DESCRIPTION, description)
                putString(ARG_TOP_BUTTON_TEXT, topButtonText)
                putString(ARG_BOTTOM_BUTTON_TEXT, bottomButtonText)
            }
        }
    }
}
