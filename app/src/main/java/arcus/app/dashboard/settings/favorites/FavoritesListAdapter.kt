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

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.common.collect.ImmutableList
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import arcus.cornea.subsystem.favorites.FavoritesController
import com.iris.client.model.DeviceModel
import com.iris.client.model.Model
import com.iris.client.model.SceneModel
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.ImageUtils
import arcus.app.common.utils.PreferenceUtils
import arcus.app.common.utils.ViewUtils
import arcus.app.subsystems.scenes.catalog.model.SceneCategory
import de.greenrobot.event.EventBus

class FavoritesListAdapter(private val context: Context, private val provider: FavoritesListDataProvider)
    : RecyclerView.Adapter<FavoritesListItemViewHolder>(),
        DraggableItemAdapter<FavoritesListItemViewHolder>,
        FavoritesListDataProvider.OnDataChanged {

    val orderedDeviceIdList: List<String>
        get() = provider.orderedDeviceIdList

    init {
        this.provider.setCallback(this)
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return provider.getItem(position).id
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.card_favorites_list_item, parent, false)
        return FavoritesListItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: FavoritesListItemViewHolder, position: Int) {
        val item = provider.getItem(position)

        holder.deviceTitle.text = item.modelName

        when (FavoritesController.determineModelType(item.model.address)) {
            FavoritesController.DEVICE_MODEL -> ImageManager.with(context)
                .putSmallDeviceImage(item.model as DeviceModel)
                .noUserGeneratedImagery()
                .withTransformForStockImages(BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .into(holder.deviceIcon)
                .execute()
            FavoritesController.SCENE_MODEL -> ImageManager.with(context)
                .putDrawableResource(SceneCategory.fromSceneModel(item.model as SceneModel).iconResId)
                .withTransformForStockImages(BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                .into(holder.deviceIcon)
                .execute()
        }

        holder.favoriteIcon.setImageResource(
            when {
                deviceIsFavorite(item.model) -> R.drawable.favorite_teal_fill_22x20
                else -> R.drawable.favorite_teal_outline_22x20
            }
        )

        holder.favoriteIcon.setOnClickListener {
            val deviceModel = item.model
            val favoriteTag = ImmutableList.of(GlobalSetting.FAVORITE_TAG)

            if (deviceIsFavorite(deviceModel)) {
                deviceModel.removeTags(favoriteTag)
                provider.removeItem(holder.adapterPosition)
                notifyDataSetChanged()
            } else {
                deviceModel.addTags(favoriteTag)
            }

            deviceModel.commit()
        }

        // Adjust the favorites icon hitbox to margins
        val horizontalMargin = 22
        val verticalMargin = (holder.deviceIcon.height - holder.favoriteIcon.height / 2) + 10

        ViewUtils.increaseTouchArea(holder.favoriteIcon, -horizontalMargin, -verticalMargin, horizontalMargin, verticalMargin)
    }

    /**
     * Logs
     * dashboard.settings.favorites.edit.reorder	User reordered favorite order on dashboard -> settings -> favorites -> edit screen
     */
    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) { return }

        provider.moveItem(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun updated() { notifyDataSetChanged() }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        EventBus.getDefault().postSticky(FavoritesOrderChangedEvent(orderedDeviceIdList))
        PreferenceUtils.putOrderedFavoritesList(orderedDeviceIdList)
    }

    private fun deviceIsFavorite(model: Model): Boolean { return model.tags.contains(GlobalSetting.FAVORITE_TAG) }

    override fun getItemCount(): Int { return provider.count }

    override fun onCheckCanStartDrag(holder: FavoritesListItemViewHolder, position: Int, x: Int, y: Int): Boolean {
        return ImageUtils.isLeftOfView(x, holder.deviceIcon, 0)
    }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean { return false }

    override fun onGetItemDraggableRange(favoritesListItemViewHolder: FavoritesListItemViewHolder, i: Int): ItemDraggableRange? { return null }

    override fun onItemDragStarted(position: Int) { /* nop */ }
}
