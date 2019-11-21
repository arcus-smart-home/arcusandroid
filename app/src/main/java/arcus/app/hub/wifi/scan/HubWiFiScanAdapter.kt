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

import android.content.res.ColorStateList
import android.net.wifi.WifiManager
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.hub.wifi.scan.HubAvailableWiFiNetwork

class HubWiFiScanAdapter(
    itemList: List<HubAvailableWiFiNetwork>,
    currentSelection: HubAvailableWiFiNetwork?,
    private val selectedCallback: (HubAvailableWiFiNetwork?) -> Unit
) : RecyclerView.Adapter<HubWiFiScanAdapter.HubWifiViewHolder>() {
    private val items = itemList.toList()
    private var selection = currentSelection?.let { current ->
        items.indexOfFirst { network ->
            network.ssid == current.ssid
        }
    } ?: -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HubWifiViewHolder {
        return HubWifiViewHolder(parent.inflate(R.layout.item_ble_wifi_select_result_row))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: HubWifiViewHolder, position: Int) {
        val item = items[position]
        holder?.bindView(item, position == selection)
    }



    inner class HubWifiViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageAndName = view.findViewById<ScleraTextView>(R.id.signal_and_name)

        fun bindView(network: HubAvailableWiFiNetwork, isSelected: Boolean) {
            if (network.isOtherNetwork) {
                imageAndName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                imageAndName.text = itemView.context.getString(R.string.wifi_other_network)
            } else {
                val signal = WifiManager.calculateSignalLevel(network.signal, 5)
                val icon = if (network.isSecure()) {
                    getSecuredSignalStrengthImage(signal)
                } else {
                    getUnsecuredSignalStrengthImage(signal)
                }
                imageAndName.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
                imageAndName.text = network.ssid
            }

            if (isSelected) {
                imageAndName.setTextColor(getColor(R.color.sclera_green))
                imageAndName.setOnClickListener(null)
            } else {
                imageAndName.setTextColor(getColor(R.color.black))
                itemView.setOnClickListener {
                    val previous = selection
                    selection = adapterPosition

                    notifyItemChanged(previous)
                    notifyItemChanged(selection)

                    selectedCallback(network)
                }
            }
        }

        @DrawableRes
        private fun getSecuredSignalStrengthImage(signalStrength: Int) : Int {
            return when (signalStrength) {
                0 -> R.drawable.wi_fi_lock_1_23x20
                1 -> R.drawable.wi_fi_lock_2_23x20
                2 -> R.drawable.wi_fi_lock_3_23x20
                3 -> R.drawable.wi_fi_lock_4_23x20
                else -> R.drawable.wi_fi_lock_5_23x20
            }
        }

        @DrawableRes
        private fun getUnsecuredSignalStrengthImage(signalStrength: Int) : Int {
            return when (signalStrength) {
                0 -> R.drawable.wifi_unsecured_1_27x20
                1 -> R.drawable.wifi_unsecured_2_27x20
                2 -> R.drawable.wifi_unsecured_3_27x20
                3 -> R.drawable.wifi_unsecured_4_27x20
                else -> R.drawable.wifi_unsecured_5_27x20
            }
        }

        private fun getColor(@ColorRes color: Int) = ColorStateList.valueOf(
            ContextCompat.getColor(itemView.context, color)
        )
    }
}
