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

import arcus.app.dashboard.settings.model.AbstractDraggableListModel;

/**
 * A data model representing a list item (cell) in the draggable service card list.
 */
public class ServiceListItemModel implements AbstractDraggableListModel {

    private final ServiceCard serviceCard;
    private final String serviceName;
    private boolean enabled = true;

    public ServiceListItemModel(String serviceName, ServiceCard serviceCard) {
        this.serviceCard = serviceCard;
        this.serviceName = serviceName;
    }

    public ServiceCard getServiceCard () {
        return serviceCard;
    }

    @Override
    public long getId() {
        return serviceCard.hashCode();
    }

    public String getText() {
        return serviceName;
    }

    public void setEnabled (boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled () {
        return this.enabled;
    }
}
