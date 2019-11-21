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
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.model.CareHistoryModel;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;

import java.util.List;

public class CareHistoryListAdapter extends ArrayAdapter<CareHistoryModel> {
    public CareHistoryListAdapter(Context context, List<CareHistoryModel> models) {
        super(context, 0);

        addAll(models);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        CareHistoryModel item = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.subsystem_history_list_item, parent, false);
        }

        View layoutView = convertView.findViewById(R.id.item_layout);
        View headerView = convertView.findViewById(R.id.heading_layout);

        if (item.isHeaderRow()) {
            layoutView.setVisibility(View.GONE);
            headerView.setVisibility(View.VISIBLE);
            return getViewForHeading(item, convertView);
        }
        else {
            boolean nextRowIsHeader = false;
            boolean isLastRow = false;
            try {
                nextRowIsHeader = getItem(position + 1).isHeaderRow();
            }
            catch (Exception ignored) {
                isLastRow = true;
            }
            layoutView.setVisibility(View.VISIBLE);
            headerView.setVisibility(View.GONE);
            return getViewForNonHeader(item, convertView, nextRowIsHeader, isLastRow);
        }
    }

    private View getViewForNonHeader(@NonNull CareHistoryModel item, View view, boolean nextRowIsHeader, boolean isLastRow) {
        TextView time = (TextView) view.findViewById(R.id.history_time);
        TextView text = (TextView) view.findViewById(R.id.history_title);
        TextView subText = (TextView) view.findViewById(R.id.history_sub_title);
        ImageView imageView = (ImageView) view.findViewById(R.id.history_icon);

        time.setText(item.getDate());
        text.setText(item.getTitle());
        subText.setText(item.getSubTitle());

        Model model = CorneaClientFactory.getModelCache().get(item.getAddress());
        if (model != null) {
            if (model instanceof DeviceModel) {
                ImageManager.with(getContext())
                      .putSmallDeviceImage((DeviceModel) model)
                      .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                      .withTransform(new CropCircleTransformation())
                      .into(imageView)
                      .execute();
            }
            else if (model instanceof PersonModel) {
                ImageManager.with(getContext())
                      .putPersonImage(item.getAddress())
                      .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                      .into(imageView)
                      .execute();
            }
            else {
                imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icon_service_care_small));
            }
        }
        else {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icon_service_care_small));
        }

        View bottomDivider = view.findViewById(R.id.bottom_divider);
        bottomDivider.setVisibility((nextRowIsHeader || isLastRow) ? View.GONE : View.VISIBLE);

        return view;
    }

    private View getViewForHeading(@NonNull CareHistoryModel headingData, View view) {
        TextView headingLabel = (TextView) view.findViewById(R.id.heading_text);
        headingLabel.setText(headingData.getTitle());
        headingLabel.setTextColor(Color.WHITE);

        view.setEnabled(false);
        view.setOnClickListener(null);

        return view;
    }
}
