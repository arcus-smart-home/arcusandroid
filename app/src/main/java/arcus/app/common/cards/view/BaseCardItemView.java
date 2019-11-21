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
package arcus.app.common.cards.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.dexafree.materialList.cards.BasicCard;
import com.dexafree.materialList.model.CardItemView;

public abstract class BaseCardItemView<T extends BasicCard> extends CardItemView<T> {
    public BaseCardItemView(Context context) {
        super(context);
    }

    public BaseCardItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @TargetApi(11)
    public BaseCardItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public float dpToPx(int dp) {
        DisplayMetrics displayMetrics = this.getContext().getResources().getDisplayMetrics();
        return (float)Math.round((float)dp * (displayMetrics.xdpi / 160.0F));
    }

    public float spToPx(int sp) {
        DisplayMetrics displayMetrics = this.getContext().getResources().getDisplayMetrics();
        return (float)Math.round((float)sp * (displayMetrics.scaledDensity / 160.0F));
    }
}
