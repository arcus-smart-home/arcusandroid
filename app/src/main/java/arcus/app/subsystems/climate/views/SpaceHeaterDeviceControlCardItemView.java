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
package arcus.app.subsystems.climate.views;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.iris.client.capability.DeviceOta;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.view.BaseCardItemView;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.climate.cards.SpaceHeaterDeviceControlCard;


public class SpaceHeaterDeviceControlCardItemView extends BaseCardItemView<SpaceHeaterDeviceControlCard> implements View.OnClickListener {
    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;
    private SpaceHeaterDeviceControlCard.OnClickListener mListener;
    private SpaceHeaterDeviceControlCard card;
    DeviceModel model;
    View banner;

    GlowableImageView topButton;
    ImageView leftButton;
    ImageView rightButton;
    Version1TextView descText;

    public SpaceHeaterDeviceControlCardItemView(Context context) {
        super(context);
    }

    public SpaceHeaterDeviceControlCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpaceHeaterDeviceControlCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void build(@NonNull SpaceHeaterDeviceControlCard card) {
        super.build(card);
        this.card = card;

        showControls();
        CardView cardView = (CardView) findViewById(R.id.cardView);
        if(cardView == null) {
            return;
        }
        cardView.setOnClickListener(this);

        View divider = findViewById(R.id.divider);
        if (card.isOffline() || card.isInOta()) {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
            divider.setBackgroundColor(getContext().getResources().getColor(R.color.black_with_10));
        } else {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
            divider.setBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
        }

        Version1TextView titleText = (Version1TextView) findViewById(R.id.card_device_control_title_text);
        descText = (Version1TextView) findViewById(R.id.card_device_control_description_text);

        leftButton = (ImageView) findViewById(R.id.card_device_control_left_btn);
        rightButton = (ImageView) findViewById(R.id.card_device_control_right_btn);
        topButton = (GlowableImageView) findViewById(R.id.card_device_control_device_image);
        topButton.setGlowMode(GlowableImageView.GlowMode.OFF);
        topButton.setGlowing(false);

        Version1TextView bottomImageText = (Version1TextView) findViewById(R.id.card_device_control_bottom_image_text);
        FrameLayout bottomContainer = (FrameLayout) findViewById(R.id.card_device_control_bottom_container);

        if(card.getLeftImageResource() == 0){
            leftButton.setVisibility(INVISIBLE);
        }else{
            leftButton.setImageResource(card.getLeftImageResource());
            leftButton.setVisibility(VISIBLE);
        }

        if(card.getRightImageResource() == 0){
            rightButton.setVisibility(INVISIBLE);
        }else{
            rightButton.setImageResource(card.getRightImageResource());
            rightButton.setVisibility(VISIBLE);
        }

        if(card.getDeviceId() !=null){
            model = SessionModelManager.instance().getDeviceWithId(card.getDeviceId(), false);
            topButton.setVisibility(VISIBLE);
            if(model!=null) {
                ImageManager.with(getContext())
                        .putSmallDeviceImage(model)
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .noUserGeneratedImagery()
                        .into(topButton)
                        .execute();
            }else{
                topButton.setVisibility(VISIBLE);
                topButton.setImageResource(R.drawable.device_list_placeholder);
            }
        }

        bottomImageText.setBackgroundResource(card.getBottomImageResource());
        bottomImageText.setText(card.getBottomImageText());

        leftButton.setEnabled(card.isLeftButtonEnabled());
        leftButton.setAlpha(card.isLeftButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        rightButton.setEnabled(card.isRightButtonEnabled());
        rightButton.setAlpha(card.isRightButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        topButton.setEnabled(card.isTopButtonEnabled());
        topButton.setAlpha(card.isTopButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        bottomContainer.setEnabled(card.isBottomButtonEnabled());
        bottomContainer.setAlpha(card.isBottomButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        titleText.setText(card.getTitle());

        updateDescription();

        mListener = card.getCallbackListener();

        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        topButton.setOnClickListener(this);
        bottomContainer.setOnClickListener(this);

        if (card.isDividerShown()) {
            showDivider();
        }

        banner = findViewById(R.id.banner);
        boolean isBannerVisible = false;

        if (card.isOffline()) {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.cardview_dark_background));
            topButton.setAlpha(BUTTON_DISABLED_ALPHA);
            divider.setBackgroundColor(getContext().getResources().getColor(R.color.black_with_10));
            String deviceName = "";
            if(model!=null) {
                deviceName = model.getName();
            }
            isBannerVisible = true;
            String bannerText = card.isOffline() ? getResources().getString(R.string.device_no_connection) : card.getDescription();
            showBanner(getResources().getColor(R.color.pink_banner), deviceName, bannerText, getResources().getColor(R.color.white));
        }
        else if (card.isInOta()) {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
            divider.setBackgroundColor(getContext().getResources().getColor(R.color.black_with_10));
            String deviceName = "";
            if(model!=null) {
                deviceName = model.getName();
            }
            isBannerVisible = true;
            showBanner(getResources().getColor(R.color.white), deviceName, card.getDescription(), getResources().getColor(R.color.black));
        }
        else {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
            topButton.setAlpha(BUTTON_ENABLED_ALPHA);
            divider.setBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
        }

        if(model!=null) {
            DeviceOta deviceOta = CorneaUtils.getCapability(model, DeviceOta.class);
            if (deviceOta != null && DeviceOta.STATUS_INPROGRESS.equals(deviceOta.getStatus())) {
                isBannerVisible = true;
                showBanner(getResources().getColor(R.color.white), model.getName(), getResources().getString(R.string.ota_firmware_update), getResources().getColor(R.color.black));
            }
        }

        if(!isBannerVisible) {
            hideBanner();
        }
    }

    private void updateDescription() {
        String description  = "-";
        if(card.isDeviceModeOn() && !card.isDeviceEcoOn()) {
            description = String.format(getContext().getString(R.string.spaceheater_on_eco_off), card.getCurrentTemp(), card.getSetPoint());
        }
        else if(card.isDeviceModeOn() && card.isDeviceEcoOn()) {
            description = String.format(getContext().getString(R.string.spaceheater_on_eco_on), card.getCurrentTemp());
        }
        else if(!card.isDeviceModeOn() && card.isDeviceEcoOn()) {
            description = getContext().getString(R.string.spaceheater_off_eco_on);
        }
        descText.setText(description);
    }

    private void showBanner(int backgroundColor, String title, String description, int textColor) {
        banner.setVisibility(View.VISIBLE);
        banner.setBackgroundColor(backgroundColor);
        Version1TextView deviceName = (Version1TextView) findViewById(R.id.device_name);
        deviceName.setText(title);
        deviceName.setTextColor(textColor);

        Version1TextView subTitle = (Version1TextView) findViewById(R.id.device_status);
        subTitle.setText(description);
        subTitle.setTextColor(textColor);
        hideControls();
    }

    private void hideBanner() {
        banner.setVisibility(View.GONE);
    }

    private void showControls() {
        findViewById(R.id.card_device_control_left_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_right_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_title_text).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_description_text).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_bottom_container).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_chevron).setVisibility(View.VISIBLE);
    }
    private void hideControls() {
        findViewById(R.id.card_device_control_left_btn).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_right_btn).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_title_text).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_description_text).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_bottom_container).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_chevron).setVisibility(View.GONE);
    }

    protected void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }

    public void onClick(@NonNull View v) {
        if (mListener == null) return;

        final int id = v.getId();
        switch (id) {
            case R.id.card_device_control_device_image:
                mListener.onTopButtonClicked();
                break;
            case R.id.card_device_control_left_btn:
                mListener.onLeftButtonClicked();
                break;
            case R.id.card_device_control_right_btn:
                mListener.onRightButtonClicked();
                break;
            case R.id.card_device_control_bottom_container:
                mListener.onBottomButtonClicked();
                break;
            default:
                mListener.onCardClicked();
                break;
        }
    }
}
