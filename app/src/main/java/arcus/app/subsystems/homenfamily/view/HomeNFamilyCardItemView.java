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
package arcus.app.subsystems.homenfamily.view;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import arcus.cornea.subsystem.presence.model.PresenceState;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.PicListItemModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.homenfamily.cards.HomeNFamilyCard;

import java.util.List;
import java.util.Locale;


public class HomeNFamilyCardItemView extends DashboardFlipViewHolder {

    ImageView serviceImage;
    Version1TextView serviceName;
    Context context;
    View cardView;

    public HomeNFamilyCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        context = view.getContext();

        cardView = view.findViewById(R.id.cardView);
    }

    public void build(@NonNull HomeNFamilyCard card) {
        serviceName.setText(context.getString(R.string.card_home_and_family_title));
        serviceImage.setImageResource(R.drawable.dashboard_homefamily);

        List<PicListItemModel> itemModels = card.getItems();
        hideAllItems(cardView);

        if (itemModels.size() == 0) {
            // TODO: What to display when nothing to display?
        }

        int plusCount = 0;
        int personCount = 0;
        for (int index = 0; index < itemModels.size(); index++) {
            PicListItemModel item = itemModels.get(index);
            if(PresenceState.HOME.equals(item.getPresenceState())) {
                if(item.hasAssignedPerson()) {
                    if(personCount < 2) {
                        showItem(cardView, index, itemModels.get(index));
                        personCount++;
                    }
                    else {
                        plusCount++;
                    }
                }
                else {
                    plusCount++;
                }
            }
        }
        if(plusCount > 0) {
            if(personCount == 0) {
                showPlusItem(cardView, plusCount, false);
            } else {
                showPlusItem(cardView, plusCount, true);
            }
        }
    }

    private void showPlusItem (View cardView, int plusCount, boolean showPlus) {
        TextView plusText = (TextView) cardView.findViewById(R.id.plus_text);
        plusText.setVisibility(View.VISIBLE);
        if(!showPlus) {
            plusText.setText(String.format(Locale.getDefault(), "%d", plusCount));
        } else {
            plusText.setText(StringUtils.getPrefixSuperscriptSpan("+", Integer.toString(plusCount)));
        }
    }

    private void hidePlusItem (View cardView) {
        TextView plusText = (TextView) cardView.findViewById(R.id.plus_text);
        plusText.setVisibility(View.GONE);
    }

    private void showItem (View cardView, int itemIndex, PicListItemModel itemModel) {

        ItemViewHolder holder = getViewForItem(cardView, itemIndex);
        holder.itemRegion.setVisibility(View.VISIBLE);

        if (itemModel.hasAssignedPerson()) {
            ImageManager.with(context)
                    .putPersonImage(itemModel.getPersonId())
                    .withTransform(new CropCircleTransformation())
                    .into(holder.itemImage)
                    .execute();
        } else {
            ImageManager.with(context)
                    .putSmallDeviceImage(itemModel.getDeviceModel())
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .into(holder.itemImage)
                    .execute();
        }
    }

    private void hideAllItems (View cardView) {
        for (int index = 0; index < 2; index++) {
            hideItem(cardView, index);
            hidePlusItem(cardView);
        }
    }

    private void hideItem (View cardView, int itemIndex) {
        getViewForItem(cardView, itemIndex).itemRegion.setVisibility(View.GONE);
    }

    private ItemViewHolder getViewForItem (View cardView, int item) {
        ItemViewHolder holder = new ItemViewHolder();

        switch (item) {
            case 0:
                holder.itemRegion = (LinearLayout) cardView.findViewById(R.id.item_1);
                holder.itemImage = (ImageView) cardView.findViewById(R.id.item_image_1);
                break;
            case 1:
                holder.itemRegion = (LinearLayout) cardView.findViewById(R.id.item_2);
                holder.itemImage = (ImageView) cardView.findViewById(R.id.item_image_2);
                break;
        }

        return holder;
    }

    private static class ItemViewHolder {
        public LinearLayout itemRegion;
        public ImageView itemImage;
        public TextView itemText;
        public TextView itemSubtext;
    }

}
