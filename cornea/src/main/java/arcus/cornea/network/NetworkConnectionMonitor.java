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
package arcus.cornea.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.SessionController;
import arcus.cornea.events.NetworkConnectedEvent;
import arcus.cornea.events.NetworkLostEvent;
import arcus.cornea.events.SessionLostEvent;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

import de.greenrobot.event.EventBus;

public class NetworkConnectionMonitor {
    private static final Logger logger = LoggerFactory.getLogger(NetworkConnectionMonitor.class);
    private static final NetworkConnectionMonitor instance = new NetworkConnectionMonitor();
    private State state;
    private ConnectivityBroadcastReceiver receiver;
    private AtomicBoolean suppressEvents = new AtomicBoolean(false);
    private AtomicBoolean isMonitoring = new AtomicBoolean(false);

    private ListenerRegistration loginCallbackReg;
    private final SessionController.LoginCallback loginCallback = new SessionController.LoginCallback() {
        @Override public void loginSuccess(
              @Nullable PlaceModel placeModel,
              @Nullable PersonModel personModel,
              @Nullable AccountModel accountModel
        ) {
            EventBus.getDefault().post(new NetworkConnectedEvent());
            logger.debug("Posted NetworkConnectedEvent.");
            Listeners.clear(loginCallbackReg);
        }

        @Override public void onError(Throwable throwable) {
            SessionLostEvent event = new SessionLostEvent();
            event.setCause(throwable);
            EventBus.getDefault().post(event);
            Listeners.clear(loginCallbackReg);
        }
    };

    private enum State {
        CONNECTED,
        DISCONNECTED
    }

    private class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                return;
            }

            ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                return;
            }

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            if (!isConnected) {
                state = State.DISCONNECTED;
                if (!suppressEvents.get()) {
                    EventBus.getDefault().post(new NetworkLostEvent());
                    logger.debug("Posted NetworkLostEvent.");
                }
            } else if (state != null) { // Avoid posting if the connection is connected & we haven't posted a disconnected event.
                state = State.CONNECTED;
                // We were previously disconnected before, try to reconnect.
                loginCallbackReg = SessionController.instance().setCallback(loginCallback);
                logger.debug("Attempting to reconnect to the Arcus service.");
                SessionController.instance().reconnect();
            }
        }
    }

    private NetworkConnectionMonitor () {
        receiver = new ConnectivityBroadcastReceiver();
    }

    public static NetworkConnectionMonitor getInstance() {
        return instance;
    }

    public void suppressEvents(boolean suppress) {
        suppressEvents.set(suppress);
    }

    public State getCurrentState() { return state; }

    public synchronized void startListening(@NonNull Context context) {
        if (isMonitoring.compareAndSet(false, true)) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

            context.registerReceiver(receiver, filter);
            logger.debug("Starting to listen for device network changes");
        }
    }

    public synchronized void stopListening(@NonNull Context context) {
        if (isMonitoring.compareAndSet(true, false)) {
            try {
                context.unregisterReceiver(receiver);
            } catch (Exception e) {
                // Nothing to do; don't die if receiver isn't registered.
            }

            logger.debug("No longer listening for device network changes");
        }
    }
}
