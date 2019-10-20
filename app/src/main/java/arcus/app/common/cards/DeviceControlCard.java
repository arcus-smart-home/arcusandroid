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
package arcus.app.common.cards;

import android.content.Context;

import com.dexafree.materialList.events.BusProvider;
import arcus.app.R;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.model.DeviceType;

import java.util.ArrayList;
import java.util.List;


public class DeviceControlCard extends SimpleDividerCard {

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
    private boolean mIsLeftButtonVisible = true;
    private boolean mIsRightButtonVisible = true;

    private boolean mShouldGlow;
    private GlowableImageView.GlowMode glowMode;
    private boolean mBevelVisible = true;

    private boolean mOffline;
    private boolean useSpecifiedTopImage = false;
    private boolean isCloudDevice = false;
    private boolean isHoneywellTcc = false;
    private boolean mRequiresLogin = false;
    private String mAuthorizationState;
    private boolean mIsEventInProcess = false;
    DeviceType deviceType = DeviceType.NOT_SUPPORTED;

    private List<String> errors = new ArrayList<String>();

    private CommandCommittedCallback callback;

    public DeviceControlCard(Context context) {
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
        return R.layout.card_device_control;
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

    public GlowableImageView.GlowMode getGlowMode() {
        return glowMode;
    }

    public void setGlowMode(GlowableImageView.GlowMode glowMode) {
        this.glowMode = glowMode;
        BusProvider.dataSetChanged();
    }

    public void setBevelVisible (boolean isVisible) {
        mBevelVisible = isVisible;
    }

    public boolean isBevelVisible () {
        return mBevelVisible;
    }

    public boolean shouldGlow() {
        return mShouldGlow;
    }

    public void setShouldGlow(boolean shouldGlow) {
        this.mShouldGlow = shouldGlow;
        BusProvider.dataSetChanged();
    }

    public boolean isOffline() {
        return mOffline;
    }

    public void setOffline(boolean offline) {
        this.mOffline = offline;
        if (offline) {
            this.setLeftButtonEnabled(false);
            this.setRightButtonEnabled(false);
            this.setDescription("Offline");
        } else {
            this.setLeftButtonEnabled(true);
            this.setRightButtonEnabled(true);
        }
    }

    public boolean isCloudDevice() {
        return isCloudDevice;
    }

    public void setCloudDevice(boolean isCloudDevice) {
        this.isCloudDevice = isCloudDevice;
    }

    public boolean isRequiresLogin() {
        return mRequiresLogin;
    }

    public void setRequiresLogin(boolean requiresLogin) {
        this.mRequiresLogin = requiresLogin;
    }

    public String getAuthorizationState() {
        return mAuthorizationState;
    }

    public void setAuthorizationState(String authorizationState) {
        this.mAuthorizationState = authorizationState;
    }

    public boolean isIsEventInProcess() {
        return mIsEventInProcess;
    }

    public void setIsEventInProcess(boolean mIsEventInProcess) {
        this.mIsEventInProcess = mIsEventInProcess;
    }

    public void setCommandCommitted() {
        if(callback != null) {
            callback.commandCommitted();
        }
    }

    public void setCommandCallback(CommandCommittedCallback callback) {
        this.callback = callback;
    }

    public boolean isLeftButtonVisible() {
        return mIsLeftButtonVisible;
    }

    public void setLeftButtonVisible(boolean isLeftButtonVisible) {
        this.mIsLeftButtonVisible = isLeftButtonVisible;
    }

    public boolean isRightButtonVisible() {
        return mIsRightButtonVisible;
    }

    public void setRightButtonVisible(boolean mIsRightButtonVisible) {
        this.mIsRightButtonVisible = mIsRightButtonVisible;
    }

    public boolean isHoneywellTcc() {
        return isHoneywellTcc;
    }

    public void setHoneywellTcc(boolean honeywellTcc) {
        isHoneywellTcc = honeywellTcc;
    }

    public interface CommandCommittedCallback {
        void commandCommitted();
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public List<String> getErrors() {
        return errors;
    }
    public void clearErrors() {
        errors.clear();
    }
    public void addError(String error) {
        errors.add(error);
    }

    public interface OnClickListener {
        void onLeftButtonClicked();
        void onRightButtonClicked();
        void onTopButtonClicked();
        void onBottomButtonClicked();
        void onCardClicked();
    }
}
