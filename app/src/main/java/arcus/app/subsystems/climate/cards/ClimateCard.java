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
package arcus.app.subsystems.climate.cards;

import android.content.Context;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;


public class ClimateCard extends SimpleDividerCard {

    public final static String TAG = ServiceCard.CLIMATE.toString();

    private String mTempTitle;
    private String mTempDescription;
    private String mHumidityDescription;
    private String mVentDescription;
    private String mFanDescription;
    private String mHeaterDescription;
    private boolean mIsPrimaryTemperatureOffline;
    private boolean mIsPrimaryTemperatureCloudDevice;
    private String mPrimaryTemperatureDeviceId;
    private boolean mClimateDevicesEnabled = true;

    public ClimateCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    @Override
    public int getLayout() {
        return R.layout.card_climate;
    }

    public String getTempTitle() {
        return mTempTitle;
    }

    public void setTempTitle(String tempTitle) {
        this.mTempTitle = tempTitle;
    }

    public String getTempDescription() {
        return mTempDescription;
    }

    public void setTempDescription(String tempDescription) {
        this.mTempDescription = tempDescription;
    }

    public String getHumidityDescription() {
        return mHumidityDescription;
    }

    public void setHumidityDescription(String humidityDescription) {
        this.mHumidityDescription = humidityDescription;
    }

    public String getVentDescription() {
        return mVentDescription;
    }

    public void setVentDescription(String ventDescription) {
        this.mVentDescription = ventDescription;
    }

    public String getFanDescription() {
        return mFanDescription;
    }

    public void setFanDescription(String fanDescription) {
        this.mFanDescription = fanDescription;
    }

    public String getHeaterDescription() {
        return mHeaterDescription;
    }

    public void setHeaterDescription(String heaterDescription) {
        this.mHeaterDescription = heaterDescription;
    }

    public boolean isPrimaryTemperatureOffline() {
        return mIsPrimaryTemperatureOffline;
    }

    public void setIsPrimaryTemperatureOffline(boolean mIsPrimaryTemperatureOffline) {
        this.mIsPrimaryTemperatureOffline = mIsPrimaryTemperatureOffline;
    }

    public boolean isPrimaryTemperatureCloudDevice() {
        return mIsPrimaryTemperatureCloudDevice;
    }

    public void setIsPrimaryTemperatureCloudDevice(boolean mIsPrimaryTemperatureCloudDevice) {
        this.mIsPrimaryTemperatureCloudDevice = mIsPrimaryTemperatureCloudDevice;
    }

    public String getPrimaryTemperatureDeviceId() {
        return mPrimaryTemperatureDeviceId;
    }

    public void setPrimaryTemperatureDeviceId(String primaryTemperatureDeviceId) {
        this.mPrimaryTemperatureDeviceId = primaryTemperatureDeviceId;
    }

    public void setClimateDevicesEnabled(Boolean b) {
        this.mClimateDevicesEnabled =  b;
    }


    public boolean isClimateDevicesEnabled() {
        return mClimateDevicesEnabled;
    }
}
