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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import arcus.cornea.common.ViewRenderType;
import arcus.cornea.model.PlaceAndRoleModel;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;

public class ModelTypeListItem implements Parcelable {
    long viewID;
    @ViewRenderType
    int viewType;
    @ModelType String modelType;
    String text1, text2, text3, text4, modelID;
    PlaceAndRoleModel associatedPlaceModel;
    Parcelable additionalData;

    public ModelTypeListItem(@ViewRenderType int viewType, @NonNull @ModelType String modelType, long viewID) {
        this.viewType = viewType;
        this.modelType = TextUtils.isEmpty(modelType) ? ModelType.UNKNOWN_TYPE : modelType;
        this.viewID = viewID;
    }

    public @NonNull String getModelID() {
        return TextUtils.isEmpty(modelID) ? "" : modelID;
    }

    public void setModelID(String modelID) {
        this.modelID = modelID;
    }

    public @NonNull String getModelAddress() {
        switch (getModelType()) {
            case ModelType.PERSON_TYPE:
                return Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(getModelID()));

            case ModelType.PLACE_TYPE:
                return Addresses.toObjectAddress(Place.NAMESPACE, Addresses.getId(getModelID()));

            default:
                return getModelID();
        }
    }

    public @ViewRenderType int getViewType() {
        return viewType;
    }

    public @ModelType String getModelType() {
        return TextUtils.isEmpty(modelType) ? ModelType.UNKNOWN_TYPE : modelType;
    }

    public @NonNull String getText1() {
        return TextUtils.isEmpty(text1) ? "" : text1;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public @Nullable String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public @Nullable String getText3() {
        return text3;
    }

    public void setText3(String text3) {
        this.text3 = text3;
    }

    public @Nullable String getText4() {
        return text4;
    }

    public void setText4(String text4) {
        this.text4 = text4;
    }

    public long getViewID() {
        return viewID;
    }

    public boolean hasAdditionalData() {
        return additionalData != null;
    }

    public void setAdditionalData(Parcelable data) {
        additionalData = data;
    }

    public @Nullable Parcelable getAdditionalData() {
        return additionalData;
    }

    public PlaceAndRoleModel getAssociatedPlaceModel() {
        return associatedPlaceModel;
    }

    public void setAssociatedPlaceModel(PlaceAndRoleModel associatedPlaceModel) {
        this.associatedPlaceModel = associatedPlaceModel;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ModelTypeListItem that = (ModelTypeListItem) o;

        if (viewID != that.viewID) {
            return false;
        }
        if (viewType != that.viewType) {
            return false;
        }
        if (modelType != null ? !modelType.equals(that.modelType) : that.modelType != null) {
            return false;
        }
        if (text1 != null ? !text1.equals(that.text1) : that.text1 != null) {
            return false;
        }
        if (text2 != null ? !text2.equals(that.text2) : that.text2 != null) {
            return false;
        }
        if (text3 != null ? !text3.equals(that.text3) : that.text3 != null) {
            return false;
        }
        if (text4 != null ? !text4.equals(that.text4) : that.text4 != null) {
            return false;
        }
        if (modelID != null ? !modelID.equals(that.modelID) : that.modelID != null) {
            return false;
        }
        if (associatedPlaceModel != null ? !associatedPlaceModel.equals(that.associatedPlaceModel) : that.associatedPlaceModel != null) {
            return false;
        }
        return additionalData != null ? additionalData.equals(that.additionalData) : that.additionalData == null;

    }

    @Override public int hashCode() {
        int result = (int) (viewID ^ (viewID >>> 32));
        result = 31 * result + viewType;
        result = 31 * result + (modelType != null ? modelType.hashCode() : 0);
        result = 31 * result + (text1 != null ? text1.hashCode() : 0);
        result = 31 * result + (text2 != null ? text2.hashCode() : 0);
        result = 31 * result + (text3 != null ? text3.hashCode() : 0);
        result = 31 * result + (text4 != null ? text4.hashCode() : 0);
        result = 31 * result + (modelID != null ? modelID.hashCode() : 0);
        result = 31 * result + (associatedPlaceModel != null ? associatedPlaceModel.hashCode() : 0);
        result = 31 * result + (additionalData != null ? additionalData.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "ModelTypeListItem{" +
              "viewID=" + viewID +
              ", viewType=" + viewType +
              ", modelType='" + modelType + '\'' +
              ", text1='" + text1 + '\'' +
              ", text2='" + text2 + '\'' +
              ", text3='" + text3 + '\'' +
              ", text4='" + text4 + '\'' +
              ", modelID='" + modelID + '\'' +
              ", associatedPlaceModel=" + associatedPlaceModel +
              ", additionalData=" + additionalData +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.viewID);
        dest.writeInt(this.viewType);
        dest.writeString(this.modelType);
        dest.writeString(this.text1);
        dest.writeString(this.text2);
        dest.writeString(this.text3);
        dest.writeString(this.text4);
        dest.writeString(this.modelID);
        dest.writeParcelable(this.associatedPlaceModel, flags);
        dest.writeParcelable(this.additionalData, flags);
    }

    @SuppressWarnings("ResourceType") protected ModelTypeListItem(Parcel in) {
        this.viewID = in.readLong();
        this.viewType = in.readInt();
        this.modelType = in.readString();
        this.text1 = in.readString();
        this.text2 = in.readString();
        this.text3 = in.readString();
        this.text4 = in.readString();
        this.modelID = in.readString();
        this.associatedPlaceModel = in.readParcelable(PlaceAndRoleModel.class.getClassLoader());
        this.additionalData = in.readParcelable(Parcelable.class.getClassLoader());
    }

    public static final Creator<ModelTypeListItem> CREATOR = new Creator<ModelTypeListItem>() {
        @Override public ModelTypeListItem createFromParcel(Parcel source) {
            return new ModelTypeListItem(source);
        }

        @Override public ModelTypeListItem[] newArray(int size) {
            return new ModelTypeListItem[size];
        }
    };
}
