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
package arcus.app.account.settings.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.inflate

class SettingsTurnOnNotificationsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_account_settings_push_detail)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<View>(R.id.push_notifications_close_container).setOnClickListener {
            requireActivity().finish()
        }

        view.findViewById<View>(R.id.contact_support_button_container).setOnClickListener {
            ActivityUtils.callSupport()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsTurnOnNotificationsFragment =
            SettingsTurnOnNotificationsFragment()
    }
}
