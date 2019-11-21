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
package arcus.app.common.adapters;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.settings.core.Abstractable;
import arcus.app.device.settings.core.Iconized;
import arcus.app.device.settings.core.Localizable;

import java.util.ArrayList;
import java.util.List;


public class EnumAdapter<T extends Localizable> extends ArrayAdapter<ListItemModel> {

    private boolean lightColorScheme = false;
    private boolean showChevrons = false;
    private boolean forceAllCaps = false;
    private T[] enumArray;

    private Version1TextView abstractText;

    public EnumAdapter(@NonNull Context context, @NonNull Class<T> enumeration) {
        this(context, enumeration.getEnumConstants());
    }

    public EnumAdapter(@NonNull Context context, @NonNull T[] enumConstants) {
        super(context, 0);

        this.enumArray = enumConstants;
        List<ListItemModel> items = new ArrayList<>();

        for (T thisValue : enumArray) {

            String enumStringValue = context.getString(thisValue.getStringResId());

            ListItemModel thisItem = new ListItemModel(enumStringValue);
            thisItem.setData(thisValue);

            if (thisValue instanceof Iconized) {
                thisItem.setImageResId(((Iconized) thisValue).getImageResId());
            }

            if (thisValue instanceof Abstractable) {
                thisItem.setAbstractText(((Abstractable)thisValue).getAbstract(context));
            }

            items.add(thisItem);
        }

        super.addAll(items);
    }

    public void setLightColorScheme(boolean lightColorScheme) {
        this.lightColorScheme = lightColorScheme;
    }

    public void setShowChevrons(boolean showChevrons) {
        this.showChevrons = showChevrons;
    }

    public boolean isLightColorScheme() {
        return lightColorScheme;
    }

    public boolean isShowChevrons() {
        return showChevrons;
    }

    public boolean isForceAllCaps() {
        return forceAllCaps;
    }

    public void setForceAllCaps(boolean forceAllCaps) {
        this.forceAllCaps = forceAllCaps;
    }

    @NonNull
    public T getEnumAt(int position) {
        return (T) getItem(position).getData();
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_checkable_item, parent, false);
        }

        Version1TextView title = (Version1TextView) convertView.findViewById(R.id.title);
        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        ImageView chevron = (ImageView) convertView.findViewById(R.id.chevron);
        abstractText = (Version1TextView) convertView.findViewById(R.id.button_abstract_text);

        ListItemModel thisItem = getItem(position);

        title.setText(isForceAllCaps() ? thisItem.getText().toUpperCase() : thisItem.getText());
        title.setTextColor(lightColorScheme ? Color.WHITE : Color.BLACK);

        chevron.setVisibility(showChevrons ? View.VISIBLE : View.GONE);
        chevron.setImageResource(lightColorScheme ? R.drawable.chevron_white : R.drawable.chevron);

        if (thisItem.getImageResId() != null) {
            Invert invert = isLightColorScheme() ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK;

            ImageManager.with(getContext())
                    .putDrawableResource(thisItem.getImageResId())
                    .withTransform(new BlackWhiteInvertTransformation(invert))
                    .withPlaceholder(R.drawable.device_list_placeholder)
                    .into(image)
                    .execute();
        } else {
            image.setVisibility(View.GONE);
        }

        if (StringUtils.isEmpty(thisItem.getAbstractText())) {
            abstractText.setVisibility(View.GONE);
        } else {
            abstractText.setVisibility(View.VISIBLE);
            abstractText.setText(thisItem.getAbstractText());
            abstractText.setTextColor(lightColorScheme ? Color.WHITE : Color.BLACK);
        }

        return convertView;
    }
}
