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
package arcus.app.seasonal.christmas.fragments.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import arcus.app.R;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.seasonal.christmas.model.SantaListItemModel;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

public class SantaListAdapter extends ArrayAdapter<SantaListItemModel> {
    private boolean allowMultipleSelections = true;
    private boolean showChecks = false;
    private SantaListItemModel checkedItem;

    public SantaListAdapter(Context context, List<SantaListItemModel> objects) {
        this(context, objects, false, false);
    }

    public SantaListAdapter(Context context, List<SantaListItemModel> objects, boolean allowMultiple) {
        this(context, objects, allowMultiple, true);
    }

    public SantaListAdapter(Context context, List<SantaListItemModel> objects, boolean allowMultiple, boolean showChecks) {
        super(context, 0, objects);
        this.allowMultipleSelections = allowMultiple;
        this.showChecks = showChecks;

        // Setup default selections if enabled.
        if (!allowMultipleSelections) {
            for (SantaListItemModel object : objects) {
                if (object.isCurrentlyChecked()) {
                    checkedItem = object;
                    break;
                }
            }
        }
    }

    @Override @Nullable
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.santa_fragment_floating_picker_list_item, parent, false);
        }

        SantaListItemModel item = getItem(position);
        ImageView checkBox = (ImageView) convertView.findViewById(R.id.action_checkbox);
        if (checkBox != null) {
            if (showChecks) {
                checkBox.setVisibility(View.VISIBLE);
                if (allowMultipleSelections) {
                    checkItem(checkBox, item.isCurrentlyChecked());
                }
                else {
                    checkItem(checkBox, item.equals(checkedItem));
                }
            }
            else {
                checkBox.setVisibility(View.GONE);
            }
        }

        ImageView deviceImage = (ImageView) convertView.findViewById(R.id.device_image);
        if (deviceImage != null) {
            RequestCreator requestCreator = null;
            if (item.getImageResource() != -1) {
                requestCreator = Picasso.with(getContext()).load(item.getImageResource());
            }
            else if (!Strings.isNullOrEmpty(item.getImageURL())) {
                requestCreator = Picasso.with(getContext()).load(item.getImageURL());
            }
            else {
                deviceImage.setVisibility(View.GONE);
            }

            if (requestCreator != null) {
                requestCreator
                      .transform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                      .transform(new CropCircleTransformation())
                      .fit()
                      .centerCrop()
                      .into(deviceImage);
            }
        }

        TextView topText = (TextView) convertView.findViewById(R.id.list_item_name);
        if (topText != null) {
            topText.setTextColor(getContext().getResources().getColor(R.color.overlay_white_with_100));
            topText.setVisibility(View.VISIBLE);
            topText.setText(item.getName());
        }

        TextView bottomText = (TextView) convertView.findViewById(R.id.list_item_description);
        if (bottomText != null) {
            if (!Strings.isNullOrEmpty(item.getDescription())) {
                bottomText.setTextColor(getContext().getResources().getColor(R.color.overlay_white_with_60));
                bottomText.setVisibility(View.VISIBLE);
                bottomText.setText(item.getDescription());
            }
            else {
                bottomText.setVisibility(View.GONE);
            }
        }

        ImageView chevron = (ImageView) convertView.findViewById(R.id.santa_chevron);
        if (chevron != null) {
            chevron.setVisibility(item.isWithChevron() ? View.VISIBLE : View.GONE);
        }

        return (convertView);
    }

    private void checkItem(ImageView checkBox, boolean isChecked) {
        if (!showChecks) {
            return;
        }

        if (isChecked) {
            checkBox.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_check_white_filled));
        }
        else {
            checkBox.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.circle_hollow_white));
        }
    }

    public boolean toggleCheck(int position) {
        if (!showChecks) {
            return false;
        }

        getItem(position).setCurrentlyChecked(!getItem(position).isCurrentlyChecked());
        checkedItem = getItem(position);
        notifyDataSetChanged();

        return checkedItem.isCurrentlyChecked();
    }
}
