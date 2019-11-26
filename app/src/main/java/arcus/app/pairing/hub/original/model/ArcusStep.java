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
package arcus.app.pairing.hub.original.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

public class ArcusStep implements Parcelable{

    private Integer stepImageResource;
    private int currentStep;
    private String stepText;
    private String subStepText;
    private String productId;
    private String stepTitle;

    public ArcusStep() {}

    public ArcusStep(String productId, int currentStep, String stepTitle, String stepText, String subStepText) {
        this.productId = productId;
        this.currentStep = currentStep;
        this.stepTitle = stepTitle;
        this.stepText = stepText;
        this.subStepText = subStepText;
    }

    public ArcusStep(int stepImageResource, int currentStep, String stepTitle, String stepText, String subStepText) {
        this.stepImageResource = stepImageResource;
        this.currentStep = currentStep;
        this.stepTitle = stepTitle;
        this.stepText = stepText;
        this.subStepText = subStepText;
    }

    public String getProductId () {
        return productId;
    }

    public void setProductId (String productId) {
        this.productId = productId;
    }

    public Integer getStepImageResource() {
        return stepImageResource;
    }

    public void setStepImageResource(Integer stepImageResource) {
        this.stepImageResource = stepImageResource;
    }

    public String getStepTitle() {
        return stepTitle;
    }

    public void setStepTitle(String stepTitle) {
        this.stepTitle = stepTitle;
    }

    public String getStepText() {
        return stepText;
    }

    public void setStepText(String stepText) {
        this.stepText = stepText;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getSubStepText() {
        return subStepText;
    }

    public void setSubStepText(String subStepText) {
        this.subStepText = subStepText;
    }

    private ArcusStep(@NonNull Parcel in){
        stepImageResource = in.readInt();
        currentStep = in.readInt();
        stepText = in.readString();
        subStepText = in.readString();
        productId = in.readString();
        stepTitle = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {

        if (stepImageResource != null) {
            dest.writeInt(stepImageResource);
        }
        dest.writeInt(currentStep);
        dest.writeString(stepText);
        dest.writeString(subStepText);
        dest.writeString(productId);
        dest.writeString(stepTitle);
    }

    public static final Creator<ArcusStep> CREATOR = new Creator<ArcusStep>() {
        @NonNull
        public ArcusStep createFromParcel(@NonNull Parcel in) {
            return new ArcusStep(in);
        }

        @NonNull
        public ArcusStep[] newArray(int size) {
            return new ArcusStep[size];
        }
    };
}
