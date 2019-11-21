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
package arcus.app.pairing.hub.activation

import android.net.Uri
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.PairingCustomizationActivity
import arcus.app.pairing.device.searching.PairingSearchAnimationView
import arcus.presentation.pairing.hub.activation.HubDeviceCustomized
import arcus.presentation.pairing.hub.activation.KitDevice
import arcus.presentation.pairing.hub.activation.KitDeviceCustomized
import arcus.presentation.pairing.hub.activation.KitDeviceInError
import arcus.presentation.pairing.hub.activation.KitDeviceNotActivated
import arcus.presentation.pairing.hub.activation.KitDeviceNotCustomized
import com.squareup.picasso.Picasso

class KitActivationRecyclerViewAdapter(
    items: List<KitDevice>
) : RecyclerView.Adapter<KitActivationViewHolder<*>>() {
    private val listItems = items.toMutableList()

    override fun getItemCount(): Int = listItems.size

    override fun getItemViewType(position: Int): Int = when (listItems[position]) {
        is KitDeviceCustomized -> CUSTOMIZED
        is KitDeviceInError -> ERROR
        is KitDeviceNotActivated -> NOT_ACTIVATED
        is KitDeviceNotCustomized -> NOT_CUSTOMIZED
        is HubDeviceCustomized -> HUB_CUSTOMIZED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KitActivationViewHolder<*> {
        return when (viewType) {
            CUSTOMIZED -> KitCustomizedViewHolder(parent)
            ERROR -> KitInErrorViewHolder(parent)
            NOT_ACTIVATED -> KitNotActivatedViewHolder(parent)
            NOT_CUSTOMIZED -> KitNotCustomizedViewHolder(parent)
            HUB_CUSTOMIZED -> HubCustomizedViewHolder(parent)
            else -> throw RuntimeException("Unkonwn ViewHolder type.") // Can never happen, but need to exhaust "when"
        }
    }

    override fun onBindViewHolder(holder: KitActivationViewHolder<*>, position: Int) {
        when (holder) {
            is KitCustomizedViewHolder -> holder.bindView(listItems[position] as KitDeviceCustomized)
            is KitNotCustomizedViewHolder -> holder.bindView(listItems[position] as KitDeviceNotCustomized)
            is KitInErrorViewHolder -> holder.bindView(listItems[position] as KitDeviceInError)
            is KitNotActivatedViewHolder -> holder.bindView(listItems[position] as KitDeviceNotActivated)
            is HubCustomizedViewHolder -> holder.bindView(listItems[position] as HubDeviceCustomized)
        }
    }

    fun updateItem(item: KitDevice) {
        val index = listItems
            .indexOfFirst {
                it.pairingDeviceAddress == item.pairingDeviceAddress
            }
        if (index != -1) {
            listItems[index] = item
            notifyItemChanged(index)
        }
    }

    companion object {
        private const val CUSTOMIZED = 1
        private const val ERROR = 2
        private const val NOT_ACTIVATED = 3
        private const val NOT_CUSTOMIZED = 4
        private const val HUB_CUSTOMIZED = 5
    }
}

abstract class KitActivationViewHolder<in T : KitDevice>(
    parent: ViewGroup,
    @LayoutRes layoutRes: Int = R.layout.item_kit_activation
) : RecyclerView.ViewHolder(parent.inflate(layoutRes)) {
    protected val configuredImage: ImageView = itemView.findViewById(R.id.product_configured)
    protected val name: TextView = itemView.findViewById(R.id.product_name)
    protected val extra: ScleraTextView = itemView.findViewById(R.id.product_extra)

    abstract fun bindView(item: T)
}

class HubCustomizedViewHolder(
    parent: ViewGroup
) : KitActivationViewHolder<HubDeviceCustomized>(
    parent,
    R.layout.item_kit_hub_activation
) {
    private val pairingSearchAnimationView: PairingSearchAnimationView = itemView.findViewById(R.id.product_image)

    override fun bindView(item: HubDeviceCustomized) {
        configuredImage.visibility = View.VISIBLE
        name.text = item.name
        extra.visibility = View.GONE

        // Animate if all items are not configured
        pairingSearchAnimationView.shouldAnimate = item.allConfigured
    }
}

class KitCustomizedViewHolder(parent: ViewGroup) : KitActivationViewHolder<KitDeviceCustomized>(parent) {
    private val productImage : ImageView = itemView.findViewById(R.id.product_image)

    override fun bindView(item: KitDeviceCustomized) {
        Picasso
            .with(itemView.context)
            .load(item.imageUrl)
            .into(productImage)
        configuredImage.visibility = View.VISIBLE

        name.text = item.name
        extra.visibility = View.GONE
    }
}

class KitNotCustomizedViewHolder(parent: ViewGroup) : KitActivationViewHolder<KitDeviceNotCustomized>(parent) {
    private val productImage : ImageView = itemView.findViewById(R.id.product_image)

    override fun bindView(item: KitDeviceNotCustomized) {
        Picasso
            .with(itemView.context)
            .load(item.imageUrl)
            .into(productImage)
        name.text = item.name

        extra.setTextColor(ContextCompat.getColor(itemView.context, R.color.sclera_tab_green))
        extra.text = item.description

        itemView.setOnClickListener {
            itemView.context.startActivity(
                PairingCustomizationActivity.newIntent(itemView.context, item.pairingDeviceAddress)
            )
        }
    }
}

class KitInErrorViewHolder(parent: ViewGroup) : KitActivationViewHolder<KitDeviceInError>(parent) {
    private val productImage : ImageView = itemView.findViewById(R.id.product_image)

    override fun bindView(item: KitDeviceInError) {
        productImage.setImageResource(R.drawable.alert_pink_80x80)
        name.setTextColor(ContextCompat.getColor(itemView.context, R.color.sclera_disabled_color))
        name.text = item.name

        extra.setTextColor(ContextCompat.getColor(itemView.context, R.color.sclera_alert))
        extra.text = item.description

        itemView.setOnClickListener {
            ActivityUtils.launchUrl(Uri.parse(GlobalSetting.KITTING_NEED_HELP_URL))
        }
    }
}

class KitNotActivatedViewHolder(parent: ViewGroup) : KitActivationViewHolder<KitDeviceNotActivated>(parent) {
    private val productImage : ImageView = itemView.findViewById(R.id.product_image)

    override fun bindView(item: KitDeviceNotActivated) {
        Picasso
            .with(itemView.context)
            .load(item.imageUrl)
            .into(productImage)
        name.setTextColor(ContextCompat.getColor(itemView.context, R.color.sclera_disabled_color))
        name.text = item.name

        extra.setItalicTypeface()
        extra.text = item.description
    }
}
