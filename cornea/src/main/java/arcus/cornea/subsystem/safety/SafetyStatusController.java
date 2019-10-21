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
package arcus.cornea.subsystem.safety;

import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.calllist.CallListEntry;
import arcus.cornea.subsystem.safety.model.DeviceCounts;
import arcus.cornea.subsystem.safety.model.SensorSummary;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.CallTreeEntry;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Person;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.capability.Subsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.PersonModel;
import com.iris.client.model.SubsystemModel;
import com.iris.client.util.Result;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SafetyStatusController extends AbstractSafetyController<SafetyStatusController.Callback> {

    public interface Callback extends AbstractSafetyController.SafetyCallback<SensorSummary> {
        void showHistory(HistoryLog event);
        void showCounts(DeviceCounts counts);
        void showBasicCallList(List<CallListEntry> callList);
        void showPremiumCallList(List<CallListEntry> callList);
    }

    private Listener<Throwable> onRequestError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onRequestError(throwable);
        }
    });
    private Listener<Subsystem.ListHistoryEntriesResponse> historyLoadedListener = Listeners.runOnUiThread(new Listener<Subsystem.ListHistoryEntriesResponse>() {
        @Override
        public void onEvent(Subsystem.ListHistoryEntriesResponse listHistoryEntriesResponse) {
            onHistoryLoaded(new HistoryLogEntries(listHistoryEntriesResponse));
        }
    });

    private static final Logger logger = LoggerFactory.getLogger(SafetyStatusController.class);

    private static final SafetyStatusController instance = new SafetyStatusController(
                    SubsystemController.instance().getSubsystemModel(SafetySubsystem.NAMESPACE),
                    PersonModelProvider.instance().getModels(Collections.<String>emptySet()),
                    DeviceModelProvider.instance().getModels(Collections.<String>emptySet()));

    public static SafetyStatusController instance() {
        return instance;
    }

    private AddressableListSource<PersonModel> callListPeople;

    SafetyStatusController(ModelSource<SubsystemModel> subsystem,
                           AddressableListSource<PersonModel> callListPeople,
                           AddressableListSource<DeviceModel> devices) {
        super(subsystem, devices);
        this.callListPeople = callListPeople;
        attachListeners();
    }

    protected void attachListeners() {
        this.callListPeople.addListener(Listeners.runOnUiThread(new Listener<List<PersonModel>>() {
            @Override
            public void onEvent(List<PersonModel> personModels) {
                updateCallList();
            }
        }));
        this.callListPeople.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if(modelEvent instanceof ModelChangedEvent) {
                    onPersonChanged(((ModelChangedEvent) modelEvent).getChangedAttributes().keySet());
                }
            }
        }));
        super.attachListeners();
    }

    @Override
    protected void handleOnAdded() {
        callListPeople.setAddresses(getCallTreeAddresses());
    }

    private void onPersonChanged(Set<String> changes) {
        if(changes.contains(Person.ATTR_FIRSTNAME) ||
           changes.contains(Person.ATTR_LASTNAME)) {

            updateCallList();
        }
    }

    @Override
    protected void handleOnChanged(Set<String> changes) {
        if(changes.contains(SafetySubsystem.ATTR_CALLTREEENABLED) ||
                changes.contains(SafetySubsystem.ATTR_CALLTREE)) {

            callListPeople.setAddresses(getCallTreeAddresses());
            updateCallList();
        }
    }

    private List<String> getCallTreeAddresses() {
        List<Map<String,Object>> entries = get().getCallTree();
        List<String> addresses = new ArrayList<>(entries.size());
        for(Map<String,Object> entry : entries) {
            addresses.add((String) entry.get(CallTreeEntry.ATTR_PERSON));
        }
        return addresses;
    }

    public void cancel() {
        SafetySubsystem safety = get();
        if(safety == null) {
            return;
        }

        safety.clear().onCompletion(new Listener<Result<SafetySubsystem.ClearResponse>>() {
            @Override
            public void onEvent(Result<SafetySubsystem.ClearResponse> clearResponseResult) {
                logger.trace("Safety alarm clear acknowledged");
            }
        });
    }

    @Override
    protected void doUpdate() {
        updateHistory();
        updateCallList();
    }

    @Override
    protected void doUpdateAlarmState() {
        updateCounts();
    }

    private List<PersonModel> getCallListPeople() {
        callListPeople.load();
        return callListPeople.get();
    }

    private void updateCounts() {
        Callback callback = getCallback();
        if(callback == null) {
            return;
        }
        SafetySubsystem safety = get();
        if(safety == null) {
            return;
        }
        callback.showCounts(getCounts(safety));
    }

    private void updateHistory() {
        Callback callback = getCallback();
        if(callback == null) {
            return;
        }
        SafetySubsystem safety = get();
        if(safety == null) {
            return;
        }

        getLatestHistoryEvent(safety);
    }

    private void updateCallList() {
        Callback callback = getCallback();
        if(callback == null) {
            return;
        }
        SafetySubsystem safety = get();
        if(safety == null) {
            return;
        }
        List<PersonModel> callListPeople = getCallListPeople();
        if(callListPeople == null) {
            return;
        }

        if(safety.getCallTreeEnabled()) {
            callback.showPremiumCallList(getCallTree());
        } else {
            callback.showBasicCallList(getCallTree());
        }
    }

    private static final String SENSOR_SMOKE = "SMOKE";
    private static final String SENSOR_CO = "CO";
    private static final String SENSOR_WATER = "WATER";

    @Override
    protected SensorSummary getSummary(SafetySubsystem safety) {
        Map<String,String> sensors = safety.getSensorState();
        Map<String,String> uiValues = new HashMap<>();
        for(Map.Entry<String,String> entry : sensors.entrySet()) {
            String sensor = entry.getKey();
            String value = formatSensorState(entry.getValue(), safety.getAlarm());

            if(value.equals("OFFLINE")) {
                value = count(sensor, DeviceConnection.ATTR_STATE, DeviceConnection.STATE_OFFLINE) + " " + value;
            }

            uiValues.put(sensor, value);
        }

        return new SensorSummary(
                uiValues.get(SENSOR_SMOKE),
                uiValues.get(SENSOR_CO),
                uiValues.get(SENSOR_WATER));
    }

    private String formatSensorState(String sensorState, String alarmState) {
        if("SAFE".equals(sensorState)) {
            return "NO";
        }
        if("DETECTED".equals(sensorState)) {
            return alarmState.equals("CLEARING") ? "CLEARING" : sensorState;
        }
        return sensorState;
    }

    private int count(String sensorType, String attr, Object expectedValue) {
        List<DeviceModel> devices = getDevices();
        int count = 0;
        for(DeviceModel d : devices) {
            Object value = d.get(attr);
            if(hasSensorType(d, sensorType) && ObjectUtils.equals(expectedValue, value)) {
                count++;
            }
        }
        return count;
    }

    private boolean hasSensorType(DeviceModel device, String sensorType) {
        switch(sensorType) {
            case SENSOR_SMOKE: return device.getCaps().contains("smoke");
            case SENSOR_CO: return device.getCaps().contains("co");
            case SENSOR_WATER: return device.getCaps().contains("leakh2o");
            default: return false;
        }
    }

    private boolean isOffline(DeviceModel device) {
        return DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE));
    }

    private DeviceCounts getCounts(SafetySubsystem safety) {
        return new DeviceCounts(safety);
    }

    private void getLatestHistoryEvent(SafetySubsystem model) {

        model
                .listHistoryEntries(1, null, true)
                .onFailure(onRequestError)
                .onSuccess(historyLoadedListener);

    }


    protected void onHistoryLoaded(HistoryLogEntries entries) {
        Callback callback = getCallback();

        if (callback != null) {

            List<HistoryLog> logEntries = entries.getEntries();
            if (logEntries != null && logEntries.size() > 0)
                callback.showHistory(entries.getEntries().get(0));
        }
    }

    private List<CallListEntry> getCallTree() {
        List<PersonModel> people = getCallListPeople();
        List<Map<String,Object>> callTree = get().getCallTree();

        List<CallListEntry> callList = new ArrayList<>(people.size());
        for(PersonModel person : people) {
            if(inCallTree(person, callTree)) {
                callList.add(new CallListEntry(
                        person.getId(),
                        person.getFirstName(),
                        person.getLastName(),
                        "Family",
                        true));
            }
        }
        return callList;
    }

    private boolean inCallTree(PersonModel person, List<Map<String,Object>> callTree) {
        for(Map<String,Object> entry : callTree) {
            if(person.getAddress().equals(entry.get(CallTreeEntry.ATTR_PERSON))) {
               return (Boolean) entry.get(CallTreeEntry.ATTR_ENABLED);
            }
        }
        return false;
    }

    protected void onRequestError(Throwable cause) {
        // TODO show the user something?
        logger.warn("Unable to complete request", cause);
    }
}
