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
package arcus.app.subsystems.water.cards;

import android.content.Context;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;


public class WaterCard extends SimpleDividerCard {

    public final static String TAG = ServiceCard.WATER.toString();

    private String displayPrimary;
    private String displaySecondary;
    private int imageValue;

    public WaterCard(Context context) {
        super(context);
        super.setTag(TAG);
        showDivider();
    }

    public WaterCard(Context context, String title,  String desc) {
        super(context);
        super.setTag(TAG);
        showDivider();
        displayPrimary = title;
        displaySecondary = desc;
    }

    @Override
    public int getLayout() {
        return R.layout.card_water;
    }

    public String getDisplayPrimary() {
        return displayPrimary;
    }

    public void setDisplayPrimary(String displayPrimary) {
        this.displayPrimary = displayPrimary;
    }

    public String getDisplaySecondary() {
        return displaySecondary;
    }

    public void setDisplaySecondary(String displaySecondary) {
        this.displaySecondary = displaySecondary;
    }

    public int getImageValue() {
        return imageValue;
    }

    public void setImageValue(int imageValue) {
        this.imageValue = imageValue;
    }
}
