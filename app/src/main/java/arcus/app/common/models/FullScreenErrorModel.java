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

public class FullScreenErrorModel implements Parcelable {

    private String text;

    public FullScreenErrorModel(String mainText) {
        this.text = mainText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FullScreenErrorModel that = (FullScreenErrorModel) o;

        return !(text != null ? !text.equals(that.text) : that.text != null);

    }

    @Override public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }

    @Override public String toString() {
        return "WaterHeaterErrorModel{" +
              "text='" + text + '\'' +
              '}';
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.text);
    }

    protected FullScreenErrorModel(Parcel in) {
        this.text = in.readString();
    }

    public static final Parcelable.Creator<FullScreenErrorModel> CREATOR =
          new Parcelable.Creator<FullScreenErrorModel>() {
              @Override
              public FullScreenErrorModel createFromParcel(Parcel source) {
                  return new FullScreenErrorModel(source);
              }

              @Override
              public FullScreenErrorModel[] newArray(int size) {
                  return new FullScreenErrorModel[size];
              }
          };
}
