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
package arcus.app.account.settings.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import arcus.app.R
import com.iris.client.model.MobileDeviceModel

class MobileDeviceListAdapter(context: Context?) :
    ArrayAdapter<MobileDeviceModel?>(context, 0) {
    var editEnabled = false
        set(enabled) {
            field = enabled
            notifyDataSetChanged()
        }
    private var listener: OnDeleteListener? = null

    interface OnDeleteListener {
        fun onDelete(mobileDeviceModel: MobileDeviceModel?)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_mobile_device, parent, false)
        }
        val deviceName = convertView!!.findViewById<TextView>(R.id.deviceName)
        val deviceType = convertView.findViewById<TextView>(R.id.deviceType)
        val deleteButton = convertView.findViewById<ImageView>(R.id.deleteButton)
        val divider = convertView.findViewById<View>(R.id.divider)
        val model = getItem(position)
        deviceName.text = getDeviceName(model)
        deviceType.text = context.getString(R.string.device_type, model!!.deviceModel.toString())
        deleteButton.visibility = if (editEnabled) View.VISIBLE else View.GONE
        divider.visibility = View.VISIBLE
        deleteButton.setOnClickListener {
            if (listener != null) {
                listener!!.onDelete(getItem(position))
            }
            super@MobileDeviceListAdapter.remove(getItem(position))
        }
        return convertView
    }

    /**
     * Attempts to parse the OS type and version from the strings presents in the MobileDeviceModel.
     * This is a bad idea and should be refactored. No assurances that these string formats won't
     * change in the future.
     *
     * @param deviceModel
     * @return
     */
    fun getDeviceName(deviceModel: MobileDeviceModel?): String {
        if ("ios".equals(deviceModel!!.osType, ignoreCase = true)) { // Assumes iOS version string looks like
            return if (deviceModel.osVersion != null && deviceModel.osVersion.split(" ").toTypedArray().size == 4) {
                context.getString(
                    R.string.device_name,
                    deviceModel.osType.toUpperCase(),
                    deviceModel.osVersion.split(" ").toTypedArray()[1]
                )
            } else {
                deviceModel.osType.toUpperCase()
            }
        } else if ("android".equals(deviceModel.osType, ignoreCase = true)) {
            return if (deviceModel.osVersion != null && deviceModel.osVersion.split(" ").toTypedArray().size == 3) {
                context.getString(
                    R.string.device_name,
                    deviceModel.osType.toUpperCase(),
                    deviceModel.osVersion.split(" ").toTypedArray()[2]
                )
            } else {
                deviceModel.osType.toUpperCase()
            }
        }
        return context.getString(R.string.device_name_unknown)
    }

    fun setOnDeleteListener(listener: OnDeleteListener?) {
        this.listener = listener
    }
}
