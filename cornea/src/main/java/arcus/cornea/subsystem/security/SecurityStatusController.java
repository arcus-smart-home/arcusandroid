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

import androidx.annotation.Nullable;

import com.google.common.collect.Sets;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.security.model.ArmedModel;
import arcus.cornea.subsystem.security.model.ArmingModel;
import arcus.cornea.subsystem.security.model.PromptUnsecuredModel;
import arcus.cornea.subsystem.security.model.Trigger;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.CallTreeEntry;
import com.iris.client.capability.SecurityAlarmMode;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.capability.Subsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.RuleModel;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;


public class SecurityStatusController {
    private static final Logger logger =
            LoggerFactory.getLogger(SecurityStatusController.class);
    private static final SecurityStatusController instance =
            new SecurityStatusController(SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE));

    private static final String MODE_ON = "ON";
    private static final String MODE_PARTIAL = "PARTIAL";

    private static final String FMT_NO_DEVICES = "No Devices";
    private static final String FMT_X_DEVICES = "%d Devices";
    private static final String FMT_X_OPEN_DEVICES = "%d Open";
    private static final String FMT_X_OFFLINE_DEVICES = "%d Offline";
    private static final String FMT_X_OFFLINE_X_OPEN_DEVICES = "%d Offline, %d Open";

    private static final String FMT_SENSOR_TRIGGERED = "A DOOR OR WINDOW IS OPEN";

    public static SecurityStatusController instance() {
        return instance;
    }

    private ModelSource<SubsystemModel> subsystem;

    private WeakReference<AlarmCallback> alarmCallback = new WeakReference<AlarmCallback>(null);
    private WeakReference<ButtonCallback> buttonCallback = new WeakReference<ButtonCallback>(null);
    private WeakReference<HistoryLogCallback> historyLogCallback = new WeakReference<>(null);

    private AddressableListSource<DeviceModel> onDevices;
    private AddressableListSource<DeviceModel> partialDevices;

    // TODO it would be nice if the callbacks were the only bit run on the ui threadd
    private Listener<SecuritySubsystem.ArmResponse> armResponseListener = Listeners.runOnUiThread(new Listener<SecuritySubsystem.ArmResponse>() {
        @Override
        public void onEvent(SecuritySubsystem.ArmResponse response) {
            startArmingTimer(response.getDelaySec());
        }
    });
    private Listener<SecuritySubsystem.ArmBypassedResponse> armBypassedResponseListener = Listeners.runOnUiThread(new Listener<SecuritySubsystem.ArmBypassedResponse>() {
        @Override
        public void onEvent(SecuritySubsystem.ArmBypassedResponse response) {
            startArmingTimer(response.getDelaySec());
        }
    });
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
    private Listener<Subsystem.ListHistoryEntriesResponse> singleHistoryLoadedListener = Listeners.runOnUiThread(new Listener<Subsystem.ListHistoryEntriesResponse>() {
        @Override
        public void onEvent(Subsystem.ListHistoryEntriesResponse listHistoryEntriesResponse) {
            onSingleHistoryLoaded(new HistoryLogEntries(listHistoryEntriesResponse));
        }
    });
    private final Comparator<Trigger> triggerSorter = new Comparator<Trigger>() {
        @Override
        public int compare(Trigger lhs, Trigger rhs) {
            if(lhs.getTriggeredSince() == null) {
                return rhs.getTriggeredSince() == null ? 0 : -1;
            }
            if(rhs.getTriggeredSince() == null) {
                return 1;
            }
            return rhs.getTriggeredSince().compareTo(lhs.getTriggeredSince());
        }
    };

    // TODO share this executor...
    private Timer timer = new Timer();
    private CountdownTask armingCountdown = new CountdownTask(0, null);

    protected SecurityStatusController(ModelSource<SubsystemModel> subsystem) {
        this.subsystem = subsystem;
        this.subsystem.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent event) {
                if (event instanceof ModelAddedEvent) {
                    onAdded();
                } else if (event instanceof ModelChangedEvent) {
                    onChanged(((ModelChangedEvent) event).getChangedAttributes().keySet());
                } else if (event instanceof ModelDeletedEvent) {
                    onCleared();
                }
            }
        }));
        this.onDevices = DeviceModelProvider.instance().newModelList();
        this.onDevices.addListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                updateAllDevices();
            }
        }));
        this.onDevices.addModelListener(Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
            @Override
            public void onEvent(ModelChangedEvent event) {
                if(DeviceStatus.isStatusChanged(event.getChangedAttributes().keySet())) {
                    updateAllDevices();
                }
            }
        }), ModelChangedEvent.class);
        this.partialDevices = DeviceModelProvider.instance().newModelList();
        this.partialDevices.addListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                updatePartialDevices();
            }
        }));
        this.partialDevices.addModelListener(Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
            @Override
            public void onEvent(ModelChangedEvent event) {
                if (DeviceStatus.isStatusChanged(event.getChangedAttributes().keySet())) {
                    updatePartialDevices();
                }
            }
        }), ModelChangedEvent.class);
    }

    protected SecuritySubsystem get() {
        subsystem.load();
        return (SecuritySubsystem) subsystem.get();
    }

    protected ArmingModel getArmingModel(SecuritySubsystem security) {
        String mode = security.getAlarmMode();
        ArmingModel model = new ArmingModel();
        model.setMode(mode);
        model.setDeviceCount(count((Collection<?>) ((Model) security).get(SecurityAlarmMode.ATTR_DEVICES + ":" + mode)));
        model.setCountdownSec(armingCountdown.getRemainingSec());

        return model;
    }

    protected ArmedModel getArmedModel(SecuritySubsystem security) {
        Set<String> offline = new HashSet<>(security.getOfflineDevices());
        offline.retainAll(security.getArmedDevices());
        int offlineCount = count(offline);

        ArmedModel model = new ArmedModel();
        model.setMode(security.getAlarmMode());
        model.setActive(count(security.getArmedDevices()) - offlineCount);
        model.setBypassed(count(security.getBypassedDevices()));
        model.setOffline(offlineCount);
        model.setTotal(model.getActive() + model.getBypassed() + model.getOffline());
        model.setArmedSince(date(security.getLastArmedTime()));

        return model;
    }

    protected String getUnsecuredText() {
        // TODO determine which devices are preventing arming
        return FMT_SENSOR_TRIGGERED;
    }

    protected int getCount(Model model, String key, String mode) {
        Object o = model.get(key + ":" + mode);
        if(o == null || !(o instanceof Collection)) {
            return 0;
        }
        return ((Collection<?>) o).size();
    }

    protected Set<String> getTriggeredDeviceNames(SecuritySubsystem security) {
        Set<String> bypassedDeviceAddresses = security.getTriggeredDevices();
        Set<String> bypassedDeviceNames = Sets.newHashSet();

        if (bypassedDeviceAddresses == null || bypassedDeviceAddresses.isEmpty()) {
            return bypassedDeviceNames;
        }

        for (String thisDeviceAddress : bypassedDeviceAddresses) {
            Model model = CorneaClientFactory.getModelCache().get(thisDeviceAddress);

            if (model != null && model instanceof DeviceModel) {
                bypassedDeviceNames.add(((DeviceModel) model).getName());
            }
        }

        return bypassedDeviceNames;
    }

    protected String getDevicesText(SubsystemModel security, String mode) {
        Collection<String> addresses = (Collection<String>) security.get(SecurityAlarmMode.ATTR_DEVICES + ":" + mode);
        if(addresses == null || addresses.isEmpty()) {
            return FMT_NO_DEVICES;
        }

        int opened = 0;
        int offline = 0;
        int other = 0;
        for(String address: addresses) {
            Model model = CorneaClientFactory.getModelCache().get(address);
            if(model == null || !(model instanceof  DeviceModel)) {
                logger.warn("Unable to load device model at address {}", address);
                other++;
                continue;
            }

            DeviceModel device = (DeviceModel) model;
            if(DeviceStatus.isOffline(device)) {
                offline++;
            }
            else if(DeviceStatus.isOpen(device)) {
                opened++;
            }
            else {
                other++;
            }

        }
        if (opened == 0 && offline == 0) {
            // this shouldn't happen...
            if (other == 0) {
                return FMT_NO_DEVICES;
            }
            return String.format(FMT_X_DEVICES, other);
        } else if (opened > 0 && offline > 0) {
            return String.format(FMT_X_OFFLINE_X_OPEN_DEVICES, offline, opened);
        } else if (opened > 0) {
            return String.format(FMT_X_OPEN_DEVICES, opened);
        } else {
            return String.format(FMT_X_OFFLINE_DEVICES, offline);
        }
    }

    protected List<Trigger> getTriggerModel(SecuritySubsystem security) {
        List<Trigger> triggers = new ArrayList<>();
        Map<String, Date> currentTriggers = security.getLastAlertTriggers();
        if (currentTriggers == null || currentTriggers.isEmpty()) {
            return triggers;
        }

        for (Map.Entry<String, Date> item : currentTriggers.entrySet()) {
            Model model = CorneaClientFactory.getModelCache().get(item.getKey());
            if (model != null && model instanceof RuleModel) {
                triggers.add(Trigger.wrapRuleModel(model, item.getValue()));
            } else {
                triggers.add(Trigger.wrap(model, item.getValue()));
            }
        }

        Collections.sort(triggers, triggerSorter); // Sorting so most recent is at top
        return triggers;
    }

    protected void onAdded() {
        SubsystemModel model = (SubsystemModel) get();
        this.onDevices.setAddresses(BaseSubsystemController.list((Collection<String>) model.get(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON)));
        this.partialDevices.setAddresses(BaseSubsystemController.list((Collection<String>) model.get(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL)));
        updateAlarmState();
        updateButtonState();
    }

    protected void onChanged(Set<String> attributes) {
        if(
                attributes.contains(SecuritySubsystem.ATTR_ALARMSTATE) ||
                        attributes.contains(SecuritySubsystem.ATTR_OFFLINEDEVICES) ||
                        attributes.contains(SecuritySubsystem.ATTR_ARMEDDEVICES) ||
                        attributes.contains(SecuritySubsystem.ATTR_BYPASSEDDEVICES) ||
                        attributes.contains(SecuritySubsystem.ATTR_TRIGGEREDDEVICES)
                ) {
            updateAlarmState();
            updateButtonState();
        }
        else { // already updated everything in the previous if
            SubsystemModel model = (SubsystemModel) get();
            if(attributes.contains(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON)) {
                this.onDevices.setAddresses(BaseSubsystemController.list((Collection<String>) model.get(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON)));
                updateAllDevices();
            }
            if(attributes.contains(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL)) {
                this.partialDevices.setAddresses(BaseSubsystemController.list((Collection<String>) model.get(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL)));
                updatePartialDevices();
            }
            if(
                    attributes.contains(SecuritySubsystem.ATTR_CALLTREEENABLED) ||
                            attributes.contains(SecuritySubsystem.ATTR_CALLTREE)
                    ) {
                updateContacts();
            }
            // TODO need a history event
        }
    }

    protected void onCleared() {
        // no-op
    }

    protected void syncArmingTimer() {
        if(armingCountdown.getRemainingSec() > 0) {
            return;
        }

        SecuritySubsystem securitySubsystem = get();
        if(securitySubsystem == null) {
            return;
        }

        String mode = securitySubsystem.getAlarmMode();
        Object countdown = ((Model) securitySubsystem).get( SecurityAlarmMode.ATTR_EXITDELAYSEC + ":" + mode);
        if(countdown == null || !(countdown instanceof  Number)) {
            return;
        }

        startArmingTimer(((Number) countdown).intValue());
    }

    protected void startArmingTimer(int delaySec) {
        cancelArmingTimer();

        updateTime(delaySec);
        armingCountdown = new CountdownTask(delaySec, new CountdownTask.CountdownDelegate() {
            @Override
            public void onTimerTicked(int remainingSeconds) {
                updateTime(remainingSeconds);
            }
        });
        timer.scheduleAtFixedRate(armingCountdown, 1000, 1000);
    }

    protected void cancelArmingTimer() {
        armingCountdown.cancel();
        armingCountdown.setRemainingSec(0);
    }

    protected void onArmingError(String mode, Throwable cause) {
        logger.warn("Arming error", cause);

        SecuritySubsystem security = get();
        if(security == null) {
            return;
        }

        if(!(cause instanceof ErrorResponseException)) {
            onRequestError(cause);
            return;
        }

        String code = ((ErrorResponseException) cause).getCode();
        if(!"TriggeredDevices".equals(code)) {
            onRequestError(cause);
            return;
        }

        AlarmCallback callback = alarmCallback.get();
        if(callback != null) {
            PromptUnsecuredModel model = new PromptUnsecuredModel();
            model.setTitle( getUnsecuredText() );
            model.setMode(mode);
            model.setBypassedDeviceNames(getTriggeredDeviceNames(security));
            callback.promptUnsecured(model);
        }
    }

    protected void onRequestError(Throwable cause) {
        // TODO show the user something?
        logger.warn("Unable to complete request", cause);
    }

    protected void updateAlarmState() {
        AlarmCallback callback = alarmCallback.get();
        if(callback == null) {
            return;
        }

        SecuritySubsystem security = get();
        if(security == null) {
            return;
        }

        if (!RuleModelProvider.instance().isLoaded()) {
            RuleModelProvider.instance().load().onSuccess(Listeners.runOnUiThread(new Listener<List<RuleModel>>() {
                @Override
                public void onEvent(List<RuleModel> ruleModels) {
                    updateAlarmState(); // Should be loaded already if not, we need the callback to update the view.
                }
            }));
        }

        String state = security.getAlarmState();
        switch(state) {
            case SecuritySubsystem.ALARMSTATE_DISARMED:
            case SecuritySubsystem.ALARMSTATE_CLEARING:
                callback.showOff(date(security.getLastDisarmedTime()));
                cancelArmingTimer();  // clear out any old state
                break;

            case SecuritySubsystem.ALARMSTATE_ARMING:
                callback.showArming( getArmingModel(security) );
                syncArmingTimer(); // make sure the timer is in sync if it was set from somewhere else
                break;

            case SecuritySubsystem.ALARMSTATE_ARMED:
                callback.showArmed( getArmedModel(security) );
                cancelArmingTimer();  // clear out any old state
                break;

            case SecuritySubsystem.ALARMSTATE_ALERT:
            case SecuritySubsystem.ALARMSTATE_SOAKING:
                List<Trigger> triggers = getTriggerModel( security );
                if (triggers.isEmpty()) {
                    triggers.add(Trigger.panic(date(security.getLastAlertTime())));
                }

                callback.showAlert(triggers.get(triggers.size() - 1), date(security.getLastAlertTime()), triggers);
                cancelArmingTimer();  // clear out any old state
                break;

        }
    }

    protected void updateButtonState() {
        ButtonCallback callback = buttonCallback.get();
        if(callback == null) {
            return;
        }

        SecuritySubsystem security = get();
        if(security == null) {
            return;
        }

        String state = security.getAlarmState();
        if (SecuritySubsystem.ALARMSTATE_ALERT.equals(state) || SecuritySubsystem.ALARMSTATE_SOAKING.equals(state)) {
            return;
        }

        updateAllDevices();
        updatePartialDevices();
        updateHistory();
        updateContacts();
    }

    protected void updateAllDevices() {
        ButtonCallback callback = buttonCallback.get();
        if(callback == null) {
            return;
        }

        SecuritySubsystem security = get();
        if(security == null) {
            return;
        }

        callback.updateAllDevices(getDevicesText((SubsystemModel) security, MODE_ON));
    }

    protected void updatePartialDevices() {
        ButtonCallback callback = buttonCallback.get();
        if(callback == null) {
            return;
        }

        SecuritySubsystem security = get();
        if(security == null) {
            return;
        }

        callback.updatePartialDevices(getDevicesText((SubsystemModel) security, MODE_PARTIAL));
    }

    protected void updateHistory() {
        ButtonCallback callback = buttonCallback.get();
        if(callback == null) {
            return;
        }

        SecuritySubsystem security = get();
        if(security == null) {
            return;
        }

//        callback.updateHistory(security.getLastAlertTime());
        security
                .listHistoryEntries(1, null, true)
                .onFailure(onRequestError)
                .onSuccess(singleHistoryLoadedListener);
    }

    protected void onSingleHistoryLoaded(HistoryLogEntries entries) {
        ButtonCallback callback = buttonCallback.get();
        if(callback != null && entries != null && entries.getEntries().size() > 0) {
            callback.updateHistory(entries.getEntries().get(0).getTimestamp());
        }
    }

    protected void updateContacts() {
        ButtonCallback callback = buttonCallback.get();
        if(callback == null) {
            return;
        }

        SecuritySubsystem security = get();
        if(security == null) {
            return;
        }

        // TODO determine where to grab the actual images
        if(Boolean.TRUE.equals(security.getCallTreeEnabled())) {
            List<String> icons = getCallTree(security);
            if(icons.isEmpty()) {
                callback.showBasicContact(icons);
            }
            else {
                callback.showAllContacts(icons);
            }
        }
        else {
            List<String> icons = getCallTree(security);
            callback.showBasicContact(icons);
        }
    }

    protected List<String> getCallTree(SecuritySubsystem security) {
        List<String> icons = new ArrayList<>();
        List<Map<String, Object>> callTree = security.getCallTree();
        if(callTree != null) {
            for(Map<String, Object> callTreeEntry: security.getCallTree()) {
                Object enabled = callTreeEntry.get(CallTreeEntry.ATTR_ENABLED);
                if(Boolean.TRUE.equals(enabled)) {
                    icons.add(callTreeEntry.get(CallTreeEntry.ATTR_PERSON).toString());
                }
            }
        }
        return icons;
    }

    protected void updateTime(int timeRemaining) {
        AlarmCallback callback = alarmCallback.get();
        if(callback == null) {
            return;
        }

        SecuritySubsystem subsystem = get();
        if(subsystem == null || !SecuritySubsystem.ALARMSTATE_ARMING.equals(subsystem.getAlarmState())) {
            return;
        }

        callback.updateArming(timeRemaining);
    }

    private int count(Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    private Date date(Date date) {
        return date == null ? new Date() : date;
    }

    public ListenerRegistration setAlarmCallback(AlarmCallback callback) {
        if(alarmCallback.get() != null) {
            logger.warn("Replacing existing callback");
        }
        alarmCallback = new WeakReference<AlarmCallback>(callback);
        updateAlarmState();
        // TODO move this down to base subsystem controller
        if(subsystem.isLoaded()) {
            subsystem.get().refresh();
        }
        else {
            subsystem.load();
        }
        return Listeners.wrap(alarmCallback);
    }

    public ListenerRegistration setButtonCallback(ButtonCallback callback) {
        if(buttonCallback.get() != null) {
            logger.warn("Replacing existing callback");
        }
        buttonCallback = new WeakReference<ButtonCallback>(callback);
        updateButtonState();
        return Listeners.wrap(buttonCallback);
    }

    public void arm(final String mode) {
        SecuritySubsystem security = get();
        if(security == null) {
            logger.warn("Ignoring arm request because subsystem is not loaded");
            return;
        }
        Listener<Throwable> handleArmingError = Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable cause) {
                onArmingError(mode, cause);
            }
        });
        security
                .arm(mode)
                .onSuccess(armResponseListener)
                .onFailure(handleArmingError);
    }

    public void armBypassed(String mode) {
        SecuritySubsystem security = get();
        if(security == null) {
            logger.warn("Ignoring armBypassed request because subsystem is not loaded");
            return;
        }
        security
                .armBypassed(mode)
                .onSuccess(armBypassedResponseListener);
    }

    public void armNow() {
        // not yet supported
    }

    public void disarm() {
        SecuritySubsystem security = get();
        if(security == null) {
            logger.warn("Ignoring disarm request because subsystem is not loaded");
            return;
        }
        security.disarm();
    }

    public ListenerRegistration setHistoryLogCallback(HistoryLogCallback callback) {
        historyLogCallback = new WeakReference<>(callback);

        return Listeners.wrap(historyLogCallback);
    }

    public void loadHistory(@Nullable Integer limit, @Nullable String token) {
        SecuritySubsystem security = get();
        if (security == null) {
            return; // TODO: Error;
        }

        if (limit == null || limit < 1) {
            limit = 20;
        }

        security
                .listHistoryEntries(limit, token, true)
                .onFailure(onRequestError)
                .onSuccess(historyLoadedListener);
    }

    protected void onHistoryLoaded(HistoryLogEntries entries) {
        HistoryLogCallback callback = historyLogCallback.get();
        if (callback != null) {
            callback.historyLoaded(entries);
        }
    }

    public interface HistoryLogCallback {
        void historyLoaded(HistoryLogEntries entries);
    }

    public interface AlarmCallback {

        void showOff(Date offSince);

        void showArming(ArmingModel model);

        void updateArming(int secondsRemaining);

        void showArmed(ArmedModel model);

        void showAlert(Trigger cause, Date alarmSince, List<Trigger> allAlerts);

        void promptUnsecured(PromptUnsecuredModel model);

    }

    public interface ButtonCallback {

        /**
         * Updates the description for the all devices button
         * @param label
         */
        void updateAllDevices(String label);

        /**
         * Updates teh description for the partial devices button
         * @param label
         */
        void updatePartialDevices(String label);

        /**
         * Updates the last event time of the history button
         * @param lastEvent
         */
        void updateHistory(Date lastEvent);

        /**
         * Switches the contact button to 'basic' mode
         * @param personIcon
         */
        void showBasicContact(List<String> personIcon);

        /**
         * Switches the contact button to 'all' mode
         * @param personIcons
         */
        void showAllContacts(List<String> personIcons);

    }

}
