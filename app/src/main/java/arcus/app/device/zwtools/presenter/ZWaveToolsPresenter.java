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
package arcus.app.device.zwtools.presenter;

import com.google.common.collect.ImmutableSet;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.capability.HubZwave;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.HubModel;
import com.iris.client.model.HubZwaveModel;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.CorneaUtils;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Set;


public class ZWaveToolsPresenter implements ZWaveToolsContract.Presenter {

    private WeakReference<ZWaveToolsContract.ZWaveToolsView> viewRef = new WeakReference<>(null);
    private ListenerRegistration hubListener;

    private static final Set<String> UPDATE_ON = ImmutableSet.of(
            HubZwaveModel.ATTR_HEALPERCENT,
            HubZwaveModel.ATTR_HEALCOMPLETED
    );

    public ZWaveToolsPresenter() {

        HubModel hub = SessionModelManager.instance().getHubModel();

        if (hub != null) {
            hubListener = hub.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (UPDATE_ON.contains(event.getPropertyName())) {
                        requestUpdate();
                    }
                }
            });
        }
    }

    @Override
    public void cancelRebuilding() {
        final HubZwave hubZwModel = CorneaUtils.getCapability(SessionModelManager.instance().getHubModel(), HubZwave.class);

        if (hubZwModel != null) {

            hubZwModel.cancelHeal().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(final Throwable throwable) {
                    LooperExecutor.getMainExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (viewRef.get() != null) {
                                viewRef.get().onError(throwable);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void startRebuilding() {
        final HubZwave hubZwModel = CorneaUtils.getCapability(SessionModelManager.instance().getHubModel(), HubZwave.class);

        if (hubZwModel != null) {

            hubZwModel.heal(false, null).onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(final Throwable throwable) {
                    LooperExecutor.getMainExecutor().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (viewRef.get() != null) {
                                viewRef.get().onError(throwable);
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public void requestUpdate() {
        final HubZwave hubZwModel = CorneaUtils.getCapability(SessionModelManager.instance().getHubModel(), HubZwave.class);

        if (viewRef.get() != null && hubZwModel != null) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    viewRef.get().updateView(hubZwModel);
                }
            });
        }
    }

    @Override
    public void startPresenting(ZWaveToolsContract.ZWaveToolsView view) {
        viewRef = new WeakReference<>(view);
    }

    @Override
    public void stopPresenting() {
        viewRef.clear();
        Listeners.clear(hubListener);
    }
}
