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

import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.security.model.SettingsModel;
import arcus.cornea.utils.CapabilityInstances;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Capability;
import com.iris.client.capability.SecurityAlarmMode;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


public class SecuritySettingsController extends BaseSubsystemController<SecuritySettingsController.Callback> {
    private static final SecuritySettingsController instance;
    private static final Logger logger = LoggerFactory.getLogger(SecuritySettingsController.class);

    static {
        instance = new SecuritySettingsController(SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE));
        instance.init();
    }

    public static SecuritySettingsController instance() {
        return instance;
    }

    private final Listener<Throwable> onFailure = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onFailure(throwable);
        }
    });

    SecuritySettingsController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public void setExitDelayOnSec(int exitDelayOnSec) {
        ClientRequest request = setAttributes(
                SecuritySubsystem.ALARMMODE_ON,
                SecurityAlarmMode.ATTR_EXITDELAYSEC,
                exitDelayOnSec
        );
        sendRequest(request);
    }

    public void setEntranceDelayOnSec(int entranceDelayOnSec) {
        ClientRequest request = setAttributes(
                SecuritySubsystem.ALARMMODE_ON,
                SecurityAlarmMode.ATTR_ENTRANCEDELAYSEC,
                entranceDelayOnSec
        );
        sendRequest(request);
    }

    public void setExitDelayPartialSec(int exitDelayPartialSec) {
        ClientRequest request = setAttributes(
                SecuritySubsystem.ALARMMODE_PARTIAL,
                SecurityAlarmMode.ATTR_EXITDELAYSEC,
                exitDelayPartialSec
        );
        sendRequest(request);
    }

    public void setEntranceDelayPartialSec(int entranceDelayPartialSec) {
        ClientRequest request = setAttributes(
                SecuritySubsystem.ALARMMODE_PARTIAL,
                SecurityAlarmMode.ATTR_ENTRANCEDELAYSEC,
                entranceDelayPartialSec
        );
        sendRequest(request);
    }

    /**
     * Sets the alarm sensitivity for both ON and PARTIAL modes
     * @param alarmSensitivity
     */
    public void setAlarmSensitivity(int alarmSensitivity) {
        ClientRequest request = setAttributes(
                SecurityAlarmMode.ATTR_ALARMSENSITIVITYDEVICECOUNT,
                alarmSensitivity
        );
        sendRequest(request);
    }

    public void setAlarmSensitivityOnMode(int alarmSensitivityOnMode) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(CapabilityInstances.getAttributeName(SecuritySubsystem.ALARMMODE_ON, SecurityAlarmMode.ATTR_ALARMSENSITIVITYDEVICECOUNT), alarmSensitivityOnMode);
        sendRequest(request);
    }

    public void setAlarmSensitivityPartialMode(int alarmSensitivityPartialMode) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(CapabilityInstances.getAttributeName(SecuritySubsystem.ALARMMODE_PARTIAL, SecurityAlarmMode.ATTR_ALARMSENSITIVITYDEVICECOUNT), alarmSensitivityPartialMode);
        sendRequest(request);
    }

    public void setEnableSounds(boolean enableSounds) {
        ClientRequest request = setAttributes(
                SecurityAlarmMode.ATTR_SOUNDSENABLED,
                enableSounds
        );
        sendRequest(request);
    }

    public void setSilentAlarm(boolean silentAlarm) {
        ClientRequest request = setAttributes(
                SecurityAlarmMode.ATTR_SILENT,
                silentAlarm
        );
        sendRequest(request);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        for(String key: event.getChangedAttributes().keySet()) {
            if(key.startsWith(SecurityAlarmMode.NAMESPACE)) {
                updateView();
                return;
            }
        }
    }

    @Override
    protected void updateView(Callback callback) {
        SubsystemModel subsystem = getModel();
        if(subsystem == null) {
            return;
        }

        SettingsModel model = getSettings(subsystem);
        callback.updateSettings(model);
    }

    protected SettingsModel getSettings(SubsystemModel subsystem) {
        SettingsModel settings = new SettingsModel();
        settings.setEntranceDelayOnSec(integer(getOnAttribute(subsystem, SecurityAlarmMode.ATTR_ENTRANCEDELAYSEC)));
        settings.setEntranceDelayPartialSec(integer(getPartialAttribute(subsystem, SecurityAlarmMode.ATTR_ENTRANCEDELAYSEC)));
        settings.setExitDelayOnSec(integer(getOnAttribute(subsystem, SecurityAlarmMode.ATTR_EXITDELAYSEC)));
        settings.setExitDelayPartialSec(integer(getPartialAttribute(subsystem, SecurityAlarmMode.ATTR_EXITDELAYSEC)));
        settings.setAlarmSensitivity(integer(getOnAttribute(subsystem, SecurityAlarmMode.ATTR_ALARMSENSITIVITYDEVICECOUNT)));
        settings.setEnableSounds(bool(getOnAttribute(subsystem, SecurityAlarmMode.ATTR_SOUNDSENABLED)));
        settings.setSilentAlarm(bool(getOnAttribute(subsystem, SecurityAlarmMode.ATTR_SILENT)));
        settings.setTotalOnDevices(set(getOnDevices(subsystem, SecurityAlarmMode.ATTR_DEVICES)).size());
        settings.setTotalPartialDevices(set(getPartialDevices(subsystem, SecurityAlarmMode.ATTR_DEVICES)).size());
        settings.setOnAlarmSensitivity(integer(getOnAttribute(subsystem, SecurityAlarmMode.ATTR_ALARMSENSITIVITYDEVICECOUNT)));
        settings.setPartialAlarmSensitivity(integer(getPartialAttribute(subsystem, SecurityAlarmMode.ATTR_ALARMSENSITIVITYDEVICECOUNT)));
        settings.setOnMotionSensorsCount(integer(getOnAttribute(subsystem, SecurityAlarmMode.ATTR_MOTIONSENSORCOUNT)));
        settings.setPartialMotionSensorsCount(integer(getPartialAttribute(subsystem, SecurityAlarmMode.ATTR_MOTIONSENSORCOUNT)));
        return settings;
    }

    @SuppressWarnings("unchecked")
    protected Collection<Object> getOnDevices(SubsystemModel subsystem, String attribute) {
        return (Collection<Object>) CapabilityInstances.getAttributeValue(subsystem, SecuritySubsystem.ALARMMODE_ON, attribute);
    }

    @SuppressWarnings("unchecked")
    protected Collection<Object> getPartialDevices(SubsystemModel subsystem, String attribute) {
        return (Collection<Object>) CapabilityInstances.getAttributeValue(subsystem, SecuritySubsystem.ALARMMODE_PARTIAL, attribute);
    }

    protected Object getOnAttribute(SubsystemModel subsystem, String attribute) {
        return CapabilityInstances.getAttributeValue(subsystem, SecuritySubsystem.ALARMMODE_ON, attribute);
    }

    protected Object getPartialAttribute(SubsystemModel subsystem, String attribute) {
        return CapabilityInstances.getAttributeValue(subsystem, SecuritySubsystem.ALARMMODE_PARTIAL, attribute);
    }

    protected ClientRequest setAttributes(String instance, String attribute, Object value) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(CapabilityInstances.getAttributeName(instance, attribute), value);
        return request;
    }

    /**
     * Sets an attribute on both PARTIAL and ON mode.
     * @param attribute
     * @param value
     * @return
     */
    protected ClientRequest setAttributes(String attribute, Object value) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(CapabilityInstances.getAttributeName(SecuritySubsystem.ALARMMODE_ON, attribute), value);
        request.setAttribute(CapabilityInstances.getAttributeName(SecuritySubsystem.ALARMMODE_PARTIAL, attribute), value);
        return request;
    }

    protected void sendRequest(ClientRequest request) {
        SubsystemModel model = getModel();
        if(model == null) {
            logger.warn("Unable to send request [{}] because model is not yet loaded", request);
            return;
        }

        model
            .request(request)
            .onFailure(onFailure)
            ;
    }

    protected void onFailure(Throwable cause) {
        Callback callback = getCallback();
        if(callback == null) {
            logger.debug("Not showing error {} because no callback is registered", cause);
            return;
        }

        ErrorModel error = Errors.translate(cause);
        callback.showError(error);
    }

    public interface Callback {

        void updateSettings(SettingsModel model);

        void showError(ErrorModel error);
    }

}
