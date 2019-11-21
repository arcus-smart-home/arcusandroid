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
package arcus.app.subsystems.alarm.promonitoring.cards;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableString;
import android.view.View;

import com.dexafree.materialList.events.BusProvider;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.subsystems.alarm.promonitoring.ProMonitoringUtil;
import arcus.app.subsystems.alarm.promonitoring.models.SecurityAlarmStatusModel;


public class ProMonitoringAlarmCard extends SimpleDividerCard {

    public enum AlarmState {
        ON,
        OFF,
        PARTIAL,
        ARMING,
        ALERT,
    }


    private SpannableString centerTopText;
    private SpannableString centerBottomText;
    private SecurityAlarmStatusModel.SecurityAlarmArmingState alarmState = SecurityAlarmStatusModel.SecurityAlarmArmingState.OFF;
    private int offlineDevices;
    private int bypassDevices;
    private int activeDevices;
    private @DrawableRes Integer alarmTypeResId;
    private boolean toggleOn;
    private boolean toggleEnabled;
    private ProMonitoringUtil.AlarmType alarmType = ProMonitoringUtil.AlarmType.SECURITY;
    private View.OnClickListener careToggleListener;
    private int totalDevices;
    private String subText;
    private boolean showDashedCircle = true;
    private int backgroundColor = android.R.color.transparent;

    public ProMonitoringAlarmCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout(){
        return R.layout.card_promon_top;
    }

    public void setAlarmState(SecurityAlarmStatusModel.SecurityAlarmArmingState state) {
        alarmState = state;
        BusProvider.dataSetChanged();
    }

    public SecurityAlarmStatusModel.SecurityAlarmArmingState getAlarmState() {
        return alarmState;
    }

    public SpannableString getCenterTopText() {
        return centerTopText;
    }

    public void setCenterTopText(SpannableString centerTopText) {
        this.centerTopText = centerTopText;
        BusProvider.dataSetChanged();
    }

    public SpannableString getCenterBottomText() {
        return centerBottomText;
    }

    public void setCenterBottomText(SpannableString centerBottomText) {
        this.centerBottomText = centerBottomText;
        BusProvider.dataSetChanged();
    }

    public int getOfflineDevices() {
        return offlineDevices;
    }

    public void setOfflineDevices(int offlineDevices) {
        this.offlineDevices = offlineDevices;
        BusProvider.dataSetChanged();
    }

    public int getBypassDevices() {
        return bypassDevices;
    }

    public void setBypassDevices(int bypassDevices) {
        this.bypassDevices = bypassDevices;
        BusProvider.dataSetChanged();
    }

    public int getActiveDevices() {
        return activeDevices;
    }

    public void setActiveDevices(int activeDevices) {
        this.activeDevices = activeDevices;
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

    public void setCareToggleListener(View.OnClickListener listener) {
        this.careToggleListener = listener;
    }

    public View.OnClickListener getCareToggleListener() {
        return careToggleListener;
    }

    public @Nullable @DrawableRes Integer getAlarmTypeResId() {
        return alarmTypeResId;
    }

    public void setAlarmTypeResId(@Nullable @DrawableRes Integer alarmTypeResId) {
        this.alarmTypeResId = alarmTypeResId;
    }

    public ProMonitoringUtil.AlarmType getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(@NonNull ProMonitoringUtil.AlarmType alarmType) {
        this.alarmType = alarmType;
    }

    public void setTotalDevices (int devices){
        this.totalDevices = devices;
    }

    public int getTotalDevices (){
        return totalDevices;
    }

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public boolean isShowDashedCircle() {
        return showDashedCircle;
    }

    public void setShowDashedCircle(boolean showDashedCircle) {
        this.showDashedCircle = showDashedCircle;
    }

    @Override
    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }
}
