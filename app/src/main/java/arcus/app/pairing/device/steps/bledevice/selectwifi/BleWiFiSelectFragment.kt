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
package arcus.app.pairing.device.steps.bledevice.selectwifi

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.pairing.device.steps.bledevice.BleStepsNavigationDelegate
import arcus.app.R
import arcus.app.activities.GenericFragmentActivity
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import arcus.app.pairing.device.steps.bledevice.BleConnected
import arcus.app.pairing.device.steps.bledevice.BleDisconnectedPopup
import arcus.presentation.ble.BleWiFiNetwork
import arcus.presentation.ble.BleConnector
import arcus.presentation.pairing.device.steps.bledevice.BleConnectionStatus
import arcus.presentation.pairing.device.steps.bledevice.selectwifi.AndroidBleWiFiSelectPresenter
import arcus.presentation.pairing.device.steps.bledevice.selectwifi.BleWiFiSelectView
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class BleWiFiSelectFragment : Fragment(),
    TitledFragment,
    ViewPagerSelectedFragment,
    BleConnected,
    BleWiFiSelectView {
    private val onNetworkSelected : (BleWiFiNetwork?) -> Unit = {
        selectedNetwork = it
        updateSelectedNetwork()
    }
    private val wifiHelpVisibiltyToggle = {
        if (isAdded && !isDetached) {
            wifiNotFoundLink.visibility = View.VISIBLE
        }
    }

    private lateinit var searchingContainer : View
    private lateinit var scanRecyclerView : RecyclerView

    private lateinit var pairingDevice : String

    private var selectedNetwork : BleWiFiNetwork? = null
    private var bleStepsNavigationDelegate by Delegates.notNull<BleStepsNavigationDelegate>()
    private val presenter = AndroidBleWiFiSelectPresenter()
    private lateinit var wifiNotFoundUri: Uri
    private lateinit var wifiNotFoundLink: ScleraLinkView

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bleStepsNavigationDelegate = context as BleStepsNavigationDelegate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ble_wifi_select, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            pairingDevice = bundle.getString(ARG_DEVICE_NAME)!!
            wifiNotFoundUri = bundle.getParcelable(ARG_WIFI_NOT_FOUND_URI)!!
        }

        searchingContainer = view.findViewById(R.id.searching_container)
        scanRecyclerView = view.findViewById(R.id.scan_results)
        scanRecyclerView.layoutManager = LinearLayoutManager(context)
        view.findViewById<ScleraTextView>(R.id.title).text = getString(
            R.string.which_wifi_to_use_for_setup,
            pairingDevice
        )
        wifiNotFoundLink = view.findViewById(R.id.dont_see_device)
        wifiNotFoundLink.setOnClickListener { _ ->
            context?.let { context ->
                startActivity(GenericFragmentActivity.getLaunchIntent(
                    context,
                    WiFiTipsFragment::class.java,
                    WiFiTipsFragment.createBundle(wifiNotFoundUri)
                ))
            }
        }
        ViewCompat.setNestedScrollingEnabled(scanRecyclerView, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.clearView()
    }

    override fun getTitle() : String = pairingDevice

    override fun onPageSelected() {
        updateSelectedNetwork()
        presenter.setView(this)
        presenter.initializeBleInteractionCallback()
        presenter.scanForNetworks(selectedNetwork)

        val wifiMgr = context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        presenter.setCurrentWiFiConnection(wifiMgr?.connectionInfo?.ssid)
    }

    private fun updateSelectedNetwork() {
        val currentSelection = selectedNetwork

        if(currentSelection != null){
            wifiNotFoundLink.visibility = View.INVISIBLE
        }

        presenter.setSelectedNetwork(currentSelection)
        if (currentSelection == null) {
            bleStepsNavigationDelegate.disableContinue()
        } else {
            bleStepsNavigationDelegate.enableContinue()
            bleStepsNavigationDelegate.setSelectedNetwork(currentSelection)
        }

        if (wifiNotFoundLink.visibility != View.VISIBLE) {
            wifiNotFoundLink.removeCallbacks(wifiHelpVisibiltyToggle)
            wifiNotFoundLink.postDelayed(wifiHelpVisibiltyToggle, MS_DELAY_HELP_LINK)
        }
    }

    override fun onNotSelected() {
        presenter.clearView()
        presenter.stopScanningForNetworks()
    }

    override fun setBleConnector(bleConnector: BleConnector<Context>) {
        presenter.setBleConnector(bleConnector)
    }

    override fun onNetworksFound(networks: List<BleWiFiNetwork>) {
        scanRecyclerView.adapter = BleSelectWifIAdapter(networks, selectedNetwork, onNetworkSelected)
    }

    override fun onNoNetworksFound() {
        scanRecyclerView.adapter = null
        selectedNetwork = null
        updateSelectedNetwork()
    }

    override fun onUnhandledError() {
        // No-Op Log?
    }

    override fun onScanningForNetworksActive(active: Boolean) {
        searchingContainer.visibility = if (active) View.VISIBLE else View.INVISIBLE
    }

    override fun onBleStatusChange(status: BleConnectionStatus) {
        when (status) {
            BleConnectionStatus.BLE_CONNECTED -> {
                // No - Op
            }

            BleConnectionStatus.BLE_CONNECT_FAILURE,
            BleConnectionStatus.BLE_DISCONNECTED -> {
                val popup = BleDisconnectedPopup()
                popup.okButtonClickAction = {
                    bleStepsNavigationDelegate.rewindToEnterBleModeInstructionsPage()
                }
                popup.show(fragmentManager)
            }
        }
    }

    override fun currentNetworkSet(network: BleWiFiNetwork) {
        selectedNetwork = network
        updateSelectedNetwork()
    }

    companion object {
        private const val ARG_DEVICE_NAME = "ARG_DEVICE_NAME"
        private const val ARG_WIFI_NOT_FOUND_URI = "ARG_WIFI_NOT_FOUND_URI"

        private val MS_DELAY_HELP_LINK = TimeUnit.SECONDS.toMillis(30)

        @JvmStatic
        fun newInstance(
            shortName: String,
            wifiNotFoundUri: Uri
        ) = BleWiFiSelectFragment().also {
            with(Bundle()) {
                putString(ARG_DEVICE_NAME, shortName)
                putParcelable(ARG_WIFI_NOT_FOUND_URI, wifiNotFoundUri)
                it.arguments = this
            }
        }
    }
}
