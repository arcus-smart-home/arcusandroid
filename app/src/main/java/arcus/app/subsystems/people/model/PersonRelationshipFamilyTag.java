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


public enum PersonRelationshipFamilyTag implements Localizable {

    SPOUSE(R.string.people_family_spouse),
    CHILDHOME(R.string.people_family_child_home),
    CHILDNOTHOME(R.string.people_family_child_not_home),
    MOTHER(R.string.people_family_mother),
    FATHER(R.string.people_family_father),
    GRANDMOTHER(R.string.people_family_grandmother),
    GRANDFATHER(R.string.people_family_grandfather),
    AUNT(R.string.people_family_aunt),
    UNCLE(R.string.people_family_uncle),
    COUSIN(R.string.people_family_cousin);

    private final int resourceId;

    PersonRelationshipFamilyTag(int resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public int getStringResId() {
        return resourceId;
    }
}
