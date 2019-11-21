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

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * An EventBus event indicating that the set of displayed service cards has changed (in either
 * order or in the set of visible cards).
 */
public class CardListChangedEvent {

    private final List<ServiceCard> serviceOrder;
    private final Set<ServiceCard> visibleCards;

    public CardListChangedEvent (List<ServiceCard> serviceOrder, Set<ServiceCard> visibleCards) {
        this.serviceOrder = serviceOrder;
        this.visibleCards = visibleCards;
    }

    public List<ServiceCard> getServiceOrder () {
        return this.serviceOrder;
    }

    public Set<ServiceCard> getVisibleCards () {
        return this.visibleCards;
    }

    @NonNull
    public List<ServiceCard> getOrderedVisibleCards () {
        List<ServiceCard> orderedCards = new ArrayList<>();
        for (ServiceCard thisCard : serviceOrder) {
            if (visibleCards.contains(thisCard)) {
                orderedCards.add(thisCard);
            }
        }

        return orderedCards;
    }
}
