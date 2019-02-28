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
import arcus.app.common.image.ImageManager;
import arcus.app.common.models.ListItemModel;

import java.util.ArrayList;


public class CatalogBrandAdapter extends ArrayAdapter<ListItemModel> {

    public CatalogBrandAdapter(Context context, @NonNull ArrayList<ListItemModel> brands) {
        super(context, 0, brands);
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.brand_item, parent, false);
        }

        ListItemModel listData = getItem(position);

        ImageView brandImageView = (ImageView) convertView.findViewById(R.id.imgBrand);
        ImageView chevronImage = (ImageView) convertView.findViewById(R.id.imgChevron);
        TextView deviceText = (TextView) convertView.findViewById(R.id.tvDeviceText);

        ImageManager.with(getContext())
                .putBrandImage(listData.getText())
                .withPlaceholder(R.drawable.icon_cat_placeholder)
                .withError(R.drawable.icon_cat_placeholder)
                .into(brandImageView)
                .execute();

        chevronImage.setImageResource(R.drawable.chevron);
        deviceText.setText(listData.getSubText());

        return convertView;
    }
}
