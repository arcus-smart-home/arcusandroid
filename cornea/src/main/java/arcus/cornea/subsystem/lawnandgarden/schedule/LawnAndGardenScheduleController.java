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
package arcus.cornea.subsystem.lawnandgarden.schedule;

import androidx.annotation.Nullable;

import com.google.gson.internal.LinkedTreeMap;
import arcus.cornea.subsystem.lawnandgarden.BaseLawnAndGardenController;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.bean.IrrigationScheduleStatus;
import com.iris.client.bean.IrrigationTransitionEvent;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.IrrigationController;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;

import java.util.ArrayList;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class LawnAndGardenScheduleController extends BaseLawnAndGardenController<LawnAndGardenScheduleController.Callback> {

    private static final LawnAndGardenScheduleController instance;

    public interface Callback {
        void updateView();
        void subsystemUpdate();
    }

    static {
        instance = new LawnAndGardenScheduleController();
    }

    public static LawnAndGardenScheduleController instance() {
        return instance;
    }

    private AddressableModelSource<DeviceModel> irrigationcontroller;

    private Listener<DeviceModel> onModelLoaded = Listeners.runOnUiThread(new Listener<DeviceModel>() {
        @Override
        public void onEvent(DeviceModel deviceModel) {
            updateView();
        }
    });
    private Listener<ModelEvent> modelListeners = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent modelEvent) {
            if (modelEvent instanceof ModelChangedEvent) {
                Set<String> changed = ((ModelChangedEvent) modelEvent).getChangedAttributes().keySet();
                if(
                    changed.contains(DeviceConnection.ATTR_STATE) ||
                    changed.contains(IrrigationController.ATTR_NUMZONES) ||
                    changed.contains(IrrigationController.ATTR_CONTROLLERSTATE) ||
                    changed.contains(IrrigationController.ATTR_MAXDAILYTRANSITIONS) ||
                    changed.contains(IrrigationController.ATTR_MAXIRRIGATIONTIME) ||
                    changed.contains(IrrigationController.ATTR_RAINDELAY) ||
                    changed.contains(IrrigationController.ATTR_ZONESINFAULT) ||
                    changed.contains(IrrigationController.ATTR_RAINDELAY) ||
                    changed.contains(IrrigationController.ATTR_BUDGET) ||
                    changed.contains(IrrigationController.ATTR_MAXTRANSITIONS)
                ) {
                  if(getCallback() != null) {
                      getCallback().updateView();
                  }
                }
            }
            /*else {
                if(getCallback() != null) {
                    getCallback().updateView();
                }
            }*/

        }
    });

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);
        updateView();
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changed = event.getChangedAttributes().keySet();
        if(changed.contains(DeviceConnection.ATTR_STATE) ||
                changed.contains(LawnNGardenSubsystem.ATTR_EVENSCHEDULES) ||
                changed.contains(LawnNGardenSubsystem.ATTR_WEEKLYSCHEDULES) ||
                changed.contains(LawnNGardenSubsystem.ATTR_ODDSCHEDULES) ||
                changed.contains(LawnNGardenSubsystem.ATTR_INTERVALSCHEDULES) ||
                changed.contains(LawnNGardenSubsystem.ATTR_SCHEDULESTATUS)
                ){
            if(getCallback() != null) {
                getCallback().subsystemUpdate();
            }
        }
        super.onSubsystemChanged(event);
    }

    LawnAndGardenScheduleController() {
        super();
        irrigationcontroller = CachedModelSource.newSource();
        init();
    }


    public void init() {
        super.init();
        this.irrigationcontroller.addModelListener(modelListeners);
    }

    public void setAddress(String address) {
        irrigationcontroller.setAddress(address);
        irrigationcontroller.load();
    }

    public void updateScheduleStatus(String deviceId, String mode, Listener<Throwable> failureListener) {
        getLawnNGardenSubsystem().enableScheduling(deviceId);
        getLawnNGardenSubsystem().switchScheduleMode(deviceId, mode).onFailure(failureListener);

    }

    public void disableScheduling(String deviceId, Listener<Throwable> failureListener) {
        getLawnNGardenSubsystem().disableScheduling(deviceId).onFailure(failureListener);
    }

    public String getSelectedScheduleType(String deviceId) {
        Map<String, Object> stuff = getLawnNGardenSubsystem().getScheduleStatus().get(deviceId);
        if(stuff == null || !(Boolean)stuff.get("enabled")) {
            return "";
        }
        return (String)stuff.get("mode");
    }

    public int getBudget(IrrigationController irrigationController) {
        try {
            return irrigationController.getBudget();
        }
        catch (NullPointerException e) {
            String error = e.getMessage();
        }
        return -1;
    }

    public void updateBudget(int value, DeviceModel irrigationController, Listener<ClientEvent> onSuccess, Listener<Throwable> onFailure) {
        ((IrrigationController)irrigationController).setBudget(value);
        irrigationController.commit().onSuccess(onSuccess).onFailure(onFailure);
    }

    public void createWeekEvent(String deviceAddress, Set<String> days, List<Map<String, Object>> zones, String time,
                                Listener<LawnNGardenSubsystem.CreateWeeklyEventResponse> onSuccess, Listener<Throwable> onFailure) {
        getLawnNGardenSubsystem().createWeeklyEvent(deviceAddress, days, time, zones).onSuccess(onSuccess).onFailure(onFailure);
    }

    public void updateWeekEvent(String deviceAddress, String eventId, Set<String> days, String timeOfDay, List<Map<String, Object>> zoneDurations, String day,
                                Listener<LawnNGardenSubsystem.UpdateWeeklyEventResponse> onSuccess, Listener<Throwable> onFailure) {
        getLawnNGardenSubsystem().updateWeeklyEvent(deviceAddress, eventId, days, timeOfDay, zoneDurations, day).onSuccess(onSuccess).onFailure(onFailure);
    }
    public void removeWeeklySchedule(String deviceAddress, String scheduleEventId, String day,
                                     Listener<LawnNGardenSubsystem.RemoveWeeklyEventResponse> onSuccess, Listener<Throwable> onFailure) {
        getLawnNGardenSubsystem().removeWeeklyEvent(deviceAddress, scheduleEventId, day).onSuccess(onSuccess).onFailure(onFailure);
    }

    public void createScheduleEvent(String deviceAddress, String mode, List<Map<String, Object>> zones, String startTime,
                                    Listener<LawnNGardenSubsystem.CreateScheduleEventResponse> onSuccess, Listener<Throwable> onFailure) {
        getLawnNGardenSubsystem().createScheduleEvent(deviceAddress, mode, startTime, zones).onSuccess(onSuccess).onFailure(onFailure);
    }

    public void updateScheduleEvent(String deviceAddress, String mode, String eventId, String timeOfDay, List<Map<String, Object>> zoneDurations,
                                    Listener<LawnNGardenSubsystem.UpdateScheduleEventResponse> onSuccess, Listener<Throwable> onFailure) {
        getLawnNGardenSubsystem().updateScheduleEvent(deviceAddress, mode, eventId, timeOfDay, zoneDurations).onSuccess(onSuccess).onFailure(onFailure);
    }

    public void removeScheduleEvent(String deviceAddress, String mode, String eventId,
                                    Listener<LawnNGardenSubsystem.RemoveScheduleEventResponse> onSuccess, Listener<Throwable> onFailure) {
        getLawnNGardenSubsystem().removeScheduleEvent(deviceAddress, mode, eventId).onSuccess(onSuccess).onFailure(onFailure);
    }

    //TODO: Not currently creating the schedule correctly.
    public void configureIntervalSchedule(String deviceAddress, Date startDate, int intervalDays,
                               Listener< LawnNGardenSubsystem.ConfigureIntervalScheduleResponse> onSuccess, Listener<Throwable> onFailure) {
        getLawnNGardenSubsystem().configureIntervalSchedule(deviceAddress, startDate, intervalDays).onSuccess(onSuccess).onFailure(onFailure);
    }

    public Map<String, Map<String, Object>> getScheduleStatus() {
        return getLawnNGardenSubsystem().getScheduleStatus();
    }

    public double getNextEventTime(String deviceAddress) {
        Map<String, Object> schedule = getLawnNGardenSubsystem().getScheduleStatus().get(deviceAddress);
        LinkedTreeMap<String, Object> nextEvent = (LinkedTreeMap<String, Object>) schedule.get("nextEvent");
        if(nextEvent != null) {
            if(nextEvent.get("startTime") != null) {
                return (double)nextEvent.get("startTime");
            }
        }
        return -1;
    }

    public @Nullable IrrigationTransitionEvent getNextEvent(String deviceAddress) {
        IrrigationScheduleStatus status = getScheduleStatus(deviceAddress);

        return (status == null || status.getNextEvent() == null) ? null : new IrrigationTransitionEvent(status.getNextEvent());
    }

    public @Nullable String getAppliedScheduleType(String deviceAddress) {
        IrrigationScheduleStatus status = getScheduleStatus(deviceAddress);

        return (status == null || !status.getEnabled()) ? null : status.getMode();
    }

    protected @Nullable IrrigationScheduleStatus getScheduleStatus(String deviceAddress) {
        LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
        if (subsystem == null) {
            return null;
        }

        Map<String, Map<String, Object>> allSchedules = subsystem.getScheduleStatus();
        if (allSchedules == null || allSchedules.isEmpty()) {
            return null;
        }

        Map<String, Object> deviceSchedule = allSchedules.get(deviceAddress);
        return (deviceSchedule == null) ? null : new IrrigationScheduleStatus(deviceSchedule);
    }

    public ArrayList<LinkedTreeMap<String, Object>> filterDeletedEvents(ArrayList<LinkedTreeMap<String, Object>> events) {
        ArrayList<LinkedTreeMap<String, Object>> returnList = new ArrayList<>();

        for(LinkedTreeMap<String, Object> event : events) {
            if(event.get("status") != null) {
                String status = (String)event.get("status");
                if(!status.equals("DELETING") && !status.equals("DELETED")) {
                    returnList.add(event);
                }
            }
        }
        return returnList;
    }

    public ArrayList<LinkedTreeMap<String, Object>> getWeeklySchedule(String deviceAddress) {
        Map<String, Map<String, Object>> map = getLawnNGardenSubsystem().getWeeklySchedules();
        Map<String, Object> deviceMap = map.get(deviceAddress);

        if(deviceMap == null) {
            return new ArrayList<>();
        }

        ArrayList<LinkedTreeMap<String, Object>> events = (ArrayList<LinkedTreeMap<String, Object>>) deviceMap.get("events");
        return filterDeletedEvents(events);
    }

    public ArrayList<LinkedTreeMap<String, Object>> getEvenSchedule(String deviceAddress) {
        Map<String, Map<String, Object>> map = getLawnNGardenSubsystem().getEvenSchedules();
        Map<String, Object> deviceMap = map.get(deviceAddress);

        if(deviceMap == null) {
            return new ArrayList<>();
        }

        ArrayList<LinkedTreeMap<String, Object>> events = (ArrayList<LinkedTreeMap<String, Object>>) deviceMap.get("events");
        return filterDeletedEvents(events);
    }

    public ArrayList<LinkedTreeMap<String, Object>> getOddSchedule(String deviceAddress) {
        Map<String, Map<String, Object>> map = getLawnNGardenSubsystem().getOddSchedules();
        Map<String, Object> deviceMap = map.get(deviceAddress);

        if(deviceMap == null) {
            return new ArrayList<>();
        }

        ArrayList<LinkedTreeMap<String, Object>> events = (ArrayList<LinkedTreeMap<String, Object>>) deviceMap.get("events");
        return filterDeletedEvents(events);
    }

    public ArrayList<LinkedTreeMap<String, Object>> getIntervalSchedule(String deviceAddress) {
        Map<String, Map<String, Object>> map = getLawnNGardenSubsystem().getIntervalSchedules();
        Map<String, Object> deviceMap = map.get(deviceAddress);

        if(deviceMap == null) {
            return new ArrayList<>();
        }

        ArrayList<LinkedTreeMap<String, Object>> events = (ArrayList<LinkedTreeMap<String, Object>>) deviceMap.get("events");
        return filterDeletedEvents(events);
    }

    public Map<String, Object> getUnfilteredIntervalSchedule(String deviceAddress) {
        Map<String, Map<String, Object>> map = getLawnNGardenSubsystem().getIntervalSchedules();
        Map<String, Object> deviceMap = map.get(deviceAddress);
        return deviceMap;
    }
}
