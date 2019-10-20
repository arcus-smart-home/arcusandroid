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


public class ListHeadingCard extends SimpleDividerCard {

    private String leftText;
    private String rightText;

    public ListHeadingCard(Context context) {
        super(context);
    }

    public ListHeadingCard(Context context, String leftText, String rightText) {
        super(context);
        this.leftText = leftText;
        this.rightText = rightText;
    }

    @Override
    public int getLayout() {
        return R.layout.card_list_heading;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }
}
