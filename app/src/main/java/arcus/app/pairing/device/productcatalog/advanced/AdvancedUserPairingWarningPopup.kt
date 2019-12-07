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
package arcus.app.pairing.device.productcatalog.advanced

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.searching.DeviceSearchingActivity

class AdvancedUserPairingWarningPopup : ModalBottomSheet() {
    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.generic_normal_popup_two_button_text_and_description

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ScleraTextView>(R.id.title_text_view).text = getString(R.string.advanced_user_pairing_title)
        view.findViewById<ScleraTextView>(R.id.description_text_view).text = getString(R.string.advanced_user_pairing_desc)

        val okButton = view.findViewById<Button>(R.id.ok_button)
        okButton.text = getString(R.string.ok)
        okButton.setOnClickListener {
            startActivity(Intent(context, DeviceSearchingActivity::class.java))
            dismiss()
        }

        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        cancelButton.text = getString(R.string.cancel)
        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() : AdvancedUserPairingWarningPopup = AdvancedUserPairingWarningPopup()
    }
}
