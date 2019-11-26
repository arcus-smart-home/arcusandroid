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
package arcus.app.pairing.device.steps.wifismartswitch.connect.swannwifi.controller;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.events.NetworkLostEvent;
import arcus.cornea.network.NetworkConnectionMonitor;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClientFactory;
import com.iris.client.capability.Capability;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.DeviceModel;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;
import arcus.app.ArcusApplication;
import arcus.app.common.machine.State;
import arcus.app.common.machine.StateException;
import arcus.app.common.machine.StateMachine;
import arcus.app.common.wifi.ChangeWifiNetworkTask;
import arcus.app.common.wifi.PhoneWifiHelper;
import arcus.app.pairing.device.steps.wifismartswitch.connect.swannwifi.client.SwannProvisioningClient;
import arcus.app.pairing.device.steps.wifismartswitch.connect.swannwifi.client.SwannResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.greenrobot.event.EventBus;

/**
 * Implements a state machine responsible for provisioning a Swann Wifi Smart plug for use with
 * Arcus.
 * <p/>
 * The general provisioning steps are:
 * <p/>
 * 1. Disconnect from Platform
 * 2. Change the phone's WiFi connection to associate with the access point built into the Swann device.
 * 3. Connect to the provisioning server running on the Swann device
 * 4. Provision the user's home WiFi network configuration in the device and request the MAC address
 * of the smart plug.
 * 5. Disconnect from the Swann device and restore the user's original network connection
 * 6. Re-establish the Cornea connection
 * 7. Upon success, notify the client of success and pass along the device MAC we retrieved.
 * <p/>
 * Upon success, the client may use the returned MAC address to add/register the device with the
 * user's account.
 */
public class SwannProvisioningController {

    private final static Logger logger = LoggerFactory.getLogger(SwannProvisioningController.class);

    private final static String ALREADY_CLAIMED_CODE = "request.invalid";
    private final static String NOT_FOUND_CODE = "request.destination.notfound";

    public interface ProvisioningControllerListener {
        void onStateChange(State lastState, State currentState);
        void onError(State state, Throwable e);
        void onSuccess(DeviceModel deviceModel);
    }

    private static String swannApSsid;                  // SSID of the Swann AP (i.e., 'Smart Plug XXXX')
    private static String swannMacAddress;              // MAC address of the device
    private static String targetNetworkSsid;            // SSID of the user's network the device should connect to
    private static String targetNetworkPassword;        // Password for the user's network or null if unprotected
    private static WifiInfo currentNetwork;             // Network that phone is connected to when provisioning starts
    private static Map<String, String> requestParams = Collections.emptyMap();

    // Indication that we've succeeded in provisioning the device; used to signal separate error copy
    private static boolean deviceProvisionedSuccessfully;

    private static ListenerRegistration corneaConnectListener;
    private static ListenerRegistration deviceAddedListener;

    public static void setRequestParams(@NonNull Map<String, String> params) {
        requestParams = new HashMap<>(params);
    }

    public static void provisionSmartPlug(@NonNull final ProvisioningControllerListener listener, @NonNull String swannApSsid, @NonNull String targetNetworkSsid, @Nullable String targetNetworkPassword) {
        SwannProvisioningController.swannApSsid = swannApSsid;
        SwannProvisioningController.targetNetworkSsid = targetNetworkSsid;
        SwannProvisioningController.targetNetworkPassword = targetNetworkPassword;
        SwannProvisioningController.currentNetwork = PhoneWifiHelper.getCurrentWifiNetwork(ArcusApplication.getContext());
        SwannProvisioningController.swannMacAddress = null;
        SwannProvisioningController.deviceProvisionedSuccessfully = false;

        final StateMachine executor = new StateMachine(new DisconnectCorneaState());
        executor.setObserver(new StateMachine.StateMachineObserver() {
            @Override
            public void onStateChanged(State lastState, State currentState) {
                listener.onStateChange(lastState, currentState);
            }

            @Override
            public void onTerminated(State terminalState) {
                if (terminalState instanceof TerminalFailedState) {
                    listener.onError(terminalState, executor.getLastException());
                } else if (terminalState instanceof TerminalSuccessState) {
                    listener.onSuccess(((TerminalSuccessState)terminalState).model);
                } else {
                    throw new IllegalStateException("Bug! State machine terminated in non-terminal state. Should not be possible.");
                }
            }
        });
        executor.run();
    }

