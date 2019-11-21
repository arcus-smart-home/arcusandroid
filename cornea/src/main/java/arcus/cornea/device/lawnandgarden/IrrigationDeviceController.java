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
package arcus.cornea.device.lawnandgarden;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Function;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.lawnandgarden.BaseLawnAndGardenController;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.PropertyChangeMonitor;
import com.iris.client.bean.IrrigationScheduleStatus;
import com.iris.client.bean.IrrigationTransitionEvent;
import com.iris.client.bean.ZoneWatering;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.IrrigationController;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class IrrigationDeviceController
      extends BaseLawnAndGardenController<IrrigationDeviceController.Callback>
{
    public interface Callback {
        void showDeviceControls(IrrigationControllerDetailsModel model);
        void errorOccurred(Throwable throwable);
    }

    public static IrrigationDeviceController newController(String deviceId) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        IrrigationDeviceController controller = new IrrigationDeviceController(source);
        controller.init();
        return controller;
    }

    private ModelSource<DeviceModel> source;
    private static final int TIMEOUT_MS = 30_000;
    private final Listener<Throwable> updateErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            updateError(throwable);

            String address = getDeviceAddress();
            if (!TextUtils.isEmpty(address)) {
                PropertyChangeMonitor.instance().removeAllFor(address);
            }

            updateView();
        }
    });
    private final Function<String, Void> onFailureFunction = new Function<String, Void>() {
        @Override public Void apply(@Nullable String input) {
            refreshModel();

            return null;
        }
    };
    private final PropertyChangeMonitor.Callback propertyChangeMonitorCB = new PropertyChangeMonitor.Callback() {
        @Override public void requestTimedOut(String address, String attribute) {
            refreshModel();
        }

        @Override public void requestSucceeded(String address, String attribute) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override public void run() {
                    updateView();
                }
            });
        }
    };

    IrrigationDeviceController(
          ModelSource<DeviceModel> source
    ) {
        this.source = source;
        this.source.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override public void onEvent(ModelEvent modelEvent) {
                if (!(modelEvent instanceof ModelChangedEvent)) {
                    return;
                }

                ModelChangedEvent mce = (ModelChangedEvent) modelEvent;
                Set<String> changes = mce.getChangedAttributes().keySet();
                if (changes.contains(Device.ATTR_NAME) ||
                      changes.contains(DeviceConnection.ATTR_STATE) ||
                      changes.contains(IrrigationController.ATTR_CONTROLLERSTATE) ||
                      changes.contains(IrrigationController.ATTR_ZONESINFAULT) ||
                      changes.contains(IrrigationController.ATTR_BUDGET)
                      ) {
                    updateView();
                }
            }
        }));
        this.source.load();
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        Set<String> changes = event.getChangedAttributes().keySet();
        if (changes.contains(LawnNGardenSubsystem.ATTR_SCHEDULESTATUS) ||
              changes.contains(LawnNGardenSubsystem.ATTR_ODDSCHEDULES) ||
              changes.contains(LawnNGardenSubsystem.ATTR_EVENSCHEDULES) ||
              changes.contains(LawnNGardenSubsystem.ATTR_WEEKLYSCHEDULES) ||
              changes.contains(LawnNGardenSubsystem.ATTR_INTERVALSCHEDULES) ||
              changes.contains(LawnNGardenSubsystem.ATTR_NEXTEVENT) ||
              changes.contains(LawnNGardenSubsystem.ATTR_ZONESWATERING)) { // Will this cause 2x update?
            updateView();
        }
    }

    @Override protected void updateView(Callback callback) {
        if(!source.isLoaded()) {
            source.load();
            return;
        }
        callback.showDeviceControls(update(source.get()));
    }

    protected IrrigationControllerDetailsModel update(DeviceModel device) {
        if(!(device instanceof IrrigationController)) {
            return new IrrigationControllerDetailsModel();
        }

        LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
        if (subsystem == null) {
            return new IrrigationControllerDetailsModel();
        }

        IrrigationControllerDetailsModel model = new IrrigationControllerDetailsModel();
        model.setDeviceAddress(device.getAddress());
        model.setDeviceName(device.getName());

        model.setIsInOTA(DeviceOta.STATUS_INPROGRESS.equals(device.get(DeviceOta.ATTR_STATUS)));

        // Unless it's been explicitly marked offline, leave it online.
        model.setIsOnline(!DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        Number zones = (Number) device.get(IrrigationController.ATTR_NUMZONES);
        model.setIsMultiZone(zones != null && zones.intValue() > 1);

        // Skipped
        // Watering
        // Next Event
        ZoneWatering watering = getCurrentlyWatering(subsystem, device.getAddress());
        if (watering != null) {
            if (ZoneWatering.TRIGGER_MANUAL.equals(watering.getTrigger())) {
                model.setControllerState(IrrigationControllerState.MANUAL_WATERING);
            }
            else {
                model.setControllerState(IrrigationControllerState.SCHEDULED_WATERING);
            }
            Date date = watering.getStartTime();
            model.setWateringStartedAt(date != null ? date.getTime() : 0);
            model.setWateringDuration(watering.getDuration() != null ? watering.getDuration() : 0);

            String name = (String) device.get(String.format("%s:%s", IrrigationZone.ATTR_ZONENAME, watering.getZone()));
            if (!TextUtils.isEmpty(name)) {
                model.setZoneNameWatering(name);
            }
            else {
                Number zone = (Number) device.get(String.format("%s:%s", IrrigationZone.ATTR_ZONENUM, watering.getZone()));
                model.setZoneNameWatering(String.format("Zone %s", zone != null ? zone.intValue() : ""));
            }
        }

        IrrigationScheduleStatus scheduleStatus = getScheduleStatus(subsystem, device.getAddress());
        if (scheduleStatus != null) {
            if (scheduleStatus.getEnabled()) {
                Map<String, Object> nextEvent = scheduleStatus.getNextEvent();
                if (nextEvent != null) {
                    IrrigationTransitionEvent event = new IrrigationTransitionEvent(scheduleStatus.getNextEvent());
                    model.setNextEventZone(getZoneName(event.getZone()));
                    model.setNextEventTime(DateUtils.format(event.getStartTime()));
                }

                model.setScheduleMode(getScheduleMode(scheduleStatus.getMode()));
            }

            // If skip is in the future.
            if (scheduleStatus.getSkippedUntil() != null && scheduleStatus.getSkippedUntil().getTime() > System.currentTimeMillis()) {
                model.setSkipUntilTime(DateUtils.format(scheduleStatus.getSkippedUntil()));
                model.setControllerState(IrrigationControllerState.SKIPPED);
            }
        }

        IrrigationControllerState state = getControllerState();
        if (IrrigationControllerState.OFF.equals(state) || model.getControllerState() == null) {
            model.setControllerState(state);
        }

        model.setHasRequestInFlight(PropertyChangeMonitor.instance().hasAnyChangesFor(device.getAddress()));

        return model;
    }

    protected String getZoneName(String zone) {
        DeviceModel model = getDeviceModel();
        if (model == null) {
            return "";
        }

        String name = (String) model.get(String.format("%s:%s", IrrigationZone.ATTR_ZONENAME, zone));
        if (TextUtils.isEmpty(name)) {
            Number zoneNum = (Number) model.get(String.format("%s:%s", IrrigationZone.ATTR_ZONENUM, zone));
            name = String.format("Zone %s", zoneNum == null ? "" : zoneNum.intValue());
        }
        return name;
    }

    public int getDefaultDuration(String zone) {
        DeviceModel device = getDeviceModel();
        if (device == null) {
            return 1;
        }

        Number dfltWater = (Number) device.get(String.format("%s:%s", IrrigationZone.ATTR_DEFAULTDURATION, zone));
        return (dfltWater != null) ? dfltWater.intValue() : 1;
    }

    public void cancelSkip() {
        LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
        String address = getDeviceAddress();

        if (subsystem != null && !TextUtils.isEmpty(address)) {
            startMonitorFor(address, IrrigationController.ATTR_RAINDELAYDURATION, null);
            subsystem.cancelSkip(address).onFailure(updateErrorListener);
        }
        else {
            updateError(new RuntimeException("Unable to send command. Controller was null."));
        }
    }

    public void skipWatering(@IntRange(from = 1, to = 72) int hours) {
        LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
        String address = getDeviceAddress();

        if (subsystem != null && !TextUtils.isEmpty(address)) {
            startMonitorFor(
                  address,
                  IrrigationController.ATTR_RAINDELAYDURATION,
                  null
            );

            if (hours > 72) { // TODO: Wasnt sure if were using the minutes elsewhere... So just updating here for now.
                // Should really go and change the delay event if we don't need minutes
                hours = (int) TimeUnit.MINUTES.toHours(hours);
            }

            subsystem.skip(address, hours).onFailure(updateErrorListener);
        }
        else {
            updateError(new RuntimeException("Unable to send command. Controller was null."));
        }
    }

    public void waterNow(String zone, @IntRange(from = 1) int minutesToWater) {
        if (minutesToWater < 1) {
            return;
        }

        IrrigationController controller = getControllerFromSource();
        String address = getDeviceAddress();
        if (controller != null && !TextUtils.isEmpty(address)) {
            startMonitorFor(
                  address, // TODO: Monitor this or the duration? Or the zone?...
                  IrrigationController.ATTR_CONTROLLERSTATE,
                  IrrigationController.CONTROLLERSTATE_WATERING
            );

            controller.waterNowV2(zone, minutesToWater).onFailure(updateErrorListener);
        }
        else {
            updateError(new RuntimeException("Unable to send command. Controller was null."));
        }
    }

    public void stopWatering(boolean allZones) {
        LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
        String address = getDeviceAddress();

        if (subsystem != null && !TextUtils.isEmpty(address)) {
            startMonitorFor(
                  address,
                  IrrigationController.ATTR_CONTROLLERSTATE,
                  IrrigationController.CONTROLLERSTATE_NOT_WATERING
            );

            subsystem
                  .stopWatering(address, !allZones)
                  .onFailure(updateErrorListener);
        }
        else {
            updateError(new RuntimeException("Unable to send command. Controller was null."));
        }
    }

    protected void startMonitorFor(String address, String attribute, Object shouldBe) {
        PropertyChangeMonitor.instance().removeAllFor(address);
        PropertyChangeMonitor.instance().startMonitorFor(
              address, attribute, TIMEOUT_MS, propertyChangeMonitorCB, shouldBe, onFailureFunction
        );
    }

    protected void updateError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.errorOccurred(throwable);
        }
    }

    protected void refreshModel() {
        DeviceModel model = getDeviceModel();
        if (model != null) {
            model.refresh();
        }
    }

    protected @Nullable DeviceModel getDeviceModel() {
        if (source == null) {
            return null;
        }

        return source.get();
    }

    protected @Nullable String getDeviceAddress() {
        DeviceModel model = getDeviceModel();
        if (model == null) {
            return null;
        }

        return model.getAddress();
    }

    protected @Nullable IrrigationController getControllerFromSource() {
        DeviceModel model = getDeviceModel();
        if (model != null && model.getCaps().contains(IrrigationController.NAMESPACE)) {
            return (IrrigationController) model;
        }

        return null;
    }

    protected IrrigationControllerState getControllerState() {
        IrrigationController controller = getControllerFromSource();
        if (controller == null || IrrigationController.CONTROLLERSTATE_OFF.equals(controller.getControllerState())) {
            return IrrigationControllerState.OFF;
        }

        return IrrigationControllerState.IDLE;
    }

    protected @Nullable ZoneWatering getCurrentlyWatering(LawnNGardenSubsystem subsystem, String deviceAddress) {
        Map<String, Map<String, Object>> zonesWatering = subsystem.getZonesWatering();
        if (zonesWatering == null) {
            return null;
        }

        Map<String, Object> zone = zonesWatering.get(deviceAddress);
        return (zone != null) ? new ZoneWatering(zone) : null;
    }

    protected @Nullable IrrigationScheduleStatus getScheduleStatus(LawnNGardenSubsystem subsystem, String deviceAddress) {
        Map<String, Map<String, Object>> allDevices = subsystem.getScheduleStatus();
        if (allDevices == null) {
            return null;
        }

        Map<String, Object> singleDevice = allDevices.get(deviceAddress);
        return (singleDevice != null) ? new IrrigationScheduleStatus(singleDevice) : null;
    }

    protected IrrigationScheduleMode getScheduleMode(String mode) {
        if (TextUtils.isEmpty(mode)) {
            return IrrigationScheduleMode.MANUAL;
        }

        switch (mode) {
            case IrrigationScheduleStatus.MODE_WEEKLY:
                return IrrigationScheduleMode.WEEKLY;

            case IrrigationScheduleStatus.MODE_INTERVAL:
                return IrrigationScheduleMode.INTERVAL;

            case IrrigationScheduleStatus.MODE_ODD:
                return IrrigationScheduleMode.ODD;

            case IrrigationScheduleStatus.MODE_EVEN:
                return IrrigationScheduleMode.EVEN;

            default:
                return IrrigationScheduleMode.MANUAL;
        }
    }
}
