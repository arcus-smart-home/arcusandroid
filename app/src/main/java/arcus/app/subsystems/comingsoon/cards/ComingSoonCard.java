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
package arcus.app.subsystems.comingsoon.cards;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;

public class ComingSoonCard extends SimpleDividerCard {


    @NonNull
    private final String serviceTitle;
    @NonNull
    private final String serviceDescription;
    private final int serviceIconId;

    public ComingSoonCard(@NonNull Context context, @NonNull ServiceCard serviceCard) {
        super(context);

        this.serviceTitle = context.getResources().getString(serviceCard.getTitleStringResId());
        this.serviceDescription = context.getResources().getString(serviceCard.getDescriptionStringResId());
        this.serviceIconId= serviceCard.getSmallIconDrawableResId();

        setTag(serviceCard.toString());
        showDivider();
    }

    public int getLayout() {
        return R.layout.card_coming_soon;
    }
    @NonNull
    public String getServiceTitle () {
        return this.serviceTitle;
    }
    @NonNull
    public String getServiceDescription () {
        return this.serviceDescription;
    }
    public int getServiceIconId () {
        return this.serviceIconId;
    }
}
