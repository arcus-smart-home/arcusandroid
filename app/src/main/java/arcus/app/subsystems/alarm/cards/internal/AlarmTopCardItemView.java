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
package arcus.app.subsystems.alarm.cards.internal;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.ToggleButton;

import arcus.cornea.SessionController;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.view.DashedCircleView;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.cards.AlarmTopCard;

import static android.graphics.Color.*;

/**
 *
 * THIS IS USED IN MULTIPLE AREAS
 * CHECK YOURSELF BEFORE YOU WRECK YOSELF
 *
 */
public class AlarmTopCardItemView extends RecyclerView.ViewHolder {

    private Version1TextView mCenterTopTextView;
    private Version1TextView mCenterBottomTextView;

    private ImageView mTopIconView;
    private View mTopLineView;
    private ImageView mLeftAlarmIcon;
    private Version1TextView mCenterAlarmText;
    private ImageView mRightAlarmIcon;
    private CardView cardView;
    private View careOnOffView;
    private Version1TextView placeName;
    private Version1TextView placeAddress;

    private DashedCircleView mDashedCircleView;

    private View placeLayout;
    private View topDivider;
    private Context context;
    private ToggleButton toggleButton;

    // Default constructors
    public AlarmTopCardItemView(View view) {
        super(view);
        context = view.getContext();
        mDashedCircleView = (DashedCircleView) view.findViewById(R.id.dashed_circle);

        mCenterTopTextView = (Version1TextView) view.findViewById(R.id.center_top_text);
        mCenterBottomTextView = (Version1TextView) view.findViewById(R.id.center_bottom_text);

        mTopIconView = (ImageView) view.findViewById(R.id.top_icon);
        mTopLineView = view.findViewById(R.id.top_line);
        mLeftAlarmIcon = (ImageView) view.findViewById(R.id.left_alarm_icon);
        mCenterAlarmText = (Version1TextView) view.findViewById(R.id.center_alarm_text);
        mRightAlarmIcon = (ImageView) view.findViewById(R.id.right_alarm_icon);
        placeName = (Version1TextView) view.findViewById(R.id.place_name);
        placeAddress = (Version1TextView) view.findViewById(R.id.place_address);
        cardView = (CardView) view.findViewById(R.id.cardView);
        placeLayout = view.findViewById(R.id.place_layout);
        topDivider = view.findViewById(R.id.topdivider);
        careOnOffView = view.findViewById(R.id.care_on_off_toggle_container);
        toggleButton = (ToggleButton) view.findViewById(R.id.care_on_off_toggle);
    }

    public void build(@NonNull AlarmTopCard card) {
        if(card.getAlarmState().equals(AlarmTopCard.AlarmState.ALERT)) {
            PlaceModel place = SessionController.instance().getPlace();
            if(place != null) {
                placeName.setText(place.getName());
                placeAddress.setText(place.getStreetAddress1());
            }
            placeLayout.setVisibility(View.VISIBLE);
            topDivider.setVisibility(View.VISIBLE);
        }
        else {
            placeLayout.setVisibility(View.GONE);
            topDivider.setVisibility(View.GONE);
        }


        cardView.setCardBackgroundColor(Color.TRANSPARENT);


        if (card.getShowCareOnOff()) {
            careOnOffView.setVisibility(View.VISIBLE);

            if (card.getCareToggleListener() != null) {
                toggleButton.setOnClickListener(card.getCareToggleListener());
            }
            toggleButton.setChecked(card.isToggleOn());
            toggleButton.setEnabled(card.isToggleEnabled());
        }
        else {
            careOnOffView.setVisibility(View.GONE);
        }

        if (card.getCenterBottomText() != null) {
            mCenterBottomTextView.setText(card.getCenterBottomText());
            mCenterBottomTextView.setVisibility(View.VISIBLE);
        }
        else {
            mCenterBottomTextView.setVisibility(View.GONE);
        }

        // Configure the card view based on the alarmstate
        handleAlarmState(card);
    }

    private void handleAlarmState(@NonNull AlarmTopCard card) {
        resetVisibility();

        switch (card.getAlarmState()) {
            case OFF:
                setStateOff(card);
                break;
            case ON:
                setStateOn(card);
                break;
            case PARTIAL:
                setStatePartial(card);
                break;
            case ARMING:
                setStateArming(card);
                break;
            case ALERT:
                setStateAlert(card);
                break;
            default:
                break;
        }
    }

    private void setStateOff(@NonNull AlarmTopCard card) {
        // Set Ring
        mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
        mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.OFF);
        cardView.setCardBackgroundColor(Color.TRANSPARENT);

