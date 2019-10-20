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
package arcus.app.dashboard.settings.favorites

import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import arcus.cornea.CorneaClientFactory
import arcus.cornea.provider.DeviceModelProvider
import arcus.cornea.provider.PlaceModelProvider
import arcus.cornea.provider.SceneModelProvider
import arcus.cornea.subsystem.favorites.FavoritesController
import com.iris.client.model.Model
import com.iris.client.service.SceneService
import arcus.app.common.utils.CorneaUtils
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.PreferenceUtils
import arcus.app.dashboard.settings.model.DraggableListDataProvider
import de.greenrobot.event.EventBus

class FavoritesListDataProvider : DraggableListDataProvider, FavoritesController.Callback {

    private val favoriteItems = ArrayList<FavoriteListItemModel>()
    private val mController: FavoritesController = FavoritesController.getInstance()
    private var mDelegate: OnDataChanged? = null

    val orderedDeviceIdList: List<String>
        get() {
            val deviceIdList = ArrayList<String>()

            for (thisFavorite in favoriteItems) {
                deviceIdList.add(thisFavorite.modelId)
            }

            return deviceIdList
        }

    init { mController.setCallback(this) }

    override fun showFavorites(favoriteModels: List<Model>) {
        favoriteItems.clear()

        val devices = DeviceModelProvider.instance().store.values()
        val scenes = SceneModelProvider.instance().store.values()
        val prefs = PreferenceUtils.getOrderedFavorites(devices + scenes).map { model -> FavoriteListItemModel(model) }

        if (prefs.isEmpty()) {
            favoriteItems.addAll(devices.map { deviceModel -> FavoriteListItemModel(deviceModel) })
            favoriteItems.addAll(scenes.map { sceneModel -> FavoriteListItemModel(sceneModel) })
        }
        else {
            favoriteItems.addAll(prefs)
        }

        mDelegate?.updated()
    }

    override fun removeItem(fromPosition: Int) {
        val deviceId = favoriteItems[fromPosition].modelId
        // Try to find the item in the list of devices and delete it...

        DeviceModelProvider.instance().load()
            .onSuccess { devices ->
                if (devices != null) {
                    for (thisDevice in devices) {
                        if (thisDevice.id == deviceId) {
                            thisDevice.removeTags(HashSet(ImmutableList.of(GlobalSetting.FAVORITE_TAG)))
                            thisDevice.commit()
                        }
                    }
                }

                // ... if not there, look in our scenes list:
                val placeId = CorneaUtils.getIdFromAddress(PlaceModelProvider.getCurrentPlace().address)
                CorneaClientFactory.getService(SceneService::class.java).listScenes(placeId).onSuccess { listScenesResponse ->
                    val sceneModels = CorneaClientFactory.getModelCache().addOrUpdate(listScenesResponse.scenes)
                    for (thisScene in sceneModels) {
                        if (thisScene.id == deviceId) {
                            thisScene.removeTags(ImmutableSet.of(GlobalSetting.FAVORITE_TAG))
                            thisScene.commit()
                        }
                    }
                }

                EventBus.getDefault().postSticky(FavoritesOrderChangedEvent(orderedDeviceIdList))
            }
            .onFailure { /* Do nothing...? */ }
    }

    override fun moveItem(fromPosition: Int, toPosition: Int) {
        val item = favoriteItems.removeAt(fromPosition)
        favoriteItems.add(toPosition, item)
    }

    interface OnDataChanged { fun updated() }

    fun setCallback(delegate: OnDataChanged) { mDelegate = delegate }

    fun removeCallback() { mDelegate = null }

    override fun getCount(): Int { return favoriteItems.size }

    override fun getItem(index: Int): FavoriteListItemModel { return favoriteItems[index] }

    override fun showAddFavorites() {
        showFavorites(emptyList())
    }

    override fun showNoItemsToFavorite() {
        // No Op
    }

    override fun onError(throwable: Throwable) { /* nop */ }
}