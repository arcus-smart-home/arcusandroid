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
package arcus.app.subsystems.favorites.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.SceneModel;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class FavoriteItemModel {
    private final String mTitle;
    @NonNull private final Model mModel;
    @Nullable private Integer mImageId;
    private boolean disabled = false;
    private boolean keepimagecolor = false;

    public FavoriteItemModel(@NonNull DeviceModel model) {
        mTitle = model.getName();
        mModel = model;
        mImageId = null;
    }

    public FavoriteItemModel(@NonNull SceneModel model) {
        mTitle = model.getName();
        mModel = model;
        mImageId = SceneCategory.fromSceneModel(model).getIconResId();
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getTitle() {
        return mTitle;
    }

    @NonNull
    public Model getModel() {
        return mModel;
    }

    public Integer getImageResource() {
        return mImageId;
    }

    public void setImageResource(int imageId) {
        mImageId = imageId;
    }

    public boolean getKeepImageColor() {
        return keepimagecolor;
    }

    public void setKeepImageColor(boolean keepimagecolor) {
        this.keepimagecolor = keepimagecolor;
    }
    @Override
    public boolean equals(@Nullable Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
