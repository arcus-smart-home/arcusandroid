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

import java.io.Serializable;

public class PlaybackModel implements Serializable {
    public enum PlaybackType {
        STREAM,
        RECORDING,
        CLIP
    }

    private String deviceAddress;
    private String url;
    private String recordingID;
    private boolean isStreaming = true;
    private boolean isNewStream = true;
    private boolean isClip = false;
    private PlaybackType type = PlaybackType.STREAM;

    public String getRecordingID() {
        return recordingID;
    }

    public void setRecordingID(String recordingID) {
        this.recordingID = recordingID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isStreaming() {
        return isStreaming;
    }

    public void setIsStreaming(boolean isStreaming) {
        if (!isStreaming) type = PlaybackType.RECORDING;

        this.isStreaming = isStreaming;
    }

    public boolean isNewStream() {
        return isNewStream;
    }

    public void setIsNewStream(boolean isNewStream) {
        this.isNewStream = isNewStream;
    }

    public boolean isClip() {
        return isClip;
    }

    public void setIsClip(boolean isClip) {
        if (isClip) type = PlaybackType.CLIP;

        this.isClip = isClip;
    }

    public PlaybackType getType() {
        return type;
    }

    public void setType(PlaybackType type) {
        this.type = type;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlaybackModel that = (PlaybackModel) o;

        if (deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null)
            return false;
        return recordingID != null ? recordingID.equals(that.recordingID) : that.recordingID == null;
    }

    @Override
    public int hashCode() {
        int result = deviceAddress != null ? deviceAddress.hashCode() : 0;
        result = 31 * result + (recordingID != null ? recordingID.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PlaybackModel{" +
                "deviceAddress='" + deviceAddress + '\'' +
                ", url='" + url + '\'' +
                ", recordingID='" + recordingID + '\'' +
                ", isStreaming=" + isStreaming +
                ", isNewStream=" + isNewStream +
                ", isClip=" + isClip +
                ", type=" + type +
                '}';
    }
}
