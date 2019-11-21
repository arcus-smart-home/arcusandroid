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
package arcus.app.subsystems.alarm.safety.cards.internal;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;

import arcus.cornea.subsystem.safety.model.SensorSummary;
import arcus.app.R;
import arcus.app.subsystems.alarm.safety.cards.SafetyStatusCard;
import arcus.app.common.cards.view.BaseCardItemView;
import arcus.app.common.view.Version1TextView;


public class SafetyStatusCardItemView extends BaseCardItemView<SafetyStatusCard> {


    private Version1TextView smokeStatus;
    private Version1TextView coStatus;
    private Version1TextView waterLeakStatus;

    // Default constructors
    public SafetyStatusCardItemView(Context context) {
        super(context);
    }

    public SafetyStatusCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SafetyStatusCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(@NonNull SafetyStatusCard card) {
        super.build(card);

        View smokeView = findViewById(R.id.alarm_smoke_status);
        View coView = findViewById(R.id.alarm_co_status);
        View waterLeakView = findViewById(R.id.alarm_water_leak_status);

        Version1TextView smokeTitle = (Version1TextView) smokeView.findViewById(R.id.top_status_text);
        Version1TextView coTitle = (Version1TextView) coView.findViewById(R.id.top_status_text);
        Version1TextView waterLeakTitle = (Version1TextView) waterLeakView.findViewById(R.id.top_status_text);

        smokeTitle.setText(getResources().getString(R.string.smoke_status_title));
        coTitle.setText(getResources().getString(R.string.co_status_title));
        waterLeakTitle.setText(getResources().getString(R.string.water_leak_status_title));

        smokeStatus = (Version1TextView) smokeView.findViewById(R.id.bottom_status_text);
        coStatus = (Version1TextView) coView.findViewById(R.id.bottom_status_text);
        waterLeakStatus = (Version1TextView) waterLeakView.findViewById(R.id.bottom_status_text);

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (card.isDividerShown()) {
            showDivider();
        }

        handleStatusChange(card);
    }

    private void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }

    private void handleStatusChange(@NonNull final SafetyStatusCard card){
        final SensorSummary summary = card.getSummary();
        if(summary!=null){
            smokeStatus.setText(summary.getSmokeStatus());
            coStatus.setText(summary.getCoStatus());
            waterLeakStatus.setText(summary.getWaterLeakStatus());
        }
    }
}
