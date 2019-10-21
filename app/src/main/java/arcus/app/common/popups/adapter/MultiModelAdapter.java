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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.google.common.base.Strings;
import arcus.cornea.model.PlaceAndRoleModel;
import com.iris.capability.util.Addresses;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.model.SceneModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;

import java.util.List;

public class MultiModelAdapter extends ArrayAdapter<ListItemModel> {
    private boolean allowMultipleSelections = true;
    private boolean hideCheckShowChevron = false;
    private int itemLastSelected = -1;
    private int total = -1;
    private boolean isLightScheme = false;
    private int layoutId = R.layout.floating_device_picker_item;

    public MultiModelAdapter(Context context, List<ListItemModel> objects) {
        super(context, 0, objects);
        total = objects.size() - 1;
    }

    public MultiModelAdapter(Context context, List<ListItemModel> objects, boolean allowMultiple) {
        this(context, objects);
        allowMultipleSelections = allowMultiple;
    }

    public MultiModelAdapter(Context context, List<ListItemModel> objects, boolean isLightColorScheme, boolean hideCheckShowChevy) {
        this(context, objects, false);
        isLightScheme = isLightColorScheme;
        hideCheckShowChevron = hideCheckShowChevy;
    }

    public MultiModelAdapter(Context context, List<ListItemModel> objects, int layoutId) {
        super(context, 0, objects);
        total = objects.size() - 1;
        this.layoutId = layoutId;
    }

    public MultiModelAdapter(Context context, List<ListItemModel> objects, boolean allowMultiple, int layoutId) {
        this(context, objects);
        allowMultipleSelections = allowMultiple;
        this.layoutId = layoutId;
    }

    public MultiModelAdapter(Context context, List<ListItemModel> objects, boolean isLightColorScheme, boolean hideCheckShowChevy, int layoutId) {
        this(context, objects, false);
        isLightScheme = isLightColorScheme;
        hideCheckShowChevron = hideCheckShowChevy;
        this.layoutId = layoutId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layoutId, parent, false);
        }

        ListItemModel currentModel = getItem(position);
        Object corneaModel = currentModel.getData();

        ImageView checkBox = (ImageView) convertView.findViewById(R.id.action_checkbox);
        ImageView chevron  = (ImageView) convertView.findViewById(R.id.image_chevron);
        View divider = convertView.findViewById(R.id.divider);
        if (!hideCheckShowChevron) {
            checkBox.setVisibility(View.VISIBLE);
            chevron.setVisibility(View.GONE);
            divider.setVisibility(View.GONE);
            if (currentModel.isChecked()) {
                itemLastSelected = position;
                checkBox.setImageResource(R.drawable.circle_check_black_filled);
            }
            else {
                checkBox.setImageResource(R.drawable.circle_hollow_black);
            }
        }
        else {
            checkBox.setVisibility(View.GONE);
            chevron.setVisibility(View.VISIBLE);
            divider.setVisibility((position == total) ? View.GONE : View.VISIBLE);
        }

        Version1TextView name = (Version1TextView) convertView.findViewById(R.id.list_item_name);
        if (name != null && !Strings.isNullOrEmpty(currentModel.getText())) {
            name.setText(currentModel.getText());
            name.text100Opacity(isLightScheme);
        }
        Version1TextView device = (Version1TextView) convertView.findViewById(R.id.list_item_description);
        if (device != null && !Strings.isNullOrEmpty(currentModel.getSubText())) {
            device.setText(currentModel.getSubText());
            device.text60Opacity(isLightScheme);
            device.setVisibility(View.VISIBLE);
        } else {
            device.setVisibility(View.GONE);
        }
        ImageView image = (ImageView) convertView.findViewById(R.id.device_image);

        // Model is a device; draw the device image
        if (corneaModel != null && corneaModel instanceof DeviceModel) {
            ImageManager.with(getContext())
                  .putSmallDeviceImage((DeviceModel) corneaModel)
                  .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                  .withTransformForUgcImages(new CropCircleTransformation())
                  .into(image)
                  .execute();
        }

        // Model is a person; draw the person image
        else if (corneaModel != null && corneaModel instanceof PersonModel) {
            ImageManager.with(getContext())
                    .putPersonImage(((PersonModel) corneaModel).getId())
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                  .withTransform(new CropCircleTransformation())
                    .into(image)
                    .execute();
        }

        // Model is a place; draw the place image
        else if (corneaModel != null && corneaModel instanceof PlaceModel) {
            ImageManager.with(getContext())
                    .putPlaceImage(((PlaceModel) corneaModel).getId())
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                    .into(image)
                    .execute();
        }

        // Model is a place; special case (place image has to be specially sized)
        else if (currentModel.isPlaceModel()) {

            image.getLayoutParams().width = image.getLayoutParams().height * 2;
            image.requestLayout();

            ImageManager.with(getContext())
                  .putPlaceImage(Addresses.getId(currentModel.getAddress()))
                  .into(image)
                  .execute();
        }

        // Model is a Scene; draw the scene category icon
        else if (corneaModel != null && corneaModel instanceof SceneModel) {
            SceneCategory category = SceneCategory.fromSceneModel((SceneModel) corneaModel);
            ImageManager.with(getContext())
                    .putSceneCategoryImage(category)
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                    .into(image)
                    .execute();
        }

        else if (corneaModel == null && currentModel.getImageResId() != null) {
            ImageManager.with(getContext())
                  .putDrawableResource(currentModel.getImageResId())
                  .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                  .into(image)
                  .execute();
        }

        // Don't know how to draw an icon for this model type; use placeholder (empty circle) instead
        else {
            ImageManager.with(getContext())
                    .putDrawableResource(R.drawable.icon_cat_placeholder)
                    .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                    .into(image)
                    .execute();
        }

        Version1TextView subDescription1 = (Version1TextView) convertView.findViewById(R.id.list_item_sub_description1);
        if (corneaModel instanceof PlaceAndRoleModel) {
            if (name != null) { // To avoid side effects, doing this only for places.
                name.setMaxLines(1);
                name.setAllCaps(true);
                name.setEllipsize(TextUtils.TruncateAt.END);
                name.setText(currentModel.getText()); // w/o resetting the text the ellipsize wasn't correct
            }
            subDescription1.setVisibility(View.VISIBLE);

            PlaceAndRoleModel place = (PlaceAndRoleModel) corneaModel;
            String city = TextUtils.isEmpty(place.getCity()) ? "" : place.getCity();
            String state = TextUtils.isEmpty(place.getState()) ? "" : ", " + place.getState();
            String zip = TextUtils.isEmpty(place.getZipCode()) ? "" : " " + place.getZipCode();
            String location = String.format("%s%s%s", city, state, zip);
            subDescription1.setText(location);
            subDescription1.text60Opacity(true);
        }
        else {
            subDescription1.setVisibility(View.GONE);
        }
        return convertView;
    }

    public boolean selectItem(int position) {
        if (!allowMultipleSelections && itemLastSelected != -1) {
            getItem(itemLastSelected).setChecked(false);
        }

        itemLastSelected = position;
        ListItemModel item = getItem(position);
        item.setChecked(!item.isChecked());
        notifyDataSetChanged();

        return item.isChecked();
    }
}
