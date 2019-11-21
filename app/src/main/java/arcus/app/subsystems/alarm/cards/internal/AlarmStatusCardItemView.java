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
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import arcus.app.R;
import arcus.app.subsystems.alarm.cards.AlarmStatusCard;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static android.graphics.Color.TRANSPARENT;


public class AlarmStatusCardItemView extends RecyclerView.ViewHolder {

    private ImageView mLeftButton;
    private ImageView mRightButton;

    private Version1TextView mStatusTextView;
    private Version1TextView mTimeTextView;
    private LinearLayout mStatusLayout;
    private LinearLayout buttonLayoutView;
    private Version1TextView leftButtonText;
    private RelativeLayout rightButtonLayout;
    private Version1TextView rightButtonText;
    private CardView cardView;
    private Context context;

    // Default constructors
    public AlarmStatusCardItemView(View view) {
        super(view);
        context = view.getContext();
        mLeftButton = (ImageView) view.findViewById(R.id.left_button_image);
        mRightButton = (ImageView) view.findViewById(R.id.right_button_image);
        mStatusTextView = (Version1TextView) view.findViewById(R.id.status_text);
        mTimeTextView = (Version1TextView) view.findViewById(R.id.time_text);
        mStatusLayout = (LinearLayout) view.findViewById(R.id.status_layout);
        leftButtonText = (Version1TextView) view.findViewById(R.id.left_button_text);
        rightButtonText = (Version1TextView) view.findViewById(R.id.left_button_text);
        rightButtonLayout = (RelativeLayout) view.findViewById(R.id.right_button_layout);
        buttonLayoutView = (LinearLayout) view.findViewById(R.id.button_layout_view);
        cardView = (CardView) view.findViewById(R.id.cardView);
    }

    public void build(@NonNull AlarmStatusCard card) {


        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        // Configure the card view based on the alarmstate
        handleAlarmState(card);
    }

    private void handleAlarmState(@NonNull AlarmStatusCard card) {
        resetViewState();

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
                //Does nothing else except for modify view
                setOffVisibility(card);
                break;
            case ALERT:
                setStateAlert(card);
                break;
            default:
                break;
        }
    }

    private void setStateOff(@NonNull AlarmStatusCard card) {

        // Set Buttons
        // On
        mStatusLayout.setVisibility(View.VISIBLE);
        mLeftButton.setVisibility(View.VISIBLE);
        mLeftButton.setImageResource(R.drawable.button_on);
        mLeftButton.setOnClickListener(card.getLeftButtonListener());
        leftButtonText.setVisibility(View.GONE);

        // Partial
        rightButtonLayout.setVisibility(View.VISIBLE);
        mRightButton.setVisibility(View.VISIBLE);
        mRightButton.setImageResource(R.drawable.button_partial);
        mRightButton.setOnClickListener(card.getRightButtonListener());
        rightButtonText.setVisibility(View.GONE);
        buttonLayoutView.setBackgroundColor(TRANSPARENT);

        // Set Status Text
        if (card.getStatus() != null) mStatusTextView.setText(card.getStatus());

        // Set Time
        if (card.getSinceDate() != null) {
            Date sinceTime = card.getSinceDate();
            long difference = StringUtils.howManyDaysBetween(Calendar.getInstance().getTime(), sinceTime);
            if(difference >0){
                String days = difference ==1 ? "day" : "days";
                mTimeTextView.setText(StringUtils.getSuperscriptSpan(String.valueOf(difference)," " + days));
            }else{
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
                SimpleDateFormat ampmFormat = new SimpleDateFormat("aaa");
                mTimeTextView.setText(StringUtils.getSuperscriptSpan(timeFormat.format(sinceTime), " " + ampmFormat.format(sinceTime)));
            }
        }
    }

    private void setStateOn(@NonNull AlarmStatusCard card) {

        // Set Buttons
        // On
        setOffVisibility(card);

        // Set Status Text
        if (card.getStatus() != null) mStatusTextView.setText(card.getStatus());

        // Set Time
        if (card.getSinceDate() != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
            SimpleDateFormat ampmFormat = new SimpleDateFormat("aaa");
            Date sinceTime = card.getSinceDate();
            mTimeTextView.setText(StringUtils.getSuperscriptSpan(timeFormat.format(sinceTime), " " + ampmFormat.format(sinceTime)));
        }
    }

    private void setStatePartial(@NonNull AlarmStatusCard card) {
        // Set Buttons
        // On

        setOffVisibility(card);

        // Set Status Text
        if (card.getStatus() != null) mStatusTextView.setText(card.getStatus());

        // Set Time
        if (card.getSinceDate() != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
            SimpleDateFormat ampmFormat = new SimpleDateFormat("aaa");
            Date sinceTime = card.getSinceDate();
            mTimeTextView.setText(StringUtils.getSuperscriptSpan(timeFormat.format(sinceTime), " " + ampmFormat.format(sinceTime)));
        }
    }

    private void setStateAlert(@NonNull AlarmStatusCard card) {
        // Set Buttons
        // On
        mStatusLayout.setVisibility(View.GONE);
        mLeftButton.setImageResource(R.drawable.icon_checkmark_white);
        mLeftButton.setOnClickListener(card.getLeftButtonListener());
        mLeftButton.setVisibility(View.VISIBLE);
        leftButtonText.setText("Cancel");
        leftButtonText.setVisibility(View.VISIBLE);
        if (AlarmStatusCard.AlarmType.CARE.equals(card.getAlarmType())) {
            buttonLayoutView.setBackgroundColor(context.getResources().getColor(R.color.care_alarm_light_purple));
        }
        else {
            buttonLayoutView.setBackgroundColor(Color.rgb(237,116,157));
        }
        rightButtonLayout.setVisibility(View.GONE);

        // Set Status Text
        if (card.getStatus() != null) mStatusTextView.setText(card.getStatus());

        // Set Time
        if (card.getSinceDate() != null) {
            SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm");
            SimpleDateFormat ampmFormat = new SimpleDateFormat("aaa");
            Date sinceTime = card.getSinceDate();
            mTimeTextView.setText(StringUtils.getSuperscriptSpan(timeFormat.format(sinceTime), " " + ampmFormat.format(sinceTime)));
        }
    }

    private void resetViewState() {

        leftButtonText.setVisibility(View.GONE);
        rightButtonText.setVisibility(View.GONE);
        mLeftButton.setVisibility(View.VISIBLE);
        mRightButton.setVisibility(View.VISIBLE);

        mLeftButton.setOnClickListener(null);
        mRightButton.setOnClickListener(null);

        mStatusTextView.setText("");
        mTimeTextView.setText("...");
        if (buttonLayoutView != null) {
            buttonLayoutView.setBackgroundColor(TRANSPARENT);
        }
    }

    private void setOffVisibility(AlarmStatusCard card){

        // Set Buttons
        // On
        mStatusLayout.setVisibility(View.VISIBLE);
        mLeftButton.setVisibility(View.VISIBLE);
        mLeftButton.setImageResource(R.drawable.button_off);
        if(card.getAlarmState().equals(AlarmStatusCard.AlarmState.ARMING)) {
            mLeftButton.setOnClickListener(card.getRightButtonListener());
            mStatusTextView.setText("ARMING");
        }else{
            mLeftButton.setOnClickListener(card.getLeftButtonListener());
        }

        // Partial
        rightButtonLayout.setVisibility(View.GONE);


    }

}
