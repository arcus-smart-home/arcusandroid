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
package arcus.app.subsystems.care.cards;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.cards.view.BaseCardItemView;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.view.DashedCircleView;
import arcus.app.common.view.Version1TextView;


public class CareTopCardBehaviorView extends BaseCardItemView<CareStatusCard> {

    //TODO: Point away from alarm and to care

        private Version1TextView mCenterTopTextView;
        private Version1TextView mCenterBottomTextView;

        private ImageView mTopIconView;
        private View mTopLineView;
        private ImageView mLeftAlarmIcon;
        private Version1TextView mCenterAlarmText;
        private ImageView mRightAlarmIcon;

        private DashedCircleView mDashedCircleView;

        // Default constructors
        public CareTopCardBehaviorView(Context context) {
            super(context);
        }

        public CareTopCardBehaviorView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CareTopCardBehaviorView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
        }

        @Override
        public void build(@NonNull CareStatusCard card) {
            super.build(card);

            mDashedCircleView = (DashedCircleView) findViewById(R.id.dashed_circle);

            mCenterTopTextView = (Version1TextView) findViewById(R.id.center_top_text);
            mCenterBottomTextView = (Version1TextView) findViewById(R.id.center_bottom_text);

            mTopIconView = (ImageView) findViewById(R.id.top_icon);
            mTopLineView = findViewById(R.id.top_line);
            mLeftAlarmIcon = (ImageView) findViewById(R.id.left_alarm_icon);
            mCenterAlarmText = (Version1TextView) findViewById(R.id.center_alarm_text);
            mRightAlarmIcon = (ImageView) findViewById(R.id.right_alarm_icon);

            CardView cardView = (CardView) findViewById(R.id.cardView);
            if (cardView != null) {
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
            }

            if (card.isDividerShown()) {
                showDivider();
            }

            // Configure the card view based on the alarmstate
            handleAlarmState(card);
        }

        private void showDivider() {
            View divider = findViewById(R.id.divider);
            if (divider != null) divider.setVisibility(View.VISIBLE);
        }

        private void handleAlarmState(@NonNull CareStatusCard card) {
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

        private void setStateOff(@NonNull CareStatusCard card) {
            // Set Ring
            mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
            mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.OFF);

            // Set Center Text
            if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
            if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());

        }

        private void setStateOn(@NonNull CareStatusCard card) {
            // Set Ring
            mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
            mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.ON);

            // Set Center Text
            if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
            if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());
        }

        private void setStatePartial(@NonNull CareStatusCard card) {
            // Set Ring
            mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
            mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.PARTIAL);

            // Set Center Text
            if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
            if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());
        }

        private void setStateArming(@NonNull CareStatusCard card) {
            // Set Ring
            mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
            mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.ARMING);

            // Set Center Text
            if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
            if (card.getCenterBottomText() != null) mCenterBottomTextView.setText(card.getCenterBottomText());
        }

        private void setStateAlert(@NonNull CareStatusCard card) {
            // Set Ring
            mDashedCircleView.setDevicesCount(card.getOfflineDevices(), card.getBypassDevices(), card.getActiveDevices());
            mDashedCircleView.setAlarmState(DashedCircleView.AlarmState.ALERT);

            // Show Center Icon
            mTopIconView.setVisibility(View.VISIBLE);

            if (card.getDeviceModel() != null) {
                ImageManager.with(getContext())
                        .putSmallDeviceImage(card.getDeviceModel())
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .noUserGeneratedImagery()
                        .into(mTopIconView)
                        .execute();
            }
            else {
                // TODO is this the right icon for panic?
                Drawable icon = ContextCompat.getDrawable(getContext(), R.drawable.icon_cat_securityalarm);
                mTopIconView.setImageDrawable(icon);
            }

            // Show Center Bar
            mTopLineView.setVisibility(View.VISIBLE);

            // Show (( ALARM ))
            mCenterAlarmText.setText("ALARM");
            mCenterAlarmText.setVisibility(View.VISIBLE);
            mLeftAlarmIcon.setVisibility(View.VISIBLE);
            mRightAlarmIcon.setVisibility(View.VISIBLE);

            //hide center bottom text
            mCenterBottomTextView.setVisibility(GONE);

            // Set Center Text
            if (card.getCenterTopText() != null) mCenterTopTextView.setText(card.getCenterTopText());
        }

        private void resetVisibility() {
            mCenterBottomTextView.setVisibility(VISIBLE);
            mTopIconView.setVisibility(View.GONE);
            mTopLineView.setVisibility(View.GONE);
            mLeftAlarmIcon.setVisibility(View.GONE);
            mRightAlarmIcon.setVisibility(View.GONE);
            mCenterAlarmText.setVisibility(View.GONE);
        }

}
