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
package arcus.app.device.pairing.multi.controller;

import androidx.annotation.NonNull;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import arcus.app.ArcusApplication;
import arcus.app.common.controller.FragmentController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class MultipairingListFragmentController extends FragmentController<MultipairingListFragmentController.Callbacks> {

    public interface Callbacks {
        void onDeviceProvisioningStatusChanged(List<DeviceModel> devices);
        void onError(Throwable cause);
    }

    private final static Logger logger = LoggerFactory.getLogger(MultipairingListFragmentController.class);
    private final static MultipairingListFragmentController instance = new MultipairingListFragmentController();

    private ListenerRegistration deviceAddedListener;

    private List<DeviceModel> pairingDeviceModels = new ArrayList<>();
    private MultipairingListFragmentController () {}

    public static MultipairingListFragmentController getInstance() {
        return instance;
    }

    public void startMultipairing(final List<String> deviceAddresses) {

        if (deviceAddresses != null && deviceAddresses.size() >0) {
            pairingDeviceModels.clear();

            DeviceModelProvider.instance().getModels(deviceAddresses).load().onSuccess(new Listener<List<DeviceModel>>() {
                @Override
                public void onEvent(List<DeviceModel> deviceModels) {
                    for (DeviceModel deviceModel : deviceModels) {
                        boolean isDeviceAlreadyPaired = false;

                        for (DeviceModel pairedDeviceModel : pairingDeviceModels) {
                            if (deviceModel.getId().equalsIgnoreCase(pairedDeviceModel.getId())) {
                                isDeviceAlreadyPaired = true;
                            }
                        }

                        if (!isDeviceAlreadyPaired) {
                            pairingDeviceModels.add(deviceModel);
                        }
                    }

                    notifyProvisioningStateChanged(pairingDeviceModels);

                }
            }).onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    fireOnError(throwable);
                }
            });
        }

        notifyProvisioningStateChanged(pairingDeviceModels);
    }

    public void startMonitoringForNewDevices() {

        // Start monitoring devices that get added
        pairingDeviceModels.clear();
        Listeners.clear(deviceAddedListener);
        deviceAddedListener = DeviceModelProvider.instance().getStore().addListener(ModelAddedEvent.class,Listeners.runOnUiThread( new Listener<ModelAddedEvent>() {
            @Override
            public void onEvent(@NonNull ModelAddedEvent modelAddedEvent) {
                if (modelAddedEvent.getModel() instanceof DeviceModel) {
                    logger.debug("deviceAddedListener - Device added to Multipairing List : {}", ((DeviceModel) modelAddedEvent.getModel()).getName());
                    pairingDeviceModels.add((DeviceModel) modelAddedEvent.getModel());
                    notifyProvisioningStateChanged (pairingDeviceModels);
                }
            }
        }));
    }

    public void stopMonitoringForNewDevices() {
        Listeners.clear(deviceAddedListener);
    }

    private void notifyProvisioningStateChanged (List<DeviceModel> deviceModels) {
        if (deviceModels != null && !deviceModels.isEmpty()) {
            fireOnDeviceProvisioningStatusChanged(deviceModels);
        }
    }

    private void fireOnError (final Throwable cause) {
        if (ArcusApplication.getArcusApplication().getForegroundActivity() != null) {
            ArcusApplication.getArcusApplication().getForegroundActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onError(cause);
                    }
                }
            });
        }
    }

    private void fireOnDeviceProvisioningStatusChanged(final List<DeviceModel> devices) {
        if (ArcusApplication.getArcusApplication().getForegroundActivity() != null) {
            ArcusApplication.getArcusApplication().getForegroundActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onDeviceProvisioningStatusChanged(devices);
                    }
                }
            });
        }
    }
}
