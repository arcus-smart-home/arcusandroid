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
package arcus.app.pairing.hub.kickoff.adapters

import android.content.Context
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.activities.BaseActivity
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.utils.ImageUtils
import arcus.app.common.view.ScleraDivider
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.hub.original.HubParentFragment
import arcus.app.pairing.hub.kickoff.V3HubKickoffStepHostFragment
import arcus.presentation.kits.ProductAddModel
import arcus.presentation.kits.ProductDataModel
import arcus.presentation.kits.ProductHeaderModel
import com.squareup.picasso.Picasso


class HubKitAdapter(private val context: Context?, private val data: List<ProductAddModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var inflater: LayoutInflater

    override fun getItemViewType(position: Int): Int {
        return if (data[position] is ProductHeaderModel) {
            HEADER_TYPE
        } else {
            DATA_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            HEADER_TYPE -> HeaderViewHolder(inflater.inflate(R.layout.header_list_item, parent, false))
            else -> DataViewHolder(inflater.inflate(R.layout.hubkit_line_item, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            HEADER_TYPE -> {
                val headerHolder = holder as HeaderViewHolder
                val (name) = data[position] as ProductHeaderModel
                headerHolder.name.text = name
            }
            DATA_TYPE -> {
                val dataHolder = holder as DataViewHolder
                val item = data[position] as ProductDataModel
                Picasso
                    .with(holder.itemView.context)
                    .load(getImageUrl(item.id))
                    .into(dataHolder.productImage)

                when {
                    position == data.size - 1 || getItemViewType(position + 1) == HEADER_TYPE ->
                        dataHolder.divider.visibility = View.GONE
                    else -> dataHolder.divider.visibility = View.VISIBLE
                }

                if (item.canUseBle) {
                    dataHolder.container.setOnClickListener {
                        context?.startActivity(GenericConnectedFragmentActivity
                                .getLaunchIntent(
                                context,
                                V3HubKickoffStepHostFragment::class.java,
                                keepScreenOn = true)
                        )
                        showActionBar()
                    }
                }
                else {
                    dataHolder.container.setOnClickListener {
                        BackstackManager.getInstance().navigateToFragment(HubParentFragment.newInstance(), true)
                        dataHolder.divider.visibility = View.GONE
                        showActionBar()
                    }
                }

                dataHolder.productName.text = item.name
            }
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    private fun showActionBar() {
        val actionBar = (context as BaseActivity).supportActionBar
        actionBar?.show()
    }
    
    inner class HeaderViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var name: ScleraTextView = itemView.findViewById(R.id.header_text)

    }

    inner class DataViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var productImage: AppCompatImageView = itemView.findViewById(R.id.productImage)
        internal var productName: ScleraTextView = itemView.findViewById(R.id.productName)
        internal var container: RelativeLayout = itemView.findViewById(R.id.constraintLayout)
        internal var divider: ScleraDivider = itemView.findViewById(R.id.item_divider)
    }

    companion object {
        private const val KIT_URL_FORMAT = "%s/o/products/%s/product_large_color-and-%s.png"
        private val screenDensity = ImageUtils.screenDensity
        private val SRS_URL = SessionController.instance().staticResourceBaseUrl

        private fun getImageUrl(productId: String) : String = KIT_URL_FORMAT.format(
            SRS_URL,
            productId.toLowerCase(),
            screenDensity
        )

        private const val HEADER_TYPE = 0
        private const val DATA_TYPE = 1
    }
}
