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
package arcus.app.device.pairing.multi.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.models.ListItemModel;

import java.util.List;

public class MultipairingListAdapter extends ArrayAdapter<ListItemModel> {

    public MultipairingListAdapter(Context context, @NonNull List<ListItemModel> deviceList) {
        super(context, 0, deviceList);
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        ListItemModel item = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.multi_pairing_list_item, parent, false);
        }

        ImageView detailImageView = (ImageView) convertView.findViewById(R.id.imgDetail);
        ImageView chevronImageView = (ImageView) convertView.findViewById(R.id.imgChevron);
        TextView text = (TextView) convertView.findViewById(R.id.tvName);
        TextView subText = (TextView) convertView.findViewById(R.id.tvSubText);

        ImageManager.with(getContext())
                .putSmallDeviceImage((DeviceModel) item.getData())
                .withTransformForUgcImages(new CropCircleTransformation())
                .withPlaceholder(R.drawable.icon_cat_placeholder)
                .withError(R.drawable.icon_cat_placeholder)
                .into(detailImageView)
                .execute();

        chevronImageView.setImageResource(item.isChecked() ? R.drawable.icon_checkmark : R.drawable.chevron);
        text.setText(item.getText());
        subText.setText(item.getSubText());

        return convertView;
    }

}
