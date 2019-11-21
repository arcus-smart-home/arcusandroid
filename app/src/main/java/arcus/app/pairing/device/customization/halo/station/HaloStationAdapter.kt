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
package arcus.app.pairing.device.customization.halo.station

import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.customization.halo.station.RadioStation

class HaloStationAdapter(
    defaultSelection : Int,
    private var stations : List<RadioStation>,
    private val callback : HaloStationAdapterCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var stationPlaying : Int? = null
    var lastSelection = defaultSelection

    interface HaloStationAdapterCallback {
        fun onSelectionChanged()
        fun onPlaybackChanged(playing: Int?)
    }

    override fun getItemCount() = stations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return HaloStationViewHolder(parent.inflate(R.layout.halo_station_item),
                callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HaloStationViewHolder).bindView(
                stations[position],
                (lastSelection == position),
                (stationPlaying == position)
        )
    }

    fun updateLastSelection(last: Int) {
        lastSelection = last
    }

    fun updateLastPlaying(last: Int?) : Int? {
        stationPlaying = last
        return stationPlaying
    }

    inner class HaloStationViewHolder(view: View,
                                   val callback : HaloStationAdapterCallback
    ) : RecyclerView.ViewHolder(view) {
        private val stationSelector = view.findViewById<AppCompatRadioButton>(R.id.station_selector)
        private val stationName = view.findViewById<ScleraTextView>(R.id.station_name)
        private val stationFreq = view.findViewById<ScleraTextView>(R.id.station_freq)
        private val playSelector = view.findViewById<AppCompatRadioButton>(R.id.play_selector)

        fun bindView(station: RadioStation,
                     checked: Boolean,
                     playing: Boolean) = with(itemView) {
            stationSelector.isChecked = checked
            playSelector.isChecked = playing
            stationName.text = station.name
            stationFreq.text = station.frequency

            itemView.setOnClickListener {
                stationSelector.isChecked = true
                updateLastSelection(adapterPosition)
                notifyDataSetChanged()
                callback.onSelectionChanged()
            }

            playSelector.setOnClickListener {
                playSelector.isChecked = playing
                /**
                 * If nothing was playing, return the position clicked.
                 * Otherwise, null to turn off the radio
                 **/
                val stationPlaying = if(playSelector.isChecked) {
                    null
                } else {
                    stationSelector.isChecked = true
                    updateLastSelection(adapterPosition)
                    callback.onSelectionChanged()
                    adapterPosition
                }
                updateLastPlaying(stationPlaying)
                notifyDataSetChanged()
                callback.onPlaybackChanged(stationPlaying)
            }
        }
    }
}
