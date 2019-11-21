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
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.subsystems.lawnandgarden.cards.IrrigationModeSelectionCard;
import arcus.app.common.cards.view.BaseCardItemView;


public class IrrigationModeSelectionCardItemView extends BaseCardItemView<IrrigationModeSelectionCard> {
    private ImageView imageView;
    private ImageView mRadioButton;

    public IrrigationModeSelectionCardItemView(Context context) {
        super(context);
    }

    public IrrigationModeSelectionCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IrrigationModeSelectionCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void build(@NonNull IrrigationModeSelectionCard card) {
        super.build(card);

        TextView title = (TextView) this.findViewById(R.id.title);

        mRadioButton = (ImageView) this.findViewById((R.id.rad_button));

        title.setText(card.getTitle());
        if (card.getTitleColor() != -1) {
            title.setTextColor(card.getTitleColor());
        }

        TextView description = (TextView) this.findViewById(R.id.description);
        if (card.getDescription() != null) {
            description.setVisibility(VISIBLE);
            description.setText(card.getDescription());
            if (card.getDescriptionColor() != -1) {
                description.setTextColor(card.getDescriptionColor());
            }
        } else {
            description.setVisibility(GONE);
        }

        TextView rightText = (TextView) this.findViewById(R.id.right_text);
        if (card.getRightText() == null) {
            rightText.setVisibility(GONE);
        } else {
            rightText.setText(card.getRightText());
        }

        imageView = (ImageView) this.findViewById(R.id.image);
        imageView.setVisibility(VISIBLE);

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (card.isChevronShown()) {
            showChevron();
        }

        if (card.isDividerShown()) {
            showDivider();
        }

        checkRadioButton(card);

        if(card.getShowScheduleIcon()) {
            findViewById(R.id.sched_icon).setVisibility(View.VISIBLE);
        }

    }


    public void checkRadioButton(IrrigationModeSelectionCard card) {
        if (card.isRadioChecked())
            mRadioButton.setBackgroundResource(R.drawable.circle_check_white_filled);
        else
            mRadioButton.setBackgroundResource(R.drawable.circle_hollow_white);
    }

    private void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }

    private void showChevron() {
        ImageView chevron = (ImageView) findViewById(R.id.chevron);
        if (chevron != null) chevron.setVisibility(View.VISIBLE);
    }
}
