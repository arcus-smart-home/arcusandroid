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
package arcus.cornea.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.IrisClient;
import com.iris.client.capability.Capability;
import com.iris.client.event.Listener;
import com.iris.client.session.SessionEvent;
import com.iris.client.session.SessionExpiredEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PropertyChangeMonitor {
    private static final Logger logger = LoggerFactory.getLogger(PropertyChangeMonitor.class);
    private static final String ADDRESS = "ADDRESS";
    private static final String ATTRIBUTE = "ATTRIBUTE";

    private static final PropertyChangeMonitor INSTANCE;
    static {
        HandlerThread handlerThread = new HandlerThread("MonitorPool", Process.THREAD_PRIORITY_BACKGROUND);
        handlerThread.start();

        INSTANCE = new PropertyChangeMonitor(CorneaClientFactory.getClient(), handlerThread.getLooper());
    }

    protected final Handler monitorHandler;
    protected final Map<String, ResultInstructions> monitorMap = Collections.synchronizedMap(new HashMap<String, ResultInstructions>());

    public interface Callback {
        void requestTimedOut(String address, String attribute);
        void requestSucceeded(String address, String attribute);
    }

    protected PropertyChangeMonitor(IrisClient client, Looper looper) {
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(looper);

        monitorHandler = new MonitorHandler(looper);
        client.addMessageListener(new Listener<ClientMessage>() {
            @Override public void onEvent(ClientMessage clientMessage) {
                ClientEvent event = clientMessage.getEvent();
                if (event == null) {
                    return;
                }

                if (!(event instanceof Capability.ValueChangeEvent)) {
                    return;
                }

                Capability.ValueChangeEvent vc = (Capability.ValueChangeEvent) event;
                String source = vc.getSourceAddress();
                Set<String> changedKeys = vc.getAttributes().keySet();

                synchronized (monitorMap) {
                    ResultInstructions instructions = monitorMap.get(source);
                    if (instructions != null && changedKeys.contains(instructions.getAttribute())) {
                        Object newValue = vc.getAttribute(instructions.getAttribute());
                        Object expectedValue = instructions.getValue();

                        // If the expected value is not set, we saw this property go ahead and succeed
                        // Or - If the value was set and the values are equal, we can succeed.
                        if (expectedValue == null || Objects.equal(expectedValue, newValue)) {
                            monitorMap.remove(source);
                            notifySuccess(source, instructions);
                        }
                    }
                }
            }
        });

        client.addSessionListener(new Listener<SessionEvent>() {
            @Override public void onEvent(SessionEvent event) {
                if (event instanceof SessionExpiredEvent) {
                    try {
                        monitorMap.clear();
                        monitorHandler.removeCallbacksAndMessages(null);
                    } catch (Exception ignored) {}
                }
            }
        });
    }

    public static PropertyChangeMonitor instance() {
        return INSTANCE;
    }

    public void startMonitorFor(
          @NonNull String address,
          @NonNull String attribute,
          long timeoutInMS,
          @Nullable PropertyChangeMonitor.Callback callback,
          @Nullable Object valueShouldBe,
          @Nullable Function<String, ?> noCallbackFailedUpdate
    ) {
        Reference<Callback> callbackRef = new WeakReference<>(callback);
        ResultInstructions instructions = new ResultInstructions(attribute, valueShouldBe, callbackRef, noCallbackFailedUpdate);
        monitorMap.put(address, instructions);

        Bundle bundle = new Bundle(2);
        bundle.putString(ADDRESS, address);
        bundle.putString(ATTRIBUTE, attribute);

        Message message = monitorHandler.obtainMessage();
        message.setData(bundle);

        monitorHandler.sendMessageDelayed(message, timeoutInMS);
    }

    public boolean hasAnyChangesFor(@NonNull String address) {
        return !TextUtils.isEmpty(address) && monitorMap.containsKey(address);
    }

    public void removeAllFor(String address) {
        monitorMap.remove(address);
    }

    protected void notifySuccess(String address, ResultInstructions instructions) {
        Callback callback = instructions.getUpdateCallback();
        if (callback != null) {
            try {
                callback.requestSucceeded(address, instructions.getAttribute());
            }
            catch (Exception ex) {
                logger.error("Could not deliver successful request update.", ex);
            }
        }
    }

    private class MonitorHandler extends Handler {
        public MonitorHandler(Looper looper) {
            super(looper);
        }

        @Override public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String address = bundle.getString(ADDRESS);
            String attribute = bundle.getString(ATTRIBUTE);
            if (TextUtils.isEmpty(address) || TextUtils.isEmpty(attribute)) {
                return;
            }

            ResultInstructions resultInstructions = monitorMap.remove(address);
            if (resultInstructions == null) {
                return;
            }

            PropertyChangeMonitor.Callback callback = resultInstructions.getUpdateCallback();
            if (callback != null) {
                try { // Let the caller take care of the update if they choose to.
                    callback.requestTimedOut(address, attribute);
                }
                catch (Exception ex) {
                    logger.error("Could not deliver unsuccessful request update.", ex);
                }
            }
            else {
                // View was destroyed, navigated away, lost reference.  Go ahead and update.
                Function<String, ?> noCBFunction = resultInstructions.getUpdateFailedFunction();
                if (noCBFunction != null) {
                    noCBFunction.apply(address);
                }
            }
        }
    }
}
