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
package arcus.cornea.subsystem.security;

import com.iris.client.capability.Contact;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Glass;
import com.iris.client.capability.Motion;
import com.iris.client.model.DeviceModel;

import java.util.Set;


public class DeviceStatus {
    public static boolean isOpen(DeviceModel model) {
        if(model.getCaps().contains(Contact.NAMESPACE) && Contact.CONTACT_OPENED.equals(model.get(Contact.ATTR_CONTACT))) {
            return true;
        }
        if(model.getCaps().contains(Motion.NAMESPACE) && Motion.MOTION_DETECTED.equals(model.get(Motion.ATTR_MOTION))) {
            return true;
        }
        return model.getCaps().contains(Glass.NAMESPACE) && Glass.BREAK_DETECTED.equals(model.get(Glass.ATTR_BREAK));

    }

    public static boolean isOffline(DeviceModel model) {
        return !DeviceConnection.STATE_ONLINE.equals(model.get(DeviceConnection.ATTR_STATE));
    }

    public static boolean isStatusChanged(Set<String> attributes) {
        return
                attributes.contains(DeviceConnection.ATTR_STATE) ||
                attributes.contains(Contact.ATTR_CONTACT) ||
                attributes.contains(Motion.ATTR_MOTION) ||
                attributes.contains(Glass.ATTR_BREAK);
    }
}
