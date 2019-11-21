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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.models.ListItemModel;


public class RuleCategoriesListAdapter extends ArrayAdapter<ListItemModel> {

    public RuleCategoriesListAdapter(Context context) {
        super(context, 0);
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        ListItemModel listData = getItem(position);

        ImageView categoryImageView;
        ImageView chevron;
        TextView text;
        TextView subText;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_item, parent, false);
        }

        categoryImageView = (ImageView) convertView.findViewById(R.id.imgCategory);
        chevron = (ImageView) convertView.findViewById(R.id.imgChevron);

        text = (TextView) convertView.findViewById(R.id.tvText);
        subText = (TextView) convertView.findViewById(R.id.tvSubText);

        ImageManager.with(getContext())
                .putDrawableResource(listData.getImageResId())
                .withPlaceholder(R.drawable.icon_cat_placeholder)
                .withError(R.drawable.icon_cat_placeholder)
                .into(categoryImageView)
                .execute();

        ImageManager.with(getContext())
                .putDrawableResource(R.drawable.chevron)
                .into(chevron)
                .execute();

        text.setText(listData.getText());
        subText.setText(listData.getSubText());


        return convertView;
    }
}
