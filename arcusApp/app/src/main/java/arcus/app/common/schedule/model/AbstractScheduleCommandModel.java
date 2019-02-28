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
package arcus.app.common.schedule.model;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.capability.Schedule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;



public abstract class AbstractScheduleCommandModel implements ScheduleCommandModel {

    protected String schedulerGroupId;
    protected String commandMessageType = Schedule.CMD_SET_ATTRIBUTES;
    protected String id;
    protected Set<DayOfWeek> repeatOnDays;
    protected TimeOfDay timeOfDay;
    protected Map<String,Object> commandAttributes = new HashMap<>();
    protected String commandAbstract;

    private AbstractScheduleCommandModel() {}

    public AbstractScheduleCommandModel(String groupId, TimeOfDay timeOfDay, DayOfWeek repeatOnDay) {
        this(groupId, timeOfDay, EnumSet.of(repeatOnDay));
    }

    public AbstractScheduleCommandModel(String groupId, TimeOfDay timeOfDay, Set<DayOfWeek> repeatOnDays) {
        this.schedulerGroupId = groupId;
        this.repeatOnDays = repeatOnDays;
        this.timeOfDay = timeOfDay;
    }

    public void setSchedulerGroupId (String schedulerGroupId) {
        this.schedulerGroupId = schedulerGroupId;
    }

    @Override
    public String getSchedulerGroupId() {
        return schedulerGroupId;
    }

    @Override
    public String getCommandMessageType () {
        return commandMessageType;
    }

    public void setCommandMessageType (String commandMessageType) {
        this.commandMessageType = commandMessageType;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Set<DayOfWeek> getDays() {
        return repeatOnDays;
    }

    public EnumSet<DayOfWeek> getDaysAsEnumSet() {
        return EnumSet.copyOf(repeatOnDays);
    }

    @Override
    public void setDays(Set<DayOfWeek> repeatOnDays) {
        this.repeatOnDays = repeatOnDays;
    }

    @Override
    public TimeOfDay getTime() {
        return timeOfDay;
    }

    public void setTime (int selectedHour, int selectedMinute) {
        this.timeOfDay = new TimeOfDay(selectedHour, selectedMinute, 0);
    }

    @Override
    public void setTime(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return commandAttributes;
    }

    @Override
    public void setAttributes(Map<String, Object> commandAttributes) {
        this.commandAttributes = commandAttributes;
    }

    @Override
    public String getCommandAbstract () {
        return commandAbstract;
    }

    public void setCommandAbstract (String commandAbstract) {
        this.commandAbstract = commandAbstract;
    }

    public static <T extends ScheduleCommandModel> List<T> fromTimeOfDayCommands(List<? extends TimeOfDayCommand> todcs, ScheduleCommandModelFactory<T> modelFactory) {
        List<T> relativeTimes = new ArrayList<>();
        List<T> absoluteTimes = new ArrayList<>();
        for (TimeOfDayCommand thisTodc : todcs) {
            if (SunriseSunset.ABSOLUTE.equals(SunriseSunset.fromString(thisTodc.getMode()))) {
                absoluteTimes.add(fromTimeOfDayCommand(thisTodc, modelFactory));
            }
            else {
                relativeTimes.add(fromTimeOfDayCommand(thisTodc, modelFactory));
            }
        }

        final Comparator<T> eventSorter = new Comparator<T>() {
            @Override public int compare(T firstDetail, T secondDetail) {
                return firstDetail.getTime().compareTo(secondDetail.getTime());
            }
        };
        Collections.sort(relativeTimes, eventSorter);
        Collections.sort(absoluteTimes, eventSorter);
        relativeTimes.addAll(absoluteTimes);

        return relativeTimes;
    }

    public static <T extends ScheduleCommandModel> T fromTimeOfDayCommand (TimeOfDayCommand todc, ScheduleCommandModelFactory<T> modelFactory) {
        T scheduleCommand = modelFactory.newInstance();

        Set<DayOfWeek> daysOfWeek = new HashSet<>();
        for (String thisDay : todc.getDays()) {
            daysOfWeek.add(DayOfWeek.from(thisDay));
        }

        scheduleCommand.setCommandMessageType(todc.getMessageType());
        scheduleCommand.setDays(daysOfWeek);
        scheduleCommand.setTime(TimeOfDay.fromStringWithMode(todc.getTime(), SunriseSunset.fromString(todc.getMode()), todc.getOffsetMinutes()));
        scheduleCommand.setId(todc.getId());
        scheduleCommand.setAttributes(todc.getAttributes());

        return scheduleCommand;
    }

}
