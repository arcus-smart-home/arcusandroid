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

import com.dexafree.materialList.events.BusProvider;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;


public class SpaceHeaterDeviceControlCard extends SimpleDividerCard {

    public final static String TAG = "DEVICE CONTROL CARD";

    private int mLeftImageResource;
    private int mRightImageResource;
    private int mTopImageResource;
    private int mBottomImageResource;

    private OnClickListener mListener;

    private String mTopImageText;
    private String mBottomImageText;

    private String mDeviceId;

    private boolean mIsLeftButtonEnabled = true;
    private boolean mIsRightButtonEnabled = true;
    private boolean mIsTopButtonEnabled = true;
    private boolean mIsBottomButtonEnabled = true;

    private boolean mBevelVisible = true;

    private boolean mOffline;
    private boolean useSpecifiedTopImage = false;

    private boolean isInOta;
    private boolean deviceModeOn = false;
    private boolean deviceEcoOn = false;

    private int setPoint = 0;
    private int currentTemp = 0;

    public SpaceHeaterDeviceControlCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    public void setUseSpecifiedTopImage(boolean useSpecifiedTopImage) {
        this.useSpecifiedTopImage = useSpecifiedTopImage;
    }

    public boolean getUseSpecifiedTopImage() {
        return useSpecifiedTopImage;
    }

    @Override
    public int getLayout() {
        return R.layout.card_spaceheater_device_control;
    }

    public int getLeftImageResource() {
        return mLeftImageResource;
    }

    public void setLeftImageResource(int leftImageResource) {
        this.mLeftImageResource = leftImageResource;
    }

    public int getRightImageResource() {
        return mRightImageResource;
    }

    public void setRightImageResource(int rightImageResource) {
        this.mRightImageResource = rightImageResource;
    }

    public int getTopImageResource() {
        return mTopImageResource;
    }

    public void setTopImageResource(int topImageResource) {
        this.mTopImageResource = topImageResource;
    }

    public int getBottomImageResource() {
        return mBottomImageResource;
    }

    public void setBottomImageResource(int bottomImageResource) {
        this.mBottomImageResource = bottomImageResource;
    }

    public OnClickListener getCallbackListener() {
        return mListener;
    }

    public void setCallback(OnClickListener listener) {
        this.mListener = listener;
    }

    public String getTopImageText() {
        return mTopImageText;
    }

    public void setTopImageText(String topImageText) {
        this.mTopImageText = topImageText;
    }

    public String getBottomImageText() {
        return mBottomImageText;
    }

    public void setBottomImageText(String bottomImageText) {
        this.mBottomImageText = bottomImageText;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    public boolean isLeftButtonEnabled() {
        return mIsLeftButtonEnabled;
    }

    public void setLeftButtonEnabled(boolean isLeftButtonEnabled) {
        this.mIsLeftButtonEnabled = isLeftButtonEnabled;
        BusProvider.dataSetChanged();
    }

    public boolean isRightButtonEnabled() {
        return mIsRightButtonEnabled;
    }

    public void setRightButtonEnabled(boolean isRightButtonEnabled) {
        this.mIsRightButtonEnabled = isRightButtonEnabled;
        BusProvider.dataSetChanged();
    }

    public boolean isTopButtonEnabled() {
        return mIsTopButtonEnabled;
    }

    public void setTopButtonEnabled(boolean isTopButtonEnabled) {
        this.mIsTopButtonEnabled = isTopButtonEnabled;
        BusProvider.dataSetChanged();
    }

    public boolean isBottomButtonEnabled() {
        return mIsBottomButtonEnabled;
    }

    public void setBottomButtonEnabled(boolean isBottomButtonEnabled) {
        this.mIsBottomButtonEnabled = isBottomButtonEnabled;
        BusProvider.dataSetChanged();
    }

    public void setBevelVisible (boolean isVisible) {
        mBevelVisible = isVisible;
    }

    public boolean isBevelVisible () {
        return mBevelVisible;
    }

    public boolean isOffline() {
        return mOffline;
    }

    public void setOffline(boolean offline) {
        this.mOffline = offline;
        if (offline) {
            this.setLeftButtonEnabled(false);
            this.setRightButtonEnabled(false);
            this.setDescription(getString(R.string.offline));
        } else {
            this.setLeftButtonEnabled(true);
            this.setRightButtonEnabled(true);
        }
    }

    public boolean isInOta() {
        return isInOta;
    }

    public void setIsInOta(boolean isInOta) {
        this.isInOta = isInOta;
        if (isInOta) {
            this.setLeftButtonEnabled(false);
            this.setRightButtonEnabled(false);
            this.setDescription(getString(R.string.ota_firmware_update));
        } else {
            this.setLeftButtonEnabled(true);
            this.setRightButtonEnabled(true);
        }
    }

    public boolean isDeviceModeOn() {
        return deviceModeOn;
    }

    public void setDeviceModeOn(boolean deviceModeOn) {
        this.deviceModeOn = deviceModeOn;
    }

    public boolean isDeviceEcoOn() {
        return deviceEcoOn;
    }

    public void setDeviceEcoOn(boolean deviceEcoOn) {
        this.deviceEcoOn = deviceEcoOn;
    }

    public int getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(int setPoint) {
        this.setPoint = setPoint;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(int currentTemp) {
        this.currentTemp = currentTemp;
    }

    public interface OnClickListener {
        void onLeftButtonClicked();
        void onRightButtonClicked();
        void onTopButtonClicked();
        void onBottomButtonClicked();
        void onCardClicked();
    }
}
