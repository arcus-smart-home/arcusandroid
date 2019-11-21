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

import com.google.common.collect.ImmutableSet;
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.connection.CellularBackup;
import arcus.cornea.subsystem.connection.model.CellBackupModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.CellBackupSubsystem;
import com.iris.client.capability.Hub;
import com.iris.client.capability.Hub4g;
import com.iris.client.capability.HubConnection;
import com.iris.client.capability.HubNetwork;
import com.iris.client.capability.HubPower;
import com.iris.client.capability.HubWiFi;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.HubModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HubController {
    private static final Logger logger = LoggerFactory.getLogger(HubController.class);

    public interface Callback {
        void show(HubProxyModel hubProxyModel);
        void onError(Throwable throwable);
    }

    private Reference<Callback> callbackRef = new WeakReference<>(null);
    private transient HubModel hubModel;
    private ListenerRegistration modelListener, subsystemListener;
    private final ImmutableSet<String> UPDATE_ON = ImmutableSet.of(
          Hub.ATTR_STATE,
          HubConnection.ATTR_STATE,
          HubPower.ATTR_SOURCE,
          HubNetwork.ATTR_TYPE,
          HubWiFi.ATTR_WIFIRSSI,
          HubWiFi.ATTR_WIFISSID
    );
    private final Listener<Throwable> errorListener = new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    };
    private final Listener genericSuccessListener = new Listener() {
        @Override public void onEvent(Object o) {
            update();
        }
    };

    public static HubController newController() {
        return new HubController(
              HubModelProvider.instance().getHubModel(),
              SubsystemController.instance().getSubsystemModel(CellBackupSubsystem.NAMESPACE)
        );
    }

    @VisibleForTesting HubController(HubModel hubModel, @NonNull ModelSource<SubsystemModel> cellSubsystemModel) {
        this.hubModel = hubModel;
        if (this.hubModel != null) {
            attachModelListener();
        }

        cellSubsystemModel.load();
        subsystemListener = cellSubsystemModel.addModelListener(new Listener<ModelChangedEvent>() {
            @Override public void onEvent(ModelChangedEvent mce) {
                update();
            }
        }, ModelChangedEvent.class);
    }

    public final ListenerRegistration setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
        return new ListenerRegistration() {
            @Override public boolean isRegistered() {
                return callbackRef.get() != null;
            }

            @Override public boolean remove() {
                boolean isRegistered = callbackRef.get() != null;

                Listeners.clear(modelListener);
                Listeners.clear(subsystemListener);
                callbackRef.clear();

                return isRegistered;
            }
        };
    }

    public void load() {
        if (hubModel != null) {
            update(); // Show initial values - update later with 'real' values if call succeeds
        }
        else {
            refresh();
        }
    }

    public void refresh() {
        if (hubModel != null) {
            refreshHub();
        }
        else {
            HubModelProvider
                  .instance()
                  .load()
                  .onFailure(errorListener)
                  .onSuccess(new Listener<List<HubModel>>() {
                      @Override public void onEvent(List<HubModel> hubModels) {
                          if (hubModels != null && !hubModels.isEmpty()) {
                              hubModel = hubModels.get(0);
                              refreshHub();
                              attachModelListener();
                          }
                          else {
                              logger.debug("No hubs returned.");
                          }
                      }
                  });
        }
    }

    void attachModelListener() {
        Listeners.clear(modelListener);
        modelListener = hubModel.addPropertyChangeListener(Listeners.filter(UPDATE_ON, new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent event) {
                update(); // Debounce this?
            }
        }));
    }

    @VisibleForTesting boolean modelListenerRegistered() {
        return Listeners.isRegistered(modelListener);
    }

    @VisibleForTesting boolean subsystemListenerRegistered() {
        return Listeners.isRegistered(subsystemListener);
    }

    @SuppressWarnings("unchecked") protected void refreshHub() {
        hubModel.refresh().onSuccess(genericSuccessListener).onFailure(errorListener);
    }

    protected void update() {
        HubProxyModel model;
        if (hubModel == null) {
            model = new HubProxyModel("");
        }
        else {
            model = new HubProxyModel(hubModel.getId());
            model.setImei((String) hubModel.get(Hub4g.ATTR_IMEI));
            model.setSimID((String) hubModel.get(Hub4g.ATTR_ICCID));
            model.setEsn((String) hubModel.get(Hub4g.ATTR_SERIALNUMBER));
            model.setOnline(!Hub.STATE_DOWN.equals(hubModel.get(Hub.ATTR_STATE)));
            model.setConnectionType((String) hubModel.get(HubNetwork.ATTR_TYPE));
            model.setLastChanged(getLastChangeOrNow());
            model.setOnlineDays(getOnlineDays());
            model.setOnlineHours(getOnlineHours());
            model.setOnlineMinutes(getOnlineMinutes());
            model.setBatteryType((String) hubModel.get(HubPower.ATTR_SOURCE));
            model.setBatteryLevel(getBatteryLevel());
            model.setCellBackupModel(getCellBackupModel());
            model.setWifiNetwork((String) hubModel.get(HubWiFi.ATTR_WIFISSID));

            Object hubWiFiSignal = hubModel.get(HubWiFi.ATTR_WIFIRSSI);
            if (hubWiFiSignal instanceof Number) {
                model.setWifiSignal(((Number) hubWiFiSignal).intValue());
            } else {
                model.setWifiSignal(0);
            }

            model.setWifiConnectedState((String) hubModel.get(HubWiFi.ATTR_WIFISTATE));
            model.setHubModelNumber(String.valueOf(hubModel.get(Hub.ATTR_MODEL)));

            String wifiId = (String) hubModel.get(HubWiFi.ATTR_WIFISSID);
            model.setHasWiFiCredentials(wifiId != null && wifiId.length() > 0);
        }

        show(model);
    }

    protected void show(final HubProxyModel hubProxyModel) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                Callback callback = callbackRef.get();
                if (callback != null) {
                    try {
                        callback.show(hubProxyModel);
                    }
                    catch (Exception ex) {
                        logger.error("Error dispatching success callback for hub.", ex);
                    }
                }
            }
        });
    }

    protected void onError(final Throwable throwable) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                Callback callback = callbackRef.get();
                if (callback != null) {
                    try {
                        callback.onError(throwable);
                    }
                    catch (Exception ex) {
                        logger.error("Error dispatching failure callback for hub. Cause: [{}]", ex.getCause(), throwable);
                    }
                }
            }
        });
    }

    protected long getOnlineDays() {
        return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - getLastChangeOrNow());
    }

    protected long getOnlineHours() {
        return TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - getLastChangeOrNow()) % 24;
    }

    protected long getOnlineMinutes() {
        return TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - getLastChangeOrNow()) % 60;
    }

    private long getLastChangeOrNow() {
        if (hubModel == null) {
            return System.currentTimeMillis();
        }

        Number date = (Number) hubModel.get(HubConnection.ATTR_LASTCHANGE);
        if (date == null) {
            return System.currentTimeMillis();
        }

        return date.longValue();
    }

    protected double getBatteryLevel() {
        if (hubModel == null) {
            return -1;
        }

        Number batteryLevel = (Number) hubModel.get(HubPower.ATTR_BATTERY);
        return batteryLevel == null ? -1D : batteryLevel.doubleValue();
    }

    protected CellBackupModel getCellBackupModel() {
        return CellularBackup.instance().getStatus();
    }
}
