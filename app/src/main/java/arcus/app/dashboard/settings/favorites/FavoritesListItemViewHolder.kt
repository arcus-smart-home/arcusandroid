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

import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import arcus.app.R

class FavoritesListItemViewHolder(itemView: View) : AbstractDraggableItemViewHolder(itemView) {

    val container: LinearLayout = itemView.findViewById(R.id.card_favor_list_item_container)
    val handle: ImageView = itemView.findViewById(R.id.move_handle)
    val deviceTitle: TextView = itemView.findViewById(R.id.device_name)
    val deviceIcon: ImageView = itemView.findViewById(R.id.device_icon)
    val favoriteIcon: ImageView = itemView.findViewById(R.id.favorite_icon)
}
