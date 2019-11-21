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
import android.widget.RelativeLayout;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.PetControlCard;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;


public class DeviceControlCardItemPetView extends BaseCardItemView<PetControlCard> implements View.OnClickListener {

    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;
    private DeviceControlCard.OnClickListener mListener;

    public DeviceControlCardItemPetView(Context context) {
        super(context);
    }

    public DeviceControlCardItemPetView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeviceControlCardItemPetView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(@NonNull PetControlCard card) {
        super.build(card);

        CardView cardView = (CardView) findViewById(R.id.cardView);
        View divider = findViewById(R.id.divider);
        if (cardView != null) {
            if (card.isOffline()) {
                cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.black_with_10));
            } else {
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
            }
        }

        Version1TextView titleText = (Version1TextView) findViewById(R.id.card_device_control_title_text);
        Version1TextView modeButton = (Version1TextView) findViewById(R.id.mode_button);
        Version1TextView descText = (Version1TextView) findViewById(R.id.card_device_control_description_text);
        RelativeLayout modeButtonRegion = (RelativeLayout) findViewById(R.id.mode_button_clickable_region);

        ImageView chevronButton = (ImageView) findViewById(R.id.card_device_control_chevron);
        GlowableImageView topButton = (GlowableImageView) findViewById(R.id.card_device_control_device_image);

        if(card.getDeviceId() !=null){
            final DeviceModel model = SessionModelManager.instance().getDeviceWithId(card.getDeviceId(), false);
            if(model!=null) {
                topButton.setVisibility(VISIBLE);
                ImageManager.with(getContext())
                        .putSmallDeviceImage(model)
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .noUserGeneratedImagery()
                        .into(topButton)
                        .execute();
                if(card.getGlowMode() !=null){
                    topButton.setGlowMode(card.getGlowMode());
                    topButton.setGlowing(card.shouldGlow());
                }
            }else{
                topButton.setVisibility(VISIBLE);
                topButton.setImageResource(R.drawable.device_list_placeholder);
            }
        }else{
            if(card.getTopImageResource() == 0){
                topButton.setVisibility(INVISIBLE);
            }else{
                topButton.setVisibility(VISIBLE);
                topButton.setImageResource(card.getTopImageResource());

            }
        }

        topButton.setEnabled(card.isTopButtonEnabled());
        topButton.setAlpha(card.isTopButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        titleText.setText(card.getTitle());
        descText.setText(card.getDescription());

        modeButton.setText(card.getPetDoorStatus());

        mListener = card.getCallbackListener();

        cardView.setOnClickListener(this);
        modeButton.setOnClickListener(this);
        chevronButton.setOnClickListener(this);
        topButton.setOnClickListener(this);
        modeButtonRegion.setOnClickListener(this);

        if (card.isDividerShown()) {
            showDivider();
        }
    }

    protected void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (mListener == null) return;

        final int id = v.getId();
        switch (id) {
            case R.id.mode_button_clickable_region:
            case R.id.mode_button:
                mListener.onTopButtonClicked();
                break;
            default:
                mListener.onCardClicked();
                break;

        }
    }
}
