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
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.activities.GenericConnectedFragmentActivity;
import arcus.app.common.cards.CenteredTextCard;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.dashboard.settings.favorites.FavoritesListFragment;
import arcus.app.subsystems.favorites.cards.NoFavoritesCard;


public class DashboardCenteredTextCardItemView extends RecyclerView.ViewHolder {
    private Context context;
    private AppCompatImageView image;
    private TextView title;
    private CardView cardView;
    private TextView description;
    public DashboardCenteredTextCardItemView(View view) {
        super(view);
        context = view.getContext();
        image = view.findViewById(R.id.image);
        title = (TextView)view.findViewById(R.id.title_text);
        cardView = (CardView)view.findViewById(R.id.cardView);
        description = (TextView)view.findViewById(R.id.description);
    }

    public void build(@NonNull CenteredTextCard card) {
        final CenteredTextCard.OnClickCallaback callaback = card.getCallaback();

        if(card instanceof NoFavoritesCard) {
            image.setVisibility(View.VISIBLE);
            ImageManager.with(context)
                    .putDrawableResource(R.drawable.favorite_24x21)
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .into(image)
                    .execute();

            Boolean canAddFavorites = NoFavoritesCard.Reason.NO_FAVORITES_SELECTED.name().equals(card.getTag().toString());
            if(canAddFavorites) {
                itemView.setOnClickListener( view ->
                    context.startActivity(GenericConnectedFragmentActivity
                        .getLaunchIntent(
                            context,
                            FavoritesListFragment.class,
                            null,
                            false)
                    )
                );
            } else {
                itemView.setOnClickListener(view -> { /* Do nothing if no devices/scenes */ } );
            }
        }

        title.setText(card.getTitle());
        if (callaback != null) {
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callaback.onTitleClicked();
                }
            });
        }
        if(card.getTitleColor() != -1) {
            title.setTextColor(card.getTitleColor());
        }


        if (cardView != null) {
            if (card.isTransparentBackground()) {
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
            }
            else {
                cardView.setCardBackgroundColor(context.getResources().getColor(R.color.overlay_white_with_10));
            }
        }

        if (card.getDescription() != null) {

            description.setVisibility(View.VISIBLE);
            description.setText(card.getDescription());
            description.setTextColor(card.getDescriptionColor());
            if (card.getDescriptionBackground() != -1) {
                description.setBackgroundResource(card.getDescriptionBackground());
            }
            if (callaback != null) {
                description.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        callaback.onDescriptionClicked();
                    }
                });
            }
        }
    }
}
