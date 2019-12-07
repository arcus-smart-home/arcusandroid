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
package arcus.app.pairing.device.steps.bledevice

import android.os.Bundle
import androidx.annotation.LayoutRes
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button

class BleTurnedOffPopup : ModalBottomSheet() {
    var tryAgainAction: (() -> Unit)? = null
    override fun allowDragging() = false
    override fun isCancelable(): Boolean = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.popup_ble_turned_off

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.try_again_button).setOnClickListener {
            dismiss()
            tryAgainAction?.invoke()
        }
    }
}
