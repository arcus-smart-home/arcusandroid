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
package arcus.app.pairing.device.customization.halo.room

import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.customization.halo.room.HaloRoom

class HaloRoomAdapter(
    firstSelection : Int,
    private val haloRooms : List<HaloRoom>,
    private val callback : HaloRoomAdapterCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var lastSelection = firstSelection

    interface HaloRoomAdapterCallback {
        fun onSelectionChanged()
    }

    override fun getItemCount() = haloRooms.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HaloRoomViewHolder(parent.inflate(R.layout.list_item_halo_room),
                callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HaloRoomViewHolder).bindView(haloRooms[position], (lastSelection == position))
    }

    fun updateLastSelection(last: Int) {
        lastSelection = last
    }

    inner class HaloRoomViewHolder(view: View,
                                   val callback : HaloRoomAdapterCallback
    ) : RecyclerView.ViewHolder(view) {
        val roomName = view.findViewById<ScleraTextView>(R.id.room_name)
        val roomSelector = view.findViewById<AppCompatRadioButton>(R.id.room_selector)

        fun bindView(room: HaloRoom,
                     checked: Boolean) = with(itemView) {
            roomSelector.isChecked = checked
            roomName.text = room.displayName

            this.setOnClickListener {
                roomSelector.isChecked = true
                updateLastSelection(adapterPosition)
                notifyDataSetChanged()
                callback.onSelectionChanged()
            }
        }
    }
}
