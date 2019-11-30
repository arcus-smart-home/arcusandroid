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
package arcus.app.hub.wifi.scan

import android.content.Context
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import arcus.app.R
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.inflate
import arcus.app.common.utils.swapAdapaterIfPresent
import arcus.app.hub.wifi.connect.HubWiFiConnectFragment
import arcus.presentation.hub.wifi.scan.HubAvailableWiFiNetwork
import arcus.presentation.hub.wifi.scan.HubWiFiScanPresenter
import arcus.presentation.hub.wifi.scan.HubWiFiScanPresenterImpl
import arcus.presentation.hub.wifi.scan.HubWiFiScanView
import kotlin.properties.Delegates


class HubWiFiScanFragment : Fragment(), HubWiFiScanView {
    private lateinit var searchingContainer: View
    private lateinit var dontSeeDevice: View
    private lateinit var scanResults: RecyclerView
    private lateinit var nextButton: Button
    private lateinit var alertBanner: View
    private lateinit var title: TextView
    private var fragmentContainerHolder: FragmentContainerHolder? = null

    private val hubWiFiScanPresenter: HubWiFiScanPresenter = HubWiFiScanPresenterImpl()
    private val currentWiFiNetwork by lazy {
        val wifiMgr = context?.applicationContext?.getSystemService(Context.WIFI_SERVICE) as WifiManager?
        wifiMgr?.connectionInfo?.ssid?.replace("\"", "")
    }

    override var selectedNetwork by Delegates.observable(HubAvailableWiFiNetwork.EMPTY) { _, _, newValue ->
        if (isAdded && !isDetached && alertBanner.visibility == View.GONE) {
            nextButton.isEnabled = newValue != HubAvailableWiFiNetwork.EMPTY
        }
    }
    override var showCantFindDevice by Delegates.observable(false) { _, _, newValue ->
        if (isAdded && !isDetached) {
            dontSeeDevice.visibility = if (newValue) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_hub_wifi_scan)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        title = view.findViewById(R.id.title)
        searchingContainer = view.findViewById(R.id.searching_container)
        dontSeeDevice = view.findViewById(R.id.dont_see_device)
        scanResults = view.findViewById(R.id.scan_results)
        nextButton = view.findViewById(R.id.next_button)
        alertBanner = view.findViewById(R.id.alert_banner)

        dontSeeDevice.setOnClickListener {
            ActivityUtils.launchUrl(Uri.parse(GlobalSetting.HUB_WIFI_NEED_HELP_URL))
        }

        nextButton.isEnabled = false
        nextButton.setOnClickListener {
            fragmentContainerHolder?.replaceFragmentContainerWith(
                HubWiFiConnectFragment.newInstance(selectedNetwork)
            )
        }
        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            activity?.finish()
        }

        scanResults.let { rv ->
            ViewCompat.setNestedScrollingEnabled(rv, false)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        fragmentContainerHolder = context as? FragmentContainerHolder?
    }

    override fun onResume() {
        super.onResume()
        hubWiFiScanPresenter.setView(this)
        hubWiFiScanPresenter.getHubShortName()
        hubWiFiScanPresenter.scanNetworks()

        fragmentContainerHolder?.run {
            showBackButtonOnToolbar(false)
            setTitle(getString(R.string.hub_wifi_connect_after_paired_toolbar_title))
        }
    }

    override fun onPause() {
        super.onPause()
        hubWiFiScanPresenter.stopScanningNetworks()
        hubWiFiScanPresenter.clearView()
    }

    override fun onHubShortNameFound(name: String) {
        title.text = getString(R.string.hub_wifi_connect_after_paired_title, name)
    }

    override fun onSearching() {
        searchingContainer.visibility = View.VISIBLE
    }

    override fun onDoneSearching() {
        searchingContainer.visibility = View.INVISIBLE
    }

    override fun onNetworksFound(networks: List<HubAvailableWiFiNetwork>) {
        val adapterItems = hubWiFiScanPresenter.sortNetworksAndSetSelected(
            networks,
            currentWiFiNetwork,
            selectedNetwork
        )

        val newAdapter = HubWiFiScanAdapter(adapterItems, selectedNetwork) { maybeNetwork ->
            maybeNetwork?.let { nnNetwork ->
                selectedNetwork = nnNetwork
            }
        }

        view?.findViewById<RecyclerView>(R.id.scan_results)?.swapAdapaterIfPresent(newAdapter)
    }

    override fun onHubDisconnected() {
        alertBanner.visibility = View.VISIBLE
        nextButton.isEnabled = false
        hubWiFiScanPresenter.stopScanningNetworks()
    }

    override fun onHubConnected() {
        alertBanner.visibility = View.GONE
        val current = selectedNetwork
        selectedNetwork = current
        hubWiFiScanPresenter.scanNetworks()
    }
}
