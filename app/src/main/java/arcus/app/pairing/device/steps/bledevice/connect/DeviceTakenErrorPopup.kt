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

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.view.ScleraButton
import arcus.app.common.view.ScleraLinkView


class DeviceTakenErrorPopup : ModalBottomSheet() {
    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.popup_ipcd_device_taken

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ScleraLinkView>(R.id.call_support_link).setOnClickListener {
            ActivityUtils.callSupport()
        }

        view.findViewById<ScleraButton>(R.id.close_button).setOnClickListener {
            dialog.dismiss()
        }
    }
}