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

import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.subsystems.alarm.cards.AlarmInfoCard;


public class AlarmInfoCardItemView extends RecyclerView.ViewHolder {

    TextView title;
    TextView description;
    CardView cardView;
    ImageView imageView;
    View chevron;
    private GlobalSetting.AlertCardTags cardType;

    public AlarmInfoCardItemView(View view) {
        super(view);
        title = (TextView)view.findViewById(R.id.title_text);
        description = (TextView)view.findViewById(R.id.description_text);
        cardView = (CardView) view.findViewById(R.id.cardView);
        imageView = (ImageView) view.findViewById(R.id.image);
        chevron = view.findViewById(R.id.chevron);
    }


    public GlobalSetting.AlertCardTags getCardType() {
        return cardType;
    }

    public void build(@NonNull AlarmInfoCard card) {
        cardType = (GlobalSetting.AlertCardTags) card.getTag();
        title.setText(card.getTitle());
        if(card.getTitleColor() != -1) {
            title.setTextColor(card.getTitleColor());
        }


        description.setText(card.getDescription());
        if(card.getDescriptionColor() != -1) {
            description.setTextColor(card.getDescriptionColor());
        }

        if (cardView != null) {
            cardView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (card.isChevronShown()) {
            showChevron(card.isChevronShown());
        }

        if(card.getImageResource() != -1){
            imageView.setImageResource(card.getImageResource());
        } else {
            imageView.setImageDrawable(null);
        }

    }


    private void showChevron(boolean shown) {
        if (chevron != null)  chevron.setVisibility(shown ? View.VISIBLE : View.GONE);
    }
}
