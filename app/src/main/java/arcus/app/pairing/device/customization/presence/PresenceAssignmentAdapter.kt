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
package arcus.app.pairing.device.customization.presence

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.app.ArcusApplication
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.customization.presence.AssignmentOption
import arcus.presentation.pairing.device.customization.presence.PersonAssignmentOption
import arcus.presentation.pairing.device.customization.presence.UnassignedAssignmentOption


class PresenceAssignmentAdapter(
    val assignmentOptionList: List<AssignmentOption>,
    val callback: PresenceAssignmentAdapterCallback
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var selectedPosition: Int = 0
    private lateinit var presenceRadioButton: View
    private lateinit var presenceRadioButtonImage: ImageView
    private lateinit var presencePersonImage: ImageView
    private lateinit var presencePersonName: ScleraTextView


    interface PresenceAssignmentAdapterCallback {
        fun onSelectionChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return CustomViewHolder(
            parent.inflate(R.layout.presence_assignment_list_item, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val checked = R.drawable.check_teal_30x30
        val unchecked = R.drawable.uncheck_30x30
        val assignmentOption: AssignmentOption = assignmentOptionList[position]

        (holder as CustomViewHolder).bindView(assignmentOption)

        presenceRadioButton = holder.presenceRadioButton
        presenceRadioButtonImage = holder.presenceRadioButtonImage
        presencePersonImage = holder.presencePersonImage
        presencePersonName = holder.presencePersonName

        presenceRadioButtonImage.setImageResource(
                if (selectedPosition == position){
                    checked
                } else{
                    unchecked
                })

        presenceRadioButton.tag = position
        presenceRadioButton.setOnClickListener {
            if(it.isEnabled){
                presenceRadioButtonImage.setImageResource(checked)
                selectedPosition = it.tag as Int
                notifyDataSetChanged()
                callback.onSelectionChanged()
            }
        }

        when(assignmentOption) {
            is PersonAssignmentOption -> {
                val personId = assignmentOption.personAddress
                presencePersonImage.setImageResource(R.drawable.person_45x45)

                ImageManager.with(ArcusApplication.getContext())
                        .putPersonImage(personId)
                        .withTransform(CropCircleTransformation())
                        .withTransformForStockImages(BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                        .withPlaceholder(R.drawable.person_45x45)
                        .withError(R.drawable.person_45x45)
                        .into(presencePersonImage)
                        .execute()

                presencePersonName.text = assignmentOption.name
            }
            is UnassignedAssignmentOption -> {
                presencePersonImage.setImageResource(R.drawable.person_unassigned_45x45)
                presencePersonName.text = assignmentOption.name
            }
        }
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = assignmentOptionList.size
}

class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val presenceRadioButton: View = itemView.findViewById(R.id.presence_assignment_radio_button)
    val presenceRadioButtonImage: ImageView = itemView.findViewById(R.id.presence_radio_button_image)
    val presencePersonImage: ImageView = itemView.findViewById(R.id.presence_person_image)
    val presencePersonName: ScleraTextView = itemView.findViewById(R.id.presence_person_name)

    fun bindView(assignmentOption: AssignmentOption) = with(itemView) {
        // Work done in onBindViewHolder() above
    }
}
