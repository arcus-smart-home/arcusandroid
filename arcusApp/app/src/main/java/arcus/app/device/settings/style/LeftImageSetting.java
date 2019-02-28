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
package arcus.app.device.settings.style;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.device.settings.core.AbstractSetting;
import arcus.app.device.settings.core.Setting;

/**
 * A setting that renders a cell containing a title, description, selection abstract (optional) and
 * chevron (>). Clicking the setting invokes an OnClickHandler allowing for custom behavior.
 */
public class LeftImageSetting extends AbstractSetting implements Setting {

    private final View.OnClickListener listener;
    private Drawable image;
    private DeviceModel model;
    private boolean hideChevron;

    public LeftImageSetting(String title, String description, Drawable image, View.OnClickListener listener) {
        super(title, description, R.layout.setting_left_image);
        this.listener = listener;
        this.image = image;
    }

    public LeftImageSetting(String title, String description, String initialSelectionAbstract, Drawable image, View.OnClickListener listener) {
        super(title, description, initialSelectionAbstract, R.layout.setting_left_image);
        this.listener = listener;
        this.image = image;
    }

    public LeftImageSetting(String title, String description, String initialSelectionAbstract, Drawable image, boolean hideChevron, View.OnClickListener listener) {
        super(title, description, initialSelectionAbstract, R.layout.setting_left_image);
        this.listener = listener;
        this.image = image;
        this.hideChevron = hideChevron;
    }

    public LeftImageSetting(String title, String description, String initialSelectionAbstract, DeviceModel model, boolean hideChevron, View.OnClickListener listener) {
        super(title, description, initialSelectionAbstract, R.layout.setting_left_image);
        this.listener = listener;
        this.model = model;
        this.hideChevron = hideChevron;
    }

    @Override
    public View getView(Context context, ViewGroup root) {

        View settingView = LayoutInflater.from(context).inflate(getLayoutId(), root, false);

        TextView title = (TextView) settingView.findViewById(R.id.setting_title);
        TextView description = (TextView) settingView.findViewById(R.id.setting_description);
        TextView selectionAbstract = (TextView) settingView.findViewById(R.id.selection_abstract);
        ImageView chevron = (ImageView) settingView.findViewById(R.id.setting_parent_chevron);
        ImageView imageView = (ImageView) settingView.findViewById(R.id.setting_image);
        View divider = settingView.findViewById(R.id.divider);

        divider.setBackgroundColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_10) : context.getResources().getColor(R.color.black_with_10));

        if(hideChevron){
            chevron.setVisibility(View.GONE);
        }
        title.setText(getTitle());

        // TODO: A bug in some versions of Android results in the last line of dynamically-set text being cutoff; the newline is a suggested workaround.
        description.setText(getDescription());

        selectionAbstract.setVisibility(hasSelectionAbstract() ? View.VISIBLE : View.GONE);
        selectionAbstract.setText(getSelectionAbstract());

        settingView.setOnClickListener(listener);

        if (this.image != null) {
            imageView.setImageDrawable(this.image);
        }

        if (this.model != null) {
            ImageManager.with(context)
                    .putSmallDeviceImage(this.model)
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .withTransform(new CropCircleTransformation())
                    .fit().centerCrop()
                    .into(imageView)
                    .execute();
        }

        return settingView;
    }

}
