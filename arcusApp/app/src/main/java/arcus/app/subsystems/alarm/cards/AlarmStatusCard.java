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
package arcus.app.subsystems.alarm.cards;

import android.content.Context;
import android.view.View;

import com.dexafree.materialList.events.BusProvider;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;

import java.util.Date;


public class AlarmStatusCard extends SimpleDividerCard {


    public enum AlarmState {
        ON,
        OFF,
        PARTIAL,
        ARMING,
        ALERT,
    }

    public enum AlarmType {
        SECURITY,
        SAFETY,
        CARE
    }

    private String mStatus;
    private Date mSinceDate;
    private AlarmState mAlarmState = AlarmState.OFF;
    private AlarmType alarmType = AlarmType.SECURITY;
    private View.OnClickListener mLeftButtonListener;
    private View.OnClickListener mRightButtonListener;


    public AlarmStatusCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout(){
        return R.layout.card_alarm_status;
    }

    public void setAlarmState(AlarmState state) {
        mAlarmState = state;
        BusProvider.dataSetChanged();
    }

    public AlarmState getAlarmState() {
        return mAlarmState;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        this.mStatus = status;
        BusProvider.dataSetChanged();
    }

    public Date getSinceDate() {
        return mSinceDate;

    }

    public void setSinceDate(Date sinceDate) {
        this.mSinceDate = sinceDate;
        BusProvider.dataSetChanged();
    }

    public View.OnClickListener getLeftButtonListener() {
        return mLeftButtonListener;
    }

    public void setLeftButtonListener(View.OnClickListener leftButtonListener) {
        this.mLeftButtonListener = leftButtonListener;
        BusProvider.dataSetChanged();
    }

    public View.OnClickListener getRightButtonListener() {
        return mRightButtonListener;
    }

    public void setRightButtonListener(View.OnClickListener rightButtonListener) {
        this.mRightButtonListener = rightButtonListener;
        BusProvider.dataSetChanged();
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(AlarmType alarmType) {
        this.alarmType = alarmType;
    }
}
