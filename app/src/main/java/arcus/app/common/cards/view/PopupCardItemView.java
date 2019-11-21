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
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.cards.PopupCard;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;


public class PopupCardItemView extends BaseCardItemView<PopupCard> implements View.OnClickListener {
    private PopupCard.ClickListener listener;

    public PopupCardItemView(Context context) {
        super(context);
    }

    public PopupCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PopupCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void build(@NonNull PopupCard card) {
        super.build(card);

        TextView title = (TextView)this.findViewById(R.id.title);
        title.setText(card.getTitle());
        if(card.getTitleColor() != -1) {
            title.setTextColor(card.getTitleColor());
        }
        else if (card.isDarkColorScheme()) {
            title.setTextColor(Color.BLACK);
        }

        TextView description = (TextView)this.findViewById(R.id.description);
        if (card.getDescription() != null) {
            description.setText(card.getDescription());
            if (card.getTitleColor() != -1) {
                description.setTextColor(card.getTitleColor());
            }
            else if (card.isDarkColorScheme()) {
                description.setTextColor(Color.BLACK);
            }
        }
        else {
            description.setVisibility(GONE);
        }

        CardView cardView = (CardView) findViewById(R.id.cardView);
        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
            cardView.setOnClickListener(this);
        }
        listener = card.getClickListener();

        TextView rightText = (TextView) this.findViewById(R.id.right_text);
        if (card.getRightText() != null) {
            rightText.setVisibility(VISIBLE);
            rightText.setText(card.getRightText());
            if (card.isDarkColorScheme()) {
                rightText.setTextColor(Color.BLACK);
            }
        }
        else {
            rightText.setVisibility(GONE);
        }

        if (card.isDividerShown()) {
            showDivider(card.isDarkColorScheme());
        }

        showChevron(card.isDarkColorScheme());
    }

    private void showDivider(boolean darkColorScheme) {
        View divider = findViewById(R.id.divider);
        if (divider != null) divider.setVisibility(View.VISIBLE);
        if (darkColorScheme && divider != null) {
            divider.setBackgroundColor(getResources().getColor(R.color.black_with_10));
        }
    }

    private void showChevron(boolean darkColorScheme) {
        ImageView chevron = (ImageView) findViewById(R.id.chevron);
        if (chevron != null) chevron.setVisibility(View.VISIBLE);
        if (darkColorScheme && chevron != null) {
            ImageManager.with(getContext())
                  .putDrawableResource(R.drawable.chevron_white)
                  .withTransform(new BlackWhiteInvertTransformation(Invert.WHITE_TO_BLACK))
                  .into(chevron)
                  .execute();
        }
    }

    @Override
    public void onClick(View view) {
        if (listener == null) {
            return;
        }

        listener.cardClicked(view);
    }
}
