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
package arcus.app.device.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.cornea.device.smokeandco.WeatherAlertModel;
import arcus.app.R;
import arcus.app.common.utils.ViewUtils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WeatherAlertCategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final List<WeatherAlertModel> checked; // Checked items, including the header
    final List<WeatherAlertModel> popular; // Popular items, including the header
    final List<WeatherAlertModel> other; // Not Popular items, including the header

    List<WeatherAlertModel> allItems; // All items, with the header(s)

    final WeatherAlertModel noneCheckedItem; // Default item to show for 'nothin selected'

    Drawable checkedItem, notCheckedItem;
    Reference<Callback> callbackRef = new WeakReference<>(null);

    public interface Callback {
        void updateSelected(List<WeatherAlertModel> checkedItems);
    }

    public WeatherAlertCategoryAdapter(
          @NonNull List<WeatherAlertModel> checked,
          @NonNull List<WeatherAlertModel> popular,
          @NonNull List<WeatherAlertModel> other,
          @NonNull WeatherAlertModel noneCheckedItem,
          @NonNull Context context
    ) {
        this.noneCheckedItem = noneCheckedItem;

        this.checked = new ArrayList<>(checked);
        if (checked.size() == 1) { // If it only contains the header... add the 'non'checked item in.
            checked.add(noneCheckedItem);
        }
        this.popular = new ArrayList<>(popular);
        this.other = new ArrayList<>(other);

        allItems = new ArrayList<>();
        this.allItems.addAll(checked);
        this.allItems.addAll(popular);
        this.allItems.addAll(other);

        // On click of one of these (adding a check mark or deleting it from the selected list)
        // update the respective model (checked -> not checked taking from, or putting back to, popular/not popular)
        // If checked has 0 items, add the nonCheckedItem in it's list
        setHasStableIds(true);

        checkedItem = ContextCompat.getDrawable(context, R.drawable.circle_check_white_filled);
        notCheckedItem = ContextCompat.getDrawable(context, R.drawable.circle_hollow_white);
    }

    public void setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
    }

    @Override
    public long getItemId(int position) {
        return allItems.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return allItems.get(position).getViewType();
    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case 1:
                ((HeaderAlertCategoryViewHolder) holder).bind(allItems.get(position).mainText);
                break;

            case 2:
                ((BlankAlertCategoryViewHolder) holder).bind(allItems.get(position).mainText);
                break;

            case 3:
                ((WeatherAlertCategoryViewHolder) holder).bind(allItems.get(position), allItems.get(position - 1).getViewType() == 1);
                break;

            default:
                // No-Op
                break;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case 1:
                return new HeaderAlertCategoryViewHolder(inflater.inflate(R.layout.weather_alert_category_header_item, parent, false));

            case 2:
                return new BlankAlertCategoryViewHolder(inflater.inflate(R.layout.weather_alert_blank_item, parent, false));

            default: // case 2
                return new WeatherAlertCategoryViewHolder(inflater.inflate(R.layout.weather_alert_item, parent, false));
        }
    }

    protected void removeCheckedItem(int position) {
        WeatherAlertModel item = allItems.get(position);
        if (item.isChecked) {
            checked.remove(item);
            if (checked.size() == 1) {
                checked.add(noneCheckedItem);
            }
            Collections.sort(checked);

            item.isChecked = false;
            if (item.isPopular) {
                popular.add(item);
                Collections.sort(popular);
            } else {
                other.add(item);
                Collections.sort(other);
            }

            reInitializeItems();
        }
    }

    protected void toggleCheck(int position) {
        if (position < 0 || position >= getItemCount()) {
            return;
        }

        WeatherAlertModel item = allItems.get(position);
        if (item.isChecked) {
            removeCheckedItem(position);
        } else {
            if (item.isPopular) {
                popular.remove(item);
                Collections.sort(popular);
            } else {
                other.remove(item);
                Collections.sort(other);
            }

            item.isChecked = true;
            checked.add(item);
            checked.remove(noneCheckedItem);
            Collections.sort(checked);

            reInitializeItems();
        }
    }

    protected void reInitializeItems() {
        allItems = new ArrayList<>();

        this.allItems.addAll(checked);
        this.allItems.addAll(popular);
        this.allItems.addAll(other);

        updateCallback();
        notifyDataSetChanged();
    }

    protected void updateCallback() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.updateSelected(checked);
        }
    }

    public class WeatherAlertCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView weatherItem;
        View divider;

        public WeatherAlertCategoryViewHolder(View itemView) {
            super(itemView);

            divider = itemView.findViewById(R.id.weather_alert_divider);
            weatherItem = (TextView) itemView.findViewById(R.id.weather_alert_item);
            ViewUtils.increaseTouchArea(weatherItem, -50, -50, 50, 50);
            weatherItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleCheck(getAdapterPosition());
                }
            });
        }

        public void bind(WeatherAlertModel model, boolean isFirst) {
            weatherItem.setCompoundDrawablesWithIntrinsicBounds(model.isChecked ? checkedItem : notCheckedItem, null, null, null);
            weatherItem.setText(model.mainText);
            divider.setVisibility(isFirst ? View.GONE : View.VISIBLE);
        }
    }

    public class BlankAlertCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView content;

        public BlankAlertCategoryViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.blank_item_content);
        }

        public void bind(String blankItemText) {
            content.setText(blankItemText);
        }
    }

    public class HeaderAlertCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;

        public HeaderAlertCategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = (TextView) itemView.findViewById(R.id.category_title);
        }

        public void bind(String title) {
            categoryTitle.setText(title);
        }
    }
}
