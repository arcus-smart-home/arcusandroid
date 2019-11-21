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
package arcus.app.device.pairing.steps.controller;

import android.app.Activity;
import androidx.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Hub;
import com.iris.client.capability.ProductCatalog;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ProductModel;
import arcus.app.ArcusApplication;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.pairing.nohub.model.NoHubDevice;
import arcus.app.device.pairing.steps.model.DevicePairedListener;
import arcus.app.device.pairing.steps.model.PairingStep;
import arcus.app.device.pairing.steps.model.PairingStepTransition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;


public class PairingStepFragmentController extends FragmentController<PairingStepFragmentController.Callbacks> {

    private final static Logger logger = LoggerFactory.getLogger(PairingStepFragmentController.class);
    private static final PairingStepFragmentController instance = new PairingStepFragmentController();

    private Activity activity;
    private List<Map<String, Object>> pairingStepsData;
    private String deviceName;
    private String productId;
    private String tutorialVideoUrl;
    private int currentStep;

    private ListenerRegistration pairedDeviceListener;
    private ListenerRegistration activeHubListener;

    public interface Callbacks extends DevicePairedListener {
        void onShowPairingStep (PairingStep step);
        void onLoading ();
        void onError (Throwable cause);
    }

    private PairingStepFragmentController() {}

    public static PairingStepFragmentController instance () {
        return instance;
    }

    public void showInitialPairingStep(final Activity activity, String productAddress, final boolean showLastStepFirst) {
        logger.debug("Loading pairing instructions for product address: {}.", productAddress);
        this.activity = activity;

        loadInitialPairingStep(productAddress, showLastStepFirst);

        // Turn off pairing for "no-pair" devices like the Alexa
        if (NoHubDevice.isNoPairDevice(CorneaUtils.getIdFromAddress(productAddress))) {
            stopPairing();
        }

        // For all other devices, monitor changes to hub state and newly added devices
        else {
            monitorForPairedDevices();
            monitorForHubStateChanges();
        }
    }

    public PairingStepTransition showNextPairingStep () {
        if (currentStep < pairingStepsData.size() - 1) {
            fireOnShowPairingStep(getPairingStep(++currentStep));
            return PairingStepTransition.GO_NEXT;
        }

        return NoHubDevice.isNoPairDevice(getProductId()) ?
                PairingStepTransition.GO_NO_PAIRING : PairingStepTransition.GO_END;
    }

    public PairingStepTransition showPreviousPairingStep () {
        if (currentStep != 0) {
            fireOnShowPairingStep(getPairingStep(--currentStep));
            return PairingStepTransition.GO_PREVIOUS;
        }
        return PairingStepTransition.GO_START;
    }

    public String getDeviceName () {
        return deviceName;
    }

    public String getProductId () { return productId; }

    private void stopPairing () {
        // Stop listening for hub state and paired device changes
        Listeners.clear(activeHubListener);
        Listeners.clear(pairedDeviceListener);

        PlaceModelProvider.getCurrentPlace().get().stopAddingDevices().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnError(throwable);
            }
        });
    }

    private void monitorForHubStateChanges () {
        // Listen for changes to the hub pairing state
        HubModel activeHub = HubModelProvider.instance().getHubModel();
        if (activeHub != null) {
            Listeners.clear(activeHubListener);
            activeHubListener = activeHub.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (Hub.ATTR_STATE.equals(event.getPropertyName()) && Hub.STATE_NORMAL.equals(event.getNewValue())) {
                        fireOnHubPairingTimeout();
                    }
                }
            });
        }
    }

    private void monitorForPairedDevices () {
        // Listen for devices that get paired while we're on this screen
        Listeners.clear(pairedDeviceListener);
        pairedDeviceListener = ArcusApplication.getArcusApplication().getCorneaService().addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(@NonNull ClientMessage clientMessage) {
                if (clientMessage.getEvent() instanceof Capability.AddedEvent) {
                    final String deviceAddress = String.valueOf(clientMessage.getEvent().getAttribute(Capability.ATTR_ADDRESS));
                    DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
                        @Override
                        public void onEvent(DeviceModel deviceModel) {
                            fireOnDeviceFound(deviceModel);
                        }
                    }).onFailure(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            logger.error("Failed to load device model for newly paired device {}. Something ain't right.", deviceAddress);
                        }
                    });
                }
            }
        });
    }

    private void loadInitialPairingStep(String productAddress, final boolean showLastStepFirst) {
        fireOnLoading();

        // Load the pairing steps from the product catalog
        ProductCatalog.GetProductRequest request = new ProductCatalog.GetProductRequest();
        request.setAddress(Addresses.toServiceAddress(ProductCatalog.NAMESPACE));
        request.setId(CorneaUtils.getIdFromAddress(productAddress));

        CorneaClientFactory.getClient().request(request)
                .onSuccess(Listeners.runOnUiThread(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {
                        ProductCatalog.GetProductResponse response = new ProductCatalog.GetProductResponse(clientEvent);
                        if (response.getProduct() != null) {
                            ProductModel productModel = (ProductModel) CorneaClientFactory.getModelCache().addOrUpdate(response.getProduct());
                            if (productModel != null) {
                                pairingStepsData = productModel.getPair();
                                deviceName = productModel.getName();
                                productId = productModel.getId();
                                tutorialVideoUrl = productModel.getPairVideoUrl();

                                currentStep = showLastStepFirst ? pairingStepsData.size() - 1 : 0;
                                logger.debug("Pairing instructions successfully loaded. Displaying step {} of {}.", currentStep, pairingStepsData.size());
                                fireOnShowPairingStep(getPairingStep(currentStep));
                            }
                        }
                    }
                })).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                logger.debug("Failed to load pairing instructions due to {}.", throwable.getMessage());
                fireOnError(throwable);
            }
        });
    }

    private PairingStep getPairingStep (int stepNumber) {
        return new PairingStep(productId, deviceName, tutorialVideoUrl, stepNumber, pairingStepsData.get(stepNumber));
    }

    private void fireOnDeviceFound (final DeviceModel deviceModel) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if(listener != null) {
                        listener.onDeviceFound(deviceModel);
                    }
                }
            });
        }
    }

    private void fireOnShowPairingStep (final PairingStep step) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if(listener != null) {
                        listener.onShowPairingStep(step);
                    }
                }
            });
        }
    }

    private void fireOnError (final Throwable cause) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
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

    private void fireOnLoading () {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if(listener != null) {
                        listener.onLoading();
                    }
                }
            });
        }
    }

    private void fireOnHubPairingTimeout () {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onHubPairingTimeout();
                    }
                }
            });
        }
    }
}
