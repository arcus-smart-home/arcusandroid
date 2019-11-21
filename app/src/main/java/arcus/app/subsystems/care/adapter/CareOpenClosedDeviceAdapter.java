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
package arcus.app.subsystems.care.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.ImageRequestBuilder;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.view.Version1TextView;

import java.lang.ref.WeakReference;
import java.util.List;

public class CareOpenClosedDeviceAdapter extends ArrayAdapter<ListItemModel> {
    ImageView checkBox;
    ImageView deviceImage;
    Version1TextView name;
    Version1TextView desc;
    Version1TextView abstext;
    ImageView chevron;
    View divider;
    private boolean isEditing;
    private int black60;
    private int white35;
    private int black20;
    private int white20;
    private WeakReference<Callback> callback;

    public interface Callback {
        void checkBoxAreaClicked(ListItemModel listItemModel);
        void numericPickerAreaClicked(ListItemModel listItemModel);
    }

    public CareOpenClosedDeviceAdapter(Context context, @NonNull List<ListItemModel> items, boolean isEditMode) {
        super(context, 0);
        addAll(items);
        isEditing = isEditMode;
        white35 = context.getResources().getColor(R.color.white_with_35);
        black60 = context.getResources().getColor(R.color.black_with_60);
        black20 = context.getResources().getColor(R.color.black_with_20);
        white20 = context.getResources().getColor(R.color.overlay_white_with_20);
    }

    public CareOpenClosedDeviceAdapter(Context context, int resource) {
        super(context, resource);
    }

    public CareOpenClosedDeviceAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public CareOpenClosedDeviceAdapter(Context context, int resource, ListItemModel[] objects) {
        super(context, resource, objects);
    }

    public CareOpenClosedDeviceAdapter(Context context, int resource, int textViewResourceId, ListItemModel[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public CareOpenClosedDeviceAdapter(Context context, int resource, List<ListItemModel> objects) {
        super(context, resource, objects);
    }

    public CareOpenClosedDeviceAdapter(Context context, int resource, int textViewResourceId, List<ListItemModel> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public void setCallback(Callback cb) {
        callback = new WeakReference<>(cb);
    }

    protected Callback getCallback() {
        if (callback != null) {
            return callback.get();
        }

        return null;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.care_open_closed_device_item, parent, false);
        }

        initViews(convertView);
        adjustColorScheme(position);

        ListItemModel item = getItem(position);
        name.setText(item.getText());
        desc.setText(item.getSubText());
        if (item.getCount() > 0) {
            abstext.setText(String.format("%d", item.getCount()));
        }

        if (item.getData() instanceof DeviceModel) {
            BlackWhiteInvertTransformation transform;
            if (isEditing) {
                transform = new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE);
            }
            else {
                transform = new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK);
            }

            ImageRequestBuilder builder = ImageManager.with(getContext())
                  .putSmallDeviceImage((DeviceModel) item.getData());

            if (builder != null) {
                builder
                      .withTransformForStockImages(transform)
                      .withTransform(new CropCircleTransformation())
                      .withPlaceholder(isEditing ? R.drawable.circle_hollow_white : R.drawable.circle_hollow_black)
                      .into(deviceImage)
                      .execute();
            }
        }

        final int clickPosition = position;
        convertView.findViewById(R.id.checkbox_and_image_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Callback callback = getCallback();
                if (callback != null) {
                    callback.checkBoxAreaClicked(getItem(clickPosition));
                }
            }
        });
        View.OnClickListener clickRightListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Callback callback = getCallback();
                if (callback != null) {
                    callback.numericPickerAreaClicked(getItem(clickPosition));
                }
            }
        };
        convertView.findViewById(R.id.text_container).setOnClickListener(clickRightListener);
        convertView.findViewById(R.id.chevron_and_abstract_container).setOnClickListener(clickRightListener);

        return convertView;
    }

    private void initViews(View convertView) {
        checkBox = (ImageView) convertView.findViewById(R.id.action_checkbox);
        deviceImage = (ImageView) convertView.findViewById(R.id.device_image);
        name = (Version1TextView) convertView.findViewById(R.id.list_item_name);
        desc = (Version1TextView) convertView.findViewById(R.id.list_item_description);
        abstext = (Version1TextView) convertView.findViewById(R.id.abstract_text);
        chevron = (ImageView) convertView.findViewById(R.id.image_chevron);
        divider = convertView.findViewById(R.id.item_divider);
    }

    protected void adjustColorScheme(int position) {
        chevron.setImageResource(isEditing ? R.drawable.chevron_white : R.drawable.chevron);
        name.setTextColor(isEditing ? Color.WHITE : Color.BLACK);
        desc.setTextColor(isEditing ? white35 : black60);
        abstext.setTextColor(isEditing ? white35 : black60);

        divider.setBackgroundColor(isEditing ? white20 : black20);
        try {
            getItem(position + 1);
            divider.setVisibility(View.VISIBLE);
        }
        catch (Exception ex) {
            divider.setVisibility(View.INVISIBLE);
        }

        if (getItem(position).isChecked()) {
            if (isEditing) {
                checkBox.setImageResource(R.drawable.circle_check_white_filled);
            }
            else {
                checkBox.setImageResource(R.drawable.circle_check_black_filled);
            }
        }
        else {
            if (isEditing) {
                checkBox.setImageResource(R.drawable.circle_hollow_white);
            }
            else {
                checkBox.setImageResource(R.drawable.circle_hollow_black);
            }
        }
    }
}
