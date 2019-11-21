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
import androidx.annotation.Nullable;

import arcus.app.R;


public class TopImageCard extends SimpleDividerCard {
    String placeID;
    String placeName;
    boolean hideSettingsGear = false;

    public final static String TAG = "TOP_IMAGE";

    public TopImageCard(Context context) {
        super(context);
        setTag(TAG);
    }

    public String getPlaceID() {
        return placeID;
    }

    public void setPlaceID(String placeID) {
        this.placeID = placeID;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public @Nullable String getPlaceName() {
        return placeName;
    }

    public boolean isHideSettingsGear() {
        return hideSettingsGear;
    }

    public void setHideSettingsGear(boolean hideSettingsGear) {
        this.hideSettingsGear = hideSettingsGear;
    }

    @Override
    public int getLayout(){
        return R.layout.card_top_image;
    }
}
