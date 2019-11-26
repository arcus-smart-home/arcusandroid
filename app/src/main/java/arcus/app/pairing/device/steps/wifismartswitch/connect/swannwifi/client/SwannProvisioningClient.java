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
package arcus.app.pairing.device.steps.wifismartswitch.connect.swannwifi.client;

import android.os.Handler;
import android.os.Looper;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A facade for sending commands to the Swann provisioning server running on the smart plug device.
 *
 * This class delegates to {@link SwannSocketClient} for communication, but handles threading and
 * message response correlation.
 */
public class SwannProvisioningClient {

    private final static Logger logger = LoggerFactory.getLogger(SwannProvisioningClient.class);

    private final static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("swann-provisioning-client-%d").build();
    private final static ExecutorService requestExecutor = Executors.newSingleThreadExecutor(threadFactory);

    public interface SwannResponseListener {
        void onSuccess(SwannResponse response);
        void onError(Throwable t);
    }

    public interface ReachabilityListener {
        void isReachable(boolean reachable);
    }

    public static void isServerReachable(final int timeout, final ReachabilityListener listener) {
        requestExecutor.submit(new Runnable() {
            @Override
            public void run() {
                boolean isReachable = SwannSocketClient.getInstance().isServerReachable(timeout);

                // Call back on a UI thread to prevent deadlocks (in the case the listener then invokes another provisioning method)
                fireNetworkReachability(listener, isReachable);
            }
        });
    }

    public static void requestMac(final SwannResponseListener listener) {
        requestExecutor.submit(new SwannRequestTask(SwannRequest.ofMacRequest(), SwannResponse.ResponseType.MAC, SwannResponse.ResponseType.NACK, listener));
    }

    public static void setHomeNetworkSsid(String ssid, SwannResponseListener listener) {
        requestExecutor.submit(new SwannRequestTask(SwannRequest.ofSetTargetSsid(ssid), SwannResponse.ResponseType.ACK, SwannResponse.ResponseType.NACK, listener));
    }

    public static void setHomeNetworkPassword(String password, SwannResponseListener listener) {
        requestExecutor.submit(new SwannRequestTask(SwannRequest.ofSetTargetPassword(password), SwannResponse.ResponseType.ACK, SwannResponse.ResponseType.NACK, listener));
    }

    public static void requestReboot(final SwannResponseListener listener) {
        requestExecutor.submit(new SwannRequestTask(SwannRequest.ofRebootRequest(), SwannResponse.ResponseType.ACK, SwannResponse.ResponseType.NACK, listener));
    }

    private static class SwannRequestTask implements Runnable {

        private final SwannRequest request;
        final SwannResponse.ResponseType successResponse;
        final SwannResponse.ResponseType errorResponse;
        final SwannResponseListener listener;

        public SwannRequestTask(final SwannRequest request,
                                final SwannResponse.ResponseType successResponse,
                                final SwannResponse.ResponseType errorResponse,
                                final SwannResponseListener listener)
        {
            this.request = request;
            this.successResponse = successResponse;
            this.errorResponse = errorResponse;
            this.listener = listener;
        }

        @Override
        public void run() {
            logger.debug("Request {} is beginning to execute.", request.getType());

            // Open a connection to the Swann server on the device and start listening for responses
            ResponseMessageListener responseMessageListener = new ResponseMessageListener(successResponse, errorResponse, listener);

            if (SwannSocketClient.getInstance().openConnection(responseMessageListener)) {

                if (!SwannSocketClient.getInstance().sendMessage(request)) {
                    listener.onError(new IllegalStateException("Failed to send message to Swann server"));
                }
            }

            // Not able to open a socket connection
            else {
                listener.onError(new IllegalStateException("Failed to open socket to Swann server."));
            }
        }
    }

    private static class ResponseMessageListener implements SwannMessageListener {

        private boolean complete = false;
        private final SwannResponse.ResponseType successResponse;
        private final SwannResponse.ResponseType errorResponse;
        private final SwannResponseListener listener;

        public ResponseMessageListener(SwannResponse.ResponseType successResponse, SwannResponse.ResponseType errorResponse, SwannResponseListener listener) {
            this.successResponse = successResponse;
            this.errorResponse = errorResponse;
            this.listener = listener;
        }

        @Override
        public void onMessageReceived(SwannResponse message) {

            // Swann responded with expected message type
            if (message.getType() == successResponse) {
                complete = true;
                SwannSocketClient.getInstance().closeConnection();
                listener.onSuccess(message);
            }

            // Swann responded with an error response
            else if (message.getType() == errorResponse) {
                complete = true;
                SwannSocketClient.getInstance().closeConnection();
                listener.onError(new IllegalStateException("Received error in response to request."));
            }

            // Swann responded, but message is uninteresting to us.
            else {
                logger.debug("Received non-terminal response 0x{} of type {}. Waiting for terminal response {}", message.toHexString(), message.getType(), successResponse);
            }
        }

        @Override
        public void onSocketClosed() {
            if (!complete) {
                listener.onError(new IllegalStateException("Socket closed before successful response was received."));
            }
        }
    }

    private static void fireNetworkReachability (final ReachabilityListener listener, final boolean isReachable) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.isReachable(isReachable);
            }
        });
    }
}
