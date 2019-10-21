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


public enum PersonRelationshipServiceTag implements Localizable {

    BABYSITTER(R.string.people_service_babysitter),
    DOGWALKER(R.string.people_service_dogwalker),
    ELECTRICIAN(R.string.people_service_electrician),
    HANDYMAN(R.string.people_service_handyman),
    HOMECLEANER(R.string.people_service_homecleaner),
    HVAC(R.string.people_service_hvac),
    LANDSCAPER(R.string.people_service_landscaper),
    NURSE(R.string.people_service_nurse),
    PAINTER(R.string.people_service_painter),
    PESTCONTROL(R.string.people_service_pestcontrol),
    PLUMBER(R.string.people_service_plumber),
    OTHER(R.string.people_service_other);

    private final int resourceId;

    PersonRelationshipServiceTag(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getStringResId() {
        return resourceId;
    }
}
