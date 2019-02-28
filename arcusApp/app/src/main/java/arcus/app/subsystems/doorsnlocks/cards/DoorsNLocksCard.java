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
package arcus.app.subsystems.doorsnlocks.cards;

import android.content.Context;

import arcus.cornea.subsystem.doorsnlocks.model.StateSummary;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;


public class DoorsNLocksCard extends SimpleDividerCard {

    private StateSummary stateSummary;
    private String textualSummary;
    private LOCK_STATE lock_state;
    private boolean locksOffline = false;
    private boolean garageOffline = false;
    private boolean doorOffline = false;

    public final static String TAG = "Doors and locks card";

    public enum LOCK_STATE{
        LOCKED,
        OPEN
    }

    public DoorsNLocksCard(Context context) {
        super(context);
        setTag(TAG);
        showDivider();
    }

    public LOCK_STATE getLock_state() {
        return lock_state;
    }

    public void setLock_state(LOCK_STATE lock_state) {
        this.lock_state = lock_state;
    }

    public String getTextualSummary() {
        return textualSummary;
    }

    public void setTextualSummary(String textualSummary) {
        this.textualSummary = textualSummary;
    }

    public StateSummary getStateSummary() {
        return stateSummary;
    }

    public void setStateSummary(StateSummary stateSummary) {
        this.stateSummary = stateSummary;
    }

    public boolean isLocksOffline() {
        return locksOffline;
    }

    public void setLocksOffline(boolean locksOffline) {
        this.locksOffline = locksOffline;
    }

    public boolean isGarageOffline() {
        return garageOffline;
    }

    public void setGarageOffline(boolean garageOffline) {
        this.garageOffline = garageOffline;
    }

    public boolean isDoorOffline() {
        return doorOffline;
    }

    public void setDoorOffline(boolean doorOffline) {
        this.doorOffline = doorOffline;
    }

    @Override
    public int getLayout() {
        return R.layout.card_doors_and_locks;
    }
}
