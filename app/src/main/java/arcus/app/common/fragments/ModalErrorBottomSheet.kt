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
import android.support.annotation.LayoutRes
import android.view.View
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.view.ScleraButton
import arcus.app.common.view.ScleraTextView


class ModalErrorBottomSheet : ModalBottomSheet() {

    private lateinit var errorTitle : String
    private lateinit var errorDescription : String
    private lateinit var topButtonText : String
    private lateinit var bottomButtonText : String
    private var actionListener : () -> Unit = {
        ActivityUtils.launchSupport()
    }

    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.modal_sclera_error_popup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            errorTitle = it.getString(ModalErrorBottomSheet.ARG_TITLE)!!
            errorDescription = it.getString(ModalErrorBottomSheet.ARG_DESCRIPTION)!!
            topButtonText = it.getString(ModalErrorBottomSheet.ARG_TOP_BUTTON_TEXT)!!
            bottomButtonText = it.getString(ModalErrorBottomSheet.ARG_BOTTOM_BUTTON_TEXT)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val errorTitleTextView = view.findViewById<ScleraTextView>(R.id.modal_error_title)
        errorTitleTextView?.text = errorTitle

        val errorDescriptionTextView = view.findViewById<ScleraTextView>(R.id.modal_error_description)
        errorDescriptionTextView?.text = errorDescription

        view.findViewById<ScleraButton>(R.id.modal_error_support_button)?.text = topButtonText
        view.findViewById<ScleraButton>(R.id.modal_error_dismiss_button)?.text = bottomButtonText
        view.findViewById<ScleraButton>(R.id.modal_error_support_button)?.setOnClickListener {
            actionListener.invoke()
            dialog.dismiss()
        }

        view.findViewById<ScleraButton>(R.id.modal_error_dismiss_button).setOnClickListener {
            dialog.dismiss()
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
        private val ARG_TITLE = "ARG_TITLE"
        private val ARG_DESCRIPTION = "ARG_DESCRIPTION"
        private val ARG_TOP_BUTTON_TEXT = "ARG_TOP_BUTTON_TEXT"
        private val ARG_BOTTOM_BUTTON_TEXT = "ARG_BOTTOM_BUTTON_TEXT"

        @JvmStatic
        fun newInstance(title : String, description :String, topButtonText: String, bottomButtonText: String) : ModalErrorBottomSheet {
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_DESCRIPTION, description)
            args.putString(ARG_TOP_BUTTON_TEXT, topButtonText)
            args.putString(ARG_BOTTOM_BUTTON_TEXT, bottomButtonText)
            val fragment = ModalErrorBottomSheet()
            fragment.arguments = args
            return fragment
        }
    }
}
