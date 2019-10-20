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
package arcus.app.subsystems.doorsnlocks.controllers;

import android.content.Context;

import arcus.cornea.subsystem.doorsnlocks.DoorsNLocksCardController;
import arcus.cornea.subsystem.doorsnlocks.model.StateSummary;
import com.iris.client.event.ListenerRegistration;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.doorsnlocks.cards.DoorsNLocksCard;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DoorsnlocksCardController extends AbstractCardController<SimpleDividerCard> implements DoorsNLocksCardController.Callback{

    private ListenerRegistration mCallback;
    private Logger logger = LoggerFactory.getLogger(DoorsnlocksCardController.class);
    private DoorsNLocksCard doorsNLocksCard;

    public DoorsnlocksCardController(Context context) {
        super(context);

        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.DOORS_AND_LOCKS));
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mCallback = DoorsNLocksCardController.instance().setCallback(this);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void showLearnMore() {
        setCurrentCard(new LearnMoreCard(getContext(), ServiceCard.DOORS_AND_LOCKS));
    }

    @Override
    public void showSummary(StateSummary summary) {
        logger.debug("Got state summary:{}",summary);
        if(doorsNLocksCard == null){
            doorsNLocksCard = new DoorsNLocksCard(getContext());
        }
        doorsNLocksCard.setLock_state(DoorsNLocksCard.LOCK_STATE.OPEN);
        doorsNLocksCard.setStateSummary(summary);
        setCurrentCard(doorsNLocksCard);
    }

    @Override
    public void showTextualSummary(String summary) {
        logger.debug("Got textual summary: {}", summary);
        if(doorsNLocksCard == null){
            doorsNLocksCard = new DoorsNLocksCard(getContext());
        }
        doorsNLocksCard.setLock_state(DoorsNLocksCard.LOCK_STATE.LOCKED);
        doorsNLocksCard.setTextualSummary(summary);
        setCurrentCard(doorsNLocksCard);
    }

    @Override
    public void updateLockOfflineState(boolean bOfflineLocks) {
        if(doorsNLocksCard == null){
            doorsNLocksCard = new DoorsNLocksCard(getContext());
        }
        doorsNLocksCard.setLocksOffline(bOfflineLocks);
    }

    @Override
    public void updateGarageOfflineState(boolean bOfflineGarage) {
        if(doorsNLocksCard == null){
            doorsNLocksCard = new DoorsNLocksCard(getContext());
        }
        doorsNLocksCard.setGarageOffline(bOfflineGarage);
    }

    @Override
    public void updateDoorOfflineState(boolean bOfflineDoor) {
        if(doorsNLocksCard == null){
            doorsNLocksCard = new DoorsNLocksCard(getContext());
        }
        doorsNLocksCard.setDoorOffline(bOfflineDoor);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mCallback.remove();
    }
}
