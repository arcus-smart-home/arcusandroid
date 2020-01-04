package arcus.app.device.list

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.CropCircleTransformation
import arcus.app.common.utils.inflate
import arcus.presentation.device.list.DeviceListItem
import arcus.presentation.device.list.ListItem

class DeviceListAdapter(
        private val itemClickListener: ItemClickListener
) : ListAdapter<ListItem, ViewHolder>(object : ItemCallback<ListItem>() {
    override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean = oldItem.areItemsTheSame(newItem)
    override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean = oldItem.areContentsTheSame(newItem)
}) {
    interface ItemClickListener {
        /**
         * Called when the device is clicked with it's [position] in the list.
         */
        fun itemClicked(position: Int)

        /**
         * Called when the "Footer" item is clicked.
         */
        fun footerClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == 0) {
            DeviceViewHolder(parent.inflate(R.layout.device_list_item))
        } else {
            FooterViewHolder(parent.inflate(R.layout.cell_device_listing_zwave_tools))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is DeviceViewHolder -> {
                holder.itemView.setOnClickListener {
                    itemClickListener.itemClicked(holder.adapterPosition)
                }
                holder.bindView(getItem(position) as DeviceListItem)
            }
            else -> holder.itemView.setOnClickListener {
                itemClickListener.footerClicked()
            }
        }
    }

    override fun getItemViewType(position: Int): Int = getItem(position).viewType.ordinal

    class DeviceViewHolder(itemView: View) : ViewHolder(itemView) {
        private val deviceImage = itemView.findViewById<ImageView>(R.id.deviceImage)
        private val name = itemView.findViewById<TextView>(R.id.itemName)
        private val redDot = itemView.findViewById<ImageView>(R.id.redDot)
        private val cloudImage = itemView.findViewById<ImageView>(R.id.cloudImage)

        fun bindView(item: DeviceListItem) {
            handleCloudConnected(item)
            handleOffline(item.isOffline)
            name.text = item.name

            ImageManager
                    .with(deviceImage.context)
                    .putSmallDeviceImage(item.device)
                    .withTransformForUgcImages(CropCircleTransformation())
                    .withError(R.drawable.device_list_placeholder)
                    .into(deviceImage)
                    .execute()
        }

        private fun handleCloudConnected(item: DeviceListItem) {
            val imageResource = if (item.isOffline) {
                R.drawable.cloud_small_pink
            } else {
                R.drawable.cloud_small_white
            }

            cloudImage.setImageResource(imageResource)
            cloudImage.visibility = if (item.isCloudConnected) View.VISIBLE else View.GONE
        }

        private fun handleOffline(isOffline: Boolean) {
            if (isOffline) {
                redDot.visibility = View.VISIBLE
            } else {
                redDot.visibility = View.GONE
            }
        }
    }

    class FooterViewHolder(itemView: View) : ViewHolder(itemView)
}
