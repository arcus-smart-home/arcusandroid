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
package arcus.app.subsystems.scenes.editor.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.capability.Device;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.subsystems.scenes.editor.model.SceneListItemModel;

import java.util.List;

public class SceneDefaultSelectorAdapter extends ArrayAdapter<SceneListItemModel> {
    private boolean isLightScheme;
    private boolean isCurrentlyEditing = false;

    public SceneDefaultSelectorAdapter(Context context, List<SceneListItemModel> objects) {
        this(context, objects, false);
    }

    public SceneDefaultSelectorAdapter(Context context, List<SceneListItemModel> objects, boolean isLightColorScheme) {
        super(context, 0, objects);
        this.isLightScheme = isLightColorScheme;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_scene_action_list_default_item, parent, false);
        }

        SceneListItemModel itemModel = getItem(position);

        ImageView editImage = (ImageView) convertView.findViewById(R.id.edit_image);
        if (isCurrentlyEditing) {
            editImage.setVisibility(View.VISIBLE);
        }
        else {
            editImage.setVisibility(View.GONE);
        }

        ImageView leftImage = (ImageView) convertView.findViewById(R.id.left_image);
        Model model = CorneaClientFactory.getModelCache().get(itemModel.getAddressAssociatedTo());
        if (model != null && model.getCaps().contains(Device.NAMESPACE)) {
            DeviceModel deviceModel = (DeviceModel) model;
            ImageManager.with(getContext())
                    .putSmallDeviceImage(deviceModel)
                    .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE), isLightScheme)
                    .into(leftImage)
                    .execute();
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(itemModel.getTitle());
        if (isLightScheme) {
            title.setTextColor(Color.WHITE);
        }

        TextView rightText = (TextView) convertView.findViewById(R.id.right_text);
        ImageView chevron = (ImageView) convertView.findViewById(R.id.chevron);
        if (!isCurrentlyEditing && !Strings.isNullOrEmpty(itemModel.getRightText())) {
            rightText.setVisibility(View.VISIBLE);
            rightText.setText(itemModel.getRightText());
            if (isLightScheme) {
                rightText.setTextColor(Color.WHITE);
            }

            chevron.setVisibility(View.VISIBLE);
            if (isLightScheme) {
                chevron.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.chevron_white));
            }
        }
        else {
            rightText.setVisibility(View.GONE);
            chevron.setVisibility(View.GONE);
        }

        if (!isCurrentlyEditing && itemModel.getOnClickListener() != null) {
            convertView.setOnClickListener(itemModel.getOnClickListener());
        }

        if (isCurrentlyEditing) {
            convertView.setClickable(false);
        }

        return convertView;
    }

    public void isEditMode(boolean isEditMode) {
        this.isCurrentlyEditing = isEditMode;
        notifyDataSetChanged();
    }

    public void remove(int position) {
        super.remove(getItem(position));
        notifyDataSetChanged();
    }

    public void remove(SceneListItemModel model) {
        super.remove(model);
        notifyDataSetChanged();
    }

}
