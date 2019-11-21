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
package arcus.app.subsystems.lawnandgarden.views;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.text.TextUtils;
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
import arcus.app.subsystems.lawnandgarden.cards.IrrigationDeviceControlCard;



public class IrrigationDeviceControlCardItemView extends BaseCardItemView<IrrigationDeviceControlCard> implements View.OnClickListener {
    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;
    private IrrigationDeviceControlCard.OnClickListener mListener;
    DeviceModel model;
    View banner;

    ImageView leftButton;
    ImageView rightButton;

    private String deviceId;

    public IrrigationDeviceControlCardItemView(Context context) {
        super(context);
    }

    public IrrigationDeviceControlCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IrrigationDeviceControlCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    public void build(@NonNull IrrigationDeviceControlCard card) {
        super.build(card);
        if(card.getDeviceId() !=null){
            deviceId = card.getDeviceId();
        }

        showControls();
        CardView cardView = (CardView) findViewById(R.id.cardView);
        View divider = findViewById(R.id.divider);
        if (cardView != null) {
            if (card.isOffline() || card.isInAutoMode() || card.isInOta()) {
                cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.black_with_10));
            } else {
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
                divider.setBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
            }
        }

        Version1TextView titleText = (Version1TextView) findViewById(R.id.card_device_control_title_text);
        Version1TextView scheduleMode = (Version1TextView) findViewById(R.id.card_device_control_schedule_mode_text);

        leftButton = (ImageView) findViewById(R.id.card_device_control_left_btn);
        rightButton = (ImageView) findViewById(R.id.card_device_control_right_btn);
        GlowableImageView topButton = (GlowableImageView) findViewById(R.id.card_device_control_device_image);
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
        }

        leftButton.setEnabled(card.isLeftButtonEnabled());
        leftButton.setAlpha(card.isLeftButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        rightButton.setEnabled(card.isRightButtonEnabled());
        rightButton.setAlpha(card.isRightButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        topButton.setEnabled(card.isTopButtonEnabled());
        topButton.setAlpha(card.isTopButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        bottomContainer.setEnabled(card.isBottomButtonEnabled());
        bottomContainer.setAlpha(card.isBottomButtonEnabled() ? BUTTON_ENABLED_ALPHA : BUTTON_DISABLED_ALPHA);

        titleText.setText(card.getTitle());
        scheduleMode.setText(card.getScheduleMode());

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

        if(TextUtils.isEmpty(card.getScheduleLocation())) {
            findViewById(R.id.card_device_control_schedule_container).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.card_device_control_schedule_container).setVisibility(View.VISIBLE);
            Version1TextView now = (Version1TextView) findViewById(R.id.card_device_control_schedule_current_event);
            now.setText(card.getScheduleLocation());
        }
        if(TextUtils.isEmpty(card.getNextScheduleLocation()) && TextUtils.isEmpty(card.getNextScheduleTime())) {
            findViewById(R.id.card_device_control_schedule_container_next).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.card_device_control_schedule_container_next).setVisibility(View.VISIBLE);
            Version1TextView next = (Version1TextView) findViewById(R.id.card_device_control_schedule_next_event);
            if(TextUtils.isEmpty(card.getScheduleLocation())) {
                next.setText(card.getNextScheduleTime());
            }
            else {
                next.setText(card.getNextScheduleLocation());
            }
            Version1TextView nextTitle = (Version1TextView) findViewById(R.id.card_device_control_schedule_next_event_title);
            nextTitle.setText(card.getNextScheduleTitle());
        }

        banner = findViewById(R.id.banner);
        boolean isBannerVisible = false;
        if (card.isOffline() || card.isInAutoMode()) {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
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
        findViewById(R.id.card_device_control_schedule_mode_text).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_bottom_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_chevron).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_schedule_container_next).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_schedule_container).setVisibility(View.VISIBLE);
    }
    private void hideControls() {
        findViewById(R.id.card_device_control_left_btn).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_right_btn).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_title_text).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_schedule_mode_text).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_bottom_btn).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_chevron).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_schedule_container_next).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_schedule_container).setVisibility(View.GONE);
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
