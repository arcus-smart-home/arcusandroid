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
package arcus.app.pairing.device.customization.orbit.list

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.activities.GenericFragmentActivity
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.customization.orbit.list.IrrigationZone
import arcus.app.pairing.device.customization.orbit.edit.OrbitZoneEditFragment
import arcus.presentation.pairing.device.customization.CustomizationStep
import arcus.presentation.pairing.device.customization.CustomizationType

class OrbitZonesAdapter(
        val pairingDeviceAddress: String,
        val stepId: String,
        val activity : Activity,
        val zones: List<IrrigationZone>) : RecyclerView.Adapter<CustomizationListItemViewHolder>() {

    private val itemsList = zones.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizationListItemViewHolder {

        return CustomizationListItemViewHolder(
            parent.inflate(R.layout.customization_step_list, false),
            activity
        )
    }

    override fun getItemCount(): Int = itemsList.size

    override fun onBindViewHolder(holder: CustomizationListItemViewHolder, position: Int) {
        holder.bindView(itemsList[position], pairingDeviceAddress, stepId)
    }
}

class CustomizationListItemViewHolder(view: View, val activity: Activity) : RecyclerView.ViewHolder(view){
    private val zoneName = view.findViewById<ScleraTextView>(R.id.list_item_name)
    private val zoneDescription = view.findViewById<ScleraTextView>(R.id.list_item_description)

    fun bindView(zone: IrrigationZone, pairingDeviceAddress: String, stepId: String) = with(itemView) {
        zoneName.text = zone.zone
        zoneDescription.text = zone.name
        if (zone.name.isNotBlank()) {
            zoneDescription.visibility = View.VISIBLE
        } else {
            zoneDescription.visibility = View.GONE
        }

        setOnClickListener {
            // Go to naming
            val bundle = OrbitZoneEditFragment.createArgumentBundle(
                pairingDeviceAddress,
                CustomizationStep(
                    stepId /* "customization/irrigationzone/orbitzone" */,
                    1,
                    CustomizationType.IRRIGATION_ZONE,
                    null,
                    null,
                    emptyList()
                ),
                true,
                R.string.generic_save_text,
                true,
                zone.zoneInstanceId
            )
            activity.startActivity(
                GenericFragmentActivity.getLaunchIntent(
                    activity,
                    OrbitZoneEditFragment::class.java,
                    bundle
                )
            )
        }
    }
}
