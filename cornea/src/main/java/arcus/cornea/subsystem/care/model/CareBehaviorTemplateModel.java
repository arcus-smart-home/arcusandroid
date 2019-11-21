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

import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CareBehaviorTemplateModel implements BehaviorTemplate {
    private static final String TUPLE_DELIM = ",";
    private static final String RANGE_DELIM = "-";

    private String name;
    private String description;
    private String templateID;

    private String participatingDevicesTitle;
    private String participatingDevicesDescription;
    private String inactivityTitle;
    private String inactivityDescription;
    private String timeWindowTitle;
    private String timeWindowDescription;
    private String lowTempTitle;
    private String highTempTitle;

    private String lowTempUnit;
    private String highTempUnit;
    private TimeWindowSupport timeWindowsUnit;
    private String devicesUnit;
    private DurationType durationType;

    private Collection<String> availableDevices;

    private String lowTempValues;
    private String highTempValues;
    private String timeWindowsValues;
    private String devicesValues;
    private String durationValues;
    private String durationTypeValues;

    private TimeWindowSupport timeWindowSupport;

    private static String editNameLabel = "EDIT NAME";

    private CareBehaviorTemplateModel() {
        //no instance
    }

    @SuppressWarnings({"unchecked"}) public static CareBehaviorTemplateModel fromMap(@NonNull Map<String, Object> templateMap) {
        CareBehaviorTemplateModel model = new CareBehaviorTemplateModel();
        model.name = (String) templateMap.get(CareKeys.ATTR_BEHAVIOR_NAME.attrName());
        model.description = (String) templateMap.get(CareKeys.ATTR_BEHAVIOR_DESCRIPTION.attrName());
        model.templateID = (String) templateMap.get(CareKeys.ATTR_BEHAVIOR_ID.attrName());

        model.participatingDevicesTitle = getLabel(CareKeys.ATTR_BEHAVIOR_DEVICES.attrName(), templateMap);
        model.participatingDevicesDescription = getDescription(CareKeys.ATTR_BEHAVIOR_DEVICES.attrName(), templateMap);
        model.inactivityTitle = getLabel(CareKeys.ATTR_BEHAVIOR_DURATION.attrName(), templateMap);
        model.inactivityDescription = getDescription(CareKeys.ATTR_BEHAVIOR_DURATION.attrName(), templateMap);
        model.timeWindowTitle = getLabel(CareKeys.ATTR_BEHAVIOR_TIMEWINDOWS.attrName(), templateMap);
        model.timeWindowDescription = getDescription(CareKeys.ATTR_BEHAVIOR_TIMEWINDOWS.attrName(), templateMap);
        model.lowTempTitle = getLabel(CareKeys.ATTR_BEHAVIOR_LOWTEMP.attrName(), templateMap);
        model.highTempTitle = getLabel(CareKeys.ATTR_BEHAVIOR_HIGHTEMP.attrName(), templateMap);

        model.lowTempUnit = getUnit(CareKeys.ATTR_BEHAVIOR_LOWTEMP.attrName(), templateMap);
        model.highTempUnit = getUnit(CareKeys.ATTR_BEHAVIOR_HIGHTEMP.attrName(), templateMap);
        model.timeWindowsUnit = TimeWindowSupport.from(getUnit(CareKeys.ATTR_BEHAVIOR_TIMEWINDOWS.attrName(), templateMap));
        model.devicesUnit = getUnit(CareKeys.ATTR_BEHAVIOR_DEVICES.attrName(), templateMap);
        model.durationType = DurationType.from(getUnit(CareKeys.ATTR_BEHAVIOR_DURATION.attrName(), templateMap));

        model.lowTempUnit = getValue(CareKeys.ATTR_BEHAVIOR_LOWTEMP.attrName(), templateMap);
        model.highTempUnit = getValue(CareKeys.ATTR_BEHAVIOR_HIGHTEMP.attrName(), templateMap);
        model.timeWindowsValues = getValue(CareKeys.ATTR_BEHAVIOR_TIMEWINDOWS.attrName(), templateMap);
        model.devicesValues = getValue(CareKeys.ATTR_BEHAVIOR_DEVICES.attrName(), templateMap);
        model.durationValues = getValue(CareKeys.ATTR_BEHAVIOR_DURATION.attrName(), templateMap);
        model.durationTypeValues = getValue(CareKeys.ATTR_BEHAVIOR_DURATION_TYPE.attrName(), templateMap);

        model.availableDevices = (Collection<String>) templateMap.get(CareKeys.ATTR_BEHAVIOR_AVAILABLEDEVICES.attrName());
        model.timeWindowSupport = TimeWindowSupport.from((String) templateMap.get(CareKeys.ATTR_BEHAVIOR_TIMEWINDOWSUPPORT.attrName()));

        return model;
    }

    // Takes the initial Map<String, Object> and gets "key" from it, which should also be a map.
    private static @Nullable String getLabel(String key, Map<String, Object> templateMap) {
        Map<String, Object> obj = getMap(CareKeys.ATTR_BEHAVIOR_FIELDLABELS.attrName(), templateMap);
        return obj == null ? null : (String) obj.get(key);
    }

    private static @Nullable String getDescription(String key, Map<String, Object> templateMap) {
        Map<String, Object> obj = getMap(CareKeys.ATTR_BEHAVIOR_FIELDDESCRIPTIONS.attrName(), templateMap);
        return obj == null ? null : (String) obj.get(key);
    }

    private static @Nullable String getUnit(String key, Map<String, Object> templateMap) {
        Map<String, Object> obj = getMap(CareKeys.ATTR_BEHAVIOR_FIELDUNITS.attrName(), templateMap);
        return obj == null ? null : (String) obj.get(key);
    }

    private static @Nullable String getValue(String key, Map<String, Object> templateMap) {
        Map<String, Object> obj = getMap(CareKeys.ATTR_BEHAVIOR_FIELDVALUES.attrName(), templateMap);
        return obj == null ? null : (String) obj.get(key);
    }

    @SuppressWarnings({"unchecked"}) private static @Nullable Map<String, Object> getMap(String key, Map<String, Object> from) {
        try {
            return (Map<String, Object>) from.get(key);
        }
        catch (Exception ex) {
            LoggerFactory.getLogger(CareBehaviorTemplateModel.class).debug("Error casting map -> ", ex);
            return null;
        }
    }

    @NonNull @Override public String getName() {
        return name;
    }

    @NonNull @Override public String getDescription() {
        return description;
    }

    @NonNull @Override public String getID() {
        return templateID;
    }

    @Nullable @Override public String getParticipatingDevicesTitle() {
        return participatingDevicesTitle;
    }

    @Nullable @Override public String getParticipatingDevicesDescription() {
        return participatingDevicesDescription;
    }

    @Nullable @Override public String getInactivityTitle() {
        return inactivityTitle;
    }

    @Nullable @Override public String getInactivityDescription() {
        return inactivityDescription;
    }

    @Nullable @Override public String getTimeWindowTitle() {
        return timeWindowTitle;
    }

    @Nullable @Override public String getTimeWindowDescription() {
        return timeWindowDescription;
    }

    @Nullable @Override public String getLowTempTitle() {
        return lowTempTitle;
    }

    @Nullable @Override public String getHighTempTitle() {
        return highTempTitle;
    }

    @Nullable @Override public String getEditNameLabel() {
        return editNameLabel;
    }

    @Nullable @Override public String getLowTempUnit() {
        return lowTempUnit;
    }

    @Nullable @Override public String getHighTempUnit() {
        return highTempUnit;
    }

    @Nullable @Override public TimeWindowSupport getTimeWindowsUnit() {
        return timeWindowsUnit;
    }

    @Nullable @Override public String getDevicesUnit() {
        return devicesUnit;
    }

    @NonNull @Override public DurationType getDurationType() {
        return durationType;
    }

    @Nullable @Override public String getDurationTypeAbstract() {
        switch (durationType) {
            case DAYS:
                return "Day(s)";
            case HOURS:
                return "Hr(s)";
            case MINUTES:
                return "Min(s)";

            default:
            case UNKNOWN:
                break;
        }

        return null;
    }

    @Override @NonNull public String[] getDurationUnitValuesArray() {
        String[] values;
        if (TextUtils.isEmpty(durationValues)) {
            return new String[0];
        }

        if (durationValues.contains(TUPLE_DELIM)) {
            values = durationValues.split(TUPLE_DELIM);
        }
        else {
            values = durationValues.split(RANGE_DELIM);
        }

        if (values.length <= 1) {
            return new String[0];
        }

        return values;
    }

    @Override @NonNull public int[] getHighTemperatureUnitValuesArray() {
        return getTemperatureUnitValuesArray(false);
    }

    @Override @NonNull public int[] getLowTemperatureUnitValuesArray() {
        return getTemperatureUnitValuesArray(true);
    }

    @NonNull private int[] getTemperatureUnitValuesArray(boolean isLow) {
        String[] values;
        String tempValues = isLow ? getLowTempUnit() : getHighTempUnit();
        if (TextUtils.isEmpty(tempValues)) {
            return new int[0];
        }

        if (tempValues.contains(TUPLE_DELIM)) {
            values = tempValues.split(TUPLE_DELIM);
        }
        else {
            values = tempValues.split(RANGE_DELIM);
        }

        try {
            return new int[] {
                Integer.parseInt(values[0]),
                Integer.parseInt(values[1])
            };
        }
        catch (Exception ex) {
            return new int[0];
        }
    }

    @Nullable @Override public String getLowTempValues() {
        return lowTempValues;
    }

    @Nullable @Override public String getHighTempValues() {
        return highTempValues;
    }

    @Nullable @Override public String getTimeWindowsValues() {
        return timeWindowsValues;
    }

    @Nullable @Override public String getDevicesValues() {
        return devicesValues;
    }

    @Nullable @Override public String getDurationValues() {
        return durationValues;
    }

    @Nullable @Override public String getDurationTypeValues() {
        return durationTypeValues;
    }

    @NonNull @Override public Collection<String> getAvailableDevices() {
        return availableDevices == null ? Collections.<String>emptySet() : availableDevices;
    }

    @Override public boolean supportsTimeWindows() {
        return !TimeWindowSupport.NONE.equals(timeWindowSupport);
    }

    @Override public boolean requiresTimeWindows() {
        return TimeWindowSupport.REQUIRED.equals(timeWindowSupport) || isNoDurationType();
    }

    @Override public boolean isNoDurationType() {
        return TimeWindowSupport.NODURATION.equals(timeWindowsUnit);
    }

    @Override public boolean isSatisfiable() {
        return availableDevices != null && !availableDevices.isEmpty();
    }

    @Override public int compareTo(@NonNull BehaviorTemplate another) {
        return getID().compareToIgnoreCase(another.getID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CareBehaviorTemplateModel that = (CareBehaviorTemplateModel) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (templateID != null ? !templateID.equals(that.templateID) : that.templateID != null) {
            return false;
        }
        if (participatingDevicesTitle != null ? !participatingDevicesTitle.equals(that.participatingDevicesTitle) : that.participatingDevicesTitle != null) {
            return false;
        }
        if (participatingDevicesDescription != null ? !participatingDevicesDescription.equals(that.participatingDevicesDescription) : that.participatingDevicesDescription != null) {
            return false;
        }
        if (inactivityTitle != null ? !inactivityTitle.equals(that.inactivityTitle) : that.inactivityTitle != null) {
            return false;
        }
        if (inactivityDescription != null ? !inactivityDescription.equals(that.inactivityDescription) : that.inactivityDescription != null) {
            return false;
        }
        if (timeWindowTitle != null ? !timeWindowTitle.equals(that.timeWindowTitle) : that.timeWindowTitle != null) {
            return false;
        }
        if (timeWindowDescription != null ? !timeWindowDescription.equals(that.timeWindowDescription) : that.timeWindowDescription != null) {
            return false;
        }
        if (lowTempTitle != null ? !lowTempTitle.equals(that.lowTempTitle) : that.lowTempTitle != null) {
            return false;
        }
        if (highTempTitle != null ? !highTempTitle.equals(that.highTempTitle) : that.highTempTitle != null) {
            return false;
        }
        if (lowTempUnit != null ? !lowTempUnit.equals(that.lowTempUnit) : that.lowTempUnit != null) {
            return false;
        }
        if (highTempUnit != null ? !highTempUnit.equals(that.highTempUnit) : that.highTempUnit != null) {
            return false;
        }
        if (timeWindowsUnit != null ? !timeWindowsUnit.equals(that.timeWindowsUnit) : that.timeWindowsUnit != null) {
            return false;
        }
        if (devicesUnit != null ? !devicesUnit.equals(that.devicesUnit) : that.devicesUnit != null) {
            return false;
        }
        if (durationType != that.durationType) {
            return false;
        }
        if (availableDevices != null ? !availableDevices.equals(that.availableDevices) : that.availableDevices != null) {
            return false;
        }
        if (lowTempValues != null ? !lowTempValues.equals(that.lowTempValues) : that.lowTempValues != null) {
            return false;
        }
        if (highTempValues != null ? !highTempValues.equals(that.highTempValues) : that.highTempValues != null) {
            return false;
        }
        if (timeWindowsValues != null ? !timeWindowsValues.equals(that.timeWindowsValues) : that.timeWindowsValues != null) {
            return false;
        }
        if (devicesValues != null ? !devicesValues.equals(that.devicesValues) : that.devicesValues != null) {
            return false;
        }
        if (durationValues != null ? !durationValues.equals(that.durationValues) : that.durationValues != null) {
            return false;
        }
        return !(durationTypeValues != null ? !durationTypeValues.equals(that.durationTypeValues) : that.durationTypeValues != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (templateID != null ? templateID.hashCode() : 0);
        result = 31 * result + (participatingDevicesTitle != null ? participatingDevicesTitle.hashCode() : 0);
        result = 31 * result + (participatingDevicesDescription != null ? participatingDevicesDescription.hashCode() : 0);
        result = 31 * result + (inactivityTitle != null ? inactivityTitle.hashCode() : 0);
        result = 31 * result + (inactivityDescription != null ? inactivityDescription.hashCode() : 0);
        result = 31 * result + (timeWindowTitle != null ? timeWindowTitle.hashCode() : 0);
        result = 31 * result + (timeWindowDescription != null ? timeWindowDescription.hashCode() : 0);
        result = 31 * result + (lowTempTitle != null ? lowTempTitle.hashCode() : 0);
        result = 31 * result + (highTempTitle != null ? highTempTitle.hashCode() : 0);
        result = 31 * result + (lowTempUnit != null ? lowTempUnit.hashCode() : 0);
        result = 31 * result + (highTempUnit != null ? highTempUnit.hashCode() : 0);
        result = 31 * result + (timeWindowsUnit != null ? timeWindowsUnit.hashCode() : 0);
        result = 31 * result + (devicesUnit != null ? devicesUnit.hashCode() : 0);
        result = 31 * result + (durationType != null ? durationType.hashCode() : 0);
        result = 31 * result + (availableDevices != null ? availableDevices.hashCode() : 0);
        result = 31 * result + (lowTempValues != null ? lowTempValues.hashCode() : 0);
        result = 31 * result + (highTempValues != null ? highTempValues.hashCode() : 0);
        result = 31 * result + (timeWindowsValues != null ? timeWindowsValues.hashCode() : 0);
        result = 31 * result + (devicesValues != null ? devicesValues.hashCode() : 0);
        result = 31 * result + (durationValues != null ? durationValues.hashCode() : 0);
        result = 31 * result + (durationTypeValues != null ? durationTypeValues.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CareBehaviorTemplateModel{" +
              "name='" + name + '\'' +
              ", description='" + description + '\'' +
              ", templateID='" + templateID + '\'' +
              ", participatingDevicesTitle='" + participatingDevicesTitle + '\'' +
              ", participatingDevicesDescription='" + participatingDevicesDescription + '\'' +
              ", inactivityTitle='" + inactivityTitle + '\'' +
              ", inactivityDescription='" + inactivityDescription + '\'' +
              ", timeWindowTitle='" + timeWindowTitle + '\'' +
              ", timeWindowDescription='" + timeWindowDescription + '\'' +
              ", lowTempTitle='" + lowTempTitle + '\'' +
              ", highTempTitle='" + highTempTitle + '\'' +
              ", lowTempUnit='" + lowTempUnit + '\'' +
              ", highTempUnit='" + highTempUnit + '\'' +
              ", timeWindowsUnit='" + timeWindowsUnit + '\'' +
              ", devicesUnit='" + devicesUnit + '\'' +
              ", durationType=" + durationType +
              ", availableDevices=" + availableDevices +
              ", lowTempValues='" + lowTempValues + '\'' +
              ", highTempValues='" + highTempValues + '\'' +
              ", timeWindowsValues='" + timeWindowsValues + '\'' +
              ", devicesValues='" + devicesValues + '\'' +
              ", durationValues='" + durationValues + '\'' +
              ", durationTypeValues='" + durationTypeValues + '\'' +
              '}';
    }
}
