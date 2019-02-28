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
package arcus.app.device.settings.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import arcus.app.R
import arcus.app.activities.PermissionsActivity
import arcus.app.common.fragments.BaseFragment
import arcus.app.common.image.IntentRequestCode
import arcus.app.common.popups.ScleraPopup
import arcus.app.device.more.ConnectedToWiFiSnackBar
import arcus.app.pairing.device.steps.bledevice.BlePairingStepsActivity
import arcus.presentation.device.settings.wifi.NetworkSettingsPresenterImpl
import arcus.presentation.device.settings.wifi.NetworkSettingsView
import arcus.presentation.device.settings.wifi.WiFiNetwork

class NetworkSettingsFragment : BaseFragment(), NetworkSettingsView {
    private lateinit var networkName : TextView
    private lateinit var signalStrength : ImageView
    private val presenter = NetworkSettingsPresenterImpl()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        networkName = view.findViewById(R.id.network_name)
        signalStrength = view.findViewById(R.id.signal_strength)
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        presenter.loadFromDeviceAddress(arguments?.getString(CAMERA_ADDRESS) ?: "")
        setTitle()
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    override fun getTitle(): String? {
        return getString(R.string.setting_camera_net_and_wifi)
    }

    override fun getLayoutId(): Int? {
        return R.layout.fragment_ble_camera_network
    }

    override fun onLoaded(network: WiFiNetwork) {
        networkName.text = network.networkName
        signalStrength.setImageResource(when (network.signalStrength) {
            1 -> R.drawable.wifi_white_2_24x20
            2 -> R.drawable.wifi_white_3_24x20
            3 -> R.drawable.wifi_white_4_24x20
            4 -> R.drawable.wifi_white_5_24x20
            else -> R.drawable.wifi_white_1_24x20 // 0 or unknown
        })

        view?.findViewById<View>(R.id.update_wifi_button)?.setOnClickListener { clickedView ->
            startActivityForResult(
                BlePairingStepsActivity.createIntentForReconnect(clickedView.context, network.productAddress),
                IntentRequestCode.HUB_WIFI_PAIRING_REQUEST.requestCode
            )
            activity?.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.fade_out)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val requestCodeMatches = requestCode == IntentRequestCode.HUB_WIFI_PAIRING_REQUEST.requestCode
        val successfulResult = resultCode == Activity.RESULT_OK
        var justSetupWiFi = requestCodeMatches && successfulResult

        if (justSetupWiFi) {
            activity.run {
                if (data == null || this !is PermissionsActivity) {
                    return // Can't show snackbar!
                }

                val network = data.getStringExtra(Intent.EXTRA_TEXT)
                justSetupWiFi = data.getBooleanExtra(Intent.EXTRA_RETURN_RESULT, true)
                val networkName = network ?: ""
                this.showSnackbar { layout -> ConnectedToWiFiSnackBar.make(layout).setNetworkName(networkName, justSetupWiFi) }
            }
        } else if (resultCode == Activity.RESULT_FIRST_USER) {
            ScleraPopup.newInstance(
                R.string.device_is_offline,
                R.string.device_is_offline_sub_text,
                R.string.ok,
                -1,
                true,
                true
            ).show(fragmentManager)
        }
    }



    companion object {
        private const val CAMERA_ADDRESS = "CAMERA_ADDRESS"

        @JvmStatic
        fun newInstance(cameraAddress: String) = NetworkSettingsFragment().also {
            val bundle = Bundle()
            bundle.putString(CAMERA_ADDRESS, cameraAddress)
            it.arguments = bundle
        }
    }
}
