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
package arcus.app.pairing.device.productcatalog.brandsoverview

import android.app.Activity
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import arcus.app.R
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.productcatalog.advanced.AdvancedUserPairingWarningPopup
import arcus.app.pairing.device.productcatalog.popups.ProductCatalogPopupManager
import arcus.presentation.pairing.device.productcatalog.brandsoverview.BrandCategoryProxyModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

/**
 * RecyclerView Adapter
 */
class BrandsCatalogAdapter(
        private val activity: Activity
        , brands: List<BrandCategoryProxyModel>
        , val listener: BrandSelectedListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val brandsList = brands.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ADVANCED_PAIRING) {
            AdvancedUserViewHolder(parent.inflate(R.layout.advanced_user_link_row, false), activity)

        } else {
            BrandsViewHolder(parent.inflate(R.layout.brands_catalog_row, false))
        }
    }

    override fun getItemViewType(position: Int) = if (position == brandsList.size) {
        ADVANCED_PAIRING
    } else {
        BRAND_CATALOG
    }

    override fun getItemCount(): Int {
        return brandsList.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (position) {
            brandsList.size -> {
                (holder as AdvancedUserViewHolder).bindView()
            }
            else -> {
                (holder as BrandsViewHolder).bindView(brandsList[position], listener)
            }
        }
    }

    companion object {
        private val BRAND_CATALOG = 0
        private val ADVANCED_PAIRING = 1
    }
}

class AdvancedUserViewHolder(view: View, activity: Activity) : RecyclerView.ViewHolder(view) {
    private val advancedUserPairingWarningPopup = AdvancedUserPairingWarningPopup.newInstance()
    private val advancedUserLink = view.findViewById<ScleraTextView>(R.id.advanced_pairing_link)
    private val appCompatActivity = activity as? AppCompatActivity

    fun bindView() {
        advancedUserLink.paintFlags = advancedUserLink.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        itemView.setOnClickListener {
            appCompatActivity?.let {
                val showingHubRequiredPopup = ProductCatalogPopupManager().showHubPopupsIfRequired(it)
                if (!showingHubRequiredPopup) {
                    advancedUserPairingWarningPopup.show(it.supportFragmentManager, AdvancedUserPairingWarningPopup::class.java.name)
                }
            }
        }
    }
}

/**
 * RecyclerView ViewHolder
 */
class BrandsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val count = view.findViewById<ScleraTextView>(R.id.brandCount)
    private val image = view.findViewById<ImageView>(R.id.brandImage)
    private val name = view.findViewById<ScleraTextView>(R.id.brandName)
    private inner class LoadCallback(val brandName: String) : Callback {
        override fun onSuccess() {
            name.visibility = View.GONE
        }

        override fun onError() {
            name.visibility = View.VISIBLE
            name.text = brandName
        }
    }

    fun bindView(brandEntry: BrandCategoryProxyModel, listener: BrandSelectedListener) {
        image.setImageResource(R.drawable.icon_cat_placeholder)
        count.text = brandEntry.count.toString()
        val primaryUrl = brandEntry.mainUrl
        val secondaryUrl = brandEntry.backupUrl

        val iconURL = imageLocationCache[brandEntry.name]
        if (iconURL != null) {
            itemView.loadBrandImage(image, iconURL, brandEntry.name)
        } else {
            Picasso.with(itemView.context)
                    .load(primaryUrl)
                    .error(R.drawable.icon_cat_placeholder)
                    .noFade()
                    .fetch(object : Callback {
                        override fun onSuccess() {
                            imageLocationCache[brandEntry.name] = primaryUrl
                            Picasso.with(itemView.context).load(primaryUrl).into(image)
                        }

                        override fun onError() {
                            // If the backup fails we want an error place holder
                            imageLocationCache[brandEntry.name] = secondaryUrl
                            itemView.loadBrandImage(image, secondaryUrl, brandEntry.name)
                        }
                    })
        }

        itemView.setOnClickListener {
            listener.onBrandSelected(brandEntry.name)
        }
    }

    private fun View.loadBrandImage(image: ImageView, url: String, brandName: String) = Picasso
            .with(this.context)
            .load(url)
            .error(R.drawable.icon_cat_placeholder)
            .into(image, LoadCallback(brandName))

    companion object {
        private val imageLocationCache = mutableMapOf<String, String>()
    }
}
