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
package arcus.app.subsystems.scenes.editor.model;

import android.view.View;

import java.lang.ref.WeakReference;

public class SceneListItemModel {
    private String title;
    private String subText;
    private String imageURL;
    private int    imageResource;
    private String modelAddressForImage;
    private boolean isChecked;
    private boolean isSeconds;

    private String rightText;

    private String addressAssociatedTo;

    private String  sceneSelectorName;
    private Object  sceneSelectorValue;

    // TODO Should be lists.
    private String  rightSideSelectionName;
    private Object  rightSideSelectionValue;

    private WeakReference<View.OnClickListener> onClickListenerRef;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubText() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getSceneSelectorName() {
        return sceneSelectorName;
    }

    public void setSceneSelectorName(String sceneSelectorName) {
        this.sceneSelectorName = sceneSelectorName;
    }

    public Object getSceneSelectorValue() {
        return sceneSelectorValue;
    }

    public void setSceneSelectorValue(Object sceneSelectorValue) {
        this.sceneSelectorValue = sceneSelectorValue;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getAddressAssociatedTo() {
        return addressAssociatedTo;
    }

    public void setAddressAssociatedTo(String addressAssociatedTo) {
        this.addressAssociatedTo = addressAssociatedTo;
    }

    public String getModelAddressForImage() {
        return modelAddressForImage;
    }

    public void setModelAddressForImage(String modelAddressForImage) {
        this.modelAddressForImage = modelAddressForImage;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public View.OnClickListener getOnClickListener() {
        if (onClickListenerRef == null) {
            onClickListenerRef = new WeakReference<>(null);
        }

        return onClickListenerRef.get();
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListenerRef = new WeakReference<>(onClickListener);
    }

    public Object getRightSideSelectionValue() {
        return rightSideSelectionValue;
    }

    public void setRightSideSelectionValue(Object rightSideSelectionValue) {
        this.rightSideSelectionValue = rightSideSelectionValue;
    }

    public String getRightSideSelectionName() {
        return rightSideSelectionName;
    }

    public void setRightSideSelectionName(String rightSideSelectionName) {
        this.rightSideSelectionName = rightSideSelectionName;
    }

    public boolean isSeconds() {
        return isSeconds;
    }

    public void setIsSeconds(boolean isSeconds) {
        this.isSeconds = isSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SceneListItemModel that = (SceneListItemModel) o;

        if (addressAssociatedTo != null ? !addressAssociatedTo.equals(that.addressAssociatedTo) : that.addressAssociatedTo != null) {
            return false;
        }
        if (sceneSelectorName != null ? !sceneSelectorName.equals(that.sceneSelectorName) : that.sceneSelectorName != null) {
            return false;
        }
        return !(rightSideSelectionName != null ? !rightSideSelectionName.equals(that.rightSideSelectionName) : that.rightSideSelectionName != null);

    }

    @Override
    public int hashCode() {
        int result = addressAssociatedTo != null ? addressAssociatedTo.hashCode() : 0;
        result = 31 * result + (sceneSelectorName != null ? sceneSelectorName.hashCode() : 0);
        result = 31 * result + (rightSideSelectionName != null ? rightSideSelectionName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SceneListItemModel{" +
              "title='" + title + '\'' +
              ", subText='" + subText + '\'' +
              ", imageURL='" + imageURL + '\'' +
              ", imageResource=" + imageResource +
              ", modelAddressForImage='" + modelAddressForImage + '\'' +
              ", isChecked=" + isChecked +
              ", isSeconds=" + isSeconds +
              ", rightText='" + rightText + '\'' +
              ", addressAssociatedTo='" + addressAssociatedTo + '\'' +
              ", sceneSelectorName='" + sceneSelectorName + '\'' +
              ", sceneSelectorValue=" + sceneSelectorValue +
              ", rightSideSelectionName='" + rightSideSelectionName + '\'' +
              ", rightSideSelectionValue=" + rightSideSelectionValue +
              ", onClickListenerRef=" + onClickListenerRef +
              '}';
    }
}
