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
package arcus.app.pairing.hub.popups

import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView

class Generic1ButtonErrorPopup : ModalBottomSheet() {
    private lateinit var titleText : String
    private lateinit var descriptionText : String
    private lateinit var topLinkText : String
    private lateinit var bottomLinkText : String
    private lateinit var buttonText: String

    override fun allowDragging() = false
    @LayoutRes
    override fun getLayoutResourceId() = R.layout.popup_generic_1_button_error

    private var topLinkListener : (() -> Unit)? = null
    private var bottomLinkListener : (() -> Unit)? = null
    private var buttonListener : (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            titleText = args.getString(ARG_TITLE_TEXT)!!
            descriptionText = args.getString(ARG_DESCRIPTION_TEXT)!!
            topLinkText = args.getString(ARG_TOP_LINK_TEXT)!!
            bottomLinkText = args.getString(ARG_BOTTOM_LINK_TEXT)!!
            buttonText = args.getString(ARG_BUTTON_TEXT)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ScleraTextView>(R.id.title).text = titleText
        view.findViewById<ScleraTextView>(R.id.description).text = descriptionText

        if(topLinkText.isNotEmpty()) {
            val topLink = view.findViewById<ScleraLinkView>(R.id.top_link)
            topLink.visibility = View.VISIBLE
            topLink.text = topLinkText
            topLink.setOnClickListener {
                dismiss()
                topLinkListener?.invoke()
            }
        }

        if(bottomLinkText.isNotEmpty()) {
            val bottomLink = view.findViewById<ScleraLinkView>(R.id.bottom_link)
            bottomLink.visibility = View.VISIBLE
            bottomLink.text = bottomLinkText
            bottomLink.setOnClickListener {
                dismiss()
                bottomLinkListener?.invoke()
            }
        }

        val button = view.findViewById<Button>(R.id.button)
        button.text = buttonText
        button.setOnClickListener {
            dismiss()
            buttonListener?.invoke()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }

    fun setTopLinkListener(linkListenerTop: () -> Unit) {
        topLinkListener = linkListenerTop
    }

    fun setBottomLinkListener(linkListenerBottom: () -> Unit) {
        bottomLinkListener = linkListenerBottom
    }

    fun setButtonListener(listenerButton: () -> Unit) {
        buttonListener = listenerButton
    }


    override fun cleanUp() {
        super.cleanUp()
        topLinkListener = null
        bottomLinkListener = null
        buttonListener = null
    }

    companion object {
        private const val ARG_TITLE_TEXT = "ARG_TITLE_TEXT"
        private const val ARG_DESCRIPTION_TEXT = "ARG_DESCRIPTION_TEXT"
        private const val ARG_TOP_LINK_TEXT = "ARG_TOP_LINK_TEXT"
        private const val ARG_BOTTOM_LINK_TEXT = "ARG_BOTTOM_LINK_TEXT"
        private const val ARG_BUTTON_TEXT = "ARG_BUTTON_TEXT"

        @JvmStatic
        fun newInstance(title: String,
                        description: String,
                        topLinkText: String = "",
                        bottomLinkText: String = "",
                        buttonText: String
        ) = Generic1ButtonErrorPopup().also {
            it.arguments = Bundle().also{ args ->
                args.putString(ARG_TITLE_TEXT, title)
                args.putString(ARG_DESCRIPTION_TEXT, description)
                args.putString(ARG_TOP_LINK_TEXT, topLinkText)
                args.putString(ARG_BOTTOM_LINK_TEXT, bottomLinkText)
                args.putString(ARG_BUTTON_TEXT, buttonText)
            }
        }
    }
}
