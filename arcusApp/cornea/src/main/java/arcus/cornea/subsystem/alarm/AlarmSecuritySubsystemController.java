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

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.alarm.model.AlertDeviceStateModel;
import arcus.cornea.subsystem.security.CountdownTask;

import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.DeviceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;



public class AlarmSecuritySubsystemController extends BaseSubsystemController<AlarmSecuritySubsystemController.Callback> {

    private final static AlarmSecuritySubsystemController instance = new AlarmSecuritySubsystemController();
    private final static Logger logger = LoggerFactory.getLogger(AlarmSecuritySubsystemController.class);

    private CountdownTask countdownTask = new CountdownTask(0, null);
    private Timer timer = new Timer();
    private boolean isPartialMode;

    public static AlarmSecuritySubsystemController getInstance() {
        return instance;
    }

    private AlarmSecuritySubsystemController() {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public interface Callback {
        void onAlarmSecurityError(Throwable t);

        void onArmedSuccessfully(int remainingSeconds);

        void onRequiresBypass(List<AlertDeviceStateModel> deviceNames, boolean isPartialMode);

        void onDisarmedSuccessfully();
    }

    public void armOn(boolean bypassed) {
        AlarmSubsystem model = (AlarmSubsystem) getModel();
        isPartialMode = false;

        if (bypassed) {
            model.armBypassed(AlarmSubsystem.SECURITYMODE_ON).onSuccess(armBypassResponseListener).onFailure(errorListener);
        } else {
            model.arm(AlarmSubsystem.SECURITYMODE_ON).onSuccess(armOnResponseListener).onFailure(errorListener);
        }
    }

    public void armPartial(boolean bypassed) {
        AlarmSubsystem model = (AlarmSubsystem) getModel();
        isPartialMode = true;

        if (bypassed) {
            model.armBypassed(AlarmSubsystem.SECURITYMODE_PARTIAL).onSuccess(armBypassResponseListener).onFailure(errorListener);
        } else {
            model.arm(AlarmSubsystem.SECURITYMODE_PARTIAL).onSuccess(armOnResponseListener).onFailure(errorListener);
        }
    }

    public void disarm() {
        AlarmSubsystem model = (AlarmSubsystem) getModel();

        timer.cancel();     // Stop counting down if counting
        timer = new Timer();
        model.disarm().onSuccess(disarmResponseListener).onFailure(errorListener);
    }

    private Listener<Throwable> errorListener = new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            if (getCallback() != null) {

                if (isTriggeredDeviceError(throwable)) {
                    String[] triggeredDeviceAddresses = getTriggeredDeviceAddresses((ErrorResponseException) throwable);
                    List<AlertDeviceStateModel> triggeredDeviceNames = getDeviceNamesAndStates(triggeredDeviceAddresses);
                    getCallback().onRequiresBypass(triggeredDeviceNames, isPartialMode);
                }

                else {
                    getCallback().onAlarmSecurityError(throwable);
                }
            }
        }
    };

    private Listener<AlarmSubsystem.DisarmResponse> disarmResponseListener = new Listener<AlarmSubsystem.DisarmResponse>() {
        @Override
        public void onEvent(AlarmSubsystem.DisarmResponse armResponse) {
            if (getCallback() != null) {
                getCallback().onDisarmedSuccessfully();
            }
        }
    };

    private Listener<AlarmSubsystem.ArmResponse> armOnResponseListener = new Listener<AlarmSubsystem.ArmResponse>() {
        @Override
        public void onEvent(AlarmSubsystem.ArmResponse armResponse) {
            countdownTask = new CountdownTask(armResponse.getDelaySec(), countdownTick);
            timer.scheduleAtFixedRate(countdownTask, 0, 1000);
        }
    };

    private Listener<AlarmSubsystem.ArmBypassedResponse> armBypassResponseListener = new Listener<AlarmSubsystem.ArmBypassedResponse>() {
        @Override
        public void onEvent(AlarmSubsystem.ArmBypassedResponse armResponse) {
            countdownTask = new CountdownTask(armResponse.getDelaySec(), countdownTick);
            timer.scheduleAtFixedRate(countdownTask, 0, 1000);
        }
    };

    private CountdownTask.CountdownDelegate countdownTick = new CountdownTask.CountdownDelegate() {
        @Override
        public void onTimerTicked(int remainingSeconds) {
            if (getCallback() != null) {
                getCallback().onArmedSuccessfully(remainingSeconds);
            }
        }
    };

    private boolean isTriggeredDeviceError(Throwable t) {
        return t instanceof ErrorResponseException && ((ErrorResponseException) t).getCode().equalsIgnoreCase("security.triggeredDevices");
    }

    private String[] getTriggeredDeviceAddresses(ErrorResponseException error) {
        if (error.getCode().equalsIgnoreCase("security.triggeredDevices")) {
            return error.getErrorMessage().split(",");
        }

        return new String[] {};
    }

    private List<String> getDeviceNames(String[] deviceAddresses) {
        List<String> deviceNames = new ArrayList<>();

        for (String thisDeviceAddress : deviceAddresses) {
            DeviceModel deviceModel = DeviceModelProvider.instance().getStore().get(thisDeviceAddress.split(":")[2]);
            if (deviceModel != null) {
                deviceNames.add(deviceModel.getName());
            } else {
                logger.error("No device model in cache for " + thisDeviceAddress);
            }
        }

        return deviceNames;
    }

    private List<AlertDeviceStateModel> getDeviceNamesAndStates(String[] deviceAddresses) {
        List<AlertDeviceStateModel> deviceNamesAndStates = new ArrayList<>();

        for (String thisDeviceAddress : deviceAddresses) {
            DeviceModel deviceModel = DeviceModelProvider.instance().getStore().get(thisDeviceAddress.split(":")[2]);
            if (deviceModel != null) {
                deviceNamesAndStates.add(new AlertDeviceStateModel(deviceModel));
            } else {
                logger.error("No device model in cache for " + thisDeviceAddress);
            }
        }

        return deviceNamesAndStates;
    }
}
