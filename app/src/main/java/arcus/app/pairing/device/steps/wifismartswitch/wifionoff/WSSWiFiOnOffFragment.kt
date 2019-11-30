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
package arcus.app.pairing.device.steps.wifismartswitch.wifionoff

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import arcus.app.R
import arcus.app.common.utils.LocationUtils
import arcus.app.common.view.ScleraTextView
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.steps.StepsNavigationDelegate
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import kotlin.properties.Delegates


class WSSWiFiOnOffFragment : Fragment(),
    TitledFragment,
    ViewPagerSelectedFragment {
    private lateinit var title : ScleraTextView
    private lateinit var description : ScleraTextView
    private lateinit var onOffSwitch : Switch
    private var stepsNavigationDelegate by Delegates.notNull<StepsNavigationDelegate>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wss_wifi_on_off, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById(R.id.title)
        description = view.findViewById(R.id.description)
        onOffSwitch = view.findViewById(R.id.wifi_on_off_switch)
        onOffSwitch.setOnClickListener {
            val isOn = onOffSwitch.isChecked
            setWifiEnabled(isOn)
            changeWiFiOnOffText(isOn)
            if (isOn) {
                stepsNavigationDelegate.enableContinue()
            } else {
                stepsNavigationDelegate.disableContinue()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        changeWiFiOnOffText(isWifiEnabled())
    }

    private fun changeWiFiOnOffText(enabled: Boolean) {
        if (enabled) {
            title.text = getString(R.string.wifi_is_on_title)
            description.text = getString(R.string.wifi_is_on_desc)
            onOffSwitch.isChecked = true
        } else {
            title.text = getString(R.string.wifi_is_off_title)
            description.text = getString(R.string.wifi_is_off_desc)
            onOffSwitch.isChecked = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        stepsNavigationDelegate = context as StepsNavigationDelegate
    }

    override fun onPageSelected() {
        if (!isWifiEnabled()) {
            stepsNavigationDelegate.disableContinue()
        }

        activity?.let {
            LocationUtils.requestEnableLocation(it)
        }
    }

    override fun onNotSelected() {
        // No-Op
    }

    override fun getTitle() : String = getString(R.string.wifi_smart_switch_title)

    private fun getWiFiManager() = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
    private fun isWifiEnabled() = getWiFiManager()?.isWifiEnabled ?:false
    private fun setWifiEnabled(enabled: Boolean) = getWiFiManager()?.setWifiEnabled(enabled)

    companion object {
        @JvmStatic
        fun newInstance() = WSSWiFiOnOffFragment()
    }
}
