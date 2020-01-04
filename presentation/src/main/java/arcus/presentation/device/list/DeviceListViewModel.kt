package arcus.presentation.device.list

import arcus.cornea.dto.HubDeviceModelDTO
import arcus.cornea.helpers.await
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.HubModelProvider
import arcus.presentation.common.view.ViewState
import arcus.presentation.common.view.ViewStateViewModel
import com.iris.client.capability.DeviceConnection
import com.iris.client.capability.HubConnection
import com.iris.client.capability.Presence
import com.iris.client.event.ListenerRegistration
import com.iris.client.model.DeviceModel
import com.iris.client.model.HubModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// TODO: @Inject, Clicks parsed in VM and sent to View as Navigation events.
class DeviceListViewModel(
    private val deviceProvider: DeviceModelProvider = DeviceModelProvider.instance(),
    private val hubProvider: HubModelProvider = HubModelProvider.instance()
) : ViewStateViewModel<Devices>() {
    private var deviceListener: ListenerRegistration = deviceProvider.store.addListener { loadData() }
    private var hubListener: ListenerRegistration = hubProvider.store.addListener { loadData() }

    override fun loadData() {
        safeLaunch {
            emitLoading()

            val allDevices = withContext(Dispatchers.Default) {
                val hub = hubProvider.load().await().firstOrNull().toListItem() ?: emptyList()
                deviceProvider
                    .load()
                    .await()
                    .map { it.toListItem() }
                    .sortedBy { it.name }
                    .plus(hub)
            }

            _viewState.value = ViewState.Loaded(Devices(allDevices.size, allDevices + FooterListItem))
        }
    }

    override fun onCleared() {
        deviceListener.remove()
        hubListener.remove()
    }

    private fun HubModel?.toListItem(): List<DeviceListItem>? {
        val hub = this ?: return null

        return listOf(
            DeviceListItem(
                id = hub.id,
                name = hub.name.orEmpty(),
                isCloudConnected = false,
                isOffline = HubConnection.STATE_OFFLINE == hub.get(HubConnection.ATTR_STATE),
                device = HubDeviceModelDTO(this)
            )
        )
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
