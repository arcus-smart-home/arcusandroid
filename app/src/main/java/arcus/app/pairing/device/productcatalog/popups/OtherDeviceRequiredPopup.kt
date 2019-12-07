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
package arcus.app.pairing.device.productcatalog.popups

import android.os.Bundle
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.view.ScleraTextView

class OtherDeviceRequiredPopup : ModalBottomSheet() {
    private var vendor : String = ""
    private var shortName : String = ""
    private var helpUrl : String = ""

    override fun allowDragging(): Boolean = false

    override fun getLayoutResourceId(): Int = R.layout.fragment_other_device_required_popup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            vendor = it.getString(ARG_VENDOR_NAME)!!
            shortName = it.getString(ARG_SHORT_NAME)!!
            helpUrl = it.getString(ARG_HELP_URL)!!

        }

        view.findViewById<ScleraTextView>(R.id.required_title)
                .text = resources.getString(R.string.generic_bridge_pairing_first_title, vendor, shortName)

        view.findViewById<ScleraTextView>(R.id.required_body)
                .text = resources.getString(R.string.generic_bridge_pairing_controller_first_description, vendor, shortName)

        view.findViewById<Button>(R.id.dismiss_button).setOnClickListener {
            dialog?.cancel()
        }
    }

    companion object {

        const val ARG_VENDOR_NAME  = "ARG_VENDOR_NAME"
        const val ARG_SHORT_NAME = "ARG_SHORT_NAME"
        const val ARG_HELP_URL = "ARG_HELP_URL"

        @JvmStatic
        fun newInstance(
                vendor: String,
                shortName: String,
                helpUrl: String
        ) : OtherDeviceRequiredPopup {
            val popup = OtherDeviceRequiredPopup()
            with(popup){
                val args = Bundle()
                args.putString(ARG_VENDOR_NAME, vendor)
                args.putString(ARG_SHORT_NAME, shortName)
                args.putString(ARG_HELP_URL, helpUrl)

                popup.arguments = args
            }
            return popup
        }
    }
}
