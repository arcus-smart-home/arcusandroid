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
package arcus.app.subsystems.history.cards;

import android.content.Context;

import com.dexafree.materialList.events.BusProvider;
import arcus.cornea.dto.HistoryLogEntries;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;


public class HistoryCard extends SimpleDividerCard {

    public final static String TAG = "HISTORY";
    private HistoryLogEntries entries;

    public HistoryCard(Context context, HistoryLogEntries historyLogEntries) {
        super(context);

        setEntries(historyLogEntries);
        showDivider();
        setTag(TAG);
    }

    @Override
    public int getLayout() {
        return R.layout.card_history;
    }

    public void setEntries(HistoryLogEntries entries) {
        this.entries = entries;
        BusProvider.dataSetChanged();
    }

    public HistoryLogEntries getEntries() {
        return this.entries;
    }
}
