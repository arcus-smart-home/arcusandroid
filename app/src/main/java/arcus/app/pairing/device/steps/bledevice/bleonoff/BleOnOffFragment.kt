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
package arcus.app.pairing.device.steps.bledevice.bleonoff

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import arcus.app.R
import arcus.app.common.view.ScleraTextView
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.steps.StepsNavigationDelegate
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import kotlin.properties.Delegates


class BleOnOffFragment : Fragment(), TitledFragment,
    ViewPagerSelectedFragment {
    private lateinit var title : ScleraTextView
    private lateinit var pairingDevice : String
    private lateinit var description : ScleraTextView
    private lateinit var onOffSwitch : Switch
    private var stepsNavigationDelegate by Delegates.notNull<StepsNavigationDelegate>()
    private val bleAdapter : BluetoothAdapter?
        get() {
            val mgr = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
            return mgr?.adapter
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ble_on_off, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            pairingDevice = bundle.getString(ARG_DEVICE_NAME)!!
        }

        title = view.findViewById(R.id.title)
        description = view.findViewById(R.id.description)
        onOffSwitch = view.findViewById(R.id.ble_on_off_switch)
        onOffSwitch.setOnClickListener {
            enableBle(onOffSwitch.isChecked)
        }
    }

    override fun onStart() {
        super.onStart()
        changeBleOnOffText(isBleEnabled())
    }

    private fun changeBleOnOffText(enabled: Boolean) {
        if (enabled) {
            title.text = getString(R.string.ble_is_on_title)
            description.text = getString(R.string.ble_is_on_desc, pairingDevice)
            onOffSwitch.isChecked = true
        } else {
            title.text = getString(R.string.ble_is_off_title)
            description.text = getString(R.string.ble_is_off_desc, pairingDevice)
            onOffSwitch.isChecked = false
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        stepsNavigationDelegate = context as StepsNavigationDelegate
    }

    override fun onPageSelected() {
        val enabled = isBleEnabled()
        changeBleOnOffText(enabled)
        if (!enabled) {
            stepsNavigationDelegate.disableContinue()
        } else {
            stepsNavigationDelegate.enableContinue()
        }
    }

    override fun onNotSelected() {
        // No-Op
    }

    override fun getTitle() : String = pairingDevice

    private fun isBleEnabled() = bleAdapter?.isEnabled ?:false
    private fun enableBle(enable: Boolean) = bleAdapter?.run {
        if (enable) {
            enable()
        } else {
            disable()
        }
    }

    companion object {
        private const val ARG_DEVICE_NAME= "ARG_DEVICE_NAME"

        @JvmStatic
        fun newInstance(shortName: String) = BleOnOffFragment().also {
            with (Bundle()) {
                    putString(ARG_DEVICE_NAME, shortName)
                    it.arguments = this
                }
        }
    }
}
