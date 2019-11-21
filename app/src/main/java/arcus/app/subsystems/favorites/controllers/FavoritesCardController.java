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
package arcus.app.subsystems.favorites.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.subsystem.favorites.FavoritesController;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.subsystems.favorites.cards.FavoritesCard;
import arcus.app.subsystems.favorites.cards.NoFavoritesCard;

import java.util.List;


public class FavoritesCardController extends AbstractCardController<SimpleDividerCard> implements FavoritesController.Callback {
    private ListenerRegistration listenerRegistration;

    public FavoritesCardController(Context context) {
        super(context);

        setCurrentCard(new NoFavoritesCard(getContext(), NoFavoritesCard.Reason.NO_FAVORITES_SELECTED));
    }

    public void setCallback(Callback delegate) {
        super.setCallback(delegate);
        listenerRegistration = FavoritesController.getInstance().setCallback(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void removeCallback() {
        super.removeCallback();
        listenerRegistration.remove();
    }

    @Override
    public void showFavorites(@NonNull List<Model> favoriteModels) {
        SimpleDividerCard currentCard = getCard();
        List<Model> orderdFavorites = PreferenceUtils.getOrderedFavorites(favoriteModels);

        if (currentCard instanceof FavoritesCard) {
            ((FavoritesCard) currentCard).setModels(orderdFavorites);
        }
        else {
            setCurrentCard(new FavoritesCard(getContext(), orderdFavorites));
        }
    }

    @Override
    public void showAddFavorites() {
        setCurrentCard(new NoFavoritesCard(getContext(), NoFavoritesCard.Reason.NO_FAVORITES_SELECTED));
    }

    @Override
    public void showNoItemsToFavorite() {
        setCurrentCard(new NoFavoritesCard(getContext(), NoFavoritesCard.Reason.NOTHING_TO_FAVORITE));
    }

    @Override
    public void onError(Throwable throwable) {

    }

    public void favoriteOrderChanged() {
        if (getCard() instanceof FavoritesCard) {
            FavoritesCard card = (FavoritesCard) getCard();
            card.setModels(PreferenceUtils.getOrderedFavorites(card.getModels()));
        }
    }
}
