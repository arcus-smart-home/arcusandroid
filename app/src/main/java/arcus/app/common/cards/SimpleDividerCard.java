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

import com.dexafree.materialList.cards.SimpleCard;


public class SimpleDividerCard extends SimpleCard {

    int type = -1;
    @NonNull
    private Boolean mDividerShown = false;

    public SimpleDividerCard(Context context) {
        super(context);
        setTag(this.getClass().getSimpleName());
    }

    @Override
    public int getLayout() {
        return 0;
    }

    public void showDivider() {
        this.mDividerShown = true;
    }

    @NonNull
    public Boolean isDividerShown() {
        return mDividerShown;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
