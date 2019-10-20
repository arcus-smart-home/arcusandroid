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
import arcus.cornea.utils.CapabilityInstances;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Alarm;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.Capability;
import com.iris.client.event.Listener;
import com.iris.client.model.ModelChangedEvent;

import java.util.Set;



public class AlarmSoundsController extends BaseSubsystemController<AlarmSoundsController.Callback> {

    private final static AlarmSoundsController instance = new AlarmSoundsController();

    private AlarmSoundsController() {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public interface Callback {
        void onSilentAlarmChanged(String alarmInstance, boolean isSilent);

        void onAvailableAlertsChanged(Set<String> alarmsAvailable);

        void onError(Throwable t);
    }

    public static AlarmSoundsController getInstance() {
        return instance;
    }

    public void updateView() {
        getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_PANIC, bool(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.ACTIVEALERTS_PANIC, Alarm.ATTR_SILENT)));
        getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_SECURITY, bool(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.ACTIVEALERTS_SECURITY, Alarm.ATTR_SILENT)));
        getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_SMOKE, bool(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.ACTIVEALERTS_SMOKE, Alarm.ATTR_SILENT)));
        getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_CO, bool(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.ACTIVEALERTS_CO, Alarm.ATTR_SILENT)));
        getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_WATER, bool(CapabilityInstances.getAttributeValue(getModel(), AlarmSubsystem.ACTIVEALERTS_WATER, Alarm.ATTR_SILENT)));

        Set<String> availableAlerts = ((AlarmSubsystem) getModel()).getAvailableAlerts();
        getCallback().onAvailableAlertsChanged(availableAlerts);
    }

    public void setSilentAlarm(final String alarmInstance, final boolean isSilent) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(CapabilityInstances.getAttributeName(alarmInstance, Alarm.ATTR_SILENT), isSilent);
        getModel().request(request).onFailure(failureListener);

        if (getCallback() != null) {
            getCallback().onSilentAlarmChanged(alarmInstance, isSilent);
        }
    }

    protected void onSubsystemChanged(ModelChangedEvent event) {

        if (getCallback() == null) {
            return;     // Nothing to do
        }

        String silentPanic = CapabilityInstances.getAttributeName(AlarmSubsystem.ACTIVEALERTS_PANIC, Alarm.ATTR_SILENT);
        String silentSecurity = CapabilityInstances.getAttributeName(AlarmSubsystem.ACTIVEALERTS_SECURITY, Alarm.ATTR_SILENT);
        String silentSmoke = CapabilityInstances.getAttributeName(AlarmSubsystem.ACTIVEALERTS_SMOKE, Alarm.ATTR_SILENT);
        String silentCo = CapabilityInstances.getAttributeName(AlarmSubsystem.ACTIVEALERTS_CO, Alarm.ATTR_SILENT);
        String silentWater = CapabilityInstances.getAttributeName(AlarmSubsystem.ACTIVEALERTS_WATER, Alarm.ATTR_SILENT);

        if (event.getChangedAttributes().containsKey(silentPanic)) {
            getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_PANIC, bool(event.getChangedAttributes().get(silentPanic)));
        }

        if (event.getChangedAttributes().containsKey(silentSecurity)) {
            getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_SECURITY, bool(event.getChangedAttributes().get(silentSecurity)));
        }

        if (event.getChangedAttributes().containsKey(silentSmoke)) {
            getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_SMOKE, bool(event.getChangedAttributes().get(silentSmoke)));
        }

        if (event.getChangedAttributes().containsKey(silentCo)) {
            getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_CO, bool(event.getChangedAttributes().get(silentCo)));
        }

        if (event.getChangedAttributes().containsKey(silentWater)) {
            getCallback().onSilentAlarmChanged(AlarmSubsystem.ACTIVEALERTS_WATER, bool(event.getChangedAttributes().get(silentWater)));
        }

        if (event.getChangedAttributes().containsKey(AlarmSubsystem.ATTR_AVAILABLEALERTS)){

            Set<String> availableAlerts = ((AlarmSubsystem) getModel()).getAvailableAlerts();
            getCallback().onAvailableAlertsChanged(availableAlerts);
        }
    }

    private Listener<Throwable> failureListener = new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable event) {
            Callback c = getCallback();
            if (c != null) {
                c.onError(event);
            }
        }
    };
}
