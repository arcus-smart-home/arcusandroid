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
package arcus.app.pairing.device.productcatalog.devicesoverview

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.app.R
import arcus.app.common.view.ScleraTextView
import arcus.app.common.utils.inflate
import arcus.app.pairing.device.productcatalog.popups.ProductCatalogPopupManager
import arcus.presentation.pairing.device.productcatalog.devicesoverview.ProductEntry
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * Simple RecyclerView Adapter implementation
 */
class BrandsOverviewAdapter(private val activity: Activity, products: List<ProductEntry>) : RecyclerView.Adapter<BrandsOverviewViewHolder>() {
    private val productsList = products.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsOverviewViewHolder =
            BrandsOverviewViewHolder(parent.inflate(R.layout.item_brand_overview_row))

    override fun getItemCount(): Int = productsList.size

    override fun onBindViewHolder(holder: BrandsOverviewViewHolder, position: Int) {
        holder.bindView(activity, productsList[position])
    }
}

/**
 * Simple RecyclerView ViewHolder implementation
 */
class BrandsOverviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val deviceName = view.findViewById<ScleraTextView>(R.id.deviceShortName)
    private val vendor     = view.findViewById<ScleraTextView>(R.id.deviceVendor)
    private val imageView  = view.findViewById<ImageView>(R.id.deviceImage)

    fun bindView(activity: Activity, productEntry: ProductEntry) {
        deviceName.text = productEntry.title
        vendor.text = productEntry.subTitle

        val url = imageLocationCache[productEntry.id]
        if (url != null) {
            Picasso.with(itemView.context)
                    .load(url)
                    .placeholder(R.drawable.icon_cat_placeholder)
                    .error(R.drawable.icon_cat_placeholder)
                    .into(imageView)
        } else {
            Picasso.with(itemView.context)
                    .load(productEntry.iconUrl)
                    .placeholder(R.drawable.icon_cat_placeholder)
                    .error(R.drawable.icon_cat_placeholder)
                    .fetch(object : Callback {
                        override fun onSuccess() {
                            imageLocationCache[productEntry.id] = productEntry.iconUrl
                            Picasso.with(itemView.context)
                                    .load(productEntry.iconUrl)
                                    .placeholder(R.drawable.icon_cat_placeholder)
                                    .error(R.drawable.icon_cat_placeholder)
                                    .into(imageView)
                        }

                        override fun onError() {
                            // If the backup fails we want an error place holder
                            imageLocationCache[productEntry.id] = productEntry.backupIconUrl
                            Picasso.with(itemView.context)
                                    .load(productEntry.backupIconUrl)
                                    .placeholder(R.drawable.icon_cat_placeholder)
                                    .error(R.drawable.icon_cat_placeholder)
                                    .into(imageView)
                        }
                    })
        }
        // id, init, fallback, error

        itemView.setOnClickListener {
            ProductCatalogPopupManager().navigateForwardOrShowPopup(activity, productEntry.address)
        }
    }

    companion object {
        private val imageLocationCache = mutableMapOf<String, String>()
    }
}
