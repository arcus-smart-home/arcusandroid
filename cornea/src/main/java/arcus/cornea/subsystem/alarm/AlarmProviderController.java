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

import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.model.AlarmSubsystemModel;
import com.iris.client.model.ModelChangedEvent;

import java.util.Set;



public class AlarmProviderController
        extends BaseSubsystemController<AlarmProviderController.Callback> {

    private boolean isAlarmProviderHub = false;

    public AlarmProviderController () {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public interface Callback {
        void onAlarmProviderChanged(boolean isAlarmProviderTheHub);
        void onAlarmStateChanged(String incidentAddress);
        void onSecurityModeChanged(String newMode);
        void onAvailableAlertsChanged(Set<String> newAvailableAlerts);
        void onError(Throwable error);
    }

    public void requestUpdate() {
        if (getCallback() != null) {
            updateView(getCallback());
        }
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {

        if (getCallback() == null) {
            return;     // Nothing to do
        }

        AlarmSubsystem model = (AlarmSubsystem) getModel();

        Set<String> changes = event.getChangedAttributes().keySet();

        if (changes.contains(AlarmSubsystemModel.ATTR_ALARMPROVIDER)) {
            isAlarmProviderHub = checkAlarmProviderIsHub(model);
            getCallback().onAlarmProviderChanged(isAlarmProviderHub);
        }

        if (changes.contains(AlarmSubsystemModel.ATTR_ALARMSTATE)) {
            getCallback().onAlarmStateChanged(model.getAlarmState());
        }

        if (changes.contains(AlarmSubsystemModel.ATTR_SECURITYMODE))
        {
            getCallback().onSecurityModeChanged(model.getSecurityMode());
        }

        if (changes.contains(AlarmSubsystemModel.ATTR_AVAILABLEALERTS)) {
            getCallback().onAvailableAlertsChanged(model.getAvailableAlerts());
        }

    }

    @Override
    protected void updateView(AlarmProviderController.Callback callback) {
        AlarmSubsystem model = (AlarmSubsystem) getModel();

        if(model == null ) {
            return;
        }
         isAlarmProviderHub = checkAlarmProviderIsHub(model);

        callback.onAlarmProviderChanged(isAlarmProviderHub);
        callback.onAlarmStateChanged(model.getAlarmState());
        callback.onSecurityModeChanged(model.getSecurityMode());
        callback.onAvailableAlertsChanged(model.getAvailableAlerts());
    }

    private boolean checkAlarmProviderIsHub(AlarmSubsystem alarmModel) {
        if (alarmModel.getAlarmProvider().equals(AlarmSubsystemModel.ALARMPROVIDER_HUB)) {
            return true;
        }
        else {
            return false;
        }
    }

}
