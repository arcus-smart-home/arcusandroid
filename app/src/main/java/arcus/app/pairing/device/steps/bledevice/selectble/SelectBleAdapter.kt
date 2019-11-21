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
package arcus.app.pairing.device.steps.bledevice.selectble

import android.content.res.ColorStateList
import androidx.annotation.ColorRes
import androidx.fragment.app.FragmentActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.ble.BleDevice

class SelectBleAdapter(
    itemList: List<BleDevice>,
    private val activity: FragmentActivity,
    private val clickedCallback: (BleDevice) -> Unit
) : RecyclerView.Adapter<SelectBleAdapter.BleViewHolder>() {
    private val items = itemList.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BleViewHolder {
        return BleViewHolder(parent.inflate(R.layout.item_ble_device_select_result_row))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: BleViewHolder, position: Int) {
        val item = items[position]
        holder.bindView(item)
        holder.itemView?.setOnClickListener {
            clickedCallback(item)
        }
    }

    fun showDeviceConnected(device: BleDevice) {
        items
            .onEach { it.isConnected = it == device }
            .let { notifyDataSetChanged() }
    }

    fun showNoDevicesConnected() {
        items
            .onEach { it.isConnected = false }
            .let { notifyDataSetChanged() }
    }

    inner class BleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name = view.findViewById<ScleraTextView>(R.id.name)
        private val tapText = view.findViewById<ScleraTextView>(R.id.tap_to_connect)

        fun bindView(device: BleDevice) {
            name.text = device.name.replace("_", " ")
            if (device.isConnected) {
                name.setTextColor(getColor(R.color.sclera_green))
                tapText.text = activity.getString(R.string.connected)
                tapText.setTextColor(getColor(R.color.sclera_disabled_color))
            } else {
                name.setTextColor(getColor(R.color.black))
                tapText.text = activity.getString(R.string.tap_to_connect)
                tapText.setTextColor(getColor(R.color.sclera_green))
            }
        }

        private fun getColor(@ColorRes color: Int) = ColorStateList.valueOf(
            ContextCompat.getColor(itemView.context, color)
        )
    }
}
