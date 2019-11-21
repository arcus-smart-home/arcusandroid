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
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.app.R;
import arcus.app.common.cards.BinarySwitchCard;


public class BinarySwitchCardItemView extends BaseCardItemView<BinarySwitchCard> implements View.OnClickListener {
    private BinarySwitchCard.ClickListener clickListener;

    public BinarySwitchCardItemView(Context context) {
        super(context);
    }

    public BinarySwitchCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BinarySwitchCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void build(@NonNull BinarySwitchCard card) {
        super.build(card);
        clickListener = card.getClickListener();

        TextView title = (TextView)this.findViewById(R.id.title);
        title.setText(card.getTitle());
        if(card.getTitleColor() != -1) {
            title.setTextColor(card.getTitleColor());
        }

        TextView description = (TextView)this.findViewById(R.id.description);
        description.setText(card.getDescription());
        if(card.getTitleColor() != -1) {
            description.setTextColor(card.getTitleColor());
        }

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (card.isDividerShown()) {
            showDivider();
        }

        ToggleButton button = (ToggleButton) this.findViewById(R.id.toggle);
        if (button != null) {
            button.setOnClickListener(this);
            button.setChecked(card.getToggleChecked());
        }
    }

    private void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(@NonNull View v) {
        if (clickListener == null) {
            return;
        }

        switch (v.getId()) {
            case R.id.toggle:
                clickListener.onToggleChanged((ToggleButton) v);
                break;
        }
    }
}
