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
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.view.View;

import com.dexafree.materialList.events.BusProvider;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;


public class AlarmTopCard extends SimpleDividerCard {

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

    private SpannableString mCenterTopText;
    private SpannableString mCenterBottomText;
    private AlarmState mAlarmState = AlarmState.OFF;
    private int mOfflineDevices;
    private int mBypassDevices;
    private int mActiveDevices;
    private DeviceModel mDeviceModel;
    private @DrawableRes Integer deviceImage;
    private boolean showCareOnOff;
    private boolean toggleOn;
    private boolean toggleEnabled;
    private AlarmType alarmType = AlarmType.SECURITY;
    private View.OnClickListener careToggleListener;
    private int totalDevices;

    private boolean isCareCard = false;

    public AlarmTopCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout(){
        return R.layout.card_alarm_top;
    }

    public void setAlarmState(AlarmState state) {
        mAlarmState = state;
        BusProvider.dataSetChanged();
    }

    public AlarmState getAlarmState() {
        return mAlarmState;
    }

    public SpannableString getCenterTopText() {
        return mCenterTopText;
    }

    public void setCenterTopText(SpannableString centerTopText) {
        this.mCenterTopText = centerTopText;
        BusProvider.dataSetChanged();
    }

    public SpannableString getCenterBottomText() {
        return mCenterBottomText;
    }

    public void setCenterBottomText(SpannableString centerBottomText) {
        this.mCenterBottomText = centerBottomText;
        BusProvider.dataSetChanged();
    }

    public int getOfflineDevices() {
        return mOfflineDevices;
    }

    public void setOfflineDevices(int offlineDevices) {
        this.mOfflineDevices = offlineDevices;
        BusProvider.dataSetChanged();
    }

    public int getBypassDevices() {
        return mBypassDevices;
    }

    public void setBypassDevices(int bypassDevices) {
        this.mBypassDevices = bypassDevices;
        BusProvider.dataSetChanged();
    }

    public int getActiveDevices() {
        return mActiveDevices;
    }

    public void setActiveDevices(int activeDevices) {
        this.mActiveDevices = activeDevices;
        BusProvider.dataSetChanged();
    }

    public DeviceModel getDeviceModel() {
        return mDeviceModel;
    }

    public void setDeviceModel(DeviceModel mDeviceModel) {
        this.mDeviceModel = mDeviceModel;
        BusProvider.dataSetChanged();
    }

    public boolean isToggleEnabled() {
        return toggleEnabled;
    }

    public void setToggleEnabled(boolean toggleEnabled) {
        this.toggleEnabled = toggleEnabled;
    }

    public boolean isToggleOn() {
        return toggleOn;
    }

    public void setToggleOn(boolean toggleOn) {
        this.toggleOn = toggleOn;
    }

    public void setShowCareOnOff(boolean show) {
        showCareOnOff = show;
    }

    public boolean getShowCareOnOff() {
        return showCareOnOff;
    }

    public void setCareToggleListener(View.OnClickListener listener) {
        this.careToggleListener = listener;
    }

    public View.OnClickListener getCareToggleListener() {
        return careToggleListener;
    }

    public @Nullable @DrawableRes Integer getDeviceImage() {
        return deviceImage;
    }

    public void setDeviceImage(@Nullable @DrawableRes Integer deviceImage) {
        this.deviceImage = deviceImage;
    }

    public AlarmType getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(@NonNull AlarmType alarmType) {
        this.alarmType = alarmType;
    }

    public boolean isCareCard() {
        return isCareCard;
    }

    public void setCareCard(boolean careCard) {
        isCareCard = careCard;
    }

    public void setTotalDevices (int devices){
        this.totalDevices = devices;
    }

    public int getTotalDevices (){
        return totalDevices;
    }
}
