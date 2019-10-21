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
package arcus.app.subsystems.alarm.safety.cards;

import android.content.Context;

import com.dexafree.materialList.events.BusProvider;
import arcus.cornea.subsystem.safety.model.SensorSummary;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;


public class SafetyStatusCard extends SimpleDividerCard {

    private SensorSummary summary;

    public SafetyStatusCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.card_safety_status;
    }

    public SensorSummary getSummary() {
        return summary;
    }

    public void setSummary(SensorSummary summary) {
        this.summary = summary;
        BusProvider.dataSetChanged();
    }
}
