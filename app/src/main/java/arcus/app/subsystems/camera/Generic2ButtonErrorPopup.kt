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
package arcus.app.subsystems.camera

import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class Generic2ButtonErrorPopup : ModalBottomSheet() {
    private lateinit var titleText : String
    private lateinit var descriptionText : String
    private lateinit var topButtonText: String
    private lateinit var bottomButtonText: String

    override fun allowDragging() = false
    @LayoutRes
    override fun getLayoutResourceId() = R.layout.popup_generic_2_button_error

    private var topButtonListener : Reference<(() -> Unit)?> = WeakReference(null)
    private var bottomButtonListener : Reference<(() -> Unit)?> = WeakReference(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args ->
            titleText = args.getString(ARG_TITLE_TEXT)!!
            descriptionText = args.getString(ARG_DESCRIPTION_TEXT)!!
            topButtonText = args.getString(ARG_TOP_BUTTON_TEXT)!!
            bottomButtonText = args.getString(ARG_BOTTOM_BUTTON_TEXT)!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ScleraTextView>(R.id.title).text = titleText
        view.findViewById<ScleraTextView>(R.id.description).text = descriptionText

        val topButton = view.findViewById<Button>(R.id.top_button)
        topButton.text = topButtonText
        topButton.setOnClickListener {
            dismiss()
            topButtonListener.get()?.invoke()
        }

        val bottomButton = view.findViewById<Button>(R.id.bottom_button)
        bottomButton.text = bottomButtonText
        bottomButton.setOnClickListener {
            dismiss()
            bottomButtonListener.get()?.invoke()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        topButtonListener.clear()
        bottomButtonListener.clear()
    }

    fun setTopButtonListener(listenerButton: () -> Unit) {
        topButtonListener = WeakReference(listenerButton)
    }

    fun setBottomButtonListener(listenerButton: () -> Unit) {
        bottomButtonListener = WeakReference(listenerButton)
    }


    override fun cleanUp() {
        super.cleanUp()
        topButtonListener.clear()
        bottomButtonListener.clear()
    }

    companion object {
        private const val ARG_TITLE_TEXT = "ARG_TITLE_TEXT"
        private const val ARG_DESCRIPTION_TEXT = "ARG_DESCRIPTION_TEXT"
        private const val ARG_TOP_BUTTON_TEXT = "ARG_TOP_BUTTON_TEXT"
        private const val ARG_BOTTOM_BUTTON_TEXT = "ARG_BOTTOM_BUTTON_TEXT"

        @JvmStatic
        fun newInstance(title: String,
                        description: String,
                        topButtonText: String,
                        bottomButtonText: String
        ) = Generic2ButtonErrorPopup().also {
            it.arguments = Bundle().also{ args ->
                args.putString(ARG_TITLE_TEXT, title)
                args.putString(ARG_DESCRIPTION_TEXT, description)
                args.putString(ARG_TOP_BUTTON_TEXT, topButtonText)
                args.putString(ARG_BOTTOM_BUTTON_TEXT, bottomButtonText)
            }
        }
    }
}
