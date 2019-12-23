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
package arcus.app.account.settings.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.models.ModelTypeListItem;
import arcus.cornea.common.ViewRenderType;

import java.util.List;

public class PeopleAndPlacesRVAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface OnItemClicked {
        void itemClicked(ModelTypeListItem item);
    }

    List<ModelTypeListItem> items;
    private int count = 0;
    Context context;
    OnItemClicked callback;

    public PeopleAndPlacesRVAdapter(@NonNull Context context, @NonNull List<ModelTypeListItem> items, boolean hasStableIDs) {
        this.items = items;
        setHasStableIds(hasStableIDs);
        this.context = context;
        count = items.size() - 1;
    }

    public void setPersonClickedCallback(OnItemClicked onItemClicked) {
        this.callback = onItemClicked;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;

        switch (viewType) {
            case ViewRenderType.PERSON_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.place_and_people_list_item, parent, false);
                return new PersonViewHolder(view);

            case ViewRenderType.PLACE_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.icon_text_and_abstract_item, parent, false);
                return new PlaceViewHolder(view);

            default:
            case ViewRenderType.HEADER_VIEW:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.section_heading_with_count, parent, false);
                return new HeaderVH(view);
        }
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ModelTypeListItem item;
        int previousType = ViewRenderType.NONE;
        int nextType = ViewRenderType.NONE;
        if (position > 0) {
            previousType = items.get(position - 1).getViewType();
        }
        if (position + 1 <= count) {
            nextType = items.get(position + 1).getViewType();
        }

        switch(holder.getItemViewType()) {
            case ViewRenderType.PERSON_VIEW:
                PersonViewHolder personVH = (PersonViewHolder) holder;
                item = items.get(position);
                personVH.bind(item, nextType, callback);
                break;

            case ViewRenderType.PLACE_VIEW:
                PlaceViewHolder placeVH = (PlaceViewHolder) holder;
                item = items.get(position);
                placeVH.bind(item, previousType, nextType);
                break;

            default:
            case ViewRenderType.HEADER_VIEW:
                HeaderVH headerVH = (HeaderVH) holder;
                headerVH.bind(items.get(position).getText1());
                break;
        }
    }

    @Override public int getItemViewType(int position) {
        return items.get(position).getViewType();
    }

    @Override public long getItemId(int position) {
        return items.get(position).getViewID();
    }

    @Override public int getItemCount() {
        return items.size();
    }

    class HeaderVH extends RecyclerView.ViewHolder {
        TextView headingText;
        public HeaderVH(View itemView) {
            super(itemView);
            headingText = itemView.findViewById(R.id.sectionName);
        }

        public void bind(String text) {
            headingText.setText(text);
        }
    }

    class PersonViewHolder extends RecyclerView.ViewHolder {
        View topDivider, bottomDivider;
        ImageView imageIcon, chevron;
        TextView title, description, description1, description2, description3, abstractText;

        public PersonViewHolder(View itemView) {
            super(itemView);
            topDivider = itemView.findViewById(R.id.top_divider);
            imageIcon = itemView.findViewById(R.id.image_icon);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.list_item_description);
            description1 = itemView.findViewById(R.id.list_item_sub_description1);
            description2 = itemView.findViewById(R.id.list_item_sub_description2);
            description3 = itemView.findViewById(R.id.list_item_sub_description3);
            abstractText = itemView.findViewById(R.id.abstract_text);
            chevron = itemView.findViewById(R.id.image_chevron);
            bottomDivider = itemView.findViewById(R.id.bottom_divider);
        }

        public void bind(final ModelTypeListItem item, @ViewRenderType int nextType, final OnItemClicked listener) {
            if (listener != null) {
                this.itemView.setOnClickListener(new View.OnClickListener() {
                    @SuppressWarnings("ConstantConditions") @Override public void onClick(View v) {
                        try {
                            listener.itemClicked(item);
                        } catch (Exception ignored) {}
                    }
                });
            }
            title.setText(item.getText1());
            if (!TextUtils.isEmpty(item.getText2())) {
                description.setText(item.getText2());
                description.setVisibility(View.VISIBLE);
            }
            else {
                description.setVisibility(View.GONE);
            }
            ImageManager.with(context)
                  .putPersonImage(item.getModelID())
                  .withTransform(new CropCircleTransformation())
                  .into(imageIcon)
                  .execute();

            bottomDivider.setVisibility(ViewRenderType.PERSON_VIEW == nextType ? View.VISIBLE : View.GONE);
        }
    }

    class PlaceViewHolder extends PersonViewHolder {

        public PlaceViewHolder(View itemView) {
            super(itemView);
        }

        public void bind(ModelTypeListItem item, @ViewRenderType int previousType, @ViewRenderType int nextType) {
            title.setText(item.getText1());
            description.setText(item.getText2());
            description1.setText(item.getText3());
            description1.setVisibility(View.VISIBLE);
            ImageManager.with(context)
                  .putPlaceImage(item.getModelID())
                  .withTransform(new CropCircleTransformation())
                  .into(imageIcon)
                  .execute();
            chevron.setVisibility(View.INVISIBLE);

            bottomDivider.setVisibility(ViewRenderType.HEADER_VIEW != nextType ? View.VISIBLE : View.GONE);
            topDivider.setVisibility(previousType == ViewRenderType.PERSON_VIEW ? View.VISIBLE : View.GONE);
        }
    }
}
