package arcus.presentation.device.list

import arcus.presentation.common.view.ContentsComparable
import com.iris.client.model.DeviceModel

/**
 * The current view state with the number of [devices] and the full [deviceList] that can be
 * displayed.
 */
data class Devices(val devices: Int, val deviceList: List<ListItem>)

/**
 * The type of view represented.
 */
enum class ViewType {
    DEVICE,
    FOOTER
}

/**
 * An entry in a list of a particular [viewType].
 */
sealed class ListItem(val viewType: ViewType) : ContentsComparable<ListItem>

/**
 * A footer list item.
 */
object FooterListItem : ListItem(ViewType.FOOTER) {
    override fun areItemsTheSame(newItem: ListItem): Boolean = newItem is FooterListItem
    override fun areContentsTheSame(newItem: ListItem): Boolean = true
}

/**
 * A device entry.
 *
 * @property id The unique ID of this device.
 * @property name The user given name of this device.
 * @property isCloudConnected If the device is cloud connected.
 * @property isOffline If the device is considered offline.
 * @property device the device/hubDTO model - only present b/c of ImageManager..
 */
data class DeviceListItem(
    val id: String,
    val name: String,
    val isCloudConnected: Boolean,
    val isOffline: Boolean,
    val device: DeviceModel // For ImageManager...
) : ListItem(ViewType.DEVICE), ContentsComparable<ListItem> {
    override fun areItemsTheSame(newItem: ListItem): Boolean = newItem is DeviceListItem && id == newItem.id

    override fun areContentsTheSame(newItem: ListItem): Boolean = newItem is DeviceListItem &&
            isCloudConnected == newItem.isCloudConnected &&
            isOffline == newItem.isOffline &&
            name == newItem.name
}
