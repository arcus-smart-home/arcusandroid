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
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import android.util.AttributeSet;

import arcus.app.R;
import arcus.app.common.cards.ListHeadingCard;
import arcus.app.common.view.Version1TextView;


public class ListHeadingCardItemView extends BaseCardItemView<ListHeadingCard> {

    public ListHeadingCardItemView(Context context) {
        super(context);
    }

    public ListHeadingCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListHeadingCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void build(@NonNull ListHeadingCard card) {
        super.build(card);

        Version1TextView leftText = (Version1TextView) findViewById(R.id.left_text);
        Version1TextView rightText = (Version1TextView) findViewById(R.id.right_text);

        if (card.getLeftText() == null) {
            leftText.setVisibility(GONE);
        } else {
            leftText.setText(card.getLeftText());
            leftText.setVisibility(VISIBLE);
        }

        if (card.getRightText() == null) {
            rightText.setVisibility(GONE);
        } else {
            rightText.setText(card.getRightText());
            rightText.setVisibility(VISIBLE);
        }

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));
        }
    }

}
