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
package arcus.app.subsystems.favorites.cards;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.common.cards.CenteredTextCard;


public class NoFavoritesCard extends CenteredTextCard {

    public static String TAG = "FAVORITES";

    public enum Reason {
        NOTHING_TO_FAVORITE,
        NO_FAVORITES_SELECTED,
        NO_SUBSCRIPTION
    }

    public NoFavoritesCard(Context context, @NonNull Reason reason) {
        super(context);

        switch (reason) {
            case NOTHING_TO_FAVORITE:
                TAG = Reason.NOTHING_TO_FAVORITE.name();
                setTitle(getString(R.string.card_nothing_to_favorite));
                setDescription(getString(R.string.card_nothing_to_favorite_description));
                break;
            case NO_FAVORITES_SELECTED:
                TAG = Reason.NO_FAVORITES_SELECTED.name();
                setTitle(getString(R.string.card_no_favorites_yet));
                setDescription(getString(R.string.card_no_favorites_description));
                break;
            default:
                setTitle(getString(R.string.card_no_favorites_subscription));
                break;
        }

        showDivider();
        setTag(TAG);
    }
}
