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
package arcus.app.subsystems.care.adapter;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.cards.view.BaseCardItemView;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.care.cards.CareScheduledListItemCard;


public class CareScheduledListItem extends BaseCardItemView<CareScheduledListItemCard> {

    public CareScheduledListItem(Context context) {
        super(context);
    }

    public CareScheduledListItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CareScheduledListItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void build(@NonNull final CareScheduledListItemCard card) {
        super.build(card);

        RelativeLayout checkboxClickRegion = (RelativeLayout) this.findViewById(R.id.checkbox_click_region);
        RelativeLayout chevronClickRegion = (RelativeLayout) this.findViewById(R.id.chevron_click_region);

        TextView title = (TextView)this.findViewById(R.id.title);
        title.setText(card.getTitle());
        if(card.getTitleColor() != -1) {
            title.setTextColor(card.getTitleColor());
        }

        Version1TextView description = (Version1TextView) this.findViewById(R.id.subtitle);
        description.setText(card.getDescription());
        if(card.getDescriptionColor() != -1) {
            description.setTextColor(card.getDescriptionColor());
        }

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        View divider = findViewById(R.id.divider_care_scheduled_list);
        if (card.isDividerShown()) {
            if (divider != null) divider.setVisibility(View.VISIBLE);
        } else {
            if (divider != null) divider.setVisibility(View.INVISIBLE);
        }

        if (card.isChevronShown()) {
            showChevron(card.isChevronShown());
        }

        setScheduled(card.getScheduled());
        showMinus(card.getEditMode(), card.getItemChecked());

        checkboxClickRegion.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (card.getListener() != null) {
                    card.getListener().onCheckboxRegionClicked(card, card.getItemChecked());
                }
            }
        });

        chevronClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (card.getListener() != null) {
                    card.getListener().onChevronRegionClicked(card);
                }
            }
        });
    }

    private void showChevron(boolean shown) {
        View chevron = findViewById(R.id.chevron);
        if (chevron != null)  chevron.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    private void showMinus(boolean isEditMode, boolean isChecked){
        ImageView checkbox = (ImageView) this.findViewById(R.id.checkbox);
        if (isEditMode) {
            checkbox.setImageResource(R.drawable.icon_delete);
        } else {
            checkbox.setImageResource(isChecked ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);
        }
    }

    private void setScheduled(boolean scheduled){
        ImageView schedule = (ImageView) this.findViewById(R.id.scheduled);
        if(scheduled){
            schedule.setVisibility(View.VISIBLE);
        } else {
            schedule.setVisibility(View.GONE);
        }

    }




}
