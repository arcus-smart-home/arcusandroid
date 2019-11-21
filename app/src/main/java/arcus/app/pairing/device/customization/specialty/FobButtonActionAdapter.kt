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
package arcus.app.pairing.device.customization.specialty

import android.app.Activity
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.app.device.buttons.model.ButtonAction

class FobButtonActionAdapter(
    firstSelection : Int,
    private val buttonActionList: List<ButtonAction>,
    private val callback: FobButtonActionAdapterCallback,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var currentPosition = firstSelection

    interface FobButtonActionAdapterCallback {
        fun onSelectionChanged()
    }

    override fun getItemCount() = buttonActionList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ButtonActionViewHolder(parent.inflate(R.layout.fob_button_action_list_item),
                callback)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ButtonActionViewHolder).bindView(buttonActionList[position], (currentPosition == position))
    }

    fun updateLastSelection(last: Int) {
        currentPosition = last
    }

    inner class ButtonActionViewHolder(view: View, val callback: FobButtonActionAdapterCallback)
        : RecyclerView.ViewHolder(view) {
        val buttonActionName = view.findViewById<ScleraTextView>(R.id.fob_button_action_item_text)
        val buttonActionRadioButton = view.findViewById<AppCompatRadioButton>(R.id.fob_button_action_item_radio_button)

        fun bindView(action: ButtonAction,
                     checked: Boolean) = with(itemView) {
            buttonActionRadioButton.isChecked = checked
            buttonActionName.text = activity.getString(action.stringResId)

            this.setOnClickListener {
                buttonActionRadioButton.isChecked = true
                updateLastSelection(adapterPosition)
                notifyDataSetChanged()
                callback.onSelectionChanged()
            }
        }
    }
}
