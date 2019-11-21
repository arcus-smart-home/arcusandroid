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
package arcus.app.subsystems.lawnandgarden.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.CorneaClientFactory;
import com.iris.client.capability.Device;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;

import java.util.List;

public class IrrigationZoneListAdapter extends ArrayAdapter<ListItemModel> {
    private boolean useEditModeColorScheme;
    private Drawable chevronImage;
    private int white60;
    private int black60;
    private int white20;
    private int black20;

    public IrrigationZoneListAdapter(@NonNull Context context, @NonNull List<ListItemModel> models) {
        this(context, models, true);
    }

    public IrrigationZoneListAdapter(
          @NonNull Context context,
          @NonNull List<ListItemModel> models,
          boolean editModeColorScheme
    ) {
        super(context, 0);
        addAll(models);
        useEditModeColorScheme = editModeColorScheme;

        white60 = context.getResources().getColor(R.color.overlay_white_with_60);
        black60 = context.getResources().getColor(R.color.black_with_60);

        white20 = context.getResources().getColor(R.color.overlay_white_with_20);
        black20 = context.getResources().getColor(R.color.black_with_20);

        int chevronImageRes = useEditModeColorScheme ? R.drawable.chevron_white : R.drawable.chevron;
        chevronImage = ContextCompat.getDrawable(getContext(), chevronImageRes);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ListItemModel item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.lng_zone_device_item, parent, false);
        }
        View itemView = convertView.findViewById(R.id.normal_item_container);
        View headingView = convertView.findViewById(R.id.heading_item_continer);

        if (item.isHeadingRow()) {
            itemView.setVisibility(View.GONE);
            headingView.setVisibility(View.VISIBLE);

            return getViewForHeading(item, convertView);
        }
        else {
            itemView.setVisibility(View.VISIBLE);
            headingView.setVisibility(View.GONE);

            boolean nextRowIsHeader = false;
            boolean isLastRow = false;
            try {
                nextRowIsHeader = getItem(position + 1).isHeadingRow();
            }
            catch (Exception ignored) {
                isLastRow = true;
            }
            return getViewForNonHeader(item, convertView, nextRowIsHeader, isLastRow);
        }
    }

    private View getViewForNonHeader(@NonNull ListItemModel item, View view, boolean nextRowIsHeader, boolean isLastRow) {
        TextView text = (TextView) view.findViewById(R.id.list_item_name);
        TextView subText = (TextView) view.findViewById(R.id.list_item_description);
        TextView abstractText = (TextView) view.findViewById(R.id.abstract_text);
        ImageView imageView = (ImageView) view.findViewById(R.id.device_image);
        ImageView chevron = (ImageView) view.findViewById(R.id.image_chevron);

        text.setText(item.getText());
        if (TextUtils.isEmpty(item.getSubText())) {
            subText.setVisibility(View.GONE);
        }
        else {
            subText.setText(item.getSubText());
        }

        int count = item.getCount();
        abstractText.setText(getContext().getResources().getQuantityString(R.plurals.care_minutes_plural, count, count));

        Invert inversion = useEditModeColorScheme ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK;
        Model model = CorneaClientFactory.getModelCache().get(item.getAddress());
        if (model != null && model.getCaps().contains(Device.NAMESPACE)) {
            ImageManager.with(getContext())
                  .putSmallDeviceImage((DeviceModel) model)
                  .noUserGeneratedImagery()
                  .withTransform(new BlackWhiteInvertTransformation(inversion))
                  .withPlaceholder(R.drawable.device_list_placeholder)
                  .into(imageView)
                  .execute();
        }
        else {
            int noImage = useEditModeColorScheme ? R.drawable.device_list_placeholder : R.drawable.icon_cat_placeholder;
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), noImage));
        }

        View bottomDivider = view.findViewById(R.id.item_divider);
        bottomDivider.setVisibility((nextRowIsHeader || isLastRow) ? View.GONE : View.VISIBLE);

        if (useEditModeColorScheme) {
            text.setTextColor(Color.WHITE);
            subText.setTextColor(white60);
            abstractText.setTextColor(white60);
            bottomDivider.setBackgroundColor(white20);
        }
        else {
            text.setTextColor(Color.BLACK);
            subText.setTextColor(black60);
            abstractText.setTextColor(black60);
            bottomDivider.setBackgroundColor(black20);
        }
        chevron.setImageDrawable(chevronImage);

        return view;
    }

    // No variant for edit mode headers - at least here.
    private View getViewForHeading(@NonNull ListItemModel headingData, View view) {
        TextView headingLeft = (TextView) view.findViewById(R.id.left_text_view);
        TextView headingRight = (TextView) view.findViewById(R.id.right_text_view);
        headingLeft.setText(headingData.getText());
        headingRight.setText(headingData.getSubText());

        view.setEnabled(false);
        view.setOnClickListener(null);

        return view;
    }

}
