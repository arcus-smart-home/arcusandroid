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
package arcus.app.dashboard.settings.adapters

import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.utils.FontUtils
import arcus.app.dashboard.settings.services.ServiceListItemModel
import arcus.app.dashboard.settings.services.ServiceListItemViewHolder

class InactiveServicesListAdapter(private val context: Context, private val inactiveServices: List<ServiceListItemModel>)
    : RecyclerView.Adapter<ServiceListItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.inactive_card_service_list_item, parent, false)
        return ServiceListItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceListItemViewHolder, position: Int) {
        val item = inactiveServices[position]

        ImageManager.with(context)
            .putDrawableResource(item.serviceCard.smallIconDrawableResId)
            .withTransformForStockImages(BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
            .into(holder.serviceIcon)
            .execute()

        holder.serviceIcon.setColorFilter(Color.parseColor("#FFC3C3C3"))

        holder.serviceTitle.text = item.text
        holder.serviceTitle.setTextColor(Color.parseColor("#FFC3C3C3"))
        holder.serviceTitle.typeface = FontUtils.getNormal()
    }

    override fun getItemCount(): Int { return inactiveServices.size }
}
