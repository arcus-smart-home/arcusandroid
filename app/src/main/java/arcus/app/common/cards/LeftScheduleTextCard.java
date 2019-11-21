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
package arcus.app.common.cards;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;

public class LeftScheduleTextCard extends SimpleDividerCard {

    boolean mChevronShown = false;
    boolean mRadioChecked = false;
    boolean mSchedIconShown = false;

    int mDrawableResource = -1;
    boolean mDrawableInverted = false;

    private String rightText;
    private String modelID;
    @NonNull
    private ImageDisplayType imageDisplayType = ImageDisplayType.NONE;

    public enum ImageDisplayType {
        NONE,
        PERSON,
        DEVICE,
        PLACE,
        RESOURCE
    }

    public LeftScheduleTextCard(Context context) {
        super(context);
    }

    public boolean isSchedIconShown() {
        return mSchedIconShown;
    }

    public void setSchedIconShown(boolean schedIconShown) {
        mSchedIconShown = schedIconShown;
    }

    public void setChevronShown(boolean chevronShown) {
        mChevronShown = chevronShown;
    }

    public boolean isRadioChecked() {
        return mRadioChecked;
    }

    public void setRadioChecked(boolean radioChecked) {
        mRadioChecked = radioChecked;
    }

    @Override
    public int getLayout() {
        return R.layout.card_left_water_text;
    }

    public void showChevron() {
        mChevronShown = true;
    }

    public Boolean isChevronShown() {
        return mChevronShown;
    }

    public void setDrawableResource(int resourceId) {
        imageDisplayType = ImageDisplayType.RESOURCE;
        mDrawableResource = resourceId;
    }





    public int getDrawableResource() {
        return mDrawableResource;
    }

    public void setDrawableInverted(boolean inverted) {
        mDrawableInverted = inverted;
    }

    public boolean isDrawableInverted() {
        return mDrawableInverted;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public void setDeviceID(String deviceID) {
        imageDisplayType = ImageDisplayType.DEVICE;
        this.modelID = deviceID;
    }

    public void setPersonID(String personID) {
        imageDisplayType = ImageDisplayType.PERSON;
        this.modelID = personID;
    }

    public void setPlaceID(String placeID) {
        imageDisplayType = ImageDisplayType.PLACE;
        this.modelID = placeID;
    }

    public String getRightText() {
        return this.rightText;
    }

    public String getModelID() {
        return this.modelID;
    }

    @NonNull
    public ImageDisplayType getImageDisplayType() {
        return imageDisplayType;
    }
}
