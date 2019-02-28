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
package arcus.app.subsystems.homenfamily.controllers;

import android.app.Activity;
import android.content.Context;

import arcus.cornea.subsystem.presence.PresenceDashboardCardController;
import arcus.cornea.subsystem.presence.model.PresenceModel;
import arcus.cornea.subsystem.presence.model.PresenceState;
import com.iris.client.event.ListenerRegistration;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.models.PicListItemModel;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.homenfamily.cards.HomeNFamilyCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class HomeNFamilyCardController extends AbstractCardController<SimpleDividerCard> implements PresenceDashboardCardController.Callback, HomeNFamilyFragmentController.Callbacks {

    private ListenerRegistration mListener;
    private static final Logger logger = LoggerFactory.getLogger(HomeNFamilyCardController.class);

    public HomeNFamilyCardController(Context context) {
        super(context);
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.HOME_AND_FAMILY));
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        if (mListener != null) mListener.remove();
        mListener = PresenceDashboardCardController.instance().setCallback(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        HomeNFamilyFragmentController.getInstance().removeListener();
        mListener.remove();
    }

    @Override
    public void showLearnMore() {
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.HOME_AND_FAMILY));
    }

    @Override
    public void showPresence(List<PresenceModel> presence) {
        HomeNFamilyFragmentController.getInstance().setListener(this);
        HomeNFamilyFragmentController.getInstance().getPicListItemsForPresence(presence, HomeNFamilyFragmentController.PresenceTag.ALL);
    }

    @Override
    public void onPicListItemModelsLoaded(List<PicListItemModel> picListItems, HomeNFamilyFragmentController.PresenceTag tag) {
        List<PicListItemModel> orderedPicListItems = new ArrayList<>();

        // people home
        for (PicListItemModel item : picListItems) {
            if (item.getPresenceState().equals(PresenceState.HOME) && item.getPersonId() != null) {
                orderedPicListItems.add(item);
            }
        }

        // devices home
        for (PicListItemModel item : picListItems) {
            if (item.getPresenceState().equals(PresenceState.HOME) && item.getPersonId() == null) {
                orderedPicListItems.add(item);
            }
        }

        // people away
        for (PicListItemModel item : picListItems) {
            if (item.getPresenceState().equals(PresenceState.AWAY) && item.getPersonId() != null) {
                orderedPicListItems.add(item);
            }
        }

        // device away
        for (PicListItemModel item : picListItems) {
            if (item.getPresenceState().equals(PresenceState.AWAY) && item.getPersonId() == null) {
                orderedPicListItems.add(item);
            }
        }

        if (orderedPicListItems.size() > 0) {
            setCurrentCard(new HomeNFamilyCard(getContext()));
            ((HomeNFamilyCard) getCard()).setDevices(orderedPicListItems);
        }
    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in((Activity) getContext()).showGenericBecauseOf(cause);
    }
}
