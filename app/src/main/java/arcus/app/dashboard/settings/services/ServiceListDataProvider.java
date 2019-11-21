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
package arcus.app.dashboard.settings.services;

import android.content.Context;
import androidx.annotation.NonNull;

import arcus.cornea.subsystem.SubsystemController;
import com.iris.client.capability.Alarm;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.model.SubsystemModel;
import arcus.app.R;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.dashboard.settings.model.DraggableListDataProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A list data provider representing the set of all possible service cards.
 */
public class ServiceListDataProvider implements DraggableListDataProvider {

    @NonNull
    private List<ServiceListItemModel> serviceModelList = new ArrayList<>();
    private final List<String> validNonSubsystemCards = Arrays.asList("FAVORITES", "HISTORY", "FEATURE", "TRACKER");
    private List<ServiceCard> cardOrder;
    private Set<ServiceCard> enabledServices;
    private Iterable<SubsystemModel> allSubsystems = SubsystemController.instance().getSubsystems().values();
    private List<SubsystemModel> activeSubsystems = new ArrayList<>();

    public ServiceListDataProvider(@NonNull Context context) {
        loadSavedConfiguration();
        auditFeatureCard();
        loadSubsystems();
        loadServiceModels(context);
    }

    private void loadSavedConfiguration() {
        cardOrder = PreferenceUtils.getOrderedServiceCardList();
        enabledServices = PreferenceUtils.getVisibleServiceSet();
    }

    private void auditFeatureCard() {
        if (!cardOrder.contains(ServiceCard.FEATURE)) {
            cardOrder.add(ServiceCard.FEATURE);
        }
    }

    private void loadSubsystems() {
        for (SubsystemModel subsystemModel : allSubsystems) {
            if (subsystemModel.getAvailable()) {
                if (!subsystemModel.getName().toUpperCase().contains("ALARM")) {
                    activeSubsystems.add(subsystemModel);
                }
                else {
                    if (!subsystemModel.get(AlarmSubsystem.ATTR_ALARMSTATE).equals(Alarm.ALERTSTATE_INACTIVE)) {
                        activeSubsystems.add(subsystemModel);
                    }
                }
            }
        }
    }

    private void loadServiceModels(Context context) {
        for (ServiceCard card : cardOrder) {

            // Retrieve the model for the current card
            String cardTitle = context.getResources().getString(card.getTitleStringResId());
            ServiceListItemModel cardModel = new ServiceListItemModel(cardTitle, card);

            // Synchronize the model's toggle with previously stored value if present
            cardModel.setEnabled(enabledServices.contains(card));

            // Add the card model to list of service models if this isn't a Coming Soon card and has a currently active subsystem
            if(!isComingSoonCard(card)) {
                for (SubsystemModel activeSubsystem : activeSubsystems) {
                    String subsystemName = activeSubsystem.getName().replace("Subsystem", "").toUpperCase();

                    if (subsystemName.contains(getCardShortName(cardModel))) {
                        serviceModelList.add(cardModel);
                    }
                }
            }

            // Finally, check whether this is a card we add "manually", i.e. does not have a subsystem but is still valid nonetheless
            if (validNonSubsystemCards.contains(getCardShortName(cardModel))) {
                serviceModelList.add(cardModel);
            }
        }
    }

    private boolean isComingSoonCard(ServiceCard card) {
        boolean comingSoonTitle = card.getDescriptionStringResId() == R.string.card_energy_desc;
        boolean comingSoonDescription = card.getDescriptionStringResId() == R.string.card_windows_and_blinds_desc;

        return comingSoonDescription || comingSoonTitle;
    }

    private String getCardShortName(ServiceListItemModel cardModel) {
        String cardFullName = cardModel.getServiceCard().name();
        int cardShortNameIndex = cardFullName.split("_").length - 1;

        return cardFullName.split("_")[cardShortNameIndex];
    }

    @Override
    public int getCount() {
        return serviceModelList.size();
    }

    @Override
    public ServiceListItemModel getItem(int index) {
        return serviceModelList.get(index);
    }

    @Override
    public void removeItem(int position) { /* Nothing to do; items not removable */ }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        ServiceListItemModel data = serviceModelList.remove(fromPosition);
        serviceModelList.add(toPosition, data);
    }

    @NonNull
    public Set<ServiceCard> getVisibleItems() {

        Set<ServiceCard> visibleCards = new HashSet<>();
        for (ServiceListItemModel thisModel : serviceModelList) {
            if (thisModel.isEnabled()) {
                visibleCards.add(thisModel.getServiceCard());
            }
        }

        return visibleCards;
    }

    @NonNull
    public List<ServiceCard> getOrderedListOfItems() {
        List<ServiceCard> orderedList = new ArrayList<>();

        for (ServiceListItemModel thisModel : serviceModelList) {
            orderedList.add(thisModel.getServiceCard());
        }

        return orderedList;
    }
}
