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


class NoDevicesFoundPopup : ModalBottomSheet() {
    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.popup_ble_ipcd_no_devices_found

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let { bundle ->
            val titleText = getString(R.string.no_shortnames_were_found, bundle.getString(ARG_SHORT_NAME))
            view.findViewById<TextView>(R.id.title).text = titleText

            view.findViewById<View>(R.id.factory_reset_link).setOnClickListener {
                ActivityUtils.launchUrl(bundle.getParcelable(ARG_FACTORY_RESET_LINK_URI))
            }
        }
        view.findViewById<Button>(R.id.close_button).setOnClickListener {
            dismiss()
        }
    }

    companion object {
        private const val ARG_SHORT_NAME = "ARG_SHORT_NAME"
        private const val ARG_FACTORY_RESET_LINK_URI = "ARG_FACTORY_RESET_LINK_URI"

        @JvmStatic
        fun newInstance(
            shortName: String,
            factoryResetLinkUri: Uri
        ) = NoDevicesFoundPopup().also {
            with (Bundle()) {
                putString(ARG_SHORT_NAME, shortName)
                putParcelable(ARG_FACTORY_RESET_LINK_URI, factoryResetLinkUri)
                it.arguments = this
            }
        }
    }
}
