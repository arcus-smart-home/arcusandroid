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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.ListItemModel;

import java.util.ArrayList;


public class IconizedChevronListAdapter extends ArrayAdapter<ListItemModel> {

    private boolean useLightColorScheme = true;

    public IconizedChevronListAdapter(Context context) {
        super(context, 0);
    }

    public IconizedChevronListAdapter(Context context, ArrayList<ListItemModel> data) {
        super(context, 0);
        super.addAll(data);
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.listdata_item, parent, false);
        }

        ListItemModel mListData = getItem(position);

        ImageView chevronImage = (ImageView) convertView.findViewById(R.id.imageChevron);
        ImageView imageIcon = (ImageView) convertView.findViewById(R.id.imageIcon);
        TextView topText = (TextView) convertView.findViewById(R.id.tvTopText);
        TextView bottomText = (TextView) convertView.findViewById(R.id.tvBottomText);

        chevronImage.setImageResource(isUseLightColorScheme() ? R.drawable.chevron_white : R.drawable.chevron);
        topText.setTextColor(isUseLightColorScheme() ? Color.WHITE : Color.BLACK);
        bottomText.setTextColor(isUseLightColorScheme() ? getContext().getResources().getColor(R.color.white_with_35) : getContext().getResources().getColor(R.color.black_with_60));

        topText.setText(mListData.getText());

        if (mListData.getSubText() == null || mListData.getSubText().length() < 1) {
            bottomText.setVisibility(View.GONE);
        } else {
            bottomText.setVisibility(View.VISIBLE);
            bottomText.setText(mListData.getSubText());
        }

        if (imageIcon != null && mListData.getImageResId() != null) {
            imageIcon.setVisibility(View.VISIBLE);
            BlackWhiteInvertTransformation transformation = new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK);
            if (useLightColorScheme) {
                transformation = new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE);
            }
            ImageManager.with(getContext())
                    .putDrawableResource(mListData.getImageResId())
                    .withTransformForStockImages(transformation)
                    .into(imageIcon)
                    .execute();
        }

        return (convertView);
    }

    public boolean isUseLightColorScheme() {
        return useLightColorScheme;
    }

    public void setUseLightColorScheme(boolean useLightColorScheme) {
        this.useLightColorScheme = useLightColorScheme;
    }
}

