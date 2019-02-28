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
package arcus.app.subsystems.people.model;

import arcus.app.R;
import arcus.app.device.settings.core.Localizable;


public enum PersonRelationshipTag implements Localizable {

    FAMILY(R.string.people_family),
    FRIEND(R.string.people_friend),
    GUEST(R.string.people_guest),
    NEIGHBOR(R.string.people_neighbor),
    LANDLORD(R.string.people_landlord),
    RENTER(R.string.people_renter),
    ROOMMATE(R.string.people_roommate),
    SERVICEPERSON(R.string.people_service_person),
    OTHER(R.string.people_other);

    private final int resourceId;

    PersonRelationshipTag(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getStringResId() {
        return resourceId;
    }
}
