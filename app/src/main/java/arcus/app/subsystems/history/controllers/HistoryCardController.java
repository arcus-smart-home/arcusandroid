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
package arcus.app.subsystems.history.controllers;

import android.content.Context;
import androidx.annotation.NonNull;

import com.dexafree.materialList.events.BusProvider;
import arcus.cornea.SessionController;
import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import com.iris.client.model.PlaceModel;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.subsystems.history.cards.HistoryCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HistoryCardController extends AbstractCardController<SimpleDividerCard> {

    private final static Logger logger = LoggerFactory.getLogger(HistoryCardController.class);
    private HistoryLogEntries historyLogEntries;
    private Callback callback;

    public interface Callback {
        void updateHistory();
    }

    public HistoryCardController (Context context, Callback callback) {
        super(context);
        this.callback = callback;
        setCurrentCard(new HistoryCard(getContext(), historyLogEntries));
    }

    @NonNull @Override public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    public void updateHistoryLogEntries() {
        PlaceModel placeModel = SessionController.instance().getPlace();
        if (placeModel == null) {
            return;
        }

        HistoryLogEntries
              .forDashboard(placeModel, GlobalSetting.HISTORY_LOG_ENTRIES_DASH, null)
              .onSuccess(Listeners.runOnUiThread(new Listener<HistoryLogEntries>() {
                  @Override public void onEvent(HistoryLogEntries entries) {
                      historyLogEntries = entries;
                      fireHistoryLogEntryChangedEvent();
                  }
              }))
              .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                  @Override public void onEvent(Throwable throwable) {
                      historyLogEntries = HistoryLogEntries.empty();
                      fireHistoryLogEntryChangedEvent();
                  }
              }));
    }

    private void fireHistoryLogEntryChangedEvent() {
        logger.debug("Loaded [{}] entries.", historyLogEntries.getEntries().size());
        ((HistoryCard)getCurrentCard()).setEntries(historyLogEntries);
        BusProvider.dataSetChanged();
        if(callback != null) {
            callback.updateHistory();
        }
    }
}
