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
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import arcus.cornea.subsystem.security.model.AlarmStatus;
import arcus.app.R;
import arcus.app.common.cards.view.BaseCardItemView;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class CareAlarmCardItemView extends BaseCardItemView<CareAlarmCard> {

    public CareAlarmCardItemView(Context context) {
        super(context);
    }

    public CareAlarmCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CareAlarmCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(@NonNull CareAlarmCard card) {
        super.build(card);

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
        }

        String mode = card.getMode();
        if (mode != null && !mode.equals("")) {
            ImageView stateIcon = (ImageView) findViewById(R.id.state_icon);
            switch (mode) {
                case AlarmStatus.MODE_OFF:
                    stateIcon.setImageResource(R.drawable.button_off);
                    break;
                case AlarmStatus.MODE_ON:
                    stateIcon.setImageResource(R.drawable.button_on);
                    break;
                case AlarmStatus.MODE_PARTIAL:
                    stateIcon.setImageResource(R.drawable.button_partial);
                    break;
                default:
                    break;
            }
        }

        String state = card.getState();
        if (state != null && !state.equals("")) {
            Version1TextView title = (Version1TextView) findViewById(R.id.state_title);
            title.setText("Alarm " + state);
        }

        if (card.getDate() != null) {
            Version1TextView subtitle = (Version1TextView) findViewById(R.id.state_subtitle);
            long difference = StringUtils.howManyDaysBetween(Calendar.getInstance().getTime(), card.getDate());
            if(difference >0){
                String days = difference ==1 ? "day" : "days";
                subtitle.setText("since " + difference + " " + days);
            }else{
                SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a");
                subtitle.setText("Set at " + formatter.format(card.getDate()));
            }

        }

        if (card.isDividerShown()) {
            showDivider();
        }
    }

    protected void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }
}
