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

import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import arcus.cornea.common.ViewRenderType;

public class ClipModel {
    private final String recordingId;
    private String cameraName = "Unknown";
    private String durationString = "Unknown";
    private String sizeInBytesString = "Unknown";
    private String timeString = "Unknown";
    private String actorName = "Manual Recording";
    private File   cachedClipFile;
    private boolean downloadDeleteAvailable = true;
    private boolean pinned = false;
    private int type = ViewRenderType.CLIP_VIEW;
    private ArrayList<String> tags = new ArrayList<>();
    private Date recordingTime;
    private Date deleteTime;

    public static ClipModel asHeader(String timeString) {
        ClipModel clipModel  = new ClipModel(timeString);
        clipModel.timeString = timeString;
        clipModel.type = ViewRenderType.HEADER_VIEW;

        return clipModel;
    }

    public ClipModel(@NonNull int type) {
        this.type = type;
        this.recordingId = "";
    }

    public ClipModel(@NonNull String recordingId) {
        this.recordingId = recordingId;
    }

    public ClipModel(@NonNull String recordingId, File cachedClipFile) {
        this.recordingId = recordingId;
        this.cachedClipFile = cachedClipFile;
    }

    public @NonNull String getRecordingId() {
        return recordingId;
    }

    public void setDurationString(String durationString) {
        if (durationString != null) {
            this.durationString = durationString;
        }
    }

    public String getDurationString() {
        return durationString;
    }

    public String getSizeString() {
        return sizeInBytesString;
    }

    public void setSizeInBytesString(String sizeInBytes) {
        if (sizeInBytes != null) {
            this.sizeInBytesString = sizeInBytes;
        }
    }

    public void setTimeString(String timeString) {
        if (timeString != null) {
            this.timeString = timeString;
        }
    }

    public String getTimeString() {
        return timeString;
    }

    public void setTime(Date time) {
        this.recordingTime = time;
    }

    public Date getTime() {
        return recordingTime;
    }

    public void setDeleteTime(Date deleteTime) { this.deleteTime = deleteTime; }

    public Date getDeleteTime() { return deleteTime; }

    public void setCameraName(String cameraName) { this.cameraName = cameraName; }

    public String getCameraName() { return this.cameraName; }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        if (actorName != null) {
            this.actorName = actorName;
        }
    }

    public void addTag(String tag) {
        for(String iter : tags) {
            if(iter.equals(tag)) {
                return;
            }
        }
        tags.add(tag);
    }

    public void removeTag(String tag) {
        for(String iter : tags) {
            if(iter.equals(tag)) {
                tags.remove(iter);
                return;
            }
        }
    }

    public void setTags(ArrayList<String> tags) {
        tags = tags;
    }

    public ArrayList<String> getTags() {
        return tags;
    }

    public boolean hasTag(String tag) {
        for(String iter : tags) {
            if(iter.equals(tag)) {
                return true;
            }
        }
        return false;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public boolean isDownloadDeleteAvailable() {
        return downloadDeleteAvailable;
    }

    public void setDownloadDeleteAvailable(boolean downloadDeleteAvailable) {
        this.downloadDeleteAvailable = downloadDeleteAvailable;
    }

    public File getCachedClipFile() {
        return cachedClipFile;
    }

    public void setCachedClipFile(File cachedClipFile) {
        this.cachedClipFile = cachedClipFile;
    }

    public int getType() {
        return type;
    }

    @SuppressWarnings("ConstantConditions") @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClipModel clipModel = (ClipModel) o;

        return recordingId != null ? recordingId.equals(clipModel.recordingId) : clipModel.recordingId == null;

    }

    @SuppressWarnings("ConstantConditions") @Override public int hashCode() {
        return recordingId != null ? recordingId.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ClipModel{" +
              "recordingId='" + recordingId + '\'' +
              ", sizeInBytesString=" + sizeInBytesString +
              ", actorName='" + actorName + '\'' +
              ", cachedClipFile=" + cachedClipFile +
              ", type=" + type +
              '}';
    }
}
