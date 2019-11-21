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
package arcus.app.device.ota.controller;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientMessage;
import com.iris.client.capability.DeviceOta;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;
import arcus.app.common.utils.CorneaUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FirmwareUpdateController implements ObservableMap.MapObserver<String,String> {

    @NonNull
    private static FirmwareUpdateController instance = new FirmwareUpdateController();
    private static Logger logger = LoggerFactory.getLogger(FirmwareUpdateController.class);

    private Activity activity;
    private ListenerRegistration percentCompleteListener;
    private ListenerRegistration activePlaceListener;
    private ListenerRegistration pairedDeviceListener;
    private ListenerRegistration removedDeviceListener;
    private ListenerRegistration deviceAddedListener;
    private ListenerRegistration updateStatusListener;
    private ListenerRegistration deviceUpdatingListener;
    private HashMap<String,ListenerRegistration> deviceListener = new HashMap<>();

    private WeakReference<UpdateSequenceCallback> sequenceListener = new WeakReference<>(null);
    private WeakReference<UpdateCallback> statusListener = new WeakReference<>(null);
    @NonNull
    private ObservableMap<String, String> firmwareStatusCache = new ObservableMap<>(this);

    public interface UpdateSequenceCallback {
        /**
         * Called to immediately after a request to start monitoring to indicate that the controller
         * is loading data. Implementer should make user aware of a possible delay before results
         * are available.
         */
        void onLoading ();

        /**
         * Called after a request to start monitoring to indicate that no firmware updates are required.
         * Will be invoked as soon as data has been loaded.
         */
        void onFirmwareUpdateRequired();

        /**
         * Called after a request to start monitoring to indicate that one or more firmware updates
         * are required / in progress. Will be invoked as soon as data has been loaded.
         */
        void onFirmwareUpdateNotRequired();

        /**
         * Called at some point after {@link #onFirmwareUpdateRequired()} to indicate that one or more
         * updates failed.
         */
        void onFirmwareUpdateFailed();

        /**
         * Called at some point after {@link #onFirmwareUpdateRequired()} to indicate that all updates
         * completed without error. When more than one update is in progress, invocation of this method will be
         * withheld until all updates complete successfully.
         */
        void onFirmwareUpdateSucceeded();
    }

    public interface UpdateCallback {
        /**
         * Called to indicate that the list of firmware-updating devices may have changed. No guarantee
         * that the list has actually changed.
         *
         * @param deviceList List of devices whose firmware is presently updating.
         */
        void onDevicesUpdating (List<DeviceModel> deviceList);

        /**
         * Called to indicate that the firmware update status of a given device has changed.
         * @param device Device whose status has changed
         * @param isUpdating True if the device is currently updating firmware
         * @param otherDevicesUpdating True if other devices (not including this one) are also updating.
         */
        void onDeviceFirmwareUpdateStatusChange (DeviceModel device, boolean isUpdating, boolean otherDevicesUpdating);

        /**
         * Called to indicate that the firmware update progress of a given device has changed.
         * @param device Device whose status has changed
         * @param progress Value of the current progress for device
         */
        void onDeviceFirmwareUpdateProgressChange(DeviceModel device, Double progress);
    }

    @NonNull
    public static FirmwareUpdateController getInstance () {
        return instance;
    }

    private FirmwareUpdateController () {

        // Listen for incoming percent complete events
        Listeners.clear(percentCompleteListener);
        percentCompleteListener = CorneaClientFactory.getClient().addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(@NonNull ClientMessage clientMessage) {
                if (DeviceOta.FirmwareUpdateProgressEvent.NAME.equals(clientMessage.getEvent().getType())) {
                    DeviceOta.FirmwareUpdateProgressEvent progressUpdate = new DeviceOta.FirmwareUpdateProgressEvent(clientMessage.getEvent());
                    logger.debug("Received DeviceOta update progress event. Complete: {}%, device: {}", progressUpdate.getOtaProgress(), progressUpdate.getSourceAddress());

                    fireUpdateProgressChange(progressUpdate.getSourceAddress(), progressUpdate.getOtaProgress());
                }
            }
        });

        // Listen for changes to active place; requires us to clear our cache.
        Listeners.clear(activePlaceListener);
        activePlaceListener = CorneaClientFactory.getClient().addSessionListener(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent sessionEvent) {
                if (sessionEvent instanceof SessionActivePlaceSetEvent) {
                    firmwareStatusCache.clear();
                }
            }
        });
    }

    /**
     * Begins monitoring all devices for required firmware updates and invokes callback methods as
     * appropriate to notify the caller of the state of the updates.
     *
     * Differs from {@link #startFirmwareUpdateStatusMonitor(Activity, UpdateCallback)} only
     * in the set of callbacks invoked. This method is intended to be useful in a post-pairing
     * sequence.
     *
     * @param activity
     * @param listener
     */
    public void startNewDeviceFirmwareUpdateMonitor(Activity activity, UpdateSequenceCallback listener) {
        this.activity = activity;
        this.sequenceListener = new WeakReference<>(listener);

        startMonitoring();
    }

    /**
     * Stops any future {@link arcus.app.device.ota.controller.FirmwareUpdateController.UpdateSequenceCallback}
     * callbacks from being invoked.
     */
    public void stopNewDeviceFirmwareUpdateMonitor() {
        this.sequenceListener = null;
    }

    /**
     * Begins monitoring all devices that support the DeviceOTA capability and invokes callback methods
     * as appropriate to notify the caller of changes to devices undergoing updates.
     *
     * Differs from {@link #startNewDeviceFirmwareUpdateMonitor(Activity, UpdateSequenceCallback)} only
     * in the set of callback invoked. This method is intended to be useful in the downloading
     * progress fragment and dashboard.
     *
     * @param activity
     * @param listener
     */
    public void startFirmwareUpdateStatusMonitor (Activity activity, UpdateCallback listener) {
        this.activity = activity;
        this.statusListener = new WeakReference<>(listener);

        startMonitoring();
    }

    /**
     * Stops any future {@link UpdateCallback}
     * callbacks from being invoked.
     */
    public void stopFirmwareUpdateStatusMonitor () {
        this.statusListener = null;
    }

    /**
     * Immediately fires the {@link UpdateCallback#onDeviceFirmwareUpdateProgressChange} method for
     * each OTA device being monitored with the device's currently percent complete.
     */
    public void fireCurrentStatus () {
        for (String deviceAddress : firmwareStatusCache.keySet()) {

            DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
                @Override
                public void onEvent(DeviceModel deviceModel) {
                    DeviceOta deviceOta = CorneaUtils.getCapability(deviceModel, DeviceOta.class);

                    if (deviceOta != null) {
                        fireUpdateProgressChange(deviceModel.getAddress(), deviceOta.getProgressPercent());
                    }
                }
            });
        }
    }

    private void startMonitoring () {

        // Let the UI know if might be a minute...
        fireOnLoading();

        // Load the currently paired set of devices
        Listeners.clear(pairedDeviceListener);
        pairedDeviceListener = DeviceModelProvider.instance().addStoreLoadListener(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(@NonNull List<DeviceModel> deviceModels) {
                logger.debug("DeviceOta monitor is ready with {} devices in FirmwareUpdateController.", deviceModels.size());
                startMonitoringDevices(deviceModels);

                fireDevicesUpdating(getDevicesUpdating());
                if (areDevicesUpdating()) {
                    fireUpdateRequired();
                } else {
                    fireUpdateNotRequired();
                }
            }
        });

        // Stop monitoring devices that get removed
        Listeners.clear(removedDeviceListener);
        removedDeviceListener = DeviceModelProvider.instance().getStore().addListener(ModelDeletedEvent.class, new Listener<ModelDeletedEvent>() {
            @Override
            public void onEvent(@NonNull ModelDeletedEvent modelDeletedEvent) {
                if (modelDeletedEvent.getModel() instanceof DeviceModel) {
                    stopMonitoringDevice((DeviceModel) modelDeletedEvent.getModel());
                }
            }
        });

        // Start monitoring devices that get added
        Listeners.clear(deviceAddedListener);
        deviceAddedListener = DeviceModelProvider.instance().getStore().addListener(ModelAddedEvent.class, new Listener<ModelAddedEvent>() {
            @Override
            public void onEvent(@NonNull ModelAddedEvent modelAddedEvent) {
                if (modelAddedEvent.getModel() instanceof DeviceModel) {
                    startMonitoringDevice((DeviceModel) modelAddedEvent.getModel());
                }
            }
        });
    }

    private void stopMonitoringDevice (@NonNull DeviceModel device) {
        DeviceOta deviceOta = CorneaUtils.getCapability(device, DeviceOta.class);

        if (deviceOta != null) {
            logger.debug("No longer monitoring DeviceOta changes in device {}.", device.getName());

            firmwareStatusCache.remove(device.getAddress());
            fireDevicesUpdating(getDevicesUpdating());
        }
    }

    private void startMonitoringDevice (@NonNull final DeviceModel device) {
        DeviceOta deviceOta = CorneaUtils.getCapability(device, DeviceOta.class);

        if (deviceOta != null) {
            logger.debug("Starting to monitor DeviceOta changes in device {}", device.getName());

            if (! firmwareStatusCache.containsKey(device.getAddress())) {

                Listeners.clear(deviceListener.get(device.getAddress()));
                deviceListener.put(device.getAddress(), device.addListener(new Listener<PropertyChangeEvent>() {
                    @Override
                    public void onEvent(PropertyChangeEvent propertyChangeEvent) {
                        DeviceOta deviceOta = CorneaUtils.getCapability(device, DeviceOta.class);
                        if (deviceOta != null) {
                            try {
                                fireUpdateProgressChange(device.getAddress(), Double.valueOf(String.valueOf(device.get(DeviceOta.ATTR_PROGRESSPERCENT))));
                            } catch (NumberFormatException e) {
                                // Nothing to do; don't update UI if progress not available
                            }

                            firmwareStatusCache.put(device.getAddress(), (String) device.get(DeviceOta.ATTR_STATUS));
                        }
                    }
                }));
            }

            firmwareStatusCache.put(device.getAddress(), (String) device.get(DeviceOta.ATTR_STATUS));
        }
    }

    private void startMonitoringDevices(@NonNull List<DeviceModel> devices) {
        for (final DeviceModel device : devices) {
            startMonitoringDevice(device);
        }
    }

    /**
     * Fired anytime a new device address is added to the device status cache.
     *
     * @param deviceAddress
     * @param updateStatus
     */
    @Override
    public void onKeyAdded (String deviceAddress, String updateStatus) {
        fireDevicesUpdating(getDevicesUpdating());
    }

    /**
     * Fired anytime a device is removed from the device status cache.
     * @param deviceAddress
     */
    @Override
    public void onKeyRemoved (String deviceAddress) {
        fireDevicesUpdating(getDevicesUpdating());
    }

    /**
     * Fired anytime a device's firmware update status changes in the cache.
     *
     * @param deviceAddress
     * @param lastUpdateStatus
     * @param currentUpdateStatus
     */
    @Override
    public void onValueChange(@NonNull String deviceAddress, String lastUpdateStatus, String currentUpdateStatus) {
        logger.debug("DeviceOta status changed from {} to {} in device {}.", lastUpdateStatus, currentUpdateStatus, deviceAddress);

        // All devices finished updating successfully
        if (DeviceOta.STATUS_COMPLETED.equals(currentUpdateStatus) && !areDevicesUpdating()) {
            fireUpdateSucceeded();
        }

        // Device failed update
        if (DeviceOta.STATUS_FAILED.equals(currentUpdateStatus)) {
            fireUpdateFailed();
        }

        // Device started updating
        if (DeviceOta.STATUS_INPROGRESS.equals(currentUpdateStatus) && isStatusChanged(lastUpdateStatus, currentUpdateStatus)) {
            fireDevicesUpdating(getDevicesUpdating());
        }

        if (DeviceOta.STATUS_INPROGRESS.equals(lastUpdateStatus) && isStatusChanged(lastUpdateStatus, currentUpdateStatus)) {
            fireDevicesUpdating(getDevicesUpdating());
        }

        // A device status changed
        if (isStatusChanged(lastUpdateStatus, currentUpdateStatus)) {
            fireUpdateStatusChange(deviceAddress, isDeviceUpdating(deviceAddress), areDevicesUpdating(deviceAddress));
        }
    }

    private boolean isStatusChanged (@Nullable String oldValue, @Nullable String newValue) {
        return !(oldValue == null ? newValue == null : oldValue.equals(newValue));
    }

    public boolean areDevicesUpdating () {
        return areDevicesUpdating(null);
    }

    private boolean areDevicesUpdating (@Nullable String exceptDeviceAddress) {
        List<String> updatingDevices = getDevicesUpdating();
        if (exceptDeviceAddress != null) {
            updatingDevices.remove(exceptDeviceAddress);
        }

        return updatingDevices.size() > 0;
    }

    private boolean isDeviceUpdating (String deviceAddress) {
        return DeviceOta.STATUS_INPROGRESS.equals(firmwareStatusCache.get(deviceAddress));
    }

    @NonNull
    private List<String> getDevicesUpdating () {
        List<String> devicesUpdating = new ArrayList<>();

        for (String thisDeviceAddress : firmwareStatusCache.keySet()) {
            if (isDeviceUpdating(thisDeviceAddress)) {
                devicesUpdating.add(thisDeviceAddress);
            }
        }

        return devicesUpdating;
    }

    private void fireOnLoading () {
        if (sequenceListener != null) {
            logger.debug("Firing firmware update sequence onLoading() listener.");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateSequenceCallback callback = sequenceListener == null ? null : sequenceListener.get();
                    if (callback != null) {
                        callback.onLoading();
                    }
                }
            });
        }
    }

    private void fireUpdateRequired () {
        if (sequenceListener != null) {
            logger.debug("Firing firmware update sequence onFirmwareUpdateRequired() listener.");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateSequenceCallback callback = sequenceListener == null ? null : sequenceListener.get();
                    if (callback != null) {
                        callback.onFirmwareUpdateRequired();
                    }
                }
            });
        }
    }

    private void fireUpdateNotRequired () {
        if (sequenceListener != null) {
            logger.debug("Firing firmware update sequence onFirmwareUpdateNotRequired() listener.");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateSequenceCallback callback = sequenceListener == null ? null : sequenceListener.get();
                    if (callback != null) {
                        callback.onFirmwareUpdateNotRequired();
                    }
                }
            });
        }
    }

    private void fireUpdateFailed () {
        if (sequenceListener != null) {
            logger.debug("Firing firmware update sequence onFirmwareUpdateFailed() listener.");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateSequenceCallback callback = sequenceListener == null ? null : sequenceListener.get();
                    if (callback != null) {
                        callback.onFirmwareUpdateFailed();
                    }
                }
            });
        }
    }

    private void fireUpdateSucceeded () {
        if (sequenceListener != null) {
            logger.debug("Firing firmware update sequence onFirmwareUpdateSucceeded() listener.");

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdateSequenceCallback callback = sequenceListener == null ? null : sequenceListener.get();
                    if (callback != null) {
                        callback.onFirmwareUpdateSucceeded();
                    }
                }
            });
        }
    }

    private void fireUpdateStatusChange (@NonNull final String deviceAddress, final boolean isUpdating, final boolean otherDevicesUpdating) {
        if (statusListener != null) {

            Listeners.clear(updateStatusListener);
            updateStatusListener = DeviceModelProvider.instance().addStoreLoadListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
                @Override
                public void onEvent(@NonNull List<DeviceModel> deviceModels) {

                    final DeviceModel device = CorneaUtils.filterDeviceModelsByAddress(deviceModels, deviceAddress);
                    if (device == null) {
                        logger.error("The set of {} device models do not contain a device with address {}. Something ain't right.", deviceModels.size(), deviceAddress);
                        return;
                    }

                    logger.debug("Firing onDeviceFirmwareUpdateStatusChange for device {}, isUpdating: {}", device, isUpdating);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateCallback callback = statusListener == null ? null : statusListener.get();
                            if (callback != null) {
                                callback.onDeviceFirmwareUpdateStatusChange(device, isUpdating, otherDevicesUpdating);
                            }
                        }
                    });
                }
            }));
        }
    }

    private void fireUpdateProgressChange(@NonNull final String deviceAddress, final Double progress) {
        if (statusListener != null) {

            Listeners.clear(updateStatusListener);
            updateStatusListener = DeviceModelProvider.instance().addStoreLoadListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
                @Override
                public void onEvent(@NonNull List<DeviceModel> deviceModels) {

                    final DeviceModel device = CorneaUtils.filterDeviceModelsByAddress(deviceModels, deviceAddress);
                    if (device == null) {
                        logger.error("The set of {} device models do not contain a device with address {}. Something ain't right.", deviceModels.size(), deviceAddress);
                        return;
                    }

                    logger.debug("Firing onDeviceFirmwareUpdateProgressChange for device {}, progress: {}", device, progress);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateCallback callback = statusListener == null ? null : statusListener.get();
                            if (callback != null) {
                                callback.onDeviceFirmwareUpdateProgressChange(device, progress);
                            }
                        }
                    });
                }
            }));
        }
    }

    private void fireDevicesUpdating (@NonNull final List<String> updatingDeviceAddresses) {
        if (statusListener != null) {

            Listeners.clear(deviceUpdatingListener);
            deviceUpdatingListener = DeviceModelProvider.instance().addStoreLoadListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
                @Override
                public void onEvent(@NonNull List<DeviceModel> deviceModels) {

                    final List<DeviceModel> updatingDevices = CorneaUtils.filterDeviceModelsByAddress(deviceModels, updatingDeviceAddresses);
                    if (updatingDevices.size() != updatingDeviceAddresses.size()) {
                        logger.error("Not all devices in filter list were found in model store. Something ain't right. {} device addresses requested; {} found in store of {}.", updatingDeviceAddresses, updatingDevices.size(), deviceModels.size());
                    }

                    logger.debug("Firing onDevicesUpdating() with {} devices currently updating firmware.", updatingDevices.size());
                    UpdateCallback callback = statusListener == null ? null : statusListener.get();
                    if (callback != null) {
                        callback.onDevicesUpdating(updatingDevices);
                    }
                }
            }));
        }
    }

}
