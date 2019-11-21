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
package arcus.app.pairing.device.searching

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import arcus.cornea.SessionController
import arcus.app.R
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView
import arcus.presentation.pairing.device.searching.DevicePairingData
import arcus.presentation.pairing.device.searching.PairedDeviceModel
import arcus.presentation.pairing.device.searching.PairedDeviceModel.Type.MISCONFIGURED_DEVICE
import arcus.presentation.pairing.device.searching.PairedDeviceModel.Type.MISPAIRED_DEVICE
import arcus.presentation.pairing.device.searching.PairedDeviceModel.Type.PAIRED_NEEDS_CUSTOMIZATION_DEVICE
import arcus.presentation.pairing.device.searching.PairedDeviceModel.Type.FULLY_PAIRED_DEVICE
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

interface PairedDeviceClickHandler {
    /**
     * Invoked when a misconfigured device is clicked
     *
     * @param pairingDeviceAddress PairingDevice address of the device that was clicked on
     */
    fun onMisconfiguredDeviceClicked(pairingDeviceAddress: String)

    /**
     * Invoked when a mispaired device is clicked
     *
     * @param pairingDeviceAddress PairingDevice address of the device that was clicked on
     */
    fun onMispairedDeviceClicked(pairingDeviceAddress: String)

    /**
     * Invoked when a device is clicked that can be configured
     *
     * @param pairingDeviceAddress PairingDevice address of the device that was clicked on
     */
    fun onCustomizeDeviceClicked(pairingDeviceAddress: String)
}

/**
 * Simple RecyclerView Adapter implementation
 */
class DeviceSearchingAdapter(
    private val clickHandler: PairedDeviceClickHandler,
    devices: List<DevicePairingData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items = getPairedModels(devices)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when(viewType) {
            MISCONFIGURED_DEVICE ->  MisconfiguredDeviceViewHolder(clickHandler, parent.inflate(R.layout.item_mispaired_or_misconfigured_device))
            MISPAIRED_DEVICE -> MispairedDeviceViewHolder(clickHandler, parent.inflate(R.layout.item_mispaired_or_misconfigured_device))
            PAIRED_NEEDS_CUSTOMIZATION_DEVICE -> NeedsCustomizationViewHolder(clickHandler, parent.inflate(R.layout.item_paired_device))
            FULLY_PAIRED_DEVICE -> PairedDeviceViewHolder(parent.inflate(R.layout.item_paired_device))
            else -> PartiallyPairedDeviceViewHolder(parent.inflate(R.layout.item_paired_device))
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when(holder.itemViewType) {
            MISCONFIGURED_DEVICE -> {
                holder as MisconfiguredDeviceViewHolder
                holder.bindView(item)
            }
            MISPAIRED_DEVICE ->  {
                holder as MispairedDeviceViewHolder
                holder.bindView(item)
            }
            PAIRED_NEEDS_CUSTOMIZATION_DEVICE ->  {
                holder as NeedsCustomizationViewHolder
                holder.bindView(item)
            }
            FULLY_PAIRED_DEVICE ->  {
                holder as PairedDeviceViewHolder
                holder.bindView(item)
            }
            else ->  {
                holder as PartiallyPairedDeviceViewHolder
                holder.bindView(item)
            }
        }
    }
    override fun getItemViewType(position: Int): Int
            = items[position].getViewType()

    override fun getItemCount(): Int
            = items.size

    private fun getPairedModels(devices: List<DevicePairingData>) : MutableList<PairedDeviceModel> {
        val list = mutableListOf<PairedDeviceModel>()
        return devices.mapTo(list) {
            val iconUrl = if(!it.errorState) getMainUrlFor(it.productId) else ""
            val fallbackIconUrl = if(!it.errorState) getFallbackUrlFor(it.productScreen) else ""
            PairedDeviceModel(
                it.id,
                iconUrl,
                fallbackIconUrl,
                it.name,
                it.pairingDeviceAddress,
                it.description,
                it.customized,
                it.errorState,
                it.pairingState
            )
        }
    }

    private fun getMainUrlFor(id: String) : String {
        return PRODUCTS_URL_FORMAT.format(
            SessionController.instance().staticResourceBaseUrl,
            id.replace(NON_ALPHA_NUMERIC, ""),
            ImageUtils.screenDensity
        )
    }

    private fun getFallbackUrlFor(screen: String) : String {
        return SCREENS_URL_FORMAT.format(
            SessionController.instance().staticResourceBaseUrl,
            screen,
            ImageUtils.screenDensity
        )
    }

    companion object {
        @JvmStatic
        private val NON_ALPHA_NUMERIC = "[^a-zA-Z0-9]".toRegex()
        private const val PRODUCTS_URL_FORMAT = "%s/o/products/%s/product_small-and-%s.png"
        private const val SCREENS_URL_FORMAT = "%s/o/dtypes/%s/type_small-and-%s.png"

    }
}

/**
 * The ViewHolders for each pairing state
 */
class MispairedDeviceViewHolder(val clickHandler: PairedDeviceClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val title = itemView.findViewById<ScleraTextView>(R.id.pairing_title_text)
    private val description = itemView.findViewById<ScleraTextView>(R.id.pairing_description_text)
    private val actionPrompt = itemView.findViewById<ScleraTextView>(R.id.action_prompt_text)
    private val chevron = itemView.findViewById<LinearLayout>(R.id.chevron_layout)

    fun bindView(device: PairedDeviceModel) = with(itemView) {
        title.text = device.name
        description.text = device.description
        chevron.visibility = View.VISIBLE
        actionPrompt.text = resources.getString(R.string.remove)

        itemView.setOnClickListener {
            clickHandler.onMispairedDeviceClicked(device.pairingDeviceAddress)
        }
    }
}

