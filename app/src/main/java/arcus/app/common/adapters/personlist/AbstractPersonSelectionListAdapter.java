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
package arcus.app.common.adapters.personlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.utils.ImageUtils;
import arcus.app.dashboard.settings.model.DraggableListDataProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for drag-and-drop re-orderable list of people. When in edit mode, a checkbox and drag
 * handle is rendered in each cell; when not in edit mode, only the user avatar, name, and relationship
 * is rendered.
 */
public abstract class AbstractPersonSelectionListAdapter extends RecyclerView.Adapter<AbstractPersonSelectionListAdapter.PersonListItemViewHolder> implements DraggableListDataProvider, DraggableItemAdapter<AbstractPersonSelectionListAdapter.PersonListItemViewHolder> {

    public interface OnCheckboxCheckedListener {
        boolean onCheckboxChecked (int position);
    }

    private OnCheckboxCheckedListener checkboxClickListener;
    private final List<PersonListItemModel> items = new ArrayList<>();
    private boolean isEditable = false;

    public AbstractPersonSelectionListAdapter() {
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public PersonListItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.call_list_item, parent, false);
        return new PersonListItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final PersonListItemViewHolder holder, final int position) {
        final PersonListItemModel itemData = items.get(position);

        if (itemData.getPersonId() != null) {
            ImageManager.with(ArcusApplication.getContext())
                    .putPersonImage(itemData.getPersonId())
                    .withTransform(new CropCircleTransformation())
                    .withPlaceholder(R.drawable.icon_user_small_white)
                    .withError(R.drawable.icon_user_small_white)
                    .into(holder.personImage)
                    .execute();
        } else {
            holder.personImage.setImageResource(itemData.getPersonIconResId());
        }

        holder.personName.setText(itemData.getDisplayName());
        holder.personRelationship.setText(itemData.getSubtext() != null ? itemData.getSubtext() : itemData.getDisplayRelationship());
        holder.personRelationship.setVisibility(itemData.getSubtext() == null ? View.GONE : View.VISIBLE);              // ITWO-5928: Hide relationship

        holder.checkboxIcon.setVisibility(isEditable ? View.VISIBLE : View.GONE);
        holder.checkboxIcon.setImageResource(itemData.isChecked() ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);
        holder.checkboxIcon.setAlpha(itemData.isEnabled() ? 1.0f : 0.25f);

        if (itemData.hasDisclosure()) {
            holder.handle.setVisibility(View.VISIBLE);
            holder.handle.setImageResource(R.drawable.chevron_white);
        } else {
            holder.handle.setImageResource(R.drawable.icon_side_menu_white);
            holder.handle.setVisibility(isEditable && itemData.isReorderable() ? View.VISIBLE : View.INVISIBLE);
        }

        holder.checkboxRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.checkboxIcon.getVisibility() == View.VISIBLE && itemData.isEnabled()) {

                    // If a click listener is registered, then only allow checking the box if the listener returns true.
                    if (checkboxClickListener == null || itemData.isChecked() || checkboxClickListener.onCheckboxChecked(position)) {
                        itemData.setIsChecked(!itemData.isChecked());
                        notifyItemChanged(position);
                    }
                }
            }
        });
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).getId();
    }

    public PersonListItemModel getItem(int position) {
        return items.get(position);
    }

    public List<PersonListItemModel> getItems () {
        return new ArrayList<>(items);
    }

    public void setItems (List<PersonListItemModel> items) {
        this.items.clear();
        this.items.addAll(items);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        PersonListItemModel item = items.remove(fromPosition);
        items.add(toPosition, item);

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void removeItem(int fromPosition) {
        items.remove(fromPosition);
        notifyItemRemoved(fromPosition);
    }

    @Override
    public int getItemCount() {
        return getCount();
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        moveItem(fromPosition, toPosition);
    }

    @Override
    public boolean onCheckCanStartDrag(PersonListItemViewHolder holder, int position, int x, int y) {
        return isEditable &&
                items.get(position).isReorderable() &&
                ImageUtils.isRightOfView(x, holder.handle, 20);
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return isEditable &&
                items.get(draggingPosition).isReorderable();
    }

    /**
     * Some items in the list may not be reorderable; this list allows items that are reorderable to
     * to be dragged dragged withing the range of adjacent items which are also reorderable.
     *
     * That is, non-reorderable items create a barrier that prevent other items from crossing. This
     * lets such non-reorderable items remain pinned/fixed into their original location in the
     * list.
     *
     * @param holder
     * @param position
     * @return
     */
    @Nullable
    @Override
    public ItemDraggableRange onGetItemDraggableRange(PersonListItemViewHolder holder, int position) {
        int firstDraggablePosition = 0;
        int lastDraggablePosition = items.size();

        for (int index = position; index >= 0; index--) {
            if (items.get(index).isReorderable()) {
                firstDraggablePosition = index;
            } else {
                break;
            }
        }

        for (int index = position; index < items.size(); index++) {
            if (items.get(index).isReorderable()) {
                lastDraggablePosition = index;
            } else {
                break;
            }
        }

        // Any item can be dragged to any position; no range restrictions.
        return new ItemDraggableRange(firstDraggablePosition, lastDraggablePosition);
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public void setOnCheckboxCheckedListener(OnCheckboxCheckedListener listener) {
        this.checkboxClickListener = listener;
    }

    public int getCheckedItemsCount() {
        int checkedCount = 0;
        for (PersonListItemModel thisItem : items) {
            if (thisItem.isChecked()) checkedCount++;
        }

        return checkedCount;
    }

    public static class PersonListItemViewHolder extends AbstractDraggableItemViewHolder {
        @NonNull public final LinearLayout container;
        @NonNull public final LinearLayout checkboxRegion;
        @NonNull public final ImageView checkboxIcon;
        @NonNull public final ImageView handle;
        @NonNull public final ImageView personImage;
        @NonNull public final TextView personName;
        @NonNull public final TextView personRelationship;

        public PersonListItemViewHolder(@NonNull View itemView) {
            super(itemView);

            this.container = (LinearLayout) itemView.findViewById(R.id.call_list_item_container);
            this.checkboxRegion = (LinearLayout) itemView.findViewById(R.id.checkbox_clickable_region);
            this.handle = (ImageView) itemView.findViewById(R.id.drag_handle);
            this.personImage = (ImageView) itemView.findViewById(R.id.call_list_person_image);
            this.personName = (TextView) itemView.findViewById(R.id.call_list_person_name);
            this.personRelationship = (TextView) itemView.findViewById(R.id.call_list_person_relationship);
            this.checkboxIcon = (ImageView) itemView.findViewById(R.id.call_list_checkbox);
        }
    }

    @Override
    public void onItemDragStarted(int position) {

    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {

    }
}