    // Initial state
    public static class DisconnectCorneaState extends State {

        private final static int MAX_RETRIES = 0;

        public DisconnectCorneaState() {
            super(DisconnectCorneaState.class.getSimpleName(), MAX_RETRIES);
        }

        @Override
        public void execute() throws StateException {
            try {
                NetworkConnectionMonitor.getInstance().stopListening(ArcusApplication.getContext());
                IrisClientFactory.getClient().close();
                //CorneaClientFactory.getModelCache().clearCache();

                transition(new ConnectToDeviceApState());
            }
            catch (Exception ex) {
                throw new StateException(new TerminalFailedState(TerminalFailedState.Cause.CORNEA_ERROR, "An error occurred while trying to disconnect from Cornea: " + ex.getMessage()));
            }
        }
    }

    public static class ConnectToDeviceApState extends State {

        // Change wifi task already has retries as needed.
        private final static int MAX_RETRIES = 0;

        public ConnectToDeviceApState() {
            super(ConnectToDeviceApState.class.getSimpleName(), MAX_RETRIES);
        }

        @Override
        public void execute() throws StateException {
            WifiConfiguration network = PhoneWifiHelper.getConfigurationForNetwork(ArcusApplication.getContext(), swannApSsid, null);
            new ChangeWifiNetworkTask(ArcusApplication.getContext(), new ChangeWifiNetworkTask.ChangeWifiNetworkListener() {
                @Override
                public void onWifiChangeComplete(boolean success, String currentSsid) {
                    if (success) {
                        transition(new WaitForDeviceServerState());
                    } else {
                        handleException(new StateException(new ConnectToHomeNetworkState(), "Failed to connect to the Swann device AP."));
                    }
                }
            }).execute(network);
        }
    }

    public static class WaitForDeviceServerState extends State {

        private final static int MAX_RETRIES = 8;

        public WaitForDeviceServerState() {
            super(WaitForDeviceServerState.class.getSimpleName(), MAX_RETRIES);
        }

        @Override
        public void execute() throws StateException {
            SwannProvisioningClient.isServerReachable(5000, new SwannProvisioningClient.ReachabilityListener() {
                @Override
                public void isReachable(boolean reachable) {
                    if (reachable) {
                        transition(new RequestDeviceMacState());
                    } else {
                        handleException(new StateException(new ConnectToHomeNetworkState(), "Swann provisioning server is not reachable."));
                    }
                }
            });
        }
    }

    public static class RequestDeviceMacState extends State {

        public RequestDeviceMacState() {
            super(RequestDeviceMacState.class.getSimpleName());
        }

        @Override
        public void execute() throws StateException {
            SwannProvisioningClient.requestMac(new SwannProvisioningClient.SwannResponseListener() {
                @Override
                public void onSuccess(SwannResponse response) {
                    logger.debug("Got mac request response 0x{} of type {} with payload 0x{}.", response.toHexString(), response.getType(), response.getPayload().toHexString());
                    swannMacAddress = response.getPayload().toHexString();
                    transition(new ProvisionHomeSsidState());
                }

                @Override
                public void onError(Throwable t) {
                    handleException(new StateException(new ConnectToHomeNetworkState(), t));
                }
            });
        }
    }

    public static class ProvisionHomeSsidState extends State {

        public ProvisionHomeSsidState() {
            super(ProvisionHomeSsidState.class.getSimpleName());
        }

