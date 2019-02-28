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
package arcus.app.subsystems.rules.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.iris.client.model.SchedulerModel;
import arcus.app.R;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.view.Version1TextView;

import java.util.Map;


public class RuleListAdapter extends ArrayAdapter<ListItemModel> {
    private CheckableChevronClickListener listener;
    private boolean isEditMode = false;
    Map<String, SchedulerModel> schedulers;

    public RuleListAdapter(Context context) {
        super(context, 0);
    }

    public void setListener(CheckableChevronClickListener listener) {
        this.listener = listener;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        notifyDataSetChanged();
    }

    public void setSchedules(Map<String, SchedulerModel> schedulers) {
        this.schedulers = schedulers;
        notifyDataSetChanged();
    }



    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.rules_cell_twoline_checkable_item, parent, false);
            ViewHolder holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }

        ViewHolder holder = (ViewHolder) convertView.getTag();
        final ListItemModel thisItem = getItem(position);
        holder.asHeading(thisItem.isHeadingRow());

        if (thisItem.isHeadingRow()) {
            holder.rulesSectionHeader.setText(thisItem.getText());
            holder.rulesSectionCount.setText(String.valueOf(thisItem.getCount()));

        } else {
            if (isEditMode) {
                holder.checkbox.setImageResource(R.drawable.icon_delete);
            } else {
                holder.checkbox.setImageResource(thisItem.isChecked() ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);
            }

            if (isScheduled(thisItem)) {
                holder.schedIcon.setVisibility(View.VISIBLE);
            } else {
                holder.schedIcon.setVisibility(View.INVISIBLE);
            }

            holder.chevron.setImageResource(R.drawable.chevron_white);
            holder.title.setText(thisItem.getText());
            holder.subtitle.setText(thisItem.getSubText());

            holder.checkboxClickRegion.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (listener != null) {
                        listener.onCheckboxRegionClicked(position, thisItem, thisItem.isChecked());
                    }
                }
            });

            holder.chevronClickRegion.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (listener != null) {
                        listener.onChevronRegionClicked(position, thisItem);
                    }
                }
            });
        }
        return convertView;
    }

    private boolean isScheduled(ListItemModel listItemModel) {
        if (schedulers != null) {
            SchedulerModel schedulerModel = schedulers.get(listItemModel.getAddress());
            if (schedulerModel != null && schedulerModel.getCommands() != null) {
                return schedulerModel.getCommands().size() > 0;
            }

        }
        return false;

    }

    private static class ViewHolder {
        LinearLayout sectionField, clickRegion;
        FrameLayout itemField;
        ImageView checkbox, chevron, schedIcon;
        LinearLayout checkboxClickRegion, chevronClickRegion;

        Version1TextView title, subtitle, rulesSectionHeader, rulesSectionCount;

        public ViewHolder(View view) {
            sectionField = (LinearLayout) view.findViewById(R.id.section_field);
            itemField    = (FrameLayout) view.findViewById(R.id.item_field);
            clickRegion  = (LinearLayout) view.findViewById(R.id.click_region);

            rulesSectionHeader = (Version1TextView) view.findViewById(R.id.rules_section_header);
            rulesSectionCount  = (Version1TextView) view.findViewById(R.id.rules_section_count);

            title = (Version1TextView) view.findViewById(R.id.title);
            subtitle = (Version1TextView) view.findViewById(R.id.subtitle);
            checkbox = (ImageView) view.findViewById(R.id.checkbox);
            chevron = (ImageView) view.findViewById(R.id.chevron);
            schedIcon = (ImageView) view.findViewById(R.id.sched_icon);
            checkboxClickRegion = (LinearLayout) view.findViewById(R.id.checkbox_click_region);
            chevronClickRegion = (LinearLayout) view.findViewById(R.id.chevron_click_region);
        }

        public void asHeading(boolean asHeading) {
            sectionField.setVisibility(asHeading ? View.VISIBLE : View.GONE);
            itemField.setVisibility(asHeading ? View.GONE : View.VISIBLE);
            clickRegion.setVisibility(asHeading ? View.GONE : View.VISIBLE);
        }
    }
}