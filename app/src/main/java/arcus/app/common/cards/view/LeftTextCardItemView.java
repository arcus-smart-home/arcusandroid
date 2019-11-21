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
package arcus.app.common.cards.view;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.LeftTextCard;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.ImageRequestBuilder;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;


public class LeftTextCardItemView extends BaseCardItemView<LeftTextCard> {
    private ImageView imageView;

    public LeftTextCardItemView(Context context) {
        super(context);
    }

    public LeftTextCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeftTextCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void build(@NonNull LeftTextCard card) {
        super.build(card);

        TextView title = (TextView) this.findViewById(R.id.title);
        title.setText(card.getTitle());
        if (card.getTitleColor() != -1) {
            title.setTextColor(card.getTitleColor());
        }

        TextView description = (TextView) this.findViewById(R.id.description);
        if (card.getDescription() != null) {
            description.setVisibility(VISIBLE);
            description.setText(card.getDescription());
            if (card.getDescriptionColor() != -1) {
                description.setTextColor(card.getDescriptionColor());
            }
        }
        else {
            description.setVisibility(GONE);
        }

        TextView rightText = (TextView) this.findViewById(R.id.right_text);
        if (card.getRightText() == null) {
            rightText.setVisibility(GONE);
        }
        else {
            rightText.setText(card.getRightText());
        }

        imageView = (ImageView) this.findViewById(R.id.image);
        imageView.setVisibility(VISIBLE);
        switch (card.getImageDisplayType()) {
            case RESOURCE:
                setResourceImageToView(card);
                break;
            case DEVICE:
                setDeviceImageToView(card);
                break;
            case PERSON:
                setPersonImageToView(card);
                break;
            case PLACE:
                setPlaceImageToView(card);
                break;

            default:
                hideImageView();
                break;
        }

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (card.isChevronShown()) {
            showChevron();
        }

        if (card.isDividerShown()) {
            showDivider();
        }
    }

    private void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }

    private void showChevron() {
        ImageView chevron = (ImageView) findViewById(R.id.chevron);
        if (chevron != null) chevron.setVisibility(View.VISIBLE);
    }

    private void hideImageView() {
        imageView.setVisibility(View.GONE);
    }

    private void setResourceImageToView(@NonNull LeftTextCard card) {
        if (card.getDrawableResource() == -1) {
            hideImageView();
            return;
        }

        ImageRequestBuilder builder = ImageManager.with(getContext()).putDrawableResource(card.getDrawableResource());

        if (card.isDrawableInverted()) {
            builder.withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE));
        }

        builder
              .into(imageView)
              .execute();

        imageView.setVisibility(View.VISIBLE);
    }

    private void setDeviceImageToView(@NonNull LeftTextCard card) {
        if (card.getModelID() == null) {
            hideImageView();
            return;
        }

        final DeviceModel model = SessionModelManager.instance().getDeviceWithId(card.getModelID(), false);
        if (model == null) {
            hideImageView();
            return;
        }

        ImageManager
              .with(getContext())
              .putSmallDeviceImage(model)
              .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
              .withPlaceholder(R.drawable.device_list_placeholder)
              .withError(R.drawable.device_list_placeholder)
              .noUserGeneratedImagery()
              .into(imageView)
              .execute();
    }

    private void setPersonImageToView(@NonNull LeftTextCard card) {
        if (card.getModelID() == null) {
            hideImageView();
            return;
        }

        ImageManager
              .with(getContext())
              .putPersonImage(card.getModelID())
              .withTransformForUgcImages(new CropCircleTransformation())
              .into(imageView)
              .execute();
    }

    private void setPlaceImageToView(@NonNull LeftTextCard card) {
        if (card.getModelID() == null) {
            hideImageView();
            return;
        }

        ImageManager
              .with(getContext())
              .putPlaceImage(card.getModelID())
              .withTransformForUgcImages(new CropCircleTransformation())
              .into(imageView)
              .execute();
    }
}