        @Override
        public void execute() throws StateException {
            SwannProvisioningClient.setHomeNetworkSsid(targetNetworkSsid, new SwannProvisioningClient.SwannResponseListener() {
                @Override
                public void onSuccess(SwannResponse response) {
                    logger.debug("Got ssid response 0x{} of type {}.", response.toHexString(), response.getType());
                    transition(new ProvisionHomePasswordState());
                }

                @Override
                public void onError(Throwable t) {
                    handleException(new StateException(new ConnectToHomeNetworkState(), t));
                }
            });
        }
    }

    public static class ProvisionHomePasswordState extends State {

        public ProvisionHomePasswordState() {
            super(ProvisionHomePasswordState.class.getSimpleName());
        }

        @Override
        public void execute() throws StateException {

            // Target network is not protected; nothing to do in this state
            if (StringUtils.isEmpty(targetNetworkPassword)) {
                logger.debug("No password defined for home network; skipping.");
                transition(new RebootDeviceState());
            }

            // Target network is protected; provision security
            else {
                SwannProvisioningClient.setHomeNetworkPassword(targetNetworkPassword, new SwannProvisioningClient.SwannResponseListener() {
                    @Override
                    public void onSuccess(SwannResponse response) {
                        logger.debug("Got password response 0x{} of type {}.", response.toHexString(), response.getType());
                        transition(new RebootDeviceState());
                    }

                    @Override
                    public void onError(Throwable t) {
                        handleException(new StateException(new ConnectToHomeNetworkState(), t));
                    }
                });
            }
        }
    }

    public static class RebootDeviceState extends State {

        public RebootDeviceState() {
            super(RebootDeviceState.class.getSimpleName());
        }

        @Override
        public void execute() throws StateException {
            SwannProvisioningClient.requestReboot(new SwannProvisioningClient.SwannResponseListener() {
                @Override
                public void onSuccess(SwannResponse response) {
                    logger.debug("Got reboot response 0x{} of type {}.", response.toHexString(), response.getType());
                    deviceProvisionedSuccessfully = true;
                    transition(new ForgetDeviceApState());
                }

                @Override
                public void onError(Throwable t) {
                    handleException(new StateException(new ConnectToHomeNetworkState(), t));
                }
            });
        }
    }

    public static class ForgetDeviceApState extends State {

        public ForgetDeviceApState() {
            super(ForgetDeviceApState.class.getSimpleName());
        }

        @Override
        public void execute() throws StateException {
            int currentNetwork = PhoneWifiHelper.getCurrentWifiNetwork(ArcusApplication.getContext()).getNetworkId();
            if (!PhoneWifiHelper.removeConfiguredNetwork(ArcusApplication.getContext(), currentNetwork)) {
                logger.warn("Failed to 'forget' Swann AP network.");
            }

            transition(new ConnectToHomeNetworkState());
        }
    }

    public static class ConnectToHomeNetworkState extends State {

        public ConnectToHomeNetworkState() {
            super(ConnectToHomeNetworkState.class.getSimpleName());
        }

        @Override
        public void execute() throws StateException {
            // Reconnect to previous WiFi network
            if (currentNetwork != null) {
                new ChangeWifiNetworkTask(ArcusApplication.getContext(), new ChangeWifiNetworkTask.ChangeWifiNetworkListener() {
                    @Override
                    public void onWifiChangeComplete(boolean success, String currentSsid) {
                        if (!success) {
                            handleException(new StateException(new TerminalCriticalFailedState(TerminalFailedState.Cause.DEVICE_WIFI_ERROR, "Attempt to change Wi-Fi network back to home network failed. Current network is " + currentSsid)));
                        } else {
                            transition(new ReconnectCorneaState());
                        }
                    }
                }).execute(currentNetwork.getNetworkId());
            }

            // Oops... user was previously connected via something else (cellular, one assumes...)
            else {
                PhoneWifiHelper.disconnectFromWifi(ArcusApplication.getContext());
                transition(new ReconnectCorneaState());
            }
        }
    }

    public static class ReconnectCorneaState extends State {