class MisconfiguredDeviceViewHolder(val clickHandler: PairedDeviceClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val title = itemView.findViewById<ScleraTextView>(R.id.pairing_title_text)
    private val description = itemView.findViewById<ScleraTextView>(R.id.pairing_description_text)
    private val actionPrompt = itemView.findViewById<ScleraTextView>(R.id.action_prompt_text)
    private val chevron = itemView.findViewById<LinearLayout>(R.id.chevron_layout)

    fun bindView(device: PairedDeviceModel) = with(itemView) {
        title.text = device.name
        description.text = device.description
        chevron.visibility = View.VISIBLE
        // TODO: once we have resolution steps to handle 'Misconfigured', change text to "Resolve"
        actionPrompt.text = resources.getString(R.string.remove)
        //chevronText.text = resources.getString(R.string.resolve)

        itemView.setOnClickListener {
            clickHandler.onMisconfiguredDeviceClicked(device.pairingDeviceAddress)
        }
    }
}

class PartiallyPairedDeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val imageView = itemView.findViewById<ImageView>(R.id.pairing_image)
    private val title = itemView.findViewById<ScleraTextView>(R.id.pairing_title_text)
    private val description = itemView.findViewById<ScleraTextView>(R.id.pairing_description_text)
    private val spinner = itemView.findViewById<ProgressBar>(R.id.progress_bar)

    fun bindView(device: PairedDeviceModel) = with(itemView) {
        title.text = device.name
        description.text = device.description
        spinner.visibility = View.VISIBLE
        imageView.setImageResource(R.drawable.device_detection_45x45)

        itemView.setOnClickListener(null)
    }
}

class NeedsCustomizationViewHolder(val clickHandler: PairedDeviceClickHandler, view: View) : RecyclerView.ViewHolder(view) {
    private val imageView = itemView.findViewById<ImageView>(R.id.pairing_image)
    private val title = itemView.findViewById<ScleraTextView>(R.id.pairing_title_text)
    private val description = itemView.findViewById<ScleraTextView>(R.id.pairing_description_text)
    private val actionPrompt = itemView.findViewById<ScleraTextView>(R.id.action_prompt_text)
    private val chevron = itemView.findViewById<LinearLayout>(R.id.chevron_layout)

    fun bindView(device: PairedDeviceModel) = with(itemView) {
        title.text = device.name
        description.text = device.description
        chevron.visibility = View.VISIBLE
        actionPrompt.text = resources.getString(R.string.customize)

        CachedImageUrls(device.iconUrl, device.fallbackIconUrl, device.id, imageView, itemView.context).getImageOrPlaceholder()

        itemView.setOnClickListener {
            clickHandler.onCustomizeDeviceClicked(device.pairingDeviceAddress)
        }
    }
}

class PairedDeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    private val imageView = itemView.findViewById<ImageView>(R.id.pairing_image)
    private val title = itemView.findViewById<ScleraTextView>(R.id.pairing_title_text)
    private val description = itemView.findViewById<ScleraTextView>(R.id.pairing_description_text)
    private val actionPrompt = itemView.findViewById<ScleraTextView>(R.id.action_prompt_text)
    private val checkmark = itemView.findViewById<ImageView>(R.id.checkmark)

    fun bindView(device: PairedDeviceModel) = with(itemView) {
        title.text = device.name
        description.text = device.description
        actionPrompt.visibility = View.GONE
        checkmark.visibility = View.VISIBLE

        CachedImageUrls(device.iconUrl, device.fallbackIconUrl, device.id, imageView, itemView.context).getImageOrPlaceholder()

        itemView.setOnClickListener(null)
    }
}

class CachedImageUrls(
    val iconUrl: String,
    val fallbackIconUrl: String,
    val id: String,
    val imageView: ImageView,
    val context: Context
)  {
    private val cachedUrl = imageLocationCache[id]

    fun getImageOrPlaceholder() {
        if (cachedUrl != null) {
            Picasso
                .with(context)
                .load(cachedUrl)
                .placeholder(R.drawable.device_detection_45x45)
                .into(imageView)
        } else {
            Picasso
                .with(context)
                .load(iconUrl)
                .placeholder(R.drawable.device_detection_45x45)
                .fetch(object : Callback {
                    override fun onSuccess() {
                        imageLocationCache[id] = iconUrl
                        Picasso
                            .with(context)
                            .load(iconUrl)
                            .placeholder(R.drawable.device_detection_45x45)
                            .into(imageView)
                    }
                    override fun onError() {
                        if (fallbackIconUrl.isNotEmpty()) {
                            imageLocationCache[id] = fallbackIconUrl
                            Picasso
                                .with(context)
                                .load(fallbackIconUrl)
                                .placeholder(R.drawable.device_detection_45x45)
                                .error(R.drawable.warning_alert_45x45)
                                .into(imageView)
                        } else {
                            Picasso
                                .with(context)
                                .load(R.drawable.warning_alert_45x45)
                                .into(imageView)
                        }
                    }
                })
        }
    }

    companion object {
        private val imageLocationCache = mutableMapOf<String, String>()
    }
}
