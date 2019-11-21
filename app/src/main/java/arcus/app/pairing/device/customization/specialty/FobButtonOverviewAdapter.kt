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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.app.R
import arcus.app.activities.GenericFragmentActivity
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView


class FobButtonOverviewAdapter(
    val deviceAddress: String,
    val buttonList: List<ButtonWithAction>,
    val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return CustomViewHolder(parent.inflate(R.layout.fob_button_overview_list_item, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val appCompatActivity = activity as? AppCompatActivity
        val currentButton: ButtonWithAction = buttonList[position]

        holder as CustomViewHolder
        holder.buttonNameText.text = currentButton.buttonNameText.toLowerCase().capitalize()
        holder.buttonActionText.text = currentButton.buttonActionText

        holder.itemView.setOnClickListener {
            appCompatActivity?.let {
                val bundle = FobButtonActionModal.createArgumentBundle(
                        currentButton.buttonNameText.toLowerCase().capitalize(),
                        deviceAddress
                )
                it.startActivity(
                        GenericFragmentActivity.getLaunchIntent(
                                it,
                                FobButtonActionModal::class.java,
                                bundle
                        )
                )
            }
        }

        ImageManager.with(appCompatActivity)
                .putDrawableResource(currentButton.imageResId)
                .withTransform(BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .withPlaceholder(R.drawable.sidemenu_settings_blackcircle)
                .into(holder.buttonImage)
                .execute()
    }

    override fun getItemId(position: Int) = position.toLong()

    override fun getItemCount() = buttonList.size
}

class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    val buttonImage: ImageView = itemView.findViewById(R.id.fob_button_image)
    val buttonNameText: ScleraTextView = itemView.findViewById(R.id.fob_button_name)
    val buttonActionText: ScleraTextView = itemView.findViewById(R.id.fob_button_action)
}

