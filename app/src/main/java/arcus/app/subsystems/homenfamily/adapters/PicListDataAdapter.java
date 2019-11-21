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
package arcus.app.subsystems.homenfamily.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
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
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.PicListItemModel;
import arcus.app.common.utils.StringUtils;

import java.util.List;

public class PicListDataAdapter extends ArrayAdapter<PicListItemModel> {

    private boolean showSmallImage = false;

    public PicListDataAdapter(Context context, List<PicListItemModel> data, boolean showSmallImage) {
        super(context, 0, data);
        this.showSmallImage = showSmallImage;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.piclistdata_item, parent, false);
        }

        PicListItemModel data = getItem(position);
        View layoutView = convertView.findViewById(R.id.item_layout);
        View headerView = convertView.findViewById(R.id.heading_layout);
        View blurbView = convertView.findViewById(R.id.home_away_blurb);
        if (data.isHeaderRow()) {
            layoutView.setVisibility(View.GONE);
            blurbView.setVisibility(View.GONE);
            headerView.setVisibility(View.VISIBLE);
            return getViewForHeading(data, convertView);
        }

        if (data.isBlurb()) {
            layoutView.setVisibility(View.GONE);
            blurbView.setVisibility(View.VISIBLE);
            headerView.setVisibility(View.GONE);
            return getViewForBlurb(data.getBlurb(), convertView);
        }

        ImageView deviceImage = (ImageView) convertView.findViewById(R.id.imgPic);
        ImageView smallImg = (ImageView) convertView.findViewById(R.id.imgPicSmall);
        layoutView.setVisibility(View.VISIBLE);
        blurbView.setVisibility(View.GONE);
        headerView.setVisibility(View.GONE);

        if (showSmallImage) {  // More fragment
            smallImg.setVisibility(View.VISIBLE);
            // use device image for More Fragment
            ImageManager.with(getContext())
                    .putSmallDeviceImage(data.getDeviceModel())
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .withTransform(new CropCircleTransformation())
                    .fit()
                    .into(deviceImage)
                    .execute();
            if (!StringUtils.isEmpty(data.getPersonId())) {
                ImageManager.with(getContext())
                        .putPersonImage(data.getPersonId())
                        .withTransformForUgcImages(new CropCircleTransformation())
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .into(smallImg)
                        .execute();
            } else {
                ImageManager.with(getContext())
                        .putSmallDeviceImage(data.getDeviceModel())
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .into(smallImg)
                        .execute();
            }
        } else {  // Status fragment
            smallImg.setVisibility(View.GONE);
            // use person image for Status Fragment if available
            if (!StringUtils.isEmpty(data.getPersonId())) {
                ImageManager.with(getContext())
                        .putPersonImage(data.getPersonId())
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withTransform(new CropCircleTransformation())
                        .into(deviceImage)
                        .execute();
            } else {
                ImageManager.with(getContext())
                        .putSmallDeviceImage(data.getDeviceModel())
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withTransform(new CropCircleTransformation())
                        .into(deviceImage)
                        .execute();
            }
        }

        TextView topText = (TextView) convertView.findViewById(R.id.tvTopText);
        TextView bottomText = (TextView) convertView.findViewById(R.id.tvBottomText);

        if (StringUtils.isEmpty(data.getFirstName()) && StringUtils.isEmpty(data.getLastName())) {
            topText.setText(data.getDeviceName());
            bottomText.setText(R.string.homenfamily_unassigned);
        } else {
            if (showSmallImage) {  // More fragment
                topText.setText(data.getDeviceName());
                bottomText.setText(getContext().getString(R.string.homenfamily_assigned_to_person, data.getPersonName()));
            } else {  // Status fragment
                topText.setText(data.getPersonName());
                bottomText.setText(data.getRelationship());
            }
        }

        return (convertView);
    }

    private View getViewForBlurb(String daBlurb, View view) {
        TextView daBlurbTV = (TextView) view.findViewById(R.id.home_away_blurb_tv);
        daBlurbTV.setText(String.valueOf(daBlurb));

        view.setEnabled(false);

        return view;
    }

    private View getViewForHeading(@NonNull PicListItemModel headingData, View view) {
        TextView headingLabel = (TextView) view.findViewById(R.id.header_text);
        TextView headerNumber = (TextView) view.findViewById(R.id.header_number);
        headingLabel.setText(String.valueOf(headingData.getHeaderName()));
        headerNumber.setText(String.valueOf(headingData.getListItems()));

        view.setEnabled(false);

        return view;
    }
}
