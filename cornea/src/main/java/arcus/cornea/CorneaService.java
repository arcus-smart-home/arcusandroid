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
package arcus.cornea;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import arcus.cornea.controller.IProductController;
import arcus.cornea.controller.IRuleController;
import arcus.cornea.controller.ISetupController;
import arcus.cornea.network.NetworkConnectionMonitor;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.ErrorEvent;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.Store;
import com.iris.client.session.SessionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorneaService extends Service {
    private Logger logger = LoggerFactory.getLogger(CorneaService.class);

    private static IProductController productController;
    private static ISetupController setupController;

    private final static String HANDLER_THREAD_NAME = "IOThread";
    private HandlerThread handlerThread;
    private Handler handler;

    private ListenerRegistration errorEventListener;

    public class CorneaBinder extends Binder {
        public CorneaService getService() {
            return CorneaService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (handlerThread == null || !handlerThread.isAlive()) {
            init("onBind");
        }

        return new CorneaBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        if (handlerThread == null || !handlerThread.isAlive()) {
            init("onRebind");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        throw new UnsupportedOperationException(getClass().getSimpleName() + " must be bound, not started.");
    }

    @Override
    public void onCreate() {
        init("onCreate");
    }

    private void init(String from) {
        if (handlerThread == null || !handlerThread.isAlive()) {
            handlerThread = new HandlerThread(HANDLER_THREAD_NAME, android.os.Process.THREAD_PRIORITY_BACKGROUND);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        CorneaClientFactory.init();

        setupController   = new SetupController(handler);
        productController = new ProductController();

        NetworkConnectionMonitor.getInstance().startListening(getApplicationContext());

        addLoggingErrorListener();
        logger.debug("{} created via {}", getClass().getSimpleName(), from);
    }

    @Override
    public void onDestroy() {
        logger.debug("Destroying Cornea Service.");

        try {
            removeLoggingErrorListener();
            CorneaClientFactory.getClient().close();
            NetworkConnectionMonitor.getInstance().stopListening(getApplicationContext());

            if (handlerThread != null && handlerThread.isAlive()) {
                handlerThread.quit();
            }
        }
        catch (Exception ex) {
            logger.debug("Caught Exception while destroying service.", ex);
        }
    }

    public void addLoggingErrorListener() {
        removeLoggingErrorListener();
        errorEventListener = addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(ClientMessage clientMessage) {
                if (clientMessage.getEvent() instanceof ErrorEvent) {
                    ErrorEvent event = (ErrorEvent) clientMessage.getEvent();
                    logger.error("CORNEA ErrorEvent: {}", event);
                }
            }
        });
    }

    public void removeLoggingErrorListener() {
        Listeners.clear(errorEventListener);
    }

    public void setConnectionURL(@NonNull String connectionURL) {
        Preconditions.checkNotNull(connectionURL);

        CorneaClientFactory.getClient().setConnectionURL(connectionURL);
    }

    public boolean isConnected() {
        try {
            return CorneaClientFactory.isConnected();
        }
        catch (Exception ex) {
            // If we haven't called init() yet could end up here.
            return false;
        }
    }

    @Deprecated // Use RuleModelProvider
    public IRuleController rules() { return RuleController.instance(); }

    @Deprecated // Use ProductModelProvider
    public IProductController products() { return productController; }

    public ISetupController setup() {
        return setupController;
    }

    public <M extends Model> Store<M> getStore(Class<M> storeType) {
        return CorneaClientFactory.getStore(storeType);
    }
    public <S extends com.iris.client.service.Service> S getService(Class<S> service) { return CorneaClientFactory.getService(service); }

    @Nullable
    public ListenerRegistration addSessionListener(Listener<? super SessionEvent> listener) {
        try {
            return CorneaClientFactory.getClient().addSessionListener(listener);
        }
        catch (Exception ex) {
            logger.error("Could not addSessionListener.  Is the client initialized?", ex.getClass().getSimpleName());
        }

        return null;
    }

    @Nullable
    public ListenerRegistration addRequestListener(Listener<? super ClientRequest> listener) {
        try {
            return CorneaClientFactory.getClient().addRequestListener(listener);
        }
        catch (Exception ex) {
            logger.error("Could not addRequestListener.  Is the client initialized?", ex.getClass().getSimpleName());
        }

        return Listeners.empty();
    }

    @Nullable
    public ListenerRegistration addMessageListener(Listener<? super ClientMessage> listener) {
        try {
            return CorneaClientFactory.getClient().addMessageListener(listener);
        }
        catch (Exception ex) {
            logger.info("Could not addMessageListener.  Is the client initialized?", ex.getClass().getSimpleName());
        }

        return Listeners.empty();
    }
}
