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
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.cards.StatusCard;
import arcus.app.common.view.Version1TextView;


public class StatusCardItemView extends BaseCardItemView<StatusCard> {


    // Default constructors
    public StatusCardItemView(Context context) {
        super(context);
    }

    public StatusCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StatusCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void build(@NonNull StatusCard card) {
        super.build(card);

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(getContext().getResources().getColor(R.color.overlay_white_with_10));
        }

        ImageView image = (ImageView) findViewById(R.id.image);
        Version1TextView title = (Version1TextView) findViewById(R.id.title);
        Version1TextView description = (Version1TextView) findViewById(R.id.description);

        image.setImageResource(card.getImageResource());
        title.setText(card.getTitle());
        description.setText(card.getDescription());

        if (card.isDividerShown()) {
            showDivider();
        }
    }

    private void showDivider() {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
    }
}
