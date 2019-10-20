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
package arcus.app.seasonal.christmas.cards;

import android.content.Context;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.seasonal.christmas.util.ChristmasModelUtils;
import arcus.app.seasonal.christmas.util.SantaEventTiming;

public class SantaCard extends SimpleDividerCard {
    private String cardDescription;

    public SantaCard(Context context) {
        super(context);
        updateCardDescription();
    }

    public void updateCardDescription () {
        if (ChristmasModelUtils.modelCacheExists()) {
            if (SantaEventTiming.instance().hasSantaVisited()) {
                cardDescription = getString(R.string.santa_sighting_confirmed_no_photo);
            }
            else {
                cardDescription = getString(R.string.santa_configured);
            }
        }

        else {
            cardDescription = getString(R.string.santa_card_default_title);
        }
    }

    public String getCardDescription() {
        return cardDescription;
    }

    @Override
    public int getLayout() {
        return R.layout.santa_card;
    }
}
