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
package arcus.app.common.popups.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;

import java.util.List;

public class PopupDeviceListAdapter extends PopupListAdapter<DeviceModel> {

    public PopupDeviceListAdapter(@NonNull Context context, @NonNull List<DeviceModel> devicesPaired, @NonNull String checkedItem) {
        super(context, devicesPaired, checkedItem);
    }

    @Override
    protected void setImage(final ImageView deviceImage) {
        ImageManager.with(getContext())
              .putSmallDeviceImage(getCurrentModel())
              .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
              .withTransformForUgcImages(new CropCircleTransformation())
              .withPlaceholder(R.drawable.device_list_placeholder)
              .into(deviceImage)
              .execute();
    }

    @Override
    protected void setTextName(@NonNull TextView textView) {
        textView.setText(getCurrentModel().getName());
    }

    @Override
    protected void setTextUnderName(@NonNull TextView textView) {
        textView.setText(getCurrentModel().getVendor());
    }
}
