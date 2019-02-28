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

import arcus.app.R;


public class CenteredTextCard extends SimpleDividerCard {
    private OnClickCallaback callaback;
    private boolean transparentBackground = false;
    private int descriptionBackground = -1;

    public CenteredTextCard(Context context) {
        super(context);
    }

    public void useTransparentBackground(boolean transparentBackground) {
        this.transparentBackground = transparentBackground;
    }

    public boolean isTransparentBackground() {
        return this.transparentBackground;
    }

    public int getDescriptionBackground() {
        return descriptionBackground;
    }

    public void setDescriptionBackground(int descriptionBackground) {
        this.descriptionBackground = descriptionBackground;
    }

    public OnClickCallaback getCallaback() {
        return callaback;
    }

    public void setCallaback(OnClickCallaback callaback) {
        this.callaback = callaback;
    }

    @Override
    public int getLayout() {
        return R.layout.card_centered_text;
    }

    public interface OnClickCallaback {
        void onTitleClicked();
        void onDescriptionClicked();
    }
}
