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
package arcus.app.pairing.device.steps.wifismartswitch

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import androidx.fragment.app.Fragment
import java.util.concurrent.atomic.AtomicBoolean

abstract class WiFiNetworkBaseFragment : Fragment() {
    private val isRegistered = AtomicBoolean(false)
    private val networkSearchRunnable : Runnable = Runnable {
        wiFiManager?.let {
            context?.registerReceiver(scanResultsReceiver, scanFilter)
            isRegistered.set(true)
            it.startScan()
        }
        searchingForNetworks()
    }

    private val scanResultsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (continueSearchingInLoop) {
                view?.postDelayed(networkSearchRunnable, SEARCH_DELAY_MS)
            }
            onResultsReceived(wiFiManager?.scanResults ?: emptyList(), wiFiManager?.connectionInfo)
        }
    }
    private val scanFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
    private val wiFiManager : WifiManager?
        get() = context?.getSystemService(Context.WIFI_SERVICE) as WifiManager

    open var continueSearchingInLoop : Boolean = true

    fun startNetworkSearch() {
        networkSearchRunnable.run()
    }

    override fun onPause() {
        super.onPause()
        stopNetworkSearch()
    }

    fun stopNetworkSearch() {
        view?.removeCallbacks(networkSearchRunnable)

        if (isRegistered.compareAndSet(true, false)) {
            context?.unregisterReceiver(scanResultsReceiver)
        }
    }

    abstract fun searchingForNetworks()

    abstract fun onResultsReceived(scanResults: List<ScanResult>, currentNetwork: WifiInfo?)

    companion object {
        const val SEARCH_DELAY_MS = 10_000L
    }
}
