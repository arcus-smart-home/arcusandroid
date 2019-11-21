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
package arcus.cornea.device.blinds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.DebouncedRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.PropertyChangeMonitor;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Shade;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Set;

/**
 * ShadeDeviceController returns state information about the Shade device provided by the deviceModel
 *      Allows setting the attribute of Shade.ATTR_OPEN on the device {@link ShadeDeviceController#setLevel(int)}.
 */

public class ShadeDeviceController implements DebouncedRequest.DebounceCallback {

    private static final int DEBOUNCE_REQUEST_DELAY_MS = 500;

    public interface Callback {
        void onError(Throwable throwable);
        void onSuccess(DeviceModel deviceModel);
    }

    protected Reference<Callback> callbackRef = new WeakReference<>(null);
    private ModelSource<DeviceModel> device = CachedModelSource.newSource();
    private final DebouncedRequestScheduler debouncedRequestScheduler;

    private static final int TIMEOUT_MS = 30_000;
    private final Function<String, Void> onFailureFunction = new Function<String, Void>() {
        @Override public Void apply(@Nullable String input) {
            refreshModel();
            return null;
        }
    };

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            updateView();
        }
    });

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

    public static final Set<String> updateValues = ImmutableSet.of(
        Device.ATTR_NAME,
        Shade.ATTR_LEVEL,
        Shade.ATTR_SHADESTATE,
        DevicePower.ATTR_BATTERY,
        DevicePower.ATTR_SOURCE,
        DevicePower.ATTR_SOURCECHANGED,
        DeviceConnection.ATTR_STATE,
        DeviceAdvanced.ATTR_ERRORS
    );

    private final Listener<DeviceModel> loadListener = new Listener<DeviceModel>() {
        @Override public void onEvent(DeviceModel deviceModel) {
            updateView();
        }
    };

    public ShadeDeviceController(@NonNull ModelSource<DeviceModel> deviceModel) {
        debouncedRequestScheduler = new DebouncedRequestScheduler(DEBOUNCE_REQUEST_DELAY_MS);
        device = deviceModel;
        device.load();
        device.addModelListener(new Listener<ModelEvent>() {
            @Override public void onEvent(ModelEvent modelEvent) {
                if (!(modelEvent instanceof ModelChangedEvent)) {
                    return;
                }

                ModelChangedEvent mce = (ModelChangedEvent) modelEvent;
                Set<String> results = Sets.intersection(mce.getChangedAttributes().keySet(), updateValues);
                if (!results.isEmpty()) {
                    updateView();
                }
            }
        });
    }

    public ListenerRegistration setCallback(ShadeDeviceController.Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
        if (device.isLoaded()) {
            updateView();
        }
        else {
            device.load().onSuccess(loadListener);
        }

        return new ListenerRegistration() {
            @Override public boolean isRegistered() {
                return callbackRef.get() != null;
            }

            @Override public boolean remove() {
                boolean isRegistered = isRegistered();
                callbackRef.clear();
                return isRegistered;
            }
        };
    }

    public void requestUpdate() {
        updateView();
    }

    protected void updateView() {
        final Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        final DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel != null) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    callback.onSuccess(deviceModel);
                }
            });

        }
    }

    public void setLevel(final int level) {
        DebouncedRequest request = new DebouncedRequest(device.get());
        request.setCallbackHandler(new DebouncedRequest.DebounceCallback() {
            @Override
            public void commitEvent() {
                startMonitorFor(
                        device.get().getAddress(),
                        Shade.ATTR_LEVEL,
                        (double)level
                );

                ((Shade)device.get()).setLevel(level);
                device.get().commit();
            }
        });
        request.setOnError(onError);
        debouncedRequestScheduler.schedule(Shade.ATTR_LEVEL, request);
    }

    protected void startMonitorFor(String address, String attribute, Object shouldBe) {
        PropertyChangeMonitor.instance().removeAllFor(address);
        PropertyChangeMonitor.instance().startMonitorFor(
                address, attribute, TIMEOUT_MS, propertyChangeMonitorCB, shouldBe, onFailureFunction
        );
    }

    protected void refreshModel() {
        DeviceModel model = device.get();
        if (model != null) {
            model.refresh();
        }
    }

    @Override
    public void commitEvent() {
        //no-op.  handled in setLevel
    }

    protected @Nullable DeviceModel deviceModelOrEmitError() {
        DeviceModel deviceModel = device.get();
        if (deviceModel == null) {
            emitError(new RuntimeException("Unable to get model address - model loaded?"));
            return null;
        }

        return deviceModel;
    }

    protected void emitError(Throwable throwable) {
        final Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        callback.onError(throwable);
    }
}
