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

import arcus.cornea.subsystem.safety.model.Alarm;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.TriggerEvent;
import com.iris.client.capability.Device;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

abstract class AbstractSafetyController<T extends AbstractSafetyController.SafetyCallback> {

    interface SafetyCallback<U> {
        void showUnsatisfiableCopy();
        void showAlarm(List<Alarm> alarm);
        void showSummary(U summary);
    }

    private static final String SENSOR_TYPE_DETECTED_FMT = "%s DETECTED";

    private static final Logger logger = LoggerFactory.getLogger(AbstractSafetyController.class);

    private ModelSource<SubsystemModel> safety;
    private AddressableListSource<DeviceModel> devices;
    private WeakReference<T> callback = new WeakReference<>(null);

    protected AbstractSafetyController(ModelSource<SubsystemModel> safety, AddressableListSource<DeviceModel> devices) {
        this.safety = safety;
        this.devices = devices;
    }

    protected void attachListeners() {
        this.devices.addListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                updateAlarmState();
            }
        }));
        this.devices.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelChangedEvent) {
                    onDeviceChanged(((ModelChangedEvent) modelEvent).getChangedAttributes().keySet());
                }
            }
        }));
        this.safety.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelAddedEvent) {
                    onAdded();
                } else if (modelEvent instanceof ModelChangedEvent) {
                    onChanged(((ModelChangedEvent) modelEvent).getChangedAttributes().keySet());
                } else if (modelEvent instanceof ModelDeletedEvent) {
                    onCleared();
                }
            }
        }));
    }

    private void onDeviceChanged(Set<String> changes) {
        if(changes.contains(Device.ATTR_NAME)) {
            updateAlarmState();
        }
    }

    public ListenerRegistration setCallback(T callback) {
        if(this.callback.get() != null) {
            logger.warn("Replacing existing callback");
        }
        this.callback = new WeakReference<>(callback);
        updateAll();
        return Listeners.wrap(this.callback);
    }

    protected final T getCallback() {
        return this.callback.get();
    }

    public final SafetySubsystem get() {
        safety.load();
        return (SafetySubsystem) safety.get();
    }

    protected List<DeviceModel> getDevices() {
        // don't execute a load here to prevent a race condition when clearing due to an address
        // list change and invoking load here
        return devices.get();
    }

    private void onAdded() {
        handleOnAdded();
        updateAll();
        devices.setAddresses(new LinkedList<>(get().getTotalDevices()));
    }

    protected void handleOnAdded() {
        // no op hook
    }

    private void onChanged(Set<String> changes) {
        if(changes.contains(SafetySubsystem.ATTR_TOTALDEVICES)) {
            devices.setAddresses(new LinkedList<>(get().getTotalDevices()));
        } else if(changes.contains(SafetySubsystem.ATTR_ALARM) ||
           changes.contains(SafetySubsystem.ATTR_SENSORSTATE) ||
           changes.contains(SafetySubsystem.ATTR_TRIGGERS) ||
           changes.contains(SafetySubsystem.ATTR_ACTIVEDEVICES)) {

            updateAlarmState();

        } else {
            handleOnChanged(changes);

        }
    }

    protected void handleOnChanged(Set<String> changes) {
        // no op hook
    }

    private void updateAll() {
        T callback = getCallback();
        if(callback == null) {
            return;
        }
        SafetySubsystem safety = get();
        if(safety == null) {
            return;
        }

        updateAlarmState();
        doUpdate();
    }

    private void updateAlarmState() {
        T callback = getCallback();
        if(callback == null) {
            logger.debug("not updating callback because none is set");
            return;
        }
        SafetySubsystem safety = get();
        if(safety == null) {
            logger.debug("not updating callback because safety is null");
            return;
        }
        List<DeviceModel> devices = getDevices();
        if(devices == null/* || devices.size() != safety.getTotalDevices().size()*/) {
            logger.debug("not updating callback because the devices haven't been loaded");
            return;
        }

        if(safety.getTotalDevices() == null || safety.getTotalDevices().isEmpty()) {
            callback.showUnsatisfiableCopy();
            return;
        }

        String state = safety.getAlarm();
        switch(state) {
            case SafetySubsystem.ALARM_ALERT:
                logger.debug("invoking callback to show alarm");
                callback.showAlarm(getAlarmModel(safety));
                break;
            default:
                logger.debug("invoking callback to show summary");
                callback.showSummary(getSummary(safety));
        }
        doUpdateAlarmState();
    }

    protected void doUpdateAlarmState() {
        // no op hook
    }

    protected void doUpdate() {
        // no op hook
    }

    protected void onCleared() {
        // no op
    }

    protected abstract Object getSummary(SafetySubsystem safety);

    protected final String parseId(String address) {
        return address.split(":")[2];
    }

    private List<Alarm> getAlarmModel(SafetySubsystem safety) {
        List<Map<String,Object>> triggers = (safety.getTriggers() != null) ? safety.getTriggers() : Collections.emptyList();

        // when the triggers are empty all devices have reported they are safe but no user
        // has canceled the alarm
        boolean clearing = false;
        if(triggers.isEmpty()) {
            triggers = safety.getPendingClear();
            clearing = true;
        }

        // hmmm... this really shouldn't happen and implies that the subsystem sent an event that
        // the state had changed to alert, but the triggers were not updated
        if(triggers.isEmpty()) {
            return null;
        }

        List<Map<String,Object>> sorted = sortTriggers(triggers);
        List<Alarm> allTriggers = new ArrayList<>(sorted.size());

        for (Map<String, Object> trigger : sorted) {
            String address = (String) trigger.get(TriggerEvent.ATTR_DEVICE);
            DeviceModel dm = getDevice(address);
            String devName = dm == null ? null : dm.getName();
            String id = dm == null ? parseId(address) : dm.getId();
            allTriggers.add(
                  new Alarm(
                        id,
                        devName,
                        String.format(SENSOR_TYPE_DETECTED_FMT, trigger.get(TriggerEvent.ATTR_TYPE)),
                        new Date(((Number) trigger.get(TriggerEvent.ATTR_TIME)).longValue()))
            );
        }

        return allTriggers;
    }

    private DeviceModel getDevice(String address) {
        List<DeviceModel> devices = getDevices();
        if(devices == null)  {
            return null;
        }
        for(DeviceModel dm : devices) {
            if(dm.getAddress().equals(address)) {
                return dm;
            }
        }
        return null;
    }

    private List<Map<String,Object>> sortTriggers(List<Map<String, Object>> unsorted) {
        List<Map<String,Object>> sorted = new LinkedList<>(unsorted);
        Collections.sort(unsorted, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Long t1 = ((Number) o1.get(TriggerEvent.ATTR_TIME)).longValue();
                Long t2 = ((Number) o2.get(TriggerEvent.ATTR_TIME)).longValue();
                return t2.compareTo(t1);

            }
        });
        return sorted;
    }
}
