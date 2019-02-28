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
package arcus.cornea.subsystem.care.model;

import com.iris.client.bean.CareBehavior;
import com.iris.client.bean.CareBehaviorInactivity;
import com.iris.client.bean.CareBehaviorOpenCount;
import com.iris.client.bean.CareBehaviorPresence;
import com.iris.client.bean.CareBehaviorTemperature;
import com.iris.client.bean.CareBehaviorTemplate;
import com.iris.client.bean.TimeWindow;

public enum CareKeys {
    // Behaviors
    ATTR_BEHAVIOR_DURATIONSECS (CareBehaviorInactivity.ATTR_DURATIONSECS),
    ATTR_BEHAVIOR_OPENCOUNT (CareBehaviorOpenCount.ATTR_OPENCOUNT),
    ATTR_BEHAVIOR_LOWTEMP (CareBehaviorTemperature.ATTR_LOWTEMP),
    ATTR_BEHAVIOR_HIGHTEMP (CareBehaviorTemperature.ATTR_HIGHTEMP),
    ATTR_BEHAVIOR_ID (CareBehavior.ATTR_ID),
    ATTR_BEHAVIOR_NAME (CareBehavior.ATTR_NAME),
    ATTR_BEHAVIOR_DESCRIPTION (CareBehaviorTemplate.ATTR_DESCRIPTION),
    ATTR_BEHAVIOR_TYPE (CareBehavior.ATTR_TYPE),
    ATTR_BEHAVIOR_TEMPLATEID (CareBehavior.ATTR_TEMPLATEID),
    ATTR_BEHAVIOR_DEVICES (CareBehavior.ATTR_DEVICES),
    ATTR_BEHAVIOR_AVAILABLEDEVICES (CareBehavior.ATTR_AVAILABLEDEVICES),
    ATTR_BEHAVIOR_ENABLED (CareBehavior.ATTR_ENABLED),
    ATTR_BEHAVIOR_ACTIVE (CareBehavior.ATTR_ENABLED),
    ATTR_BEHAVIOR_TIMEWINDOWS (CareBehavior.ATTR_TIMEWINDOWS),
    ATTR_BEHAVIOR_TIMEWINDOWSUPPORT (CareBehaviorTemplate.ATTR_TIMEWINDOWSUPPORT),
    ATTR_BEHAVIOR_LASTACTIVATED (CareBehavior.ATTR_LASTACTIVATED),
    ATTR_BEHAVIOR_LASTFIRED (CareBehavior.ATTR_LASTFIRED),
    ATTR_BEHAVIOR_PRESENCE_TIME (CareBehaviorPresence.ATTR_PRESENCEREQUIREDTIME),
    ATTR_BEHAVIOR_DURATION("duration"), // Not sure what object this was in.....
    ATTR_BEHAVIOR_DURATION_TYPE("durationType"), // Not sure what object this was in.....
    ATTR_BEHAVIOR_FIELDLABELS (CareBehaviorTemplate.ATTR_FIELDLABELS),
    ATTR_BEHAVIOR_FIELDDESCRIPTIONS (CareBehaviorTemplate.ATTR_FIELDDESCRIPTIONS),
    ATTR_BEHAVIOR_FIELDUNITS (CareBehaviorTemplate.ATTR_FIELDUNITS),
    ATTR_BEHAVIOR_FIELDVALUES (CareBehaviorTemplate.ATTR_FIELDVALUES),

    // Time Windows
    ATTR_TIMEWINDOW_DAY(TimeWindow.ATTR_DAY),
    ATTR_TIMEWINDOW_STARTTIME(TimeWindow.ATTR_STARTTIME),
    ATTR_TIMEWINDOW_DURATIONSECS(TimeWindow.ATTR_DURATIONSECS);

    private String attributeName;

    CareKeys(String itsName) {
        attributeName = itsName;
    }

    public String attrName() {
        return attributeName;
    }
}
