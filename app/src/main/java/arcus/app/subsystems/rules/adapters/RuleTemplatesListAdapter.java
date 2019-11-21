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
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.utils.ImageUtils;


public class RuleTemplatesListAdapter extends ArrayAdapter<ListItemModel> {

    public RuleTemplatesListAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemModel item = getItem(position);

        if (item.getData() == null) {
            return getViewForHeading(item, parent);
        } else {
            return getViewForRule(item, parent);
        }
    }

    private View getViewForRule (@NonNull ListItemModel ruleData, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.category_item, parent, false);

        ImageView categoryImageView;
        ImageView chevron;
        TextView text;
        TextView subText;

        categoryImageView = (ImageView) view.findViewById(R.id.imgCategory);
        chevron = (ImageView) view.findViewById(R.id.imgChevron);

        text = (TextView) view.findViewById(R.id.tvText);
        subText = (TextView) view.findViewById(R.id.tvSubText);

        ImageManager.with(getContext())
                .putDrawableResource(R.drawable.chevron)
                .into(chevron)
                .execute();

        categoryImageView.setVisibility(View.GONE);
        text.setText(ruleData.getText());
        subText.setText(ruleData.getSubText());
        view.setMinimumHeight(ImageUtils.dpToPx(getContext(), 65));
        view.setPadding(0, ImageUtils.dpToPx(10), 0, ImageUtils.dpToPx(10));

        return view;
    }

    private View getViewForHeading (@NonNull ListItemModel headingData, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.heading_item, parent, false);

        TextView headingLabel = (TextView) view.findViewById(R.id.heading_text);
        headingLabel.setText(headingData.getText());

        // Heading views are never clickable
        view.setEnabled(false);
        view.setOnClickListener(null);

        return view;
    }

}
