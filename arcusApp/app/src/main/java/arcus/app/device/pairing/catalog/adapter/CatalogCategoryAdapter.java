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
package arcus.app.device.pairing.catalog.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.image.DeviceCategory;
import arcus.app.common.image.ImageManager;
import arcus.app.common.models.ListItemModel;

import java.util.ArrayList;


public class CatalogCategoryAdapter extends ArrayAdapter<ListItemModel> {

    public CatalogCategoryAdapter(Context context, @NonNull ArrayList<ListItemModel> categories) {
        super(context, 0, categories);
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.category_item, parent, false);
        }

        ListItemModel listData = getItem(position);

        ImageView categoryImageView = (ImageView) convertView.findViewById(R.id.imgCategory);
        ImageView chevronImage = (ImageView) convertView.findViewById(R.id.imgChevron);
        TextView text = (TextView) convertView.findViewById(R.id.tvText);
        TextView subText = (TextView) convertView.findViewById(R.id.tvSubText);

        ImageManager.with(getContext())
                .putDeviceCategoryImage(DeviceCategory.fromProductCategoryName(listData.getText()))
                .withPlaceholder(R.drawable.icon_cat_placeholder)
                .withError(R.drawable.icon_cat_placeholder)
                .into(categoryImageView)
                .execute();

        chevronImage.setImageResource(R.drawable.chevron);

        text.setText(listData.getText());
        subText.setText(listData.getSubText());

        return convertView;
    }
}
