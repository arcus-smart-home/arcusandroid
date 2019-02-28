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

import arcus.cornea.utils.CapabilityInstances;
import com.iris.client.capability.SecurityAlarmMode;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.model.ModelChangedEvent;

import java.util.Collection;
import java.util.Set;



public class PromonSecurityDeviceListController extends BaseSecurityController<PromonSecurityDeviceListController.Callback> {

    private final static PromonSecurityDeviceListController instance = new PromonSecurityDeviceListController();

    public interface Callback {
        public void onParticipatingDevicesChanged(Set<String> securityDevices,
                                                  Set<String> triggeredDevices,
                                                  Set<String> readyDevices,
                                                  Set<String> armedDevices,
                                                  Set<String> bypassedDevices,
                                                  Set<String> offlineDevices,
                                                  Set<String> onModeDevices,
                                                  Set<String> partialModeDevices);
    }

    private PromonSecurityDeviceListController() {}

    public static PromonSecurityDeviceListController getInstance() {
        return instance;
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

        Set<String> changes = event.getChangedAttributes().keySet();

        if (changes.contains(SecuritySubsystem.ATTR_SECURITYDEVICES) ||
                changes.contains(SecuritySubsystem.ATTR_TRIGGEREDDEVICES) ||
                changes.contains(SecuritySubsystem.ATTR_READYDEVICES) ||
                changes.contains(SecuritySubsystem.ATTR_ARMEDDEVICES) ||
                changes.contains(SecuritySubsystem.ATTR_BYPASSEDDEVICES) ||
                changes.contains(SecuritySubsystem.ATTR_OFFLINEDEVICES) ||
                changes.contains(SecurityAlarmMode.ATTR_DEVICES))
        {
            updateView(getCallback());
        }
    }

    @Override
    protected void updateView(PromonSecurityDeviceListController.Callback callback) {
        SecuritySubsystem model = getSecuritySubsystem();

        if(model == null) {
            return;
        }

        getCallback().onParticipatingDevicesChanged(
                model.getSecurityDevices(),
                model.getTriggeredDevices(),
                model.getReadyDevices(),
                model.getArmedDevices(),
                model.getBypassedDevices(),
                model.getOfflineDevices(),
                set((Collection<String>) CapabilityInstances.getAttributeValue(getModel(), SecuritySubsystem.ALARMMODE_ON, SecurityAlarmMode.ATTR_DEVICES)),
                set((Collection<String>) CapabilityInstances.getAttributeValue(getModel(), SecuritySubsystem.ALARMMODE_PARTIAL, SecurityAlarmMode.ATTR_DEVICES)));
    }


}
