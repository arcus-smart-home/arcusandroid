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
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.subsystem.climate.model.ScheduledSetPoint;
import arcus.cornea.utils.DateUtils;
import arcus.app.R;
import arcus.app.common.cards.view.BaseCardItemView;
import arcus.app.subsystems.climate.cards.BaseClimateScheduleCard;


public class BaseClimateScheduleCardItemView extends BaseCardItemView<BaseClimateScheduleCard> {

    ClimateScheduleCardInterface listener;

    public BaseClimateScheduleCardItemView(Context context) {
        super(context);
    }

    public BaseClimateScheduleCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseClimateScheduleCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void setInterfaceListener(ClimateScheduleCardInterface listener) {
        this.listener = listener;
    }

    public void build(@NonNull BaseClimateScheduleCard card) {
        super.build(card);

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (card.isDividerShown()) {
            showDivider();
        }

        TextView leftText = (TextView) findViewById(R.id.climate_schedule_left_text);
        TextView rightText = (TextView) findViewById(R.id.climate_schedule_right_text);
        ImageView weatherIcon = (ImageView) findViewById(R.id.climate_schedule_weather_icon);

        ScheduledSetPoint point = card.getScheduledSetPoint();
        if(point !=null) {
            leftText.setText(DateUtils.format(point.getTimeOfDay(), true));
            switch (point.getTimeOfDay().getDayTime()) {
                case DAYTIME:
                    weatherIcon.setImageResource(R.drawable.icon_day);
                    break;
                case NIGHTTIME:
                    weatherIcon.setImageResource(R.drawable.icon_night);
                    break;
                case SUNRISE:
                    weatherIcon.setImageResource(R.drawable.icon_sunrise);
                    break;
                case SUNSET:
                    weatherIcon.setImageResource(R.drawable.icon_sunset);
                    break;
                default:
                    weatherIcon.setImageResource(R.drawable.icon_day);
                    break;
            }
            listener.updateRightText(rightText, point);
        }
    }
    protected void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }

    public interface ClimateScheduleCardInterface {
        void updateRightText(TextView rightText, ScheduledSetPoint point);
    }
}
