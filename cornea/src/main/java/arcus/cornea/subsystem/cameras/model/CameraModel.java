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
package arcus.cornea.subsystem.cameras.model;

import arcus.cornea.subsystem.cameras.CameraPreviewGetter;
import com.iris.client.capability.CameraStatus;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.WiFi;
import com.iris.client.model.DeviceModel;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CameraModel {
    private static final long TWENTY_MINUTES_MS = TimeUnit.MINUTES.toMillis(20);
    private String cameraName;
    private String cameraID;
    private File previewCacheFile;
    private Date lastPreviewUpdate;
    private CameraState cameraState = CameraState.IDLE;
    private final boolean recordable = true;
    private final boolean shouldShowFPS = true;
    private final int signalLevel;

    private boolean online;
    private boolean isUpgradingFirmware;

    public enum CameraState {
        STREAMING,
        RECORDING,
        IDLE,
        UNAVAILABLE
    }

    public CameraModel(DeviceModel model, boolean onCellular) {
        this.cameraID = model.getId();
        this.cameraName = model.getName();
        this.online = !DeviceConnection.STATE_OFFLINE.equals(model.get(DeviceConnection.ATTR_STATE));
        this.isUpgradingFirmware = DeviceOta.STATUS_INPROGRESS.equals(model.get(DeviceOta.ATTR_STATUS));
        this.previewCacheFile = CameraPreviewGetter.instance().getForID(model.getId());
        if (this.previewCacheFile != null) {
            try {
                this.lastPreviewUpdate = new Date(previewCacheFile.lastModified());
            } catch (Exception ignored) { // Occasionally get a readDirectory() failed errno=20 from this.
                this.lastPreviewUpdate = new Date();
            }
        }

        if (onCellular) {
            this.cameraState = CameraState.UNAVAILABLE;
        }

        Collection<String> caps = model.getCaps();

        if (caps == null || !caps.contains(WiFi.NAMESPACE)) {
            signalLevel = 1;
        } else {
            // RSSI from the platform seems to go 0-100 whereas rssi is typically in the range of -100 -> -55
            WiFi wiFi = (WiFi) model;
            Integer rssi = wiFi.getRssi();

            if (rssi == null || rssi < 21) {
                signalLevel = 0;
            } else if (rssi < 41) {
                signalLevel = 1;
            } else if (rssi < 61) {
                signalLevel = 2;
            } else if (rssi < 81) {
                signalLevel = 3;
            } else {
                signalLevel = 4;
            }
        }
    }

    public boolean isUpgradingFirmware() {
        return isUpgradingFirmware;
    }

    public boolean isUnavailable() {
        return CameraState.UNAVAILABLE.equals(cameraState);
    }

    public boolean isRecording() {
        return CameraState.RECORDING.equals(cameraState);
    }

    public boolean isStreaming() {
        return CameraState.STREAMING.equals(cameraState);
    }

    public boolean isIdle() {
        return CameraState.IDLE.equals(cameraState);
    }

    public String getCameraName() {
        return cameraName;
    }

    public boolean isOnline() {
        return online;
    }

    public CameraState getCameraState() {
        return cameraState;
    }

    public String getCameraID() {
        return cameraID;
    }

    public File getPreviewCacheFile() {
        return previewCacheFile;
    }

    public Date getLastPreviewUpdate() {
        return this.lastPreviewUpdate;
    }

    public void setCameraState(String cameraState) {
        if(CameraStatus.STATE_RECORDING.equals(cameraState)) {
            this.cameraState = CameraState.RECORDING;
        } else if(CameraStatus.STATE_STREAMING.equals(cameraState)) {
            this.cameraState = CameraState.STREAMING;
        } else {
            this.cameraState = CameraState.IDLE;
        }
    }

    public static boolean isLessThanTwentyMinutesOld(Date date) {
        return date != null && (date.getTime() + TWENTY_MINUTES_MS) >= System.currentTimeMillis();
    }

    public boolean isRecordable() {
        return recordable;
    }

    public boolean shouldShowFPS() {
        return shouldShowFPS;
    }

    public int getSignalLevel() {
        return signalLevel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraModel that = (CameraModel) o;
        return recordable == that.recordable &&
                online == that.online &&
                isUpgradingFirmware == that.isUpgradingFirmware &&
                shouldShowFPS == that.shouldShowFPS &&
                signalLevel == that.signalLevel &&
                Objects.equals(cameraName, that.cameraName) &&
                Objects.equals(cameraID, that.cameraID) &&
                Objects.equals(previewCacheFile, that.previewCacheFile) &&
                Objects.equals(lastPreviewUpdate, that.lastPreviewUpdate) &&
                cameraState == that.cameraState;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                cameraName,
                cameraID,
                previewCacheFile,
                lastPreviewUpdate,
                cameraState,
                recordable,
                online,
                isUpgradingFirmware,
                shouldShowFPS,
                signalLevel
        );
    }

    @Override
    public String toString() {
        return "CameraModel{" +
                "cameraName='" + cameraName + '\'' +
                ", cameraID='" + cameraID + '\'' +
                ", previewCacheFile=" + previewCacheFile +
                ", lastPreviewUpdate=" + lastPreviewUpdate +
                ", cameraState=" + cameraState +
                ", recordable=" + recordable +
                ", online=" + online +
                ", isUpgradingFirmware=" + isUpgradingFirmware +
                ", shouldShowFPS=" + shouldShowFPS +
                ", signalLevel=" + signalLevel +
                '}';
    }
}
