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
package arcus.app.subsystems.rules.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.presentation.rules.list.ListItem

class RuleListAdapter(
        private val ruleAdapterClickListener: ClickListener
) : ListAdapter<ListItem, ViewHolder>(object : ItemCallback<ListItem>() {
    override fun areItemsTheSame(
            oldItem: ListItem,
            newItem: ListItem
    ): Boolean = oldItem.areItemsTheSame(newItem)

    override fun areContentsTheSame(
            oldItem: ListItem,
            newItem: ListItem
    ): Boolean = oldItem.areContentsTheSame(newItem)
}) {
    private val updateCallback: (Int) -> Unit = { notifyItemChanged(it) }
    var isEditMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): ViewHolder = when (viewType) {
        1 -> Header(parent.inflate(R.layout.section_heading_with_count))
        else -> Item(parent.inflate(R.layout.list_item_rule))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is Header -> holder.bind(item as ListItem.Header)
            is Item -> holder.bind(
                    item as ListItem.Rule,
                    isEditMode,
                    ruleAdapterClickListener,
                    updateCallback
            )
        }
    }

    override fun getItemViewType(
            position: Int
    ): Int = if (getItem(position) is ListItem.Header) 1 else 2


    interface ClickListener {
        fun onRuleItemClick(item: ListItem.Rule)
        fun onRuleCheckboxClicked(item: ListItem.Rule)
    }

    class Header(view: View) : ViewHolder(view) {
        private val name = itemView.findViewById<TextView>(R.id.sectionName)
        private val count = itemView.findViewById<TextView>(R.id.sectionCount)

        fun bind(item: ListItem.Header) {
            name.text = item.title
            count.text = "${item.ruleCount}"
        }
    }

    class Item(view: View) : ViewHolder(view) {
        private val checkboxClickRegion = itemView.findViewById<View>(R.id.checkboxClickRegion)
        private val chevronClickRegion = itemView.findViewById<View>(R.id.chevronClickRegion)
        private val selectedRadioButton = itemView.findViewById<RadioButton>(R.id.selectedRadioButton)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
        private val scheduleIcon = itemView.findViewById<ImageView>(R.id.scheduleIcon)
        private val deleteIcon = itemView.findViewById<ImageView>(R.id.deleteIcon)

        fun bind(
                item: ListItem.Rule,
                isEditMode: Boolean,
                listener: ClickListener,
                updateCallback: (Int) -> Unit
        ) {
            checkboxClickRegion.setOnClickListener {
                listener.onRuleCheckboxClicked(item)
                updateCallback(layoutPosition)
            }
            chevronClickRegion.setOnClickListener {
                listener.onRuleItemClick(item)
            }
            title.text = item.title
            subtitle.text = item.subtitle
            scheduleIcon.visibility = if (item.hasSchedule) View.VISIBLE else View.GONE
            selectedRadioButton.isChecked = item.isEnabled

            if (isEditMode) {
                selectedRadioButton.visibility = View.INVISIBLE
                deleteIcon.visibility = View.VISIBLE
            } else {
                selectedRadioButton.visibility = View.VISIBLE
                deleteIcon.visibility = View.INVISIBLE
            }
        }
    }
}
