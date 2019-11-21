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
package arcus.app.subsystems.alarm.promonitoring.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.subsystems.alarm.promonitoring.models.AlertDeviceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.utils.StringUtils;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ProMonitoringTypeDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<AlertDeviceModel> allItems; // All items, with the header(s)
    private Reference<OnDeviceClickListener> callbackRef = new WeakReference<>(null);

    public interface OnDeviceClickListener {
        void onDeviceClicked(DeviceModel deviceModel);
    }

    public ProMonitoringTypeDeviceAdapter(@NonNull List<AlertDeviceModel> items)
    {
        allItems = new ArrayList<>();
        this.allItems.addAll(items);

        // On click of one of these (adding a check mark or deleting it from the selected list)
        // update the respective model (checked -> not checked taking from, or putting back to, popular/not popular)
        // If checked has 0 items, add the nonCheckedItem in it's list
        setHasStableIds(true);
    }

    public void setOnDeviceClickedListener(OnDeviceClickListener listener) {
        callbackRef = new WeakReference<>(listener);
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
                boolean precedesHeader = position < (allItems.size() - 1) && allItems.get(position + 1).getViewType() == 1;
                ((PromonDeviceViewHolder) holder).bind(allItems.get(position), precedesHeader);
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
                return new PromonDeviceViewHolder(inflater.inflate(R.layout.promon_device_state_item, parent, false));
        }
    }

    private class PromonDeviceViewHolder extends RecyclerView.ViewHolder {

        private ImageView chevron;
        private ImageView redDot;
        private TextView deviceName;
        private TextView deviceSubtext;
        private TextView abstractText;
        private ImageView deviceIcon;
        private View divider;

        PromonDeviceViewHolder(View itemView) {
            super(itemView);

            deviceIcon = (ImageView) itemView.findViewById(R.id.device_type_image);
            deviceName = (TextView) itemView.findViewById(R.id.promon_device_name);
            deviceSubtext = (TextView) itemView.findViewById(R.id.promon_device_state);
            abstractText = (TextView) itemView.findViewById(R.id.abstract_text);
            chevron = (ImageView) itemView.findViewById(R.id.chevron);
            redDot = (ImageView) itemView.findViewById(R.id.red_dot);
            divider = itemView.findViewById(R.id.divider);
        }

        public void bind(final AlertDeviceModel model, boolean precedesHeader) {

            deviceName.setText(model.mainText);
            ImageManager.with(ArcusApplication.getContext())
                    .putSmallDeviceImage(model.deviceModel)
                    .noUserGeneratedImagery()
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .withPlaceholder(R.drawable.device_list_placeholder)
                    .into(deviceIcon)
                    .execute();

            if (StringUtils.isEmpty(model.abstractText)) {
                abstractText.setVisibility(View.GONE);
            } else {
                abstractText.setText(model.abstractText);
                abstractText.setVisibility(View.VISIBLE);
            }

            if (StringUtils.isEmpty(model.subText)) {
                deviceSubtext.setVisibility(View.GONE);
            } else {
                deviceSubtext.setVisibility(View.VISIBLE);
                deviceSubtext.setText(model.subText);
            }

            divider.setVisibility(precedesHeader ? View.GONE : View.VISIBLE);
            chevron.setVisibility(model.hasChevron ? View.VISIBLE : View.GONE);
            redDot.setVisibility(model.isOnline ? View.GONE : View.VISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (callbackRef.get() != null) callbackRef.get().onDeviceClicked(model.deviceModel);
                }
            });
        }
    }

    private class BlankAlertCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView content;

        BlankAlertCategoryViewHolder(View itemView) {
            super(itemView);
            content = (TextView) itemView.findViewById(R.id.blank_item_content);
        }

        public void bind(String blankItemText) {
            content.setText(blankItemText);
        }
    }

    private class HeaderAlertCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryTitle;

        HeaderAlertCategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = (TextView) itemView.findViewById(R.id.category_title);
        }

        public void bind(String title) {
            categoryTitle.setText(title);
        }
    }
}