        public ReconnectCorneaState() {
            super(ReconnectCorneaState.class.getSimpleName());
        }

        @Override
        public void execute() throws StateException {

            EventBus.getDefault().removeAllStickyEvents();
            Listeners.clear(corneaConnectListener);

            corneaConnectListener = IrisClientFactory.getClient().addSessionListener(new Listener<SessionEvent>() {
                @Override
                public void onEvent(SessionEvent sessionEvent) {
                    if (sessionEvent instanceof SessionActivePlaceSetEvent) {
                        // Stop listening for session events
                        Listeners.clear(corneaConnectListener);

                        if (StringUtils.isEmpty(swannMacAddress)) {
                            transition(new TerminalFailedState(TerminalFailedState.Cause.PROVISIONING_FAILURE, "Failed to provision smart plug; aborting."));
                        } else {
                            transition(new RegisterDeviceState());
                        }
                    }
                }
            });

            SessionController.instance().reconnect();
        }
    }

    public static class RegisterDeviceState extends State {

        private final static int MAX_RETRIES = 60;
        private final static int DELAY_BETWEEN_RETRY_MS = 1000;
        private final static int MAX_TIMEOUT = MAX_RETRIES * DELAY_BETWEEN_RETRY_MS;

        public RegisterDeviceState() {
            super(RegisterDeviceState.class.getSimpleName(), MAX_RETRIES, DELAY_BETWEEN_RETRY_MS);
        }

