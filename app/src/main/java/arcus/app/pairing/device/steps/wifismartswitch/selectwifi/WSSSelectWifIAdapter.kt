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

import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.steps.wifismartswitch.selectwifi.SignalStrength
import arcus.presentation.pairing.device.steps.wifismartswitch.selectwifi.WiFiNetwork

class WSSSelectWifIAdapter(
    itemList: List<WiFiNetwork>,
    currentSelection: WiFiNetwork?,
    val selectedCallback: (WiFiNetwork?) -> Unit
) : RecyclerView.Adapter<WSSSelectWifIAdapter.WSSViewHolder>() {
    private val items = if (currentSelection != null && !itemList.contains(currentSelection)) {
        listOf(currentSelection) + itemList.toList()
    } else {
        itemList.toList()
    }
    var selection = currentSelection

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WSSViewHolder {
        return WSSViewHolder(parent.inflate(R.layout.item_wss_wifi_select_result_row))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: WSSViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item, item.name == selection?.name)
    }



    inner class WSSViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val imageAndName = view.findViewById<ScleraTextView>(R.id.signal_and_name)

        fun bindView(network: WiFiNetwork, isSelected: Boolean) {
            if (network.isOtherNetwork) {
                imageAndName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                val icon = if (network.isSecured) {
                    getSecuredSignalStrengthImage(network.signalStrength)
                } else {
                    getUnsecuredSignalStrengthImage(network.signalStrength)
                }
                imageAndName.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0)
            }

            if (isSelected) {
                imageAndName.setTextColor(getColor(R.color.sclera_green))
                imageAndName.setOnClickListener(null)
            } else {
                imageAndName.setTextColor(getColor(R.color.black))
                itemView.setOnClickListener {
                    selectedCallback(network)
                    selection = network
                    notifyDataSetChanged()
                }
            }
            imageAndName.text = network.name
        }

        @DrawableRes
        private fun getSecuredSignalStrengthImage(signalStrength: SignalStrength) : Int {
            return when (signalStrength) {
                SignalStrength.LEVEL_1 -> R.drawable.wi_fi_lock_1_23x20
                SignalStrength.LEVEL_2 -> R.drawable.wi_fi_lock_2_23x20
                SignalStrength.LEVEL_3 -> R.drawable.wi_fi_lock_3_23x20
                SignalStrength.LEVEL_4 -> R.drawable.wi_fi_lock_4_23x20
                SignalStrength.LEVEL_5 -> R.drawable.wi_fi_lock_5_23x20
            }
        }

        @DrawableRes
        private fun getUnsecuredSignalStrengthImage(signalStrength: SignalStrength) : Int {
            return when (signalStrength) {
                // TODO: Update to correct asset.
                SignalStrength.LEVEL_1 -> R.drawable.wifi_unsecured_1_27x20
                SignalStrength.LEVEL_2 -> R.drawable.wifi_unsecured_2_27x20
                SignalStrength.LEVEL_3 -> R.drawable.wifi_unsecured_3_27x20
                SignalStrength.LEVEL_4 -> R.drawable.wifi_unsecured_4_27x20
                SignalStrength.LEVEL_5 -> R.drawable.wifi_unsecured_5_27x20
            }
        }

        private fun getColor(@ColorRes color: Int) = ColorStateList.valueOf(
            ContextCompat.getColor(itemView.context, color)
        )
    }
}
