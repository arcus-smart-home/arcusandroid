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
package arcus.app.device.settings.enumeration;

import androidx.annotation.NonNull;

import com.iris.client.capability.Contact;
import arcus.app.R;
import arcus.app.device.settings.core.Localizable;


public enum SensorAssignedType implements Localizable {

    DOOR(R.string.setting_contact_assigned_to_door),
    WINDOW(R.string.setting_contact_assigned_to_window),
    OTHER(R.string.setting_contact_assigned_to_other);

    private final int displayedValueId;

    SensorAssignedType (int displayedValueResId) {
        this.displayedValueId = displayedValueResId;
    }

    @NonNull
    public static SensorAssignedType get(@NonNull String assigned) {

        // Since this could be coming from the model there is no guarantee that 'assigned' is not null, inverting check
        if (Contact.USEHINT_DOOR.equalsIgnoreCase(assigned)) {
            return DOOR;
        }
        else if (Contact.USEHINT_WINDOW.equalsIgnoreCase(assigned)) {
            return WINDOW;
        }
        else {
            return OTHER;
        }

    }

    public int getStringResId () {
        return this.displayedValueId;
    }

}
