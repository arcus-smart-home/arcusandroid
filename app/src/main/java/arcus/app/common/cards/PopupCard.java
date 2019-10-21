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
import android.view.View;

import arcus.app.R;


public class PopupCard extends SimpleDividerCard {
    private String rightText;
    private ClickListener listener;
    private boolean darkColorScheme = false;

    public PopupCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.card_popup;
    }

    public void setClickListener(ClickListener onClickListener) {
        this.listener = onClickListener;
    }

    public ClickListener getClickListener() {
        return this.listener;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public String getRightText() {
        return this.rightText;
    }

    public boolean isDarkColorScheme() {
        return darkColorScheme;
    }

    public void setDarkColorScheme(boolean darkColorScheme) {
        this.darkColorScheme = darkColorScheme;
    }

    public interface ClickListener {
        void cardClicked(View view);
    }
}