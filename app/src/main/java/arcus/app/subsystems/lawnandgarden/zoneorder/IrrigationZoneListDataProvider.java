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
package arcus.app.subsystems.lawnandgarden.zoneorder;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.dashboard.settings.model.DraggableListDataProvider;
import arcus.app.subsystems.lawnandgarden.models.IrrigationZoneInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IrrigationZoneListDataProvider implements DraggableListDataProvider {

    @NonNull
    List<IrrigationZoneListItemModel> irrigationZoneModelList = new ArrayList<>();
    boolean bShowEnabledItemsOnly = true;

    public IrrigationZoneListDataProvider(@NonNull Context context, @NonNull ArrayList<IrrigationZoneInfo> zones) {
        for(IrrigationZoneInfo info : zones) {

            IrrigationZoneCard card = new IrrigationZoneCard(info.getDisplayName(), info.getZoneDisplay(),
                    R.drawable.irrigation_zone_small, 0, info.getZoneId(), info.getDuration());
            IrrigationZoneListItemModel data = new IrrigationZoneListItemModel(card.getTitle(), card);
            data.setEnabled(info.isVisible());
            irrigationZoneModelList.add(data);
        }
    }

    @Override
    public int getCount() {
        if(bShowEnabledItemsOnly) {
            return getOrderedVisibleItems().size();
        }
        return irrigationZoneModelList.size();
    }

    public void setShowEnabledItemsOnly(boolean bShowEnabledItemsOnly) {
        this.bShowEnabledItemsOnly = bShowEnabledItemsOnly;
    }

    @Override
    public IrrigationZoneListItemModel getItem(int index) {
        if(bShowEnabledItemsOnly) {
            return getOrderedVisibleItems().get(index);
        }
        return irrigationZoneModelList.get(index);
    }

    @Override
    public void removeItem(int position) {
        // Nothing to do; items not removable
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        IrrigationZoneListItemModel data = irrigationZoneModelList.remove(fromPosition);
        irrigationZoneModelList.add(toPosition, data);
    }

    @NonNull
    public Set<IrrigationZoneCard> getVisibleItems() {
        Set<IrrigationZoneCard> visibleCards = new HashSet<>();
        for (IrrigationZoneListItemModel thisModel : irrigationZoneModelList) {
            if (thisModel.isEnabled()) {
                visibleCards.add(thisModel.getIrrigationZoneCard());
            }
        }

        return visibleCards;
    }

    @NonNull
    public List<IrrigationZoneListItemModel> getOrderedVisibleItems() {
        List<IrrigationZoneListItemModel> visibleCards = new ArrayList<>();
        for (IrrigationZoneListItemModel thisModel : irrigationZoneModelList) {
            if (thisModel.isEnabled()) {
                visibleCards.add(thisModel);
            }
        }

        return visibleCards;
    }

    @NonNull
    public List<IrrigationZoneCard> getOrderedListOfItems() {
        List<IrrigationZoneCard> orderedList = new ArrayList<>();

        for (IrrigationZoneListItemModel thisModel : irrigationZoneModelList) {
            orderedList.add(thisModel.getIrrigationZoneCard());
        }

        return orderedList;
    }
}
