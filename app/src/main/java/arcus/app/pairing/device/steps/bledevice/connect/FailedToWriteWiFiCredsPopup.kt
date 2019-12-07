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

import android.net.Uri
import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.View
import android.widget.TextView
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.utils.ActivityUtils
import android.widget.Button

class FailedToWriteWiFiCredsPopup : ModalBottomSheet() {
    var tryAgainAction : (() -> Unit)? = null
    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.popup_failed_to_write_wifi_creds

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let { args ->
            view.findViewById<TextView>(R.id.title).text = args.getString(ARG_TITLE_TEXT)
            view.findViewById<TextView>(R.id.connection_error_desc).text = args.getString(ARG_DESCRIPTION_TEXT)

            val link = view.findViewById<TextView>(R.id.factory_reset_link)
            link.text = args.getString(ARG_LINK_TEXT)
            link.setOnClickListener {
                ActivityUtils.launchUrl(args.getParcelable(ARG_AKA_LINK))
            }
        }

        view.findViewById<Button>(R.id.try_again_button).setOnClickListener {
            dismiss()
            tryAgainAction?.invoke()
        }
    }

    companion object {
        private const val ARG_TITLE_TEXT = "ARG_TITLE_TEXT"
        private const val ARG_DESCRIPTION_TEXT = "ARG_DESCRIPTION_TEXT"
        private const val ARG_LINK_TEXT = "ARG_LINK_TEXT"
        private const val ARG_AKA_LINK = "ARG_AKA_LINK"

        @JvmStatic
        fun newInstance(
            title: String,
            description: String,
            linkText: String,
            linkAka: Uri
        )= FailedToWriteWiFiCredsPopup().also {
            it.arguments = Bundle().also{ args ->
                args.putString(ARG_TITLE_TEXT, title)
                args.putString(ARG_DESCRIPTION_TEXT, description)
                args.putString(ARG_LINK_TEXT, linkText)
                args.putParcelable(ARG_AKA_LINK, linkAka)
            }
        }
    }
}