        @Override
        public void execute() throws StateException {

            // Build the request attributes
            // TODO: Do we have IPCD capability constants defined for these?
            Map<String, Object> requestArguments = new HashMap<>();
            requestArguments.put("IPCD:modelcode", "");
            requestArguments.put("IPCD:serialcode", "");
            requestArguments.put("IPCD:sn", swannMacAddress.toLowerCase());

            if (requestParams.isEmpty()) {
                requestArguments.put("IPCD:v1devicetype", "Other");
            } else {
                requestArguments.putAll(requestParams);
            }

            // Build the request
            Map<String, Object> requestAttributes = new HashMap<>();
            requestAttributes.put("attrs", requestArguments);

            ClientRequest request = new ClientRequest();
            request.setCommand("bridgesvc:RegisterDevice");
            request.setAddress("BRDG::IPCD");
            request.setAttributes(requestAttributes);
            request.setRestfulRequest(false);

            // No matter what happens, break out of this state if MAX_TIMEOUT elapses. Under certain
            // conditions the IPCD registration request may succeed, but the device will not get added
            // this will keep us stuck in this state.
            final Handler timeoutHandler = new Handler();
            timeoutHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    transition(new TerminalFailedState(TerminalFailedState.Cause.TIMEOUT, "Timeout occurred waiting for IPCD registration"));
                }
            }, MAX_TIMEOUT);

            try {
                Listeners.clear(deviceAddedListener);
                deviceAddedListener = CorneaClientFactory.getClient().addMessageListener(new Listener<ClientMessage>() {
                    @Override
                    public void onEvent(@NonNull ClientMessage clientMessage) {
                        if (clientMessage.getEvent() instanceof Capability.AddedEvent) {
                            requestParams = Collections.emptyMap(); // Clear the static map out
                            timeoutHandler.removeCallbacksAndMessages(null);
                            Listeners.clear(deviceAddedListener);

                            final String deviceAddress = String.valueOf(clientMessage.getEvent().getAttribute(Capability.ATTR_ADDRESS));
                            DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
                                @Override
                                public void onEvent(DeviceModel deviceModel) {
                                    transition(new TerminalSuccessState(deviceModel));
                                }
                            }).onFailure(new Listener<Throwable>() {
                                @Override
                                public void onEvent(Throwable throwable) {
                                    handleException(new StateException(new TerminalFailedState(TerminalFailedState.Cause.UNKNOWN, "Attempt to retrieve device model for smart plug failed."), throwable));
                                }
                            });
                        }
                    }
                });

                // Request to register the device...
                CorneaClientFactory.getClient().request(request).onFailure(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        timeoutHandler.removeCallbacksAndMessages(null);

                        if (throwable instanceof ErrorResponseException) {
                            ErrorResponseException ere = (ErrorResponseException) throwable;

                            if (NOT_FOUND_CODE.equalsIgnoreCase(ere.getCode())) {
                                handleException(new StateException(new TerminalFailedState(TerminalFailedState.Cause.NOT_FOUND, "Smart plug not found; are WiFi credentials correct?."), throwable));
                            } else if (ALREADY_CLAIMED_CODE.equalsIgnoreCase(ere.getCode())) {
                                handleExceptionWithoutRetry(new StateException(new TerminalFailedState(TerminalFailedState.Cause.DEVICE_TAKEN, "Smart plug is already associated with another account."), throwable));
                            } else {
                                handleException(new StateException(new TerminalFailedState(TerminalFailedState.Cause.UNKNOWN, "Platform rejected device registration for an unknown reason."), throwable));
                            }
                        }

                        // Unexpected exception type
                        else {
                            handleException(new StateException(new TerminalFailedState(TerminalFailedState.Cause.UNKNOWN, "Request to register device failed for an unknown reason."), throwable));
                        }
                    }
                });

            } catch (Exception e) {
                timeoutHandler.removeCallbacksAndMessages(null);
                throw new StateException(new TerminalFailedState(TerminalFailedState.Cause.UNKNOWN, "An exception occurred while trying to register device."), e);
            }
        }
    }

    public static class TerminalSuccessState extends State {

        private final static int MAX_RETRIES = 0;
        private final DeviceModel model;

        public TerminalSuccessState(DeviceModel model) {
            super(TerminalSuccessState.class.getSimpleName(), MAX_RETRIES);
            this.model = model;
        }

        @Override
        public void execute() throws StateException {
            NetworkConnectionMonitor.getInstance().startListening(ArcusApplication.getContext());
            terminate();
        }
    }

    /**
     * Indicates that pairing failed and we were unsuccessful in reconnecting to the platform
     * (as well as potentially failing to reconnect to the user's network).
     */
    public static class TerminalCriticalFailedState extends TerminalFailedState {

        public TerminalCriticalFailedState(Cause cause, String reason) {
            super(cause, reason);
        }

        @Override
        public void execute() throws StateException {
            // Turn on network connection monitor and alert that we have no network
            NetworkConnectionMonitor.getInstance().startListening(ArcusApplication.getContext());
            EventBus.getDefault().post(new NetworkLostEvent());
            terminate();
        }
    }


    /**
     * Indicates that pairing failed, but that the user can retry without further involvement.
     */
    public static class TerminalFailedState extends State {

        public enum Cause {
            CORNEA_ERROR,                   // Failed to disconnect from Cornea
            PROVISIONING_FAILURE,           // Failed to poke SSID/password into Swann device
            UNKNOWN,                        // Unknown error occured
            TIMEOUT,                        // Gave up after timeout reached
            DEVICE_WIFI_ERROR,              // Error changing/provisioning phone's WiFi connection
            NOT_FOUND,                      // Swann device not found on (i.e., failed to connect to platform)
            DEVICE_TAKEN                    // Platform reports that this plug is in use on some other account
        }

        private final static int MAX_RETRIES = 0;
        public final String reason;
        public final Cause cause;

        public TerminalFailedState(Cause cause, String reason) {
            super(TerminalFailedState.class.getSimpleName(), MAX_RETRIES);

            this.cause = cause;
            this.reason = reason;
        }

        @Override
        public void execute() throws StateException {
            // Assumption is that we're reconnected; restart monitoring for network loss
            NetworkConnectionMonitor.getInstance().startListening(ArcusApplication.getContext());
            terminate();
        }

        public boolean wasDeviceProvisioned() {
            return deviceProvisionedSuccessfully;
        }

        public Cause getCause() {
            return cause;
        }
    }
}
