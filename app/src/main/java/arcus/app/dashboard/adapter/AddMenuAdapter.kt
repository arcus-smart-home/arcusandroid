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
package arcus.app.dashboard.adapter

import android.app.Activity
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView

interface MenuItemClickHandler {

    fun onAddArcusClicked()

    fun onAddAHubClicked()

    fun onAddADeviceClicked()

    fun onAddARuleClicked()

    fun onAddASceneClicked()

    fun onAddAPlaceClicked()

    fun onAddAPersonClicked()

    fun onAddCareBehaviorClicked()
}

/**
 * RecyclerView Adapter
 */
class AddMenuAdapter(
        private val clickHandler: MenuItemClickHandler,
        private val activity: Activity) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            ADD_ARCUS -> AddArcusItemViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            ADD_A_HUB -> AddHubItemViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            ADD_A_DEVICE -> AddDeviceViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            ADDA_A_RULE -> AddRuleViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            ADD_A_SCENE -> AddSceneViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            ADD_A_PLACE -> AddPlaceViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            ADD_A_PERSON -> AddPersonViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            ADD_A_CARE_BEHAVIOR -> AddCareViewHolder(clickHandler, parent.inflate(R.layout.item_add_menu_row, false))
            else -> PhotoItemViewHolder(parent.inflate(R.layout.item_add_menu_photo_row, false))
        }
    }

    override fun getItemViewType(position: Int) =
        when(position){
            0 -> SECTION_HEADER_ITEM_DEVICES
            1 -> ADD_ARCUS
            2 -> ADD_A_HUB
            3 -> ADD_A_DEVICE
            4 -> SECTION_HEADER_ITEM_RULES_SCENES
            5 -> ADDA_A_RULE
            6 -> ADD_A_SCENE
            7 -> SECTION_HEADER_ITEM_PERSON_PLACE
            8 -> ADD_A_PLACE
            9 -> ADD_A_PERSON
            10 -> SECTION_HEADER_ITEM_CARE
            else -> ADD_A_CARE_BEHAVIOR
    }

    override fun getItemCount(): Int {
        return 12
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(position) {
            SECTION_HEADER_ITEM_DEVICES -> (holder as PhotoItemViewHolder).bindView(
                    activity.getString( R.string.add_menu_devices),
                    R.drawable.device_list_placeholder)
            SECTION_HEADER_ITEM_RULES_SCENES -> (holder as PhotoItemViewHolder).bindView(
                    activity.getString( R.string.add_menu_rules_scenes),
                    R.drawable.rules_scenes_header_313x132)
            SECTION_HEADER_ITEM_PERSON_PLACE -> (holder as PhotoItemViewHolder).bindView(
                    activity.getString(R.string.add_menu_place_person),
                    R.drawable.place_person_header_313x132)
            SECTION_HEADER_ITEM_CARE -> (holder as PhotoItemViewHolder).bindView(
                    activity.getString(R.string.add_menu_care),
                    R.drawable.caregiving_person_header_313x132)
            ADD_ARCUS -> (holder as AddArcusItemViewHolder).bindView(
                    R.drawable.home_purple_45x45,
                    activity.getString(R.string.add_arcus_to_your_home),
                    activity.getString(R.string.add_arcus_to_your_home_desc)
            )
            ADD_A_HUB -> (holder as AddHubItemViewHolder).bindView(
                    R.drawable.hub_purple_45x45,
                    activity.getString(R.string.add_hub),
                    activity.getString(R.string.add_hub_desc))
            ADD_A_DEVICE -> (holder as AddDeviceViewHolder).bindView(
                    R.drawable.device_purple_45x45,
                    activity.getString(R.string.add_device),
                    activity.getString(R.string.add_device_desc)
            )
            ADDA_A_RULE -> (holder as AddRuleViewHolder).bindView(
                    R.drawable.rule_purple_45x45,
                    activity.getString(R.string.add_rule),
                    activity.getString(R.string.add_rule_desc)
            )
            ADD_A_SCENE -> (holder as AddSceneViewHolder).bindView(
                    R.drawable.scene_purple_45x45,
                    activity.getString(R.string.add_scene),
                    activity.getString(R.string.add_scene_desc)
            )
            ADD_A_PLACE -> (holder as AddPlaceViewHolder).bindView(
                    R.drawable.home_purple_45x45,
                    activity.getString(R.string.add_place),
                    activity.getString(R.string.add_place_desc)
            )
            ADD_A_PERSON -> (holder as AddPersonViewHolder).bindView(
                    R.drawable.person_purple_45x45,
                    activity.getString(R.string.add_person),
                    activity.getString(R.string.add_person_desc)
            )
            else -> (holder as AddCareViewHolder).bindView(
                    R.drawable.care_purple_45x45,
                    activity.getString(R.string.add_care),
                    activity.getString(R.string.add_care_desc)
            )
        }
    }

    companion object {
        private const val SECTION_HEADER_ITEM_DEVICES = 0
        private const val ADD_ARCUS = 1
        private const val ADD_A_HUB = 2
        private const val ADD_A_DEVICE = 3
        private const val SECTION_HEADER_ITEM_RULES_SCENES = 4
        private const val ADDA_A_RULE = 5
        private const val ADD_A_SCENE = 6
        private const val SECTION_HEADER_ITEM_PERSON_PLACE = 7
        private const val ADD_A_PLACE = 8
        private const val ADD_A_PERSON = 9
        private const val SECTION_HEADER_ITEM_CARE = 10
        private const val ADD_A_CARE_BEHAVIOR = 11
    }
}

class PhotoItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val title = view.findViewById<ScleraTextView>(R.id.title_text)
    private val image = view.findViewById<AppCompatImageView>(R.id.image)

    fun bindView(header : String,
                 photo : Int) {
        title.text = header
        image.setImageResource(photo)
        }
    }


class AddArcusItemViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)
    private val chevron = view.findViewById<AppCompatImageView>(R.id.chevron)
    private val divider = view.findViewById<View>(R.id.divider)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        if (SessionController.instance().isAccountOwner) {
            image.visibility = View.GONE
            title.visibility = View.GONE
            description.visibility = View.GONE
            chevron.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            image.setImageResource(photo)
            title.text = titleText
            description.text = descriptionText
            itemView.setOnClickListener {
                clickHandler.onAddArcusClicked()
            }
        }
    }
}

class AddHubItemViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)
    private val chevron = view.findViewById<AppCompatImageView>(R.id.chevron)
    private val divider = view.findViewById<View>(R.id.divider)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        if (SessionController.instance().isClone) {
            image.visibility = View.GONE
            title.visibility = View.GONE
            description.visibility = View.GONE
            chevron.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            image.setImageResource(photo)
            title.text = titleText
            description.text = descriptionText
            itemView.setOnClickListener {
                clickHandler.onAddAHubClicked()
            }
        }
    }
}

class AddDeviceViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        image.setImageResource(photo)
        title.text = titleText
        description.text = descriptionText
        itemView.setOnClickListener {
            clickHandler.onAddADeviceClicked()
        }
    }
}

class AddRuleViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        image.setImageResource(photo)
        title.text = titleText
        description.text = descriptionText
        itemView.setOnClickListener {
            clickHandler.onAddARuleClicked()
        }
    }
}

class AddSceneViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        image.setImageResource(photo)
        title.text = titleText
        description.text = descriptionText
        itemView.setOnClickListener {
            clickHandler.onAddASceneClicked()
        }
    }
}

class AddPlaceViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)
    private val chevron = view.findViewById<AppCompatImageView>(R.id.chevron)
    private val divider = view.findViewById<View>(R.id.divider)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        if (SessionController.instance().isClone) {
            image.visibility = View.GONE
            title.visibility = View.GONE
            description.visibility = View.GONE
            chevron.visibility = View.GONE
            divider.visibility = View.GONE
        } else {
            image.setImageResource(photo)
            title.text = titleText
            description.text = descriptionText
            itemView.setOnClickListener {
                clickHandler.onAddAPlaceClicked()
            }
        }
    }
}

class AddPersonViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        image.setImageResource(photo)
        title.text = titleText
        description.text = descriptionText
        itemView.setOnClickListener {
            clickHandler.onAddAPersonClicked()
        }
    }
}

class AddCareViewHolder(private val clickHandler: MenuItemClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val image = view.findViewById<AppCompatImageView>(R.id.menu_image)
    private val title = view.findViewById<ScleraTextView>(R.id.add_title_text)
    private val description = view.findViewById<ScleraTextView>(R.id.add_description_text)
    private val spacer = view.findViewById<View>(R.id.spacer)

    fun bindView(photo : Int,
                 titleText : String,
                 descriptionText : String) {
        image.setImageResource(photo)
        title.text = titleText
        description.text = descriptionText
        spacer.visibility = View.VISIBLE
        itemView.setOnClickListener {
            clickHandler.onAddCareBehaviorClicked()
        }
    }
}
