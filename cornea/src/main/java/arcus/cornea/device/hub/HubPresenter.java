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
package arcus.cornea.device.hub;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Locale;

public class HubPresenter implements HubMVPContract.Presenter, HubController.Callback {
    private final int BATTERY_THRESHOLD = 30;
    protected Reference<HubMVPContract.View> viewRef;
    protected final HubController controller;
    protected ListenerRegistration listenerRegistration;

    @VisibleForTesting HubPresenter(@NonNull HubMVPContract.View view, @NonNull HubController controller) {
        this.viewRef = new WeakReference<>(view);
        this.controller = controller;
        this.listenerRegistration = controller.setCallback(this);
    }

    public HubPresenter(@NonNull HubMVPContract.View view) {
        this(view, HubController.newController());
    }

    @Override public void load() {
        controller.load();
    }

    // Could have this interact with the controller further if needed.
    @Override public void refresh() {
        controller.refresh();
    }

    @Override public void clear() {
        Listeners.clear(listenerRegistration);
    }

    @Override public void show(HubProxyModel hub) {
        HubMVPContract.View view = viewRef.get();
        if (view != null) {
            if (hub.getBatteryLevel() >= 0 && hub.getBatteryLevel() <= BATTERY_THRESHOLD) {
                hub.setBatteryLevelString(String.format(Locale.getDefault(), "%.0f%%", hub.getBatteryLevel()));
            }
            else {
                hub.setBatteryLevelString("OK");
            }
            view.show(hub);
        }
    }

    @Override public void onError(Throwable throwable) {
        HubMVPContract.View view = viewRef.get();
        if (view != null) {
            view.onError(throwable);
        }
    }
}
