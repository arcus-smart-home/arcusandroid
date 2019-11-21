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
package arcus.app.subsystems.learnmore.cards;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.controller.SubscriptionController;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.dashboard.settings.services.ServiceCard;


public class LearnMoreCard extends SimpleDividerCard {

    @NonNull
    private final String serviceTitle;
    @NonNull
    private final String serviceDescription;
    private final int serviceIconId;
    private final CardType serviceType;
    private final ServiceCard serviceCard;

    public LearnMoreCard(@NonNull Context context, @NonNull ServiceCard serviceCard) {
        super(context);

        this.serviceCard = serviceCard;
        this.serviceTitle = context.getResources().getString(serviceCard.getTitleStringResId());
        this.serviceDescription = context.getResources().getString(serviceCard.getDescriptionStringResId());
        this.serviceIconId= serviceCard.getSmallIconDrawableResId();

        setTag(serviceCard.toString());
        showDivider();

        String temp = serviceTitle.toLowerCase();
        if(temp.contains("care")) serviceType=CardType.CARE;
        else if(temp.contains("lights")) serviceType=CardType.LIGHTS;
        else if(temp.contains("security")) serviceType=CardType.SECURITY;
        else if(temp.contains("climate")) serviceType=CardType.CLIMATE;
        else if(temp.contains("doors")) serviceType=CardType.DOORS;
        else if(temp.contains("home")) serviceType=CardType.HOME;
        else if(temp.contains("safety")) serviceType=CardType.SAFETY;
        else if(temp.contains("cameras"))serviceType=CardType.CAMERAS;
        else if(temp.contains("windows"))serviceType=CardType.WINDOWS;
        else if(temp.contains("lawn"))serviceType=CardType.LAWN;
        else if(temp.contains("water")) serviceType=CardType.WATER;
        else if(temp.contains("enegry"))serviceType=CardType.ENERGY;
        else serviceType=CardType.NULL;

    }

    public int getLayout() {
        return R.layout.card_learn_more;
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
    public CardType getServiceType() {
        return this.serviceType;
    }
    public ServiceCard getServiceCard() {
        return this.serviceCard;
    }
    public boolean isPro() {
        return this.serviceCard == ServiceCard.SECURITY_ALARM && SubscriptionController.isProfessional();
    }

}
