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
import android.widget.ToggleButton;

import arcus.app.R;


public class BinarySwitchCard extends SimpleDividerCard {
    private ClickListener listener;
    private boolean toggleInitialPosition = false;

    public BinarySwitchCard(Context context) {
        super(context);
    }

    @Override
    public int getLayout() {
        return R.layout.card_binary_switch;
    }

    public void setClickListener(ClickListener onClickListener) {
        this.listener = onClickListener;
    }

    public ClickListener getClickListener() {
        return this.listener;
    }

    public void setToggleChecked(boolean checked) {
        this.toggleInitialPosition = checked;
    }

    public boolean getToggleChecked() {
        return this.toggleInitialPosition;
    }

    public interface ClickListener {
        void onToggleChanged(ToggleButton button);
    }
}
