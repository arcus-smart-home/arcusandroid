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
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import arcus.cornea.model.StringPair;
import arcus.cornea.subsystem.lawnandgarden.utils.LNGDefaults;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.model.DeviceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.HoursMinutePickerFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.ImageUtils;

import java.util.List;
import java.util.Set;

public class IrrigationZoneCardListAdapter extends RecyclerView.Adapter<IrrigationZoneListItemViewHolder> implements DraggableItemAdapter<IrrigationZoneListItemViewHolder>,
    HoursMinutePickerFragment.Callback {

    private boolean isEditable = false;

    private final Context context;
    private final IrrigationZoneListDataProvider mProvider;
    private boolean bShowSelectedOnly = true;
    private String deviceAddress;
    IrrigationZoneCardListAdapter adapter;
    boolean scheduleEditMode;
    private DeviceModel deviceModel;
    String deviceId;

    public IrrigationZoneCardListAdapter(Context context, IrrigationZoneListDataProvider dataProvider, String deviceAddress, boolean scheduleEditMode) {
        this.context = context;
        this.mProvider = dataProvider;
        this.deviceAddress = deviceAddress;
        setHasStableIds(true);
        adapter = this;
        this.scheduleEditMode = scheduleEditMode;
        deviceId = CorneaUtils.getIdFromAddress(deviceAddress);
    }

    private DeviceModel getDeviceModel() {
        if(deviceModel == null) {
            deviceModel = SessionModelManager.instance().getDeviceWithId(deviceId, false);
        }

        return deviceModel;
    }

    public int getDefaultDuration(String zone) {
        DeviceModel device = getDeviceModel();
        if (device == null) {
            return 1;
        }

        Number dfltWater = (Number) device.get(String.format("%s:%s", IrrigationZone.ATTR_DEFAULTDURATION, zone));
        return (dfltWater != null) ? dfltWater.intValue() : 1;
    }

    public List<IrrigationZoneCard> getOrderedCardList () {
        return mProvider.getOrderedListOfItems();
    }

    public Set<IrrigationZoneCard> getVisibleCards () {
        return mProvider.getVisibleItems();
    }

    @Override
    public long getItemId(int position) {
        if(bShowSelectedOnly) {
            return mProvider.getOrderedVisibleItems().get(position).getId();
        }
        return mProvider.getItem(position).getId();
    }

    @NonNull
    @Override
    public IrrigationZoneListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.card_irrigation_zone_list_item, parent, false);
        return new IrrigationZoneListItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final IrrigationZoneListItemViewHolder holder, final int position) {
        final IrrigationZoneListItemModel itemData = mProvider.getItem(position);

        holder.container.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
        holder.title.setText(itemData.getText());
        holder.description.setText(itemData.getDescription());
        holder.handle.setVisibility(isEditable ? View.VISIBLE : View.INVISIBLE);
        holder.chevron.setVisibility(isEditable ? View.INVISIBLE : View.VISIBLE);
        holder.checkboxIcon.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        holder.duration.setText(itemData.getIrrigationZoneCard().getDuration() + " min");

        int textColor = 0;
        if(scheduleEditMode) {
            textColor = ArcusApplication.getContext().getResources().getColor(R.color.white);
            holder.checkboxIcon.setImageResource(itemData.isEnabled() ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);
            holder.handle.setImageResource(R.drawable.icon_side_menu);
            holder.chevron.setImageResource(R.drawable.chevron_white);
            holder.divider.setBackgroundColor(ArcusApplication.getContext().getResources().getColor(R.color.white_with_10));
            ImageManager.with(context)
                    .putDrawableResource(itemData.getIrrigationZoneCard().getIconDrawableResId())
                    .fit()
                    .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .into(holder.serviceIcon)
                    .execute();
        }
        else {
            textColor = ArcusApplication.getContext().getResources().getColor(R.color.black);
            holder.checkboxIcon.setImageResource(itemData.isEnabled() ? R.drawable.circle_check_black_filled : R.drawable.circle_hollow_black);
            holder.serviceIcon.setImageResource(itemData.getIrrigationZoneCard().getIconDrawableResId());
            holder.divider.setBackgroundColor(ArcusApplication.getContext().getResources().getColor(R.color.black_with_10));
            ImageManager.with(context)
                    .putDrawableResource(R.drawable.icon_side_menu)
                    .fit()
                    .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                    .into(holder.handle)
                    .execute();
            holder.chevron.setImageResource(R.drawable.chevron);
        }

        holder.title.setTextColor(textColor);
        holder.description.setTextColor(textColor);
        holder.duration.setTextColor(textColor);


        holder.checkboxRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkboxIcon.getVisibility() == View.VISIBLE) {
                    itemData.setEnabled(!itemData.isEnabled());
                    notifyDataSetChanged();
                }
            }
        });
        holder.container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isEditable) {
                    HoursMinutePickerFragment fragment = HoursMinutePickerFragment.newInstance(deviceAddress,
                            ArcusApplication.getContext().getString(R.string.irrigation_duration),
                            itemData.getIrrigationZoneCard().getZoneId(),
                            LNGDefaults.wateringTimeOptions(),
                            String.valueOf(getDefaultDuration(itemData.getIrrigationZoneCard().getZoneId())));

                    fragment.setCallback(adapter);
                    BackstackManager.getInstance().navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if(bShowSelectedOnly) {
            return mProvider.getOrderedVisibleItems().size();
        }
        return mProvider.getCount();
    }

    public List<IrrigationZoneListItemModel> getOrderedVisibleItems() {
        return mProvider.getOrderedVisibleItems();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        mProvider.moveItem(fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(IrrigationZoneListItemViewHolder holder, int position, int x, int y) {
        return (isEditable && ImageUtils.isRightOfView(x, holder.handle, 20));
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return isEditable;
    }


    @Nullable
    @Override
    public ItemDraggableRange onGetItemDraggableRange(IrrigationZoneListItemViewHolder holder, int position) {
        // Any item can be dragged to any position; no range restrictions.
        return null;
    }

    public void setIsEditable (boolean isEditable) {
        this.isEditable = isEditable;
        super.notifyDataSetChanged();
    }

    public void setVisibleItemsChecked() {

        Set<IrrigationZoneCard> visibleCards = mProvider.getVisibleItems();
        List<IrrigationZoneCard> orderedListCards = mProvider.getOrderedListOfItems();

        for (int i =0; i < orderedListCards.size(); i++) {
            if (visibleCards.contains(mProvider.getItem(i))) {
                mProvider.getItem(i).setEnabled(true);
            }
        }

    }

    public void showSelectedItemsOnly(boolean bShowSelected) {
        this.bShowSelectedOnly = bShowSelected;
        mProvider.setShowEnabledItemsOnly(bShowSelected);
    }

    @Override
    public void selectionComplete(String deviceId, String zone, StringPair selected) {
        for(IrrigationZoneCard card : mProvider.getOrderedListOfItems()) {
            if(card.getZoneId().equals(zone)) {
                card.setDuration(Integer.parseInt(selected.getKey()));
                break;
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragStarted(int position) {

    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {

    }
}
