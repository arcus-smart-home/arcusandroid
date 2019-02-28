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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.app.subsystems.alarm.promonitoring.models.AlertDeviceModel;
import arcus.app.R;
import arcus.app.common.view.Version1Toggle;
import arcus.app.subsystems.alarm.promonitoring.ProMonitoringMoreFragment;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ProMonitoringSettingsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    final List<AlertDeviceModel> general;
    final List<AlertDeviceModel> security;
    final List<AlertDeviceModel> smokeAndCO;
    final List<AlertDeviceModel> waterleak;

    List<AlertDeviceModel> allItems; // All items, with the header(s)

    Reference<Callback> callbackRef = new WeakReference<>(null);

    public interface Callback {
        void updateSelected(AlertDeviceModel selectedItem);
        void updateToggleValue(int modelId, boolean on);
    }

    public ProMonitoringSettingsAdapter(
          @NonNull List<AlertDeviceModel> general,
          @NonNull List<AlertDeviceModel> security,
          @NonNull List<AlertDeviceModel> smokeAndCO,
          @NonNull List<AlertDeviceModel> waterleak,
          @NonNull Context context
    ) {

        this.general = new ArrayList<>(general);
        this.security = new ArrayList<>(security);
        this.smokeAndCO = new ArrayList<>(smokeAndCO);
        this.waterleak = new ArrayList<>(waterleak);

        allItems = new ArrayList<>();
        this.allItems.addAll(general);
        this.allItems.addAll(security);
        this.allItems.addAll(smokeAndCO);
        this.allItems.addAll(waterleak);
    }

    public void setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
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
                ((HeaderAlertCategoryViewHolder) holder).bind(allItems.get(position));
                break;

            case 2:
                ((BlankAlertCategoryViewHolder) holder).bind(allItems.get(position).mainText);
                break;

            case 3:
                ((PromonSettingViewHolder) holder).bind(allItems.get(position));
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
                return new HeaderAlertCategoryViewHolder(inflater.inflate(R.layout.alarm_setting_category_header_item, parent, false));

            case 2:
                return new BlankAlertCategoryViewHolder(inflater.inflate(R.layout.weather_alert_blank_item, parent, false));

            default: // case 2
                return new PromonSettingViewHolder(inflater.inflate(R.layout.promon_settings_item, parent, false));
        }
    }

    public void putLists(@NonNull List<AlertDeviceModel> general,
                         @NonNull List<AlertDeviceModel> security,
                         @NonNull List<AlertDeviceModel> smokeAndCO,
                         @NonNull List<AlertDeviceModel> waterleak) {
        allItems.clear();
        allItems.addAll(general);
        allItems.addAll(security);
        allItems.addAll(smokeAndCO);
        allItems.addAll(waterleak);
        notifyDataSetChanged();
    }

    public class PromonSettingViewHolder extends RecyclerView.ViewHolder {
        TextView settingName;
        TextView settingDescription;
        ImageView chevron;
        Version1Toggle toggleButton;

        public PromonSettingViewHolder(View itemView) {
            super(itemView);

            settingName = (TextView) itemView.findViewById(R.id.promon_setting_title);
            settingDescription = (TextView) itemView.findViewById(R.id.promon_setting_description);
            chevron = (ImageView) itemView.findViewById(R.id.chevron);
            toggleButton = (Version1Toggle) itemView.findViewById(R.id.toggle_button);
        }

        public void bind(final AlertDeviceModel model) {
            settingName.setText(model.mainText);
            if(TextUtils.isEmpty(model.subText)) {
                settingDescription.setVisibility(View.GONE);
            } else {
                settingDescription.setVisibility(View.VISIBLE);
                settingDescription.setText(model.subText);
            }

            if(model.id == ProMonitoringMoreFragment.WATER_SHUTOFF
                    || model.id == ProMonitoringMoreFragment.RECORD_ALARM
                    || model.id == ProMonitoringMoreFragment.SMOKE_SAFETY_SHUT_OFF
                    || model.id == ProMonitoringMoreFragment.CO_SAFETY_SHUT_OFF) {
                toggleButton.setVisibility(View.VISIBLE);
                chevron.setVisibility(View.GONE);
            } else {
                toggleButton.setVisibility(View.GONE);
                chevron.setVisibility(View.VISIBLE);
            }

            switch(model.id) {
                case ProMonitoringMoreFragment.WATER_SHUTOFF:
                    toggleButton.setChecked(model.waterShutoffEnabled);
                    break;
                case ProMonitoringMoreFragment.RECORD_ALARM:
                    toggleButton.setChecked(model.recordingSupported);
                    break;
                case ProMonitoringMoreFragment.SMOKE_SAFETY_SHUT_OFF:
                    toggleButton.setChecked(model.shutOffFansOnSmoke);
                    break;
                case ProMonitoringMoreFragment.CO_SAFETY_SHUT_OFF:
                    toggleButton.setChecked(model.shutOffFansOnCO);
                    break;
            }

            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean checked = ((ToggleButton)view).isChecked();
                    Callback callback = callbackRef.get();
                    if (callback != null) {
                        callback.updateToggleValue(model.id, checked);
                    }
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Callback callback = callbackRef.get();
                    if (callback != null) {
                        callback.updateSelected(model);
                    }
                }
            });

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
        ImageView smokeImage;
        ImageView coImage;
        ImageView waterleakImage;
        ImageView securityImage;

        public HeaderAlertCategoryViewHolder(View itemView) {
            super(itemView);
            categoryTitle = (TextView) itemView.findViewById(R.id.category_title);
            smokeImage = (ImageView) itemView.findViewById(R.id.smoke_image);
            coImage = (ImageView) itemView.findViewById(R.id.co_image);
            securityImage = (ImageView) itemView.findViewById(R.id.security_image);
            waterleakImage = (ImageView) itemView.findViewById(R.id.water_image);
        }

        public void bind(AlertDeviceModel model) {
            categoryTitle.setText(model.mainText);
            if(model.hasCO) {
                coImage.setVisibility(View.VISIBLE);
            } else {
                coImage.setVisibility(View.GONE);
            }

            if(model.hasSmoke) {
                smokeImage.setVisibility(View.VISIBLE);
            } else {
                smokeImage.setVisibility(View.GONE);
            }

            if(model.hasSecurity) {
                securityImage.setVisibility(View.VISIBLE);
            } else {
                securityImage.setVisibility(View.GONE);
            }

            if(model.hasWaterLeak) {
                waterleakImage.setVisibility(View.VISIBLE);
            } else {
                waterleakImage.setVisibility(View.GONE);
            }
        }
    }
}
