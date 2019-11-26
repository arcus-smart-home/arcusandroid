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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A "low level" communications facade for sending messages to the Swann provisioning server.
 * <p/>
 * This class has been made package-private to prevent users for accessing it directly. Callers
 * should access this functionality through {@link SwannProvisioningClient}.
 */
class SwannSocketClient {

    private final static Logger logger = LoggerFactory.getLogger(SwannSocketClient.class);

    private final String SERVERIP = "192.168.1.1";
    private final int SERVERPORT = 2501;
    private final int SOCKET_TIMEOUT_MS = 15000;

    private final static SwannSocketClient instance = new SwannSocketClient();

    private OutputStream out;
    private InputStream in;
    private Socket socket;

    private SwannSocketClient() {
    }

    private final static ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("swann-socket-listener-%d").build();
    private final static ExecutorService executorService = Executors.newSingleThreadExecutor(threadFactory);

    public static SwannSocketClient getInstance() {
        return instance;
    }

    public boolean isSocketOpen() {
        return socket != null && socket.isConnected();
    }

    public boolean isServerReachable(final int timeout) {
        logger.debug("Checking server reachability with timeout {}.", timeout);
        try {
            boolean isReachable = getServiceAddress().isReachable(timeout);
            logger.debug("Server reachability is {}.", isReachable);
            return isReachable;
        } catch (IOException e) {
            logger.error("IOException while checking server reachability.", e);
            return false;
        }
    }

    public boolean openConnection(SwannMessageListener listener) {
        logger.debug("Opening socket connection to Swann provisioning server.");

        try {

            if (socket != null && !socket.isClosed()) {
                logger.error("Bug! Socket is already open. Did you forget to close it during last use? Doing you a favor and closing it. You can thank me later.");
                socket.close();
                return false;
            }

            socket = new Socket(SERVERIP, SERVERPORT);
            socket.setSoTimeout(SOCKET_TIMEOUT_MS);

            out = socket.getOutputStream();
            in = socket.getInputStream();

            receiveMessages(listener);

        } catch (IOException e) {
            logger.error("IOException while initializing socket connection with Swann provisioning server.", e);
            return false;
        }

        logger.debug("Socket connection opened successfully.");
        return true;
    }

    public boolean closeConnection() {
        logger.debug("Closing socket connection with Swann provisioning server.");

        if (!isSocketOpen()) {
            return false;
        }

        try {
            socket.close();
        } catch (IOException e) {
            logger.error("IOException while closing socket connection with Swann provisioning server.", e);
            return false;
        }

        logger.debug("Socket closed successfully.");
        return true;
    }

    public boolean sendMessage(SwannMessage message) {
        try {
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            logger.error("IOException while sending message to Swann provisioning server.", e);
            return false;
        }

        logger.debug("Message sent successfully.");
        return true;
    }

    private void receiveMessages(final SwannMessageListener listener) {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                logger.debug("Starting to listen for incoming messages from the Swann provisioning server.");

                while (socket != null && socket.isConnected()) {
                    try {
                        byte[] inputBuffer = new byte[50];
                        int bytesRead = in.read(inputBuffer, 0, 25);

                        if (bytesRead > 0) {
                            byte[] serverMessage = new byte[bytesRead];
                            System.arraycopy(inputBuffer, 0, serverMessage, 0, bytesRead);
                            SwannResponse message = new SwannResponse(serverMessage);

                            logger.debug("Received message with payload 0x{} ('{}') from the Swann provisioning server.", message.toHexString(), message.toString());
                            fireOnMessageReceived(listener, message);
                        }

                        // Socket closed while waiting for response
                        else {
                            logger.debug("Socket closed; stopping incoming message listener.");
                            closeConnection();
                            fireOnSocketClosed(listener);
                            return;
                        }
                    } catch (IOException e) {
                        logger.debug("Socket error; closing and stopping incoming message listener.");
                        closeConnection();
                        fireOnSocketClosed(listener);
                        return;
                    }
                }

                logger.error("Socket no longer active. Shutting down listener.");
                closeConnection();
                fireOnSocketClosed(listener);
            }
        });
    }

    private InetAddress getServiceAddress() {
        try {
            return InetAddress.getByName(SERVERIP);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Bug! Server address is not resolvable.");
        }
    }

    private void fireOnMessageReceived(final SwannMessageListener listener, final SwannResponse message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onMessageReceived(message);
            }
        });
    }

    private void fireOnSocketClosed(final SwannMessageListener listener) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onSocketClosed();
            }
        });
    }

}
