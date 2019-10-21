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
package arcus.app.subsystems.camera.cards;

import android.content.Context;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;

import java.io.File;


public class DeviceCard extends SimpleDividerCard {
    public final static String TAG = "CAMERA DEVICE CARD";
    private OnClickListener mListener;
    private File cacheFile;
    private boolean isFirmwareUpdating;
    private boolean isOffline;
    private boolean isUnavailable;
    private boolean mHideButtons = false;
    private boolean isRecording;
    private boolean recordable;

    public DeviceCard(Context context) {
        super(context);
        super.setTag(TAG);
    }

    @Override
    public int getLayout() {
        return R.layout.card_camera_device;
    }

    public OnClickListener getOnClickListener() {
        return mListener;
    }

    public boolean shouldHideButtons() {
        return mHideButtons;
    }

    public void setHideButtons(boolean shouldHideButtons) {
        this.mHideButtons = shouldHideButtons;
    }

    public void setOnClickListener(OnClickListener listener) {
        this.mListener = listener;
    }

    public File getCacheFile() {
        return cacheFile;
    }

    public void setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setIsOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public boolean isFirmwareUpdating() {
        return isFirmwareUpdating;
    }

    public void setFirmwareUpdating(boolean firmwareUpdating) {
        isFirmwareUpdating = firmwareUpdating;
    }

    public boolean isUnavailable() {
        return isUnavailable;
    }

    public void setUnavailable(boolean isUnavailable) {
        this.isUnavailable = isUnavailable;
    }

    public boolean isRecording() {
        return isRecording;
    }

    public void setRecording(boolean recording) {
        isRecording = recording;
    }

    public boolean isRecordable() {
        return recordable;
    }

    public void setRecordable(boolean recordable) {
        this.recordable = recordable;
    }

    public interface OnClickListener {
        void onPlay();
        void onRecord();
        void onStream();
    }
}
