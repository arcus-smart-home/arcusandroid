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
package arcus.cornea.subsystem.weather;

import arcus.cornea.common.PresentedView;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.EASCodeProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.weather.model.WeatherSubsystemModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.EasCode;
import com.iris.client.capability.WeatherSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WeatherSubsystemController extends BaseSubsystemController<WeatherSubsystemController.Callback> {
    public interface Callback extends PresentedView<WeatherSubsystemModel> {
        void snoozeSuccessful();
    }

    private static final String UNKNOWN_DEVICE = "UNKNOWN DEVICE";
    private static final String UNKNOWN_EAS_CODE = "UNKNOWN ALERT";

    private static final WeatherSubsystemController INSTANCE;
    static {
        INSTANCE = new WeatherSubsystemController(
              SubsystemController.instance().getSubsystemModel(WeatherSubsystem.NAMESPACE)
        );
        INSTANCE.init();
        INSTANCE.loadEASCodes();
        INSTANCE.loadDeviceModels();
    }
    private final Listener genericListener = new Listener() {
        @Override
        public void onEvent(Object listResult) {
            updateView();
        }
    };

    public static WeatherSubsystemController instance() {
        return INSTANCE;
    }

    WeatherSubsystemController(String namespace) {
        super(namespace);
    }

    WeatherSubsystemController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public void snoozeAll() {
        final Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        WeatherSubsystem weatherSubsystem = (WeatherSubsystem) getModel();
        if (weatherSubsystem == null) {
            callback.onError(new RuntimeException("Subsystem not loaded. Cannot snooze."));
        } else {
            weatherSubsystem.snoozeAllAlerts()
                  .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                      @Override
                      public void onEvent(Throwable throwable) {
                          callback.onError(throwable);
                      }
                  }))
                  .onSuccess(Listeners.runOnUiThread(new Listener<WeatherSubsystem.SnoozeAllAlertsResponse>() {
                      @Override
                      public void onEvent(WeatherSubsystem.SnoozeAllAlertsResponse snoozeAllAlertsResponse) {
                          callback.snoozeSuccessful();
                      }
                  }));
        }
    }

    protected void onSubsystemChanged(ModelChangedEvent event) {
        updateView();
    }

    @Override
    protected void updateView(final Callback callback) {
        if (!DeviceModelProvider.instance().isLoaded()) {
            loadDeviceModels();
            return;
        }

        if (!EASCodeProvider.instance().isLoaded()) {
            loadEASCodes();
            return;
        }

        if (!isLoaded()) {
            return;
        }

        WeatherSubsystem weatherSubsystem = (WeatherSubsystem) getModel();

        Map<String, Set<String>> alertingDevices = new HashMap<>(weatherSubsystem.getAlertingRadios().size() + 1);
        for (Map.Entry<String, Set<String>> radioOuter : weatherSubsystem.getAlertingRadios().entrySet()) {
            EasCode easCode = EASCodeProvider.instance().getByCode(radioOuter.getKey());
            String easName = easCode == null ? UNKNOWN_EAS_CODE : easCode.getName();

            Set<String> humanReadableDevices = new HashSet<>(radioOuter.getValue().size() + 1);
            for (String singleRadio : radioOuter.getValue()) {
                ModelSource<DeviceModel> singleModelSource = DeviceModelProvider.instance().getModel(singleRadio);
                singleModelSource.load(); // Should be in there since we loaded the devices above...

                DeviceModel model = singleModelSource.get();
                humanReadableDevices.add(model != null ? string(model.getName()) : UNKNOWN_DEVICE);
            }

            alertingDevices.put(easName, humanReadableDevices);
        }

        final WeatherSubsystemModel viewModel = new WeatherSubsystemModel(
              WeatherSubsystem.WEATHERALERT_ALERT.equals(weatherSubsystem.getWeatherAlert()),
              weatherSubsystem.getLastWeatherAlertTime(),
              alertingDevices
        );

        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                callback.updateView(viewModel);
            }
        });
    }

    @SuppressWarnings("unchecked")
    protected void loadDeviceModels() {
        DeviceModelProvider.instance().load().onSuccess(genericListener);
    }

    @SuppressWarnings("unchecked")
    protected void loadEASCodes() {
        EASCodeProvider.instance().load().onSuccess(genericListener);
    }
}
