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
package arcus.app.subsystems.alarm.promonitoring.presenters;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import arcus.cornea.subsystem.alarm.model.AlarmModel;
import arcus.cornea.subsystem.security.PromonSecurityDeviceListController;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.Contact;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Motion;
import com.iris.client.capability.MotorizedDoor;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.subsystems.alarm.promonitoring.models.AlertDeviceModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmDeviceListPresenter extends AlarmProviderOfflinePresenter<AlarmDeviceListContract.AlarmDeviceListView> implements AlarmDeviceListContract.AlarmDeviceListPresenter, AlarmSubsystemController.Callback, PromonSecurityDeviceListController.Callback {

    private String presentedAlarmType;

    @Override
    public void requestUpdate(String forAlarmType) {
        this.presentedAlarmType = forAlarmType;

        String deviceListListenerId = PromonSecurityDeviceListController.class.getCanonicalName();
        String alarmListenerId = AlarmSubsystemController.class.getCanonicalName();

        // Request for security device list--Alarm subsystem can't provide all the information,
        // so we delegate to SecuritySubsystem
        if (AlarmSubsystem.ACTIVEALERTS_SECURITY.equalsIgnoreCase(forAlarmType)) {
            addListener(deviceListListenerId, PromonSecurityDeviceListController.getInstance().setCallback(this));
            PromonSecurityDeviceListController.getInstance().requestUpdate();
        }

        // Request for smoke, co or water device list--ask AlarmSubsystem for it
        else {
            addListener(alarmListenerId, AlarmSubsystemController.getInstance().setCallback(this));
            AlarmSubsystemController.getInstance().requestUpdate();
        }
    }

    @Override
    public void onAlarmStateChanged(String newAlarmState) {
        // Nothing to do
    }

    @Override
    public void onSecurityModeChanged(String newMode) {
        // Nothing to do
    }

    @Override
    public void onAlertsChanged(List<String> activeAlerts, Set<String> availableAlerts, Set<String> monitoredAlerts) {
        // Nothing to do
    }

    @Override
    public void onIncidentChanged(String incidentAddress) {
        // Nothing to do
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onActivateComplete() {

    }

    @Override
    public void onAlarmsChanged(final List<AlarmModel> alarmModels) {
        DeviceModelProvider.instance().reload().onSuccess(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {

                AlarmModel presentedModel = getModelForType(presentedAlarmType, alarmModels);

                switch (presentedAlarmType.toUpperCase()) {
                    case AlarmSubsystem.AVAILABLEALERTS_SMOKE:
                    case AlarmSubsystem.AVAILABLEALERTS_CO:
                    case AlarmSubsystem.AVAILABLEALERTS_WATER:
                        present(buildDeviceItems(deviceModels, presentedModel));
                        break;

                    default:
                        throw new IllegalArgumentException("Bug! Unimplemented alarm type: " + presentedAlarmType);
                }
            }
        });
    }

    @Override
    public void onSubsystemAvailableChange(boolean available) {

    }

    @Override
    public void onParticipatingDevicesChanged(final Set<String> securityDevices, Set<String> triggeredDevices, final Set<String> readyDevices, final Set<String> armedDevices, final Set<String> bypassedDevices, final Set<String> offlineDevices, final Set<String> onModeDevices, final Set<String> partialModeDevices) {
        DeviceModelProvider.instance().reload().onSuccess(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {

                // Get security-eligible devices that are not participating
                Set<String> notParticipatingDevices = new HashSet<>(securityDevices);
                notParticipatingDevices.removeAll(onModeDevices);
                notParticipatingDevices.removeAll(partialModeDevices);

                present(buildSecurityDeviceItems(deviceModels, offlineDevices, onModeDevices, partialModeDevices, bypassedDevices, notParticipatingDevices));
            }
        });
    }

    @NonNull private AlarmModel getModelForType(String alarmType, List<AlarmModel> models) {
        for (AlarmModel thisModel : models) {
            if (thisModel.getType().equalsIgnoreCase(presentedAlarmType)) {
                return thisModel;
            }
        }

        throw new IllegalArgumentException("Bug! No model for alarm type: " + alarmType);
    }

    @NonNull private List<AlertDeviceModel> buildDeviceItems(List<DeviceModel> deviceModels, AlarmModel presentedModel) {
        List<AlertDeviceModel> presentedItems = new ArrayList<>();

        Set<String> activeAndTriggeredDevices = new HashSet<>(presentedModel.getActiveDevices());
        activeAndTriggeredDevices.addAll(presentedModel.getTriggeredDevices());
        activeAndTriggeredDevices.addAll(presentedModel.getOfflineDevices());

        if (activeAndTriggeredDevices.size() > 0) {
            presentedItems.add(AlertDeviceModel.headerModelType(ArcusApplication.getContext().getString(R.string.security_device_participating)));

            for (String deviceAddress : activeAndTriggeredDevices) {
                DeviceModel deviceModel = getModelForAddress(deviceAddress, deviceModels);
                if (deviceModel != null) {
                    if (DeviceConnection.STATE_ONLINE.equals(deviceModel.get(DeviceConnection.ATTR_STATE)))
                    {
                        presentedItems.add(AlertDeviceModel.forOnlineDevice(deviceModel));
                    } else{
                        presentedItems.add(AlertDeviceModel.forOfflineDevice(deviceModel));
                    }
                }
            }
        }

        return presentedItems;
    }

    @NonNull private List<AlertDeviceModel> buildSecurityDeviceItems(List<DeviceModel> deviceModels, Set<String> offlineDevices, Set<String> onModeDevices, Set<String> partialModeDevices, Set<String> bypassedDevices, Set<String> notParticipatingDevices) {
        List<AlertDeviceModel> presentedItems = new ArrayList<>();

        Set<String> bypassedOnlineDevices = new HashSet<>(bypassedDevices);
        bypassedOnlineDevices.removeAll(offlineDevices);

        if (bypassedDevices.size() > 0) {
            List<AlertDeviceModel> sortedDevices = new ArrayList<>();

            for (String thisBypassedDevice : bypassedOnlineDevices) {
                DeviceModel deviceModel = getModelForAddress(thisBypassedDevice, deviceModels);
                if (deviceModel != null) {
                    //String actionText = getSecurityActionText(deviceModel);
                    String actionText = "";
                    String modeText = getSecurityModeText(onModeDevices.contains(thisBypassedDevice), partialModeDevices.contains(thisBypassedDevice));

                    boolean isOnline = DeviceConnection.STATE_ONLINE.equals(deviceModel.get(DeviceConnection.ATTR_STATE));
                    sortedDevices.add(AlertDeviceModel.forSecurityDevice(deviceModel, actionText, modeText, isOnline));
                }
            }
            Collections.sort(sortedDevices, AlertDeviceModel.sortAlphaOrder);

            if (sortedDevices.size() > 0) {
                presentedItems.add(AlertDeviceModel.headerModelType(ArcusApplication.getContext().getString(R.string.security_device_bypassed)));
                presentedItems.addAll(sortedDevices);
            }
        }

        Set<String> onAndParitalDevices = new HashSet<>(onModeDevices);
        onAndParitalDevices.addAll(partialModeDevices);
        onAndParitalDevices.removeAll(bypassedOnlineDevices);

        if (onAndParitalDevices.size() > 0) {
            presentedItems.add(AlertDeviceModel.headerModelType(ArcusApplication.getContext().getString(R.string.security_device_on_partial)));

            List<AlertDeviceModel> sortedDevices = new ArrayList<>();
            for (String onOrPartialDevice : onAndParitalDevices) {
                DeviceModel deviceModel = getModelForAddress(onOrPartialDevice, deviceModels);
                if (deviceModel != null) {
                    String actionText = getSecurityActionText(deviceModel);
                    String modeText = getSecurityModeText(onModeDevices.contains(onOrPartialDevice), partialModeDevices.contains(onOrPartialDevice));

                    boolean isOnline = DeviceConnection.STATE_ONLINE.equals(deviceModel.get(DeviceConnection.ATTR_STATE));
                    sortedDevices.add(AlertDeviceModel.forSecurityDevice(deviceModel, actionText, modeText, isOnline));
                }
            }
            Collections.sort(sortedDevices, AlertDeviceModel.sortAlphaOrder);
            presentedItems.addAll(sortedDevices);
        }

        if (notParticipatingDevices.size() > 0) {
            presentedItems.add(AlertDeviceModel.headerModelType(ArcusApplication.getContext().getString(R.string.security_device_not_participating)));

            List<AlertDeviceModel> sortedDevices = new ArrayList<>();
            for (String notParticipatingDevice : notParticipatingDevices) {
                DeviceModel deviceModel = getModelForAddress(notParticipatingDevice, deviceModels);
                if (deviceModel != null) {
                    String actionText = getSecurityActionText(deviceModel);

                    boolean isOnline = DeviceConnection.STATE_ONLINE.equals(deviceModel.get(DeviceConnection.ATTR_STATE));
                    sortedDevices.add(AlertDeviceModel.forSecurityDevice(deviceModel, actionText, null, isOnline));
                }
            }
            Collections.sort(sortedDevices, AlertDeviceModel.sortAlphaOrder);
            presentedItems.addAll(sortedDevices);
        }

        return presentedItems;
    }

    private String getSecurityModeText(boolean isOn, boolean isPartial) {
        if (isOn && isPartial) return ArcusApplication.getContext().getString(R.string.security_alarm_on_partial);
        if (isOn) return ArcusApplication.getContext().getString(R.string.security_alarm_on);
        if (isPartial) return ArcusApplication.getContext().getString(R.string.security_alarm_partial);

        return null;
    }

    private String getSecurityActionText(DeviceModel deviceModel) {

        if (CorneaUtils.hasCapability(deviceModel, Motion.class)) {
            return getSecurityActionTextForMotion(deviceModel);
        }

        else if (CorneaUtils.hasCapability(deviceModel, Contact.class)) {
            return getSecurityActionTextForContact(deviceModel);
        }

        else if (CorneaUtils.hasCapability(deviceModel, MotorizedDoor.class)) {
            return getSecurityActionTextForDoor(deviceModel);
        }

        return null;
    }

    private String getSecurityActionTextForDoor(DeviceModel deviceModel) {
        String doorState = CorneaUtils.getCapability(deviceModel, MotorizedDoor.class).getDoorstate();
        String timestamp = StringUtils.getTimestampString(CorneaUtils.getCapability(deviceModel, MotorizedDoor.class).getDoorstatechanged());

        if (MotorizedDoor.DOORSTATE_OPEN.equals(doorState) || MotorizedDoor.DOORSTATE_OPENING.equals(doorState)) {
            return ArcusApplication.getContext().getString(R.string.security_device_opened, timestamp);
        } else {
            return ArcusApplication.getContext().getString(R.string.security_device_closed, timestamp);
        }
    }

    private String getSecurityActionTextForMotion(DeviceModel deviceModel) {
        String timestamp = StringUtils.getTimestampString(CorneaUtils.getCapability(deviceModel, Motion.class).getMotionchanged());
        return ArcusApplication.getContext().getString(R.string.security_device_motion, timestamp);
    }

    private String getSecurityActionTextForContact(DeviceModel deviceModel) {
        String timestamp = StringUtils.getTimestampString(CorneaUtils.getCapability(deviceModel, Contact.class).getContactchanged());

        if (Contact.CONTACT_CLOSED.equals(CorneaUtils.getCapability(deviceModel, Contact.class).getContact())) {
            return ArcusApplication.getContext().getString(R.string.security_device_closed, timestamp);
        } else {
            return ArcusApplication.getContext().getString(R.string.security_device_opened, timestamp);
        }
    }

    private void present(final List<AlertDeviceModel> items) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(isPresenting()) {
                    getPresentedView().updateView(items);
                }
            }
        });
    }

    private DeviceModel getModelForAddress(String deviceAddress, List<DeviceModel> models) {
        for (DeviceModel thisModel : models) {
            if (thisModel.getAddress().equalsIgnoreCase(deviceAddress)) return thisModel;
        }

        return null;
    }
}
