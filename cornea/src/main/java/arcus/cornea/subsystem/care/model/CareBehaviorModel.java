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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import arcus.cornea.utils.TemperatureUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CareBehaviorModel {
    private static final Integer DAY_MULTIPLIER = 86400;
    private static final Integer HOUR_MULTIPLIER = 3600;
    private static final Integer MINUTE_MULTIPLIER = 60;

    // Common to all?
    private String name;
    private String description;
    private String behaviorID;
    private String templateID;
    private CareBehaviorType behaviorType;
    private boolean enabled;
    private boolean active;
    private List<TimeWindowModel> timeWindows;
    private Date lastActivated;
    private Date lastFired;
    private Set<String> selectedDevices; // Available come from the template.
    private String presenceTime;

    // Specific?
    private Integer durationSecs; // Inactivity, Open, Presence
    private Map<String, Integer> openCounts; // Open_Count
    private Double lowTemp; // Temperature
    private Double highTemp; // Temperature

    private CareBehaviorModel original;

    private boolean requiresTimeWindows;

    public CareBehaviorModel() {}

    @SuppressWarnings({"unchecked"}) public static CareBehaviorModel fromMap(
          @NonNull Map<String, Object> theMap,
          @NonNull String templateID
    ) {
        CareBehaviorModel model = new CareBehaviorModel();

        model.name = (String) theMap.get(CareKeys.ATTR_BEHAVIOR_NAME.attrName());
        model.description = (String) theMap.get(CareKeys.ATTR_BEHAVIOR_DESCRIPTION.attrName());
        if (theMap.get(CareKeys.ATTR_BEHAVIOR_TEMPLATEID.attrName())  != null) {
            // If we're creating from an existing template, this should be non-null.
            // Else, let the platform assign the id
            model.behaviorID = (String) theMap.get(CareKeys.ATTR_BEHAVIOR_ID.attrName());
        }
        model.templateID = templateID;
        model.behaviorType = CareBehaviorType.from((String) theMap.get(CareKeys.ATTR_BEHAVIOR_TYPE.attrName()));
        model.enabled = Boolean.TRUE.equals(theMap.get(CareKeys.ATTR_BEHAVIOR_ENABLED.attrName()));
        model.active = Boolean.TRUE.equals(theMap.get(CareKeys.ATTR_BEHAVIOR_ACTIVE.attrName()));


        List<Map<String, Object>> timeWindows = (List<Map<String, Object>>) theMap.get(CareKeys.ATTR_BEHAVIOR_TIMEWINDOWS
              .attrName());
        if (timeWindows != null && !timeWindows.isEmpty()) {
            model.timeWindows = new ArrayList<>(timeWindows.size());
            for (Map<String, Object> tw : timeWindows) {
                model.timeWindows.add(TimeWindowModel.fromMap(tw));
            }
        }
        else {
            model.timeWindows = new ArrayList<>(7);
        }

        Number lastActivated = (Number) theMap.get(CareKeys.ATTR_BEHAVIOR_LASTACTIVATED.attrName());
        if (lastActivated != null) {
            model.lastActivated = new Date(lastActivated.longValue());
        }

        Number lastFired = (Number) theMap.get(CareKeys.ATTR_BEHAVIOR_LASTFIRED.attrName());
        if (lastActivated != null) {
            model.lastActivated = new Date(lastActivated.longValue());
        }

        Collection<String> selectedDevices = (Collection<String>) theMap.get(CareKeys.ATTR_BEHAVIOR_DEVICES.attrName());
        if (selectedDevices != null) {
            model.selectedDevices = new HashSet<>(selectedDevices);
        }
        else {
            model.selectedDevices = new HashSet<>();
        }

        Number durationSeconds = (Number) theMap.get(CareKeys.ATTR_BEHAVIOR_DURATIONSECS.attrName());
        if (durationSeconds != null) {
            model.durationSecs = durationSeconds.intValue();
        }

        Map<String, Number> openCounts = (Map<String, Number>) theMap.get(CareKeys.ATTR_BEHAVIOR_OPENCOUNT.attrName());
        if (openCounts != null) {
            model.openCounts = new HashMap<>(openCounts.size());
            for (Map.Entry<String, Number> item : openCounts.entrySet()) {
                if (item.getValue() != null) {
                    model.openCounts.put(item.getKey(), item.getValue().intValue());
                }
            }
        }
        else {
            model.openCounts = new HashMap<>();
        }

        Number high = (Number) theMap.get(CareKeys.ATTR_BEHAVIOR_HIGHTEMP.attrName());
        Number low = (Number) theMap.get(CareKeys.ATTR_BEHAVIOR_LOWTEMP.attrName());
        model.highTemp =  high != null ? high.doubleValue() : null;
        model.lowTemp = low != null ? low.doubleValue() : null;

        model.presenceTime = (String) theMap.get(CareKeys.ATTR_BEHAVIOR_PRESENCE_TIME.attrName());

        model.original = model.copy();

        return model;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> attributes = new HashMap<>(15);
        attributes.put(CareKeys.ATTR_BEHAVIOR_NAME.attrName(), name);
        attributes.put(CareKeys.ATTR_BEHAVIOR_TYPE.attrName(), behaviorType.name());
        attributes.put(CareKeys.ATTR_BEHAVIOR_TEMPLATEID.attrName(), templateID);
        attributes.put(CareKeys.ATTR_BEHAVIOR_ENABLED.attrName(), enabled);
        attributes.put(CareKeys.ATTR_BEHAVIOR_ID.attrName(), behaviorID);

        if (!CareBehaviorType.OPEN_COUNT.equals(behaviorType)) {
            attributes.put(CareKeys.ATTR_BEHAVIOR_DEVICES.attrName(), selectedDevices);
        }

        if (requiresTimeWindows && !timeWindows.isEmpty()) {
            List<Map<String, Object>> twMap = new ArrayList<>(timeWindows.size());
            for (TimeWindowModel timeWindowModel : timeWindows) {
                twMap.add(timeWindowModel.toMap());
            }

            attributes.put(CareKeys.ATTR_BEHAVIOR_TIMEWINDOWS.attrName(), twMap);
        }

        attributes.put(CareKeys.ATTR_BEHAVIOR_DURATIONSECS.attrName(), durationSecs == null ? 0 : durationSecs);

        switch (behaviorType) {
            case PRESENCE:
                attributes.put(CareKeys.ATTR_BEHAVIOR_PRESENCE_TIME.attrName(), presenceTime);
                break;
            case OPEN_COUNT:
                attributes.put(CareKeys.ATTR_BEHAVIOR_OPENCOUNT.attrName(), openCounts);
                break;
            case TEMPERATURE:
                attributes.put(CareKeys.ATTR_BEHAVIOR_LOWTEMP.attrName(), lowTemp);
                attributes.put(CareKeys.ATTR_BEHAVIOR_HIGHTEMP.attrName(), highTemp);
                break;

            case UNSUPPORTED:
                return Collections.emptyMap();
        }

        return attributes;
    }

    @Nullable public Integer getDurationSecsConverted(@NonNull DurationType durationType) {
        if (durationSecs == null) {
            return null;
        }

        switch (durationType) {
            case DAYS:
                return durationSecs / DAY_MULTIPLIER;
            case HOURS:
                return durationSecs / HOUR_MULTIPLIER;

            default:
            case MINUTES:
                return durationSecs / MINUTE_MULTIPLIER;
        }
    }

    @Nullable public Integer convertToPlatformSecondsValue(
          @NonNull String value,
          @NonNull DurationType durationType
    ) {
        if (TextUtils.isEmpty(value)) {
            return null;
        }

        try {
            Integer intValue = Integer.parseInt(value);
            switch (durationType) {
                case DAYS:
                    return intValue * DAY_MULTIPLIER;
                case HOURS:
                    return intValue * HOUR_MULTIPLIER;
                case MINUTES:
                    return intValue * MINUTE_MULTIPLIER;

                default:
                case UNKNOWN:
                    break;
            }

            return null;
        }
        catch (Exception ex) {
            return null;
        }
    }

    public boolean canSave() {
        if (requiresTimeWindows && timeWindows.isEmpty()) {
            // FIXME: 2/19/16 NEED TO ADD THIS IN.
//            return false;
        }
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(templateID)) {
            return false;
        }
        if (!CareBehaviorType.OPEN_COUNT.equals(behaviorType) && selectedDevices.isEmpty()) {
            return false;
        }

        switch (behaviorType) {
            case INACTIVITY:
                return durationSecs != null;
            case OPEN: // Why is "DOOR LEFT OPEN" an OPEN type?... It has a durationSecs....
                return !(templateID.equals("3") && durationSecs == null);
            case OPEN_COUNT:
                return !openCounts.isEmpty();
            case PRESENCE:
                return !TextUtils.isEmpty(presenceTime);
            case TEMPERATURE:
                return lowTemp != null && highTemp != null;

            default:
            case UNSUPPORTED:
                return false;
        }
    }

    public boolean hasChanges() {
        return !original.equals(this);
    }

    public boolean hasScheduleEvents() {
        return !CareBehaviorType.PRESENCE.equals(behaviorType) && !timeWindows.isEmpty();
    }

    private CareBehaviorModel copy() {
        CareBehaviorModel behaviorCopy = new CareBehaviorModel();
        behaviorCopy.name = this.name;
        behaviorCopy.description = this.description;
        behaviorCopy.behaviorID = this.behaviorID;
        behaviorCopy.templateID = this.templateID;
        behaviorCopy.durationSecs = this.durationSecs;
        behaviorCopy.behaviorType = this.behaviorType;
        behaviorCopy.timeWindows = new ArrayList<>(this.timeWindows);
        behaviorCopy.lastActivated = this.lastActivated;
        behaviorCopy.lastFired = this.lastFired;
        behaviorCopy.selectedDevices = new HashSet<>(this.selectedDevices);
        behaviorCopy.durationSecs = this.durationSecs;
        behaviorCopy.openCounts = new HashMap<>(this.openCounts);
        behaviorCopy.lowTemp = this.lowTemp;
        behaviorCopy.highTemp = this.highTemp;
        behaviorCopy.presenceTime = this.presenceTime;
        return behaviorCopy;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public String getBehaviorID() {
        return behaviorID;
    }

    public String getTemplateID() {
        return templateID;
    }

    public CareBehaviorType getBehaviorType() {
        return behaviorType;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isActive() {
        return active;
    }

    public List<TimeWindowModel> getTimeWindows() {
        return timeWindows;
    }

    public Date getLastActivated() {
        return lastActivated;
    }

    public Date getLastFired() {
        return lastFired;
    }

    public Set<String> getSelectedDevices() {
        return selectedDevices;
    }

    public Integer getDurationSecs() {
        return durationSecs;
    }

    public Map<String, Integer> getOpenCounts() {
        return openCounts;
    }

    public Integer getLowTemp() {
        return lowTemp == null ? null : TemperatureUtils.roundCelsiusToFahrenheit(lowTemp);
    }

    public Integer getHighTemp() {
        return highTemp == null ? null : TemperatureUtils.roundCelsiusToFahrenheit(highTemp);
    }

    public void setName(String name) {
        this.name = String.valueOf(name).trim();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void addTimeWindow(TimeWindowModel timeWindow) {
        this.timeWindows.add(timeWindow);
    }

    public void setSelectedDevices(Set<String> selectedDevices) {
        this.selectedDevices = selectedDevices;
    }

    public void setDurationSecs(Integer durationSecs) {
        this.durationSecs = durationSecs;
    }

    public void setOpenCounts(Map<String, Integer> openCounts) {
        this.openCounts = openCounts;
    }

    public void setLowTemp(Integer lowTemp) {
        this.lowTemp = TemperatureUtils.fahrenheitToCelsius(lowTemp);
    }

    public void setHighTemp(Integer highTemp) {
        this.highTemp = TemperatureUtils.fahrenheitToCelsius(highTemp);
    }

    public void setRequiresTimeWindows(boolean requiresTWs) {
        this.requiresTimeWindows = requiresTWs;
    }

    public String getPresenceTime() {
        return presenceTime;
    }

    public void setPresenceTime(String presenceTime) {
        this.presenceTime = presenceTime;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CareBehaviorModel that = (CareBehaviorModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (behaviorID != null ? !behaviorID.equals(that.behaviorID) : that.behaviorID != null) {
            return false;
        }
        if (templateID != null ? !templateID.equals(that.templateID) : that.templateID != null) {
            return false;
        }
        if (behaviorType != that.behaviorType) {
            return false;
        }
        if (presenceTime != null ? !presenceTime.equals(that.presenceTime) : that.presenceTime != null) {
            return false;
        }
        if (!timeWindows.equals(that.timeWindows)) {
            return false;
        }
        if (lastActivated != null ? !lastActivated.equals(that.lastActivated) : that.lastActivated != null) {
            return false;
        }
        if (lastFired != null ? !lastFired.equals(that.lastFired) : that.lastFired != null) {
            return false;
        }
        if (!selectedDevices.equals(that.selectedDevices)) {
            return false;
        }
        if (durationSecs != null ? !durationSecs.equals(that.durationSecs) : that.durationSecs != null) {
            return false;
        }
        if (!openCounts.equals(that.openCounts)) {
            return false;
        }
        if (lowTemp != null ? !lowTemp.equals(that.lowTemp) : that.lowTemp != null) {
            return false;
        }
        return !(highTemp != null ? !highTemp.equals(that.highTemp) : that.highTemp != null);

    }

    @Override public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (behaviorID != null ? behaviorID.hashCode() : 0);
        result = 31 * result + (templateID != null ? templateID.hashCode() : 0);
        result = 31 * result + (presenceTime != null ? presenceTime.hashCode() : 0);
        result = 31 * result + behaviorType.hashCode();
        result = 31 * result + (enabled ? 1 : 0);
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + timeWindows.hashCode();
        result = 31 * result + (lastActivated != null ? lastActivated.hashCode() : 0);
        result = 31 * result + (lastFired != null ? lastFired.hashCode() : 0);
        result = 31 * result + selectedDevices.hashCode();
        result = 31 * result + (durationSecs != null ? durationSecs.hashCode() : 0);
        result = 31 * result + openCounts.hashCode();
        result = 31 * result + (lowTemp != null ? lowTemp.hashCode() : 0);
        result = 31 * result + (highTemp != null ? highTemp.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "CareBehaviorModel{" +
              "name='" + name + '\'' +
              ", description='" + description + '\'' +
              ", behaviorID='" + behaviorID + '\'' +
              ", templateID='" + templateID + '\'' +
              ", behaviorType=" + behaviorType +
              ", enabled=" + enabled +
              ", active=" + active +
              ", timeWindows=" + timeWindows +
              ", lastActivated=" + lastActivated +
              ", lastFired=" + lastFired +
              ", selectedDevices=" + selectedDevices +
              ", original=" + original +
              ", durationSecs=" + durationSecs +
              ", openCounts=" + openCounts +
              ", lowTemp=" + lowTemp +
              ", highTemp=" + highTemp +
              ", presenceTime=" + presenceTime +
              '}';
    }
}
