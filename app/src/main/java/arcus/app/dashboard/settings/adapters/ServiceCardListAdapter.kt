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
package arcus.app.dashboard.settings.adapters

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import arcus.app.R
import arcus.app.common.image.ImageManager
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation
import arcus.app.common.image.picasso.transformation.Invert
import arcus.app.common.utils.ImageUtils
import arcus.app.dashboard.settings.services.CardListChangedEvent
import arcus.app.dashboard.settings.services.ServiceCard
import arcus.app.dashboard.settings.services.ServiceListDataProvider
import arcus.app.dashboard.settings.services.ServiceListItemViewHolder
import de.greenrobot.event.EventBus


class ServiceCardListAdapter(private val context: Context, private val mProvider: ServiceListDataProvider)
    : RecyclerView.Adapter<ServiceListItemViewHolder>(),
        DraggableItemAdapter<ServiceListItemViewHolder> {

    private val orderedCardList: List<ServiceCard>
        get() = mProvider.orderedListOfItems

    private val visibleCards: Set<ServiceCard>
        get() = mProvider.visibleItems

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceListItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.card_service_list_item, parent, false)
        return ServiceListItemViewHolder(v)
    }

    override fun onBindViewHolder(holder: ServiceListItemViewHolder, position: Int) {
        val itemData = mProvider.getItem(position)

        ImageManager.with(context)
            .putDrawableResource(itemData.serviceCard.smallIconDrawableResId)
            .withTransformForStockImages(BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
            .into(holder.serviceIcon)
            .execute()

        holder.serviceTitle.text = itemData.text

        holder.toggleButton.isChecked = itemData.isEnabled

        holder.toggleButton.setOnCheckedChangeListener { _, isChecked ->
            itemData.isEnabled = isChecked
            EventBus.getDefault().postSticky(CardListChangedEvent(orderedCardList, visibleCards))
        }
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) { return }

        mProvider.moveItem(fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun setVisibleItemsChecked() {
        val visibleCards: Set<ServiceCard> = mProvider.visibleItems
        val orderedListCards: List<ServiceCard> = mProvider.orderedListOfItems

        for (i in orderedListCards.indices) {
            if (visibleCards.contains(mProvider.getItem(i).serviceCard)) {
                mProvider.getItem(i).isEnabled = true
            }
        }
    }

    override fun onCheckCanStartDrag(holder: ServiceListItemViewHolder, position: Int, x: Int, y: Int): Boolean {
        return ImageUtils.isLeftOfView(x, holder.serviceIcon, -18)
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        EventBus.getDefault().postSticky(CardListChangedEvent(orderedCardList, visibleCards))
    }

    override fun getItemId(position: Int): Long { return mProvider.getItem(position).id }

    override fun getItemCount(): Int { return mProvider.count }

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean { return false }

    override fun onGetItemDraggableRange(holder: ServiceListItemViewHolder, position: Int): ItemDraggableRange? { return null }

    override fun onItemDragStarted(position: Int) { }
}
