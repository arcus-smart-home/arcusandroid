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
package arcus.app.pairing.device.post.zwaveheal.popups

import android.os.Bundle
import android.view.View
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.view.ScleraButton
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class ZWaveRebuildNotePopup : ModalBottomSheet() {

    private var goToDashboardListener : Reference<(() -> Unit)?> = WeakReference({
        startActivity(DashboardActivity.getHomeFragmentIntent(context))
    })

    override fun allowDragging() = false
    override fun getLayoutResourceId() = R.layout.popup_zwave_rebuild_note

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ScleraButton>(R.id.go_to_dashboard_button).setOnClickListener {
            dismiss()
            goToDashboardListener.get()?.invoke()
        }
    }
}