        // Set Center Text
        if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
        if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());
        setFontWhite();

        mCenterTopTextView.setVisibility(View.VISIBLE);
        mCenterBottomTextView.setVisibility(View.VISIBLE);

        if(card.isCareCard()){
            mCenterTopTextView.setVisibility(View.VISIBLE);
            mCenterBottomTextView.setVisibility(View.VISIBLE);
            careOnOffView.setVisibility(View.VISIBLE);
            if(card.getTotalDevices() == 0){
                if(card.getShowCareOnOff()) mCenterTopTextView.setVisibility(View.VISIBLE);
                mCenterBottomTextView.setVisibility(View.GONE);
                mCenterTopTextView.setText("NO CARE\n BEHAVIORS\n ADDED");
                careOnOffView.setVisibility(View.GONE);
            }else{
                mCenterBottomTextView.setVisibility(View.VISIBLE);
                mCenterTopTextView.setVisibility(View.GONE);
            }
        }


    }

    private void setStateOn(@NonNull AlarmTopCard card) {
        // Set Ring
        mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
        mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.ON);

        if(card.isCareCard()) {
            mCenterTopTextView.setVisibility(View.VISIBLE);
            mCenterBottomTextView.setVisibility(View.VISIBLE);
            careOnOffView.setVisibility(View.VISIBLE);
        }

        // Set Center Text
        if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
        if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());
        setFontWhite();
    }

    private void setStatePartial(@NonNull AlarmTopCard card) {
        // Set Ring
        mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
        mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.PARTIAL);

        // Set Center Text
        if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
        if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());
        setFontWhite();
    }

    private void setStateArming(@NonNull AlarmTopCard card) {
        // Set Ring
        mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
        mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.ARMING);

        // Set Center Text
        if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
        if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());
        setFontWhite();
    }

    private void setStateAlert(@NonNull AlarmTopCard card) {
        // Set Ring
        mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
        mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.ALERT);
        if (AlarmTopCard.AlarmType.CARE.equals(card.getAlarmType())) {
            mDashedCircleView.setAlarmType(DashedCircleView.AlarmType.CARE);
        }

        // Show Center Icon
        mTopIconView.setVisibility(View.VISIBLE);

        if (card.getDeviceModel() != null) {
            ImageManager.with(context)
                    .putSmallDeviceImage(card.getDeviceModel())
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                    .withPlaceholder(R.drawable.device_list_placeholder)
                    .withError(R.drawable.device_list_placeholder)
                    .noUserGeneratedImagery()
                    .into(mTopIconView)
                    .execute();
        }
        else if (card.getDeviceImage() != null) {
            ImageManager.with(context)
                  .putDrawableResource(card.getDeviceImage())
                  .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                  .noUserGeneratedImagery()
                  .into(mTopIconView)
                  .execute();
        }
        else {
            Drawable icon = ContextCompat.getDrawable(context, R.drawable.icon_cat_securityalarm);
            mTopIconView.setImageDrawable(icon);

            //TODO: Icon should be pulled in from the service and set to black
        }

        // Show Center Bar
        mTopLineView.setVisibility(View.VISIBLE);

        // Show (( ALARM ))
        mCenterAlarmText.setText("ALARM");
        mCenterAlarmText.setVisibility(View.VISIBLE);
        mLeftAlarmIcon.setVisibility(View.VISIBLE);
        mRightAlarmIcon.setVisibility(View.VISIBLE);

        //hide center bottom text
        mCenterBottomTextView.setVisibility(View.GONE);

        // Set Center Text
        if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());

        //Change Color
        cardView.setCardBackgroundColor(Color.WHITE);
        setFontBlack();
        mTopLineView.setBackgroundColor(Color.rgb(204,204,204));
        mLeftAlarmIcon.setImageResource(R.drawable.icon_small_wavesleft_black);
        mRightAlarmIcon.setImageResource(R.drawable.icon_small_wavesright_black);

    }

    protected void setFontBlack() {
        mCenterTopTextView.setTextColor(BLACK);
        mCenterBottomTextView.setTextColor(BLACK);
        mCenterAlarmText.setTextColor(BLACK);
    }

    protected void setFontWhite() {
        mCenterTopTextView.setTextColor(WHITE);
        mCenterBottomTextView.setTextColor(WHITE);
        mCenterAlarmText.setTextColor(WHITE);
    }

    private void resetVisibility() {
        mCenterBottomTextView.setVisibility(View.VISIBLE);
        mTopIconView.setVisibility(View.GONE);
        mTopLineView.setVisibility(View.GONE);
        mLeftAlarmIcon.setVisibility(View.GONE);
        mRightAlarmIcon.setVisibility(View.GONE);
        mCenterAlarmText.setVisibility(View.GONE);
    }
}
