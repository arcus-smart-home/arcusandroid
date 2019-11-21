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
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.iris.client.bean.ActionTemplate;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.utils.ImageUtils;

import java.util.List;

public class SceneActionListAdapter extends ArrayAdapter<ListItemModel> {
    private boolean isLightColorScheme;
    private Invert colorInvert;
    
    public SceneActionListAdapter(Context context, List<ListItemModel> objects, boolean isLightColorScheme) {
        super(context, 0, objects);
        this.isLightColorScheme = isLightColorScheme;
        if (this.isLightColorScheme) {
            colorInvert = Invert.BLACK_TO_WHITE;
        }
        else {
            colorInvert = Invert.WHITE_TO_BLACK;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemModel item = getItem(position);

        if (item.isHeadingRow()) {
            return getViewForHeading(item, parent);
        } else {
            return getViewForAction(item, parent);
        }
    }

    private View getViewForAction(@NonNull ListItemModel actionData, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.category_item, parent, false);

        ImageView categoryImageView;
        ImageView chevron;
        TextView text;
        TextView subText;
        TextView deviceCountText;

        categoryImageView = (ImageView) view.findViewById(R.id.imgCategory);
        chevron = (ImageView) view.findViewById(R.id.imgChevron);

        text = (TextView) view.findViewById(R.id.tvText);
        subText = (TextView) view.findViewById(R.id.tvSubText);
        subText.setVisibility(View.GONE);

        ImageManager.with(getContext())
              .putDrawableResource(R.drawable.chevron)
              .withTransform(new BlackWhiteInvertTransformation(colorInvert))
              .into(chevron)
              .execute();

        ActionTemplate template = new ActionTemplate();
        template.setTypehint(actionData.getAddress());
        ImageManager.with(getContext())
              .putSceneActionTemplateImage(template)
              .withTransform(new BlackWhiteInvertTransformation(colorInvert))
              .withPlaceholder(R.drawable.icon_cat_placeholder)
              .into(categoryImageView)
              .execute();

        text.setText(actionData.getText());
        if (isLightColorScheme) {
            text.setTextColor(Color.WHITE);
        }

        if (Strings.isNullOrEmpty(actionData.getSubText())) {
            subText.setVisibility(View.GONE);
            try {
                RelativeLayout.LayoutParams relativeLayout = (RelativeLayout.LayoutParams) text.getLayoutParams();
                relativeLayout.addRule(RelativeLayout.CENTER_VERTICAL);
                text.setLayoutParams(relativeLayout);
            }
            catch (Exception ex) {
                // Rather a funny looking UI than a crashed one :)
            }
        }
        else {
            subText.setText(actionData.getSubText());
            if (isLightColorScheme) {
                subText.setTextColor(Color.WHITE);
            }
        }

        deviceCountText = (TextView) view.findViewById(R.id.device_count_text);
        if (deviceCountText != null && actionData.getCount() > 0) {
            deviceCountText.setVisibility(View.VISIBLE);
            deviceCountText.setText(String.format("%d", actionData.getCount()));
            if (isLightColorScheme) {
                deviceCountText.setTextColor(Color.WHITE);
            }
        }
        view.setMinimumHeight(ImageUtils.dpToPx(getContext(), 65));

        return view;
    }

    private View getViewForHeading(@NonNull ListItemModel headingData, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.heading_item, parent, false);

        TextView headingLabel = (TextView) view.findViewById(R.id.heading_text);
        headingLabel.setText(headingData.getText());
        if (isLightColorScheme) {
            headingLabel.setTextColor(Color.WHITE);
        }

        TextView headingCountText = (TextView) view.findViewById(R.id.count_heading_text);
        if (headingCountText != null) {
            headingCountText.setVisibility(View.GONE); // Change.

            // String.format() (or "" + getCount()) Else it'll get parsed as a string resource. setText(int)
            // headingCountText.setText(String.format("%d", headingData.getCount()));
        }

        // Heading views are never clickable
        view.setEnabled(false);
        view.setOnClickListener(null);

        return view;
    }
}
