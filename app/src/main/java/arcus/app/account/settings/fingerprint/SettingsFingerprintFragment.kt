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
package arcus.app.account.settings.fingerprint

import android.os.Bundle
import android.view.View
import android.widget.Switch
import arcus.app.R
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.utils.BiometricLoginUtils
import arcus.app.common.utils.PreferenceUtils

class SettingsFingerprintFragment : NoViewModelFragment() {
    private lateinit var fingerprintSwitch: Switch

    override val title: String
        get() = getString(R.string.fingerprint_title)
    override val layoutId: Int = R.layout.fragment_settings_fingerprint

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fingerprintSwitch = view.findViewById(R.id.fingerprintSwitch)
        fingerprintSwitch.isChecked = PreferenceUtils.getUsesFingerPrint()
        fingerprintSwitch.setOnCheckedChangeListener { _, isChecked ->
            fingerprintSwitch.isChecked = isChecked
            PreferenceUtils.setUseFingerPrint(isChecked)
        }
    }

    override fun onResume() {
        super.onResume()

        val message = BiometricLoginUtils.fingerprintUnavailable(requireActivity())
        if (message.isNotEmpty()) {
            fingerprintSwitch.isChecked = false
            fingerprintSwitch.isEnabled = false
        } else {
            fingerprintSwitch.isEnabled = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsFingerprintFragment =
            SettingsFingerprintFragment()
    }
}
