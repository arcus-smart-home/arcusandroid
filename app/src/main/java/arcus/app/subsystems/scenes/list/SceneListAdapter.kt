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
package arcus.app.subsystems.scenes.list

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import arcus.app.R
import arcus.app.common.fragments.ModalErrorBottomSheetSingleButton
import arcus.app.common.utils.inflate
import arcus.app.subsystems.scenes.drawableRes
import arcus.presentation.scenes.Scene

class SceneListAdapter(
    private val sceneAdapterClickListener: ClickListener,
    private val fragmentManager: FragmentManager
) : ListAdapter<Scene, SceneListAdapter.Item>(object : ItemCallback<Scene>() {
    override fun areItemsTheSame(
        oldItem: Scene,
        newItem: Scene
    ): Boolean = oldItem.areItemsTheSame(newItem)

    override fun areContentsTheSame(
        oldItem: Scene,
        newItem: Scene
    ): Boolean = oldItem.areContentsTheSame(newItem)
}) {
    var isEditMode: Boolean = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Item = Item(parent.inflate(R.layout.list_item_scene), fragmentManager)

    override fun onBindViewHolder(holder: Item, position: Int) {
        val item = getItem(position)
        holder.bind(
            item,
            isEditMode,
            sceneAdapterClickListener
        )
    }

    interface ClickListener {
        fun onSceneCheckAreaClick(item: Scene)
        fun onSceneItemAreaClick(item: Scene)
    }

    class Item(
        view: View,
        private val fragmentManager: FragmentManager
    ) : ViewHolder(view) {
        private val checkboxClickRegion = itemView.findViewById<View>(R.id.checkboxClickRegion)
        private val chevronClickRegion = itemView.findViewById<View>(R.id.chevronClickRegion)
        private val selectedRadioButton = itemView.findViewById<RadioButton>(R.id.selectedRadioButton)
        private val sceneImage = itemView.findViewById<ImageView>(R.id.sceneImage)
        private val title = itemView.findViewById<TextView>(R.id.title)
        private val subtitle = itemView.findViewById<TextView>(R.id.subtitle)
        private val scheduleIcon = itemView.findViewById<ImageView>(R.id.scheduleIcon)
        private val deleteIcon = itemView.findViewById<ImageView>(R.id.deleteIcon)

        fun bind(
            item: Scene,
            isEditMode: Boolean,
            listener: ClickListener
        ) {
            sceneImage.setImageResource(item.type.drawableRes())
            checkboxClickRegion.setOnClickListener {
                if (item.hasSchedule || isEditMode) {
                    listener.onSceneCheckAreaClick(item)
                } else {
                    ModalErrorBottomSheetSingleButton
                        .newInstance(
                            itemView.context.getString(R.string.water_schedule_no_events),
                            itemView.context.getString(R.string.water_schedule_no_events_sub),
                            itemView.context.getString(R.string.dismiss_text)
                        )
                        .show(fragmentManager)
                }
            }
            chevronClickRegion.setOnClickListener { listener.onSceneItemAreaClick(item) }
            title.text = item.name
            subtitle.text = subtitle
                .resources
                .getQuantityString(R.plurals.actions_plural, item.actionCount, item.actionCount)
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
