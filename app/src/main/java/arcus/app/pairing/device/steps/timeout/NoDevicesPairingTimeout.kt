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
package arcus.app.pairing.device.steps.timeout

import android.os.Bundle
import android.view.View
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import java.lang.ref.Reference
import java.lang.ref.WeakReference

class NoDevicesPairingTimeout : ModalBottomSheet() {
    override fun allowDragging() = false
    override fun getLayoutResourceId() = R.layout.generic_normal_popup_two_button_text_and_description
    private var listener : Reference<(() -> Unit)?> = WeakReference(null)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<ScleraTextView>(R.id.title_text_view).text = getString(R.string.pairing_time_out)
        view.findViewById<ScleraTextView>(R.id.description_text_view).text = getString(R.string.question_keep_searching)

        val cancelButton = view.findViewById<Button>(R.id.cancel_button)
        cancelButton.text = getString(R.string.no_go_to_dashboard)
        cancelButton.setOnClickListener {
            dismiss()
            activity?.let {
                startActivity(DashboardActivity.getHomeFragmentIntent(it))
            }
        }

        val okButton = view.findViewById<Button>(R.id.ok_button)
        okButton.text = getString(R.string.yes_keep_searching)
        okButton.setOnClickListener {
            dismiss()
            listener.get()?.invoke()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        listener.clear()
    }

    fun setKeepSearchingListener(keepSearchingListener: () -> Unit) {
        listener = WeakReference(keepSearchingListener)
    }

    override fun cleanUp() {
        super.cleanUp()
        listener.clear()
    }
}
