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
package arcus.cornea.subsystem.alarm;

import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.alarm.model.AlarmModel;
import arcus.cornea.utils.CapabilityInstances;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;

import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.Subsystem;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.model.AlarmSubsystemModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class AlarmSubsystemController extends BaseSubsystemController<AlarmSubsystemController.Callback> {

    private final static AlarmSubsystemController instance = new AlarmSubsystemController();

    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(final Throwable throwable) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    Callback cb = getCallback();
                    if (cb != null) {
                        cb.onError(throwable);
                    }
                }
            });
        }
    });

    private AlarmSubsystemController() {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public interface Callback {
        void onAlarmStateChanged(String newAlarmState);
        void onSecurityModeChanged(String newMode);
        void onAlertsChanged(List<String> activeAlerts, Set<String> availableAlerts, Set<String> monitoredAlerts);
        void onIncidentChanged(String incidentAddress);
        void onAlarmsChanged(List<AlarmModel> alarmModels);
        void onSubsystemAvailableChange(boolean available);
        void onError(Throwable error);
        void onActivateComplete();

    }

    public static AlarmSubsystemController getInstance() {
        return instance;
    }

    public void requestUpdate() {
        if (getCallback() != null) {
            updateView(getCallback());
        }
    }

    public void activate() {
        if(getModel() != null) {
            if(!(getModel()).getAvailable()) {
                (getModel()).activate()
                        .onFailure(Listeners.runOnUiThread(onErrorListener))
                        .onSuccess(Listeners.runOnUiThread(new Listener<Subsystem.ActivateResponse>() {
                            @Override
                            public void onEvent(Subsystem.ActivateResponse response) {
                                Callback cb = getCallback();
                                if (cb != null) {
                                    cb.onActivateComplete();
                                }
                            }
                        }));
            }
        }
    }

    public Date getSecurityLastArmedTime() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getLastArmedTime();
    }

    public Date getSecurityLastDisrmedTime() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getLastDisarmedTime();
    }

    public String getSecurityMode() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getSecurityMode();
    }

    public boolean isAlarmStateClearing() {
        boolean isAlarmStateClearing = false;

        if (getModel() != null  && AlarmSubsystem.ALARMSTATE_CLEARING.equals(((AlarmSubsystem)getModel()).getAlarmState())) {
            isAlarmStateClearing = true;
        }

        return isAlarmStateClearing;
    }

    public Boolean getRecordingSupported() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getRecordingSupported();
    }

    public Boolean getRecordOnSecurity() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getRecordOnSecurity();
    }

    public void setRecordOnSecurity(boolean recordOnSecurity) {
        ((AlarmSubsystem)getModel()).setRecordOnSecurity(recordOnSecurity);
        getModel().commit();

    }

    public Boolean getShutOffFansSupported() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getFanShutoffSupported();
    }

    public Boolean getShutOffFanOnSmoke() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getFanShutoffOnSmoke();
    }

    public String getAlarmProvider() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getAlarmProvider();
    }

    public void setShutOffFanOnSmoke(boolean shutOffFanOnSmoke) {
        ((AlarmSubsystem)getModel()).setFanShutoffOnSmoke(shutOffFanOnSmoke);
        getModel().commit();

    }

    public Boolean getShutOffFanOnCO() {
        return getModel() == null ? null : ((AlarmSubsystem)getModel()).getFanShutoffOnCO();
    }

    public void setShutOffFanOnCO(boolean shutOffFanOnCO) {
        ((AlarmSubsystem)getModel()).setFanShutoffOnCO(shutOffFanOnCO);
        getModel().commit();

    }

    public ClientFuture<AlarmSubsystem.ListIncidentsResponse> requestIncidentList() {
        return ((AlarmSubsystem) getModel()).listIncidents();
    }

    public String getCurrentIncident() {
        if (getModel() != null) {
            return ((AlarmSubsystem) getModel()).getCurrentIncident();
        } else {
            return null;
        }
    }

    public Map<String,Boolean> getAlarmActivations() {
        Map<String,Boolean> activations = new HashMap<>();

        activations.put(AlarmSubsystem.AVAILABLEALERTS_PANIC, getModel() != null && !com.iris.client.model.AlarmModel.ALERTSTATE_INACTIVE.equals(string(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.AVAILABLEALERTS_PANIC, com.iris.client.model.AlarmModel.ATTR_ALERTSTATE))));
        activations.put(AlarmSubsystem.AVAILABLEALERTS_SECURITY, getModel() != null && !com.iris.client.model.AlarmModel.ALERTSTATE_INACTIVE.equals(string(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.AVAILABLEALERTS_SECURITY, com.iris.client.model.AlarmModel.ATTR_ALERTSTATE))));
        activations.put(AlarmSubsystem.AVAILABLEALERTS_CO, getModel() != null && !com.iris.client.model.AlarmModel.ALERTSTATE_INACTIVE.equals(string(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.AVAILABLEALERTS_CO, com.iris.client.model.AlarmModel.ATTR_ALERTSTATE))));
        activations.put(AlarmSubsystem.AVAILABLEALERTS_SMOKE, getModel() != null && !com.iris.client.model.AlarmModel.ALERTSTATE_INACTIVE.equals(string(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.AVAILABLEALERTS_SMOKE, com.iris.client.model.AlarmModel.ATTR_ALERTSTATE))));
        activations.put(AlarmSubsystem.AVAILABLEALERTS_WATER, getModel() != null && !com.iris.client.model.AlarmModel.ALERTSTATE_INACTIVE.equals(string(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.AVAILABLEALERTS_WATER, com.iris.client.model.AlarmModel.ATTR_ALERTSTATE))));

        return activations;
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {

        if (getCallback() == null) {
            return;     // Nothing to do
        }

        AlarmSubsystem model = (AlarmSubsystem) getModel();
        Set<String> changes = event.getChangedAttributes().keySet();

        if (changes.contains(AlarmSubsystemModel.ATTR_SECURITYMODE))
        {
            getCallback().onSecurityModeChanged(model.getSecurityMode());
        }

        if (changes.contains(AlarmSubsystemModel.ATTR_ACTIVEALERTS) ||
            changes.contains(AlarmSubsystemModel.ATTR_AVAILABLEALERTS) ||
            changes.contains(AlarmSubsystemModel.ATTR_MONITOREDALERTS))
        {
            getCallback().onAlertsChanged(model.getActiveAlerts(), model.getAvailableAlerts(), model.getMonitoredAlerts());
        }

        if (changes.contains(AlarmSubsystemModel.ATTR_ALARMSTATE)) {
            getCallback().onAlarmStateChanged(model.getAlarmState());
        }

        if (changes.contains(AlarmSubsystemModel.ATTR_CURRENTINCIDENT)) {
            getCallback().onIncidentChanged(model.getCurrentIncident());
        }

        if (changes.contains(SubsystemModel.ATTR_STATE)) {
            getCallback().onSubsystemAvailableChange(!SubsystemModel.STATE_SUSPENDED.equals(model.getState()));
        }
        else {
            getCallback().onAlarmsChanged(buildAlarmModels(getModel()));
        }
    }

    private List<AlarmModel> buildAlarmModels(SubsystemModel subsystem) {
        List<AlarmModel> alarmModels = new ArrayList<>();

        String alarmInstances[] = new String[] {
                AlarmSubsystem.ACTIVEALERTS_SECURITY,
                AlarmSubsystem.ACTIVEALERTS_CARE,
                AlarmSubsystem.ACTIVEALERTS_CO,
                AlarmSubsystem.ACTIVEALERTS_PANIC,
                AlarmSubsystem.ACTIVEALERTS_SMOKE,
                AlarmSubsystem.ACTIVEALERTS_WATER,
                AlarmSubsystem.ACTIVEALERTS_WEATHER
        };

        for (String thisAlarmInstance : alarmInstances) {
            alarmModels.add(buildAlarmModel(subsystem, thisAlarmInstance));
        }

        return alarmModels;
    }

    private AlarmModel buildAlarmModel(SubsystemModel subsystem, String alarmInstance) {
        AlarmModel alarmModel = new AlarmModel();

        alarmModel.setType(alarmInstance);
        alarmModel.setAlertState(string(CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_ALERTSTATE)));
        alarmModel.setDevices(set((Collection<String>) CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_DEVICES)));
        alarmModel.setExcludedDevices(set((Collection<String>) CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_EXCLUDEDDEVICES)));
        alarmModel.setActiveDevices(set((Collection<String>) CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_ACTIVEDEVICES)));
        alarmModel.setOfflineDevices(set((Collection<String>) CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_OFFLINEDEVICES)));
        alarmModel.setTriggeredDevices(set((Collection<String>) CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_TRIGGEREDDEVICES)));
        alarmModel.setMonitored(bool(CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_MONITORED)));
        alarmModel.setSilent(bool(CapabilityInstances.getAttributeValue(subsystem, alarmInstance, com.iris.client.model.AlarmModel.ATTR_SILENT)));

        return alarmModel;
    }

    @Override
    protected void updateView(AlarmSubsystemController.Callback callback) {
        AlarmSubsystem model = (AlarmSubsystem) getModel();

        if(model == null || callback == null) {
            return;
        }

        callback.onAlertsChanged(model.getActiveAlerts(), model.getAvailableAlerts(), model.getMonitoredAlerts());
        callback.onSecurityModeChanged(model.getSecurityMode());
        callback.onAlarmStateChanged(model.getAlarmState());
        callback.onIncidentChanged(model.getCurrentIncident());
        callback.onAlarmsChanged(buildAlarmModels(getModel()));
        callback.onSubsystemAvailableChange(!SubsystemModel.STATE_SUSPENDED.equals(model.getState()));
    }

}
