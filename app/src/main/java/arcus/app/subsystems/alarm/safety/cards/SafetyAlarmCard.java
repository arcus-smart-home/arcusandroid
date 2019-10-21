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
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;


public class SafetyAlarmCard extends SimpleDividerCard {

    public final static String TAG = "";
    private AlarmState mAlarmState = AlarmState.OFF;
    private String summary;
    private DeviceModel mDeviceModel;

    public enum AlarmState {
        ON,
        OFF,
        PARTIAL,
        ARMING,
        ALERT,
    }

    public SafetyAlarmCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    @Override
    public int getLayout() {
        return R.layout.card_safety_alarm;
    }

    public void setAlarmState(AlarmState state) {
        this.mAlarmState = state;
        BusProvider.dataSetChanged();
    }

    public AlarmState getAlarmState() {
        return mAlarmState;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
        BusProvider.dataSetChanged();
    }

    public DeviceModel getDeviceModel() {
        return mDeviceModel;
    }

    public void setDeviceModel(DeviceModel mDeviceModel) {
        this.mDeviceModel = mDeviceModel;
        BusProvider.dataSetChanged();
    }
}
