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
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.model.DeviceType;

import java.util.List;


public class DeviceControlCardItemView extends BaseCardItemView<DeviceControlCard> implements View.OnClickListener {

    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;
    private DeviceControlCard.OnClickListener mListener;

    private String deviceId;

    public DeviceControlCardItemView(Context context) {
        super(context);
    }

    public DeviceControlCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeviceControlCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(@NonNull DeviceControlCard card) {
        super.build(card);
        if(card.getDeviceId() !=null){
            deviceId = card.getDeviceId();
        }

        CardView cardView = (CardView) findViewById(R.id.cardView);
        View divider = findViewById(R.id.divider);
        if (cardView != null) {
            if (card.isOffline()) {
                cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.black_with_10));
            } else {
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
                hideOfflineBanner();
            }
        }

        Version1TextView titleText = (Version1TextView) findViewById(R.id.card_device_control_title_text);
        Version1TextView descText = (Version1TextView) findViewById(R.id.card_device_control_description_text);

        ImageView leftButton = (ImageView) findViewById(R.id.card_device_control_left_btn);
        ImageView rightButton = (ImageView) findViewById(R.id.card_device_control_right_btn);
        GlowableImageView topButton = (GlowableImageView) findViewById(R.id.card_device_control_device_image);
        ImageView bottomButton = (ImageView) findViewById(R.id.card_device_control_bottom_btn);
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

        //todo: have to get device id from card in order to use ImageManager here,
        // need to figure out a better way to do this
        if(card.getDeviceId() !=null && !card.getUseSpecifiedTopImage()){
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

        if(card.getBottomImageResource() == 0){
            bottomContainer.setVisibility(GONE);
        }else {
            bottomContainer.setVisibility(VISIBLE);
            bottomImageText.setBackgroundResource(card.getBottomImageResource());
            bottomImageText.setText(card.getBottomImageText());
        }

        leftButton.setVisibility(card.isLeftButtonVisible() ? View.VISIBLE : View.INVISIBLE);
        leftButton.setEnabled(card.isLeftButtonEnabled());
        leftButton.setAlpha(card.isLeftButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        rightButton.setVisibility(card.isRightButtonVisible() ? View.VISIBLE : View.INVISIBLE);
        rightButton.setEnabled(card.isRightButtonEnabled());
        rightButton.setAlpha(card.isRightButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        topButton.setEnabled(card.isTopButtonEnabled());
        topButton.setAlpha(card.isTopButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        bottomContainer.setEnabled(card.isBottomButtonEnabled());
        bottomContainer.setAlpha(card.isBottomButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        titleText.setText(card.getTitle());
        descText.setText(card.getDescription());

        if (card.isOffline() && !card.isHoneywellTcc()) {
            topButton.setAlpha(BUTTON_DISABLED_ALPHA);
            showDeviceOfflineBanner(card);

        }  else if (!card.isCloudDevice() && card.getErrors().size() > 0) {
            List<String> errors = card.getErrors();
            if(errors.size() == 1){
                //show single error banner
                showErrorsBanner(card, errors.get(0));
            } else {
                //show multi-error banner
                showErrorsBanner(card, getResources().getString(R.string.water_heater_multiple_errors));
            }
        }

        if (!card.isOffline() && card.getErrors() != null && card.getErrors().size() > 0) {
            // Show first error in list
            showErrorsBanner(card, getErrorMessageForErrorCode(card.getErrors().get(0)));

            // Remove control buttons and disable mode button if error is present
            leftButton.setVisibility(GONE);
            rightButton.setVisibility(GONE);
            bottomContainer.setEnabled(false);
            bottomContainer.setAlpha(BUTTON_DISABLED_ALPHA);
        }

        else {
            if (card.isIsEventInProcess()) {
                showEventInFlight();
            } else {
                hideEventInFlight();
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
            }
        }

        mListener = card.getCallbackListener();

        leftButton.setOnClickListener(this);
        rightButton.setOnClickListener(this);
        topButton.setOnClickListener(this);
        bottomContainer.setOnClickListener(this);

        if (cardView != null) {
            cardView.setOnClickListener(this);
        }

        if (card.isDividerShown()) {
            showDivider();
        }
    }

    private void showErrorsBanner(DeviceControlCard card, String error){
        RelativeLayout banner = (RelativeLayout) findViewById(R.id.device_offline_banner);
        RelativeLayout background = (RelativeLayout) findViewById(R.id.offline_background);
        Version1TextView deviceName = (Version1TextView) findViewById(R.id.device_name_offline);
        Version1TextView errorMessage = (Version1TextView) findViewById(R.id.device_error_description);
        ImageView cloudIcon = (ImageView) findViewById(R.id.cloud_icon);

        setBackgroundColor(getResources().getColor(R.color.black_with_35));

        errorMessage.setTypeface(null, Typeface.ITALIC);
        errorMessage.setText(error);
        cloudIcon.setVisibility(card.isCloudDevice() ? VISIBLE : GONE);
        banner.setVisibility(VISIBLE);
        if(DeviceType.LOCK.equals(card.getDeviceType())) {
            deviceName.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
            errorMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.black_with_60));
            background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.error_yellow));
        } else {
            deviceName.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            errorMessage.setTextColor(ContextCompat.getColor(getContext(), R.color.white_with_60));
            background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.pink_banner));
        }
        deviceName.setText(card.getTitle());
        hideControls();
    }

    private void hideOfflineBanner() {
        View ipcdBanner = findViewById(R.id.card_device_bottom_part);
        View banner = findViewById(R.id.device_offline_banner);

        banner.setVisibility(View.GONE);
        ipcdBanner.setVisibility(View.GONE);

        setBackgroundColor(Color.TRANSPARENT);
        showControls();
    }

    private void showDeviceOfflineBanner (DeviceControlCard card) {
        RelativeLayout banner = (RelativeLayout) findViewById(R.id.device_offline_banner);
        Version1TextView deviceName = (Version1TextView) findViewById(R.id.device_name_offline);
        ImageView cloudIcon = (ImageView) findViewById(R.id.cloud_icon);

        cloudIcon.setVisibility(card.isCloudDevice() ? VISIBLE : GONE);
        banner.setVisibility(VISIBLE);
        deviceName.setText(card.getTitle());

        setBackgroundColor(getContext().getResources().getColor(R.color.cardview_dark_background));
        hideControls();
    }

    private void showCloudOfflineBanner(DeviceControlCard card) {
        View banner = findViewById(R.id.card_device_bottom_part);
        banner.setVisibility(View.VISIBLE);
        banner.setBackgroundColor(getResources().getColor(R.color.pink_banner));

        View waiting = findViewById(R.id.waiting_on_label_device_list);
        if(card.isIsEventInProcess()) {
            waiting.setVisibility(View.VISIBLE);
        }
        else {
            waiting.setVisibility(View.GONE);
        }

        if(card.getDeviceId() !=null) {
            final DeviceModel model = SessionModelManager.instance().getDeviceWithId(card.getDeviceId(), false);
            if (model != null) {
                ImageView icon = (ImageView) findViewById(R.id.product_icon);
                String vendorName = String.valueOf(model.getVendor()).toUpperCase();
                ImageManager.with(getContext())
                        .putBrandImage(vendorName)
                        .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .into(icon)
                        .execute();
            }
        }

        RelativeLayout offlineBanner = (RelativeLayout) findViewById(R.id.device_offline_banner);
        offlineBanner.setVisibility(INVISIBLE);

        setBackgroundColor(getContext().getResources().getColor(R.color.cardview_dark_background));
        hideControls();
    }

    private void showEventInFlight() {
        View banner = findViewById(R.id.card_device_bottom_part);
        banner.setVisibility(View.VISIBLE);
        banner.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));

        View waiting = findViewById(R.id.waiting_on_label_device_list);
        waiting.setVisibility(View.VISIBLE);

        if(deviceId !=null) {
            final DeviceModel model = SessionModelManager.instance().getDeviceWithId(deviceId, false);
            if (model != null) {
                ImageView icon = (ImageView) findViewById(R.id.product_icon);
                String vendorName = String.valueOf(model.getVendor()).toUpperCase();
                ImageManager.with(getContext())
                        .putBrandImage(vendorName)
                        .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .into(icon)
                        .execute();
            }
        }
    }

    private void hideEventInFlight() {
        View banner = findViewById(R.id.card_device_bottom_part);
        banner.setVisibility(View.GONE);
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

    private String getErrorMessageForErrorCode(String errorCode) {
        // this used to return custom error stuff for Nest
        return errorCode;
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
}
