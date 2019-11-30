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
package arcus.app.pairing.device.steps.wifismartswitch.selectwifi

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import arcus.app.pairing.device.steps.wifismartswitch.WSSStepsNavigationDelegate
import arcus.app.pairing.device.steps.wifismartswitch.WiFiNetworkBaseFragment
import arcus.app.pairing.device.steps.wifismartswitch.connect.WSSConnectFragment
import arcus.presentation.pairing.device.steps.wifismartswitch.selectwifi.AndroidWSSWiFiSelectPresenterImpl
import arcus.presentation.pairing.device.steps.wifismartswitch.selectwifi.WSSWiFiSelectPresenter
import arcus.presentation.pairing.device.steps.wifismartswitch.selectwifi.WiFiNetwork
import kotlin.properties.Delegates

class WSSWiFiSelectFragment : WiFiNetworkBaseFragment(),
    TitledFragment, ViewPagerSelectedFragment {
    private val onNetworkSelected : (WiFiNetwork?) -> Unit = {
        selectedNetwork = it
        enableDisableContinue()
    }

    private var searchingContainer : View? = null
    private lateinit var scanRecyclerView : RecyclerView
    private var selectedNetwork : WiFiNetwork? = null
    private var wssStepsNavigationDelegate by Delegates.notNull<WSSStepsNavigationDelegate>()
    private val presenter : WSSWiFiSelectPresenter<ScanResult, WifiInfo> =
        AndroidWSSWiFiSelectPresenterImpl()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wssStepsNavigationDelegate = context as WSSStepsNavigationDelegate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wss_wifi_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchingContainer = view.findViewById(R.id.searching_container)
        scanRecyclerView = view.findViewById(R.id.scan_results)
        scanRecyclerView.layoutManager = LinearLayoutManager(context)
        ViewCompat.setNestedScrollingEnabled(scanRecyclerView, false)
    }

    override fun getTitle() : String = getString(R.string.wifi_smart_switch_title)

    override fun onPageSelected() {
        enableDisableContinue()
        startNetworkSearch()
    }

    override fun onNotSelected() {
        stopNetworkSearch()
    }

    private fun enableDisableContinue() {
        val selected = selectedNetwork
        if (selected == null) {
            wssStepsNavigationDelegate.disableContinue()
        } else {
            val next = wssStepsNavigationDelegate.getNextFragment()
            if (next is WSSConnectFragment) {
                wssStepsNavigationDelegate.enableContinue()
                when {
                    selected.isOtherNetwork -> next.customNetworkSelected()
                    selected.isSecured -> next.securedNetworkSelected(selected.name)
                    else -> next.unsecuredNetworkSelected(selected.name)
                }
            }
        }
    }

    override fun searchingForNetworks() {
        searchingContainer?.visibility = View.VISIBLE
    }

    override fun onResultsReceived(scanResults: List<ScanResult>, currentNetwork: WifiInfo?) {
        searchingContainer?.visibility = View.INVISIBLE
        val parsedResults = presenter
            .parseScanResults(
                scanResults,
                selectedNetwork,
                currentNetwork,
                getString(R.string.wifi_other_network)
            )

        val adapter = WSSSelectWifIAdapter(parsedResults.first, parsedResults.second, onNetworkSelected)
        onNetworkSelected(parsedResults.second)

        scanRecyclerView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() = WSSWiFiSelectFragment()
    }
}
