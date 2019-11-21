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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.VentControlCard;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.ImageUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;


public class VentControlCardItemView extends DeviceControlCardItemView {

    private static final float BUTTON_ENABLED_ALPHA = 1.0f;
    private static final float BUTTON_DISABLED_ALPHA = 0.4f;
    private DeviceControlCard.OnClickListener mListener;
    private ImageView leftButton;
    private ImageView rightButton;

    public VentControlCardItemView(Context context) {
        super(context);
    }

    public VentControlCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VentControlCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(@NonNull DeviceControlCard card) {


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

        GlowableImageView topButton = (GlowableImageView) findViewById(R.id.card_device_control_device_image);

        leftButton = (ImageView) findViewById(R.id.card_device_control_left_btn);
        rightButton = (ImageView) findViewById(R.id.card_device_control_right_btn);

        topButton.setBevelVisible(card.isBevelVisible());
        leftButton.setVisibility(VISIBLE);
        rightButton.setVisibility(VISIBLE);
        leftButton.setImageResource(R.drawable.button_minus);
        rightButton.setImageResource(R.drawable.button_plus);

        DeviceSeekArc percentArc = (DeviceSeekArc) findViewById(R.id.percent_arc);

        if (card.getDeviceId() != null) {
            final DeviceModel model = SessionModelManager.instance().getDeviceWithId(card.getDeviceId(), false);
            if (model != null) {

                int nLevel = getVentLevel(model);
                percentArc.setVisibility(View.VISIBLE);
                percentArc.setSelectedResource(R.drawable.seek_arc_control_selector_small, null);
                percentArc.setProgress(DeviceSeekArc.THUMB_LOW, nLevel);
                percentArc.setDrawThumbsWhenDisabled(true);
                percentArc.setEnabled(false);
                percentArc.setArcWidth(ImageUtils.dpToPx(getContext(), 5));
            }
        }

        topButton.setGlowing(false);


        Version1TextView titleText = (Version1TextView) findViewById(R.id.card_device_control_title_text);
        Version1TextView descText = (Version1TextView) findViewById(R.id.card_device_control_description_text);


        Version1TextView bottomImageText = (Version1TextView) findViewById(R.id.card_device_control_bottom_image_text);
        FrameLayout bottomContainer = (FrameLayout) findViewById(R.id.card_device_control_bottom_container);


        if (card.getDeviceId() != null) {
            final DeviceModel model = SessionModelManager.instance().getDeviceWithId(card.getDeviceId(), false);
            if (model != null) {
                topButton.setVisibility(VISIBLE);
                ImageManager.with(getContext())
                        .putSmallDeviceImage(model)
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .noUserGeneratedImagery()
                        .into(topButton)
                        .execute();
                if (card.getGlowMode() != null && !(card instanceof VentControlCard)) {
                    topButton.setGlowMode(card.getGlowMode());
                    topButton.setGlowing(card.shouldGlow());
                }
            } else {
                topButton.setVisibility(VISIBLE);
                topButton.setImageResource(R.drawable.device_list_placeholder);
            }
        } else {
            if (card.getTopImageResource() == 0) {
                topButton.setVisibility(INVISIBLE);
            } else {
                topButton.setVisibility(VISIBLE);
                topButton.setImageResource(card.getTopImageResource());

            }
        }

        if (card.getBottomImageResource() == 0) {
            bottomContainer.setVisibility(GONE);
        } else {
            bottomContainer.setVisibility(VISIBLE);
            bottomImageText.setBackgroundResource(card.getBottomImageResource());
            bottomImageText.setText(card.getBottomImageText());
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
        descText.setText(card.getDescription());

        mListener = card.getCallbackListener();

        if (card.isOffline()) {
            topButton.setAlpha(BUTTON_DISABLED_ALPHA);
            showDeviceOfflineBanner(card);
        }
        else {
            topButton.setAlpha(BUTTON_ENABLED_ALPHA);
            hideOfflineBanner();
        }

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

    public int getVentLevel(DeviceModel model) {
        Object object = model.get("vent:level");
        if (object == null) {
            return 0;
        }

        if (object instanceof Double) {
            double dub = (Double) object;
            return (int) dub;
        } else if (object instanceof Integer) {

            return (int) object;
        } else {
            return 0;
        }
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

    private void hideOfflineBanner() {
        View banner = findViewById(R.id.device_offline_banner);
        banner.setVisibility(View.GONE);
        setBackgroundColor(Color.TRANSPARENT);
        showControls();
    }

    private void showDeviceOfflineBanner (DeviceControlCard card) {
        RelativeLayout banner = (RelativeLayout) findViewById(R.id.device_offline_banner);
        Version1TextView deviceName = (Version1TextView) findViewById(R.id.device_name_offline);

        banner.setVisibility(VISIBLE);
        deviceName.setText(card.getTitle());

        setBackgroundColor(getContext().getResources().getColor(R.color.cardview_dark_background));
        hideControls();
    }

    private void showControls() {
        findViewById(R.id.card_device_control_left_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_right_btn).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_title_text).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_description_text).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_bottom_container).setVisibility(View.VISIBLE);
        findViewById(R.id.card_device_control_chevron).setVisibility(View.VISIBLE);
        findViewById(R.id.percent_arc).setVisibility(View.VISIBLE);
    }
    private void hideControls() {
        findViewById(R.id.card_device_control_left_btn).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_right_btn).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_title_text).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_description_text).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_bottom_container).setVisibility(View.GONE);
        findViewById(R.id.card_device_control_chevron).setVisibility(View.GONE);
        findViewById(R.id.percent_arc).setVisibility(View.GONE);
    }
}
