package arcus.app.device.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import arcus.cornea.dto.HubDeviceModelDTO
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.HubModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.capability.DeviceConnection
import com.iris.client.capability.HubConnection
import com.iris.client.capability.Presence
import com.iris.client.event.ListenerRegistration
import com.iris.client.model.DeviceModel
import com.iris.client.model.HubModel

// TODO: @Inject, Clicks parsed in VM and sent to View as Navigation events.
class DeviceListViewModel(
        private val deviceProvider: DeviceModelProvider = DeviceModelProvider.instance(),
        private val hubProvider: HubModelProvider = HubModelProvider.instance()
) : ViewModel() {
    private var deviceListener: ListenerRegistration = Listeners.empty()
    private var hubListener: ListenerRegistration = Listeners.empty()
    private val devicesInternal = object : MutableLiveData<ViewState>() {
        init {
            deviceListener = deviceProvider.store.addListener { loadDevices() }
            hubListener = hubProvider.store.addListener { loadDevices() }
        }
    }

    val devices: LiveData<ViewState> = devicesInternal

    fun loadDevices() {
        deviceProvider
                .load()
                .chain { hubProvider.load() }
                .transform { Pair(it ?: emptyList(), deviceProvider.store.values()) }
                .onSuccess { (hubs, devices) ->
                    val hubListItems = hubs.firstOrNull().toListItem() ?: emptyList()
                    val deviceList = hubListItems + devices
                            .map { it.toListItem() }
                            .sortedBy { it.name }
                            .plus(FooterListItem)

                    devicesInternal.postValue(ViewState(deviceList.size, deviceList))
                }
    }

    override fun onCleared() {
        deviceListener.remove()
        hubListener.remove()
    }

    private fun HubModel?.toListItem(): List<DeviceListItem>? {
        val hub = this ?: return null

        return listOf(DeviceListItem(
                id = hub.id,
                name = hub.name.orEmpty(),
                isCloudConnected = false,
                isOffline = HubConnection.STATE_OFFLINE == hub.get(HubConnection.ATTR_STATE),
                device = HubDeviceModelDTO(this)
        ))
    }

    private fun DeviceModel.toListItem(): DeviceListItem {
        val isOffline = DeviceConnection.STATE_OFFLINE == get(DeviceConnection.ATTR_STATE)
        val isPresenceDevice = caps.orEmpty().contains(Presence.NAMESPACE)

        return DeviceListItem(
                id = id,
                name = name.orEmpty(),
                isCloudConnected = false,
                isOffline = isOffline && !isPresenceDevice,
                device = this
        )
    }
}
