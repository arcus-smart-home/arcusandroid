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
package arcus.app.common.popups

import android.content.res.Resources
import android.os.Bundle
import androidx.annotation.StringRes
import android.view.View
import android.widget.Button
import android.widget.TextView
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.common.fragments.ModalBottomSheet
import org.slf4j.LoggerFactory


class ScleraPopup : ModalBottomSheet() {
    private var topButtonAction : (() -> Unit) = {}
    private var bottomButtonAction : (() -> Unit) = {
        context?.let {
            startActivity(DashboardActivity.getHomeFragmentIntent(it))
        }
    }
    private var customDescriptionText: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            view
                .findViewById<TextView>(R.id.title_text_view)
                .text = getSafeString(it.getInt(ARG_TITLE_RES_ID), "title")

            view
                .findViewById<TextView>(R.id.description_text_view).run {
                    text = customDescriptionText ?: getSafeString(it.getInt(ARG_DESCRIPTION_RES_ID), "description")
                }

            val topButton = view.findViewById<Button>(R.id.ok_button)
            topButton.text = getSafeString(it.getInt(ARG_TOP_BUTTON_RES_ID), "top button")
            topButton.setOnClickListener {
                topButtonAction()
                dismiss()
            }
            if (it.getBoolean(ARG_HIDE_TOP_BUTTON)) {
                topButton.visibility = View.GONE
            }

            val bottomButton = view.findViewById<Button>(R.id.cancel_button)
            bottomButton.text = getSafeString(it.getInt(ARG_BOTOTM_BUTTON_RES_ID), "bottom button")
            bottomButton.setOnClickListener {
                bottomButtonAction()
                dismiss()
            }
            if (it.getBoolean(ARG_HIDE_BOTTOM_BUTTON)) {
                bottomButton.visibility = View.GONE
            }
        }
    }

    override fun allowDragging(): Boolean = false
    override fun getLayoutResourceId(): Int = arguments?.getInt(ARG_LAYOUT_RESOURCE) ?: R.layout.popup_sclera_info

    fun overrideDescriptionText(value: String) = apply {
        customDescriptionText = value
    }

    fun setTopButtonAction(action: () -> Unit) = apply {
        topButtonAction = action
    }

    fun setBottomButtonAction(action: () -> Unit) = apply {
        bottomButtonAction = action
    }

    fun ignoreTouchOnOutside() = apply {
        isCancelable = false
    }

    private fun getSafeString(@StringRes stringRes: Int, whichString: String) : String {
        return try {
            getString(stringRes)
        } catch (ex: Resources.NotFoundException) {
            LoggerFactory.getLogger(ScleraPopup::class.java).error("Could not find string for [$whichString].", ex)
            ""
        }
    }

    companion object {
        private const val ARG_TITLE_RES_ID = "ARG_TITLE_RES_ID"
        private const val ARG_DESCRIPTION_RES_ID = "ARG_DESCRIPTION_RES_ID"
        private const val ARG_TOP_BUTTON_RES_ID = "ARG_TOP_BUTTON_RES_ID"
        private const val ARG_BOTOTM_BUTTON_RES_ID = "ARG_BOTOTM_BUTTON_RES_ID"
        private const val ARG_LAYOUT_RESOURCE = "ARG_LAYOUT_RESOURCE"
        private const val ARG_HIDE_BOTTOM_BUTTON = "ARG_HIDE_BOTTOM_BUTTON"
        private const val ARG_HIDE_TOP_BUTTON = "ARG_HIDE_TOP_BUTTON"

        @JvmOverloads
        @JvmStatic
        fun newInstance(
            @StringRes titleRes : Int,
            @StringRes descriptionRes : Int,
            @StringRes topButtonRes : Int = R.string.ok,
            @StringRes bottomButtonRes : Int = R.string.go_to_dashboard,
            isErrorPopup: Boolean = false,
            hideBottomButton: Boolean = false,
            hideTopButton: Boolean = false
        ) = ScleraPopup().also {
            with (Bundle()) {
                putInt(ARG_TITLE_RES_ID, titleRes)
                putInt(ARG_DESCRIPTION_RES_ID, descriptionRes)
                putInt(ARG_TOP_BUTTON_RES_ID, topButtonRes)
                putInt(ARG_BOTOTM_BUTTON_RES_ID, bottomButtonRes)
                if (isErrorPopup) {
                    putInt(ARG_LAYOUT_RESOURCE, R.layout.popup_sclera_error)
                } else {
                    putInt(ARG_LAYOUT_RESOURCE, R.layout.popup_sclera_info)
                }
                putBoolean(ARG_HIDE_BOTTOM_BUTTON, hideBottomButton)
                putBoolean(ARG_HIDE_TOP_BUTTON, hideTopButton)
                it.arguments = this
            }
        }
    }
}
