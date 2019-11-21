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
package arcus.app.dashboard.settings.services;

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import arcus.app.R;

/**
 * View holder for a service card list item.
 */
public class ServiceListItemViewHolder extends AbstractDraggableItemViewHolder {

    @NonNull
    public final LinearLayout container;
    @NonNull
    public final LinearLayout checkboxRegion;
    @NonNull
    public final ImageView handle;
    @NonNull
    public final TextView serviceTitle;
    @NonNull
    public final ImageView serviceIcon;
    @NonNull
    public final Switch toggleButton;

    public ServiceListItemViewHolder(@NonNull View itemView) {
        super(itemView);

        this.container = (LinearLayout) itemView.findViewById(R.id.card_service_list_item_container);
        this.checkboxRegion = (LinearLayout) itemView.findViewById(R.id.checkbox_clickable_region);
        this.handle = (ImageView) itemView.findViewById(R.id.drag_handle);
        this.serviceTitle = (TextView) itemView.findViewById(R.id.service_title);
        this.serviceIcon = (ImageView) itemView.findViewById(R.id.service_icon);
        this.toggleButton = (Switch) itemView.findViewById(R.id.card_toggle);
    }
}
