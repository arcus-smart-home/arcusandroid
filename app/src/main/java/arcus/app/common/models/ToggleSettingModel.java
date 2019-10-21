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
package arcus.app.common.models;

import android.os.Parcel;
import android.os.Parcelable;

public class ToggleSettingModel implements Parcelable {
    private String title;
    private String subTitle;
    private boolean isOn = false;
    private int id = -1;

    public ToggleSettingModel(String title, String subTitle) {
        this(title, subTitle, false);
    }

    public ToggleSettingModel(String title, String subTitle, boolean isOn) {
        this(title, subTitle, isOn, -1);
    }

    public ToggleSettingModel(String title, String subTitle, boolean isOn, int id) {
        this.title = title;
        this.subTitle = subTitle;
        this.isOn = isOn;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ToggleSettingModel that = (ToggleSettingModel) o;
        if(!title.equals(that.title)) {
            return false;
        }
        return subTitle.equals(that.subTitle);

    }

    @Override public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (subTitle != null ? subTitle.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "WeatherAlertModel{" +
              "title='" + title + '\'' +
              ", subTitle=" + subTitle +
              ", isOn=" + isOn +
              ", id=" + id +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.title);
        dest.writeString(this.subTitle);
        dest.writeByte(this.isOn ? (byte) 1 : (byte) 0);
        dest.writeInt(this.id);
    }

    protected ToggleSettingModel(Parcel in) {
        this.title = in.readString();
        this.subTitle = in.readString();
        this.isOn = in.readByte() != 0;
        this.id = in.readInt();
    }

    public static final Creator<ToggleSettingModel> CREATOR = new Creator<ToggleSettingModel>() {
        @Override public ToggleSettingModel createFromParcel(Parcel source) {
            return new ToggleSettingModel(source);
        }

        @Override public ToggleSettingModel[] newArray(int size) {
            return new ToggleSettingModel[size];
        }
    };
}
