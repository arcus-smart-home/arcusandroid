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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.model.TimeZoneModel;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.service.PlaceService;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class TimeZoneLoader {
    public interface Callback {
        void loaded(List<TimeZoneModel> timeZones);
        void failed(Throwable throwable);
    }

    private static final TimeZoneLoader INSTANCE = new TimeZoneLoader();

    private final AtomicReference<PlaceService.ListTimezonesResponse> callRef = new AtomicReference<>(null);
    private Reference<Callback> callbackRef = new WeakReference<>(null);
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            callFailed(throwable);
        }
    });
    private final Listener<PlaceService.ListTimezonesResponse> responseListener = Listeners.runOnUiThread(
          new Listener<PlaceService.ListTimezonesResponse>() {
              @Override public void onEvent(PlaceService.ListTimezonesResponse response) {
                  List<Map<String, Object>> timeZones = response.getTimezones();
                  if (timeZones == null || timeZones.isEmpty()) {
                      callRef.set(null);
                      callFailed(new RuntimeException("Timezone response was empty."));
                  }
                  else {
                      callRef.set(response);
                      callLoaded(response);
                  }
              }
          }
    );

    protected TimeZoneLoader() {}

    public static TimeZoneLoader instance() {
        return INSTANCE;
    }

    public ListenerRegistration setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);

        return Listeners.wrap(callbackRef);
    }

    public void removeCallbacks() {
        callbackRef = new WeakReference<>(null);
    }

    public void loadTimezones() {
        PlaceService.ListTimezonesResponse response = callRef.get();
        if (response == null) {
            doLoadTimeZones();
        }
        else {
            callLoaded(response);
        }
    }

    protected void doLoadTimeZones() {
        PlaceService placeService = CorneaClientFactory.getService(PlaceService.class);
        placeService
              .listTimezones()
              .onSuccess(responseListener)
              .onFailure(errorListener);
    }

    protected void callLoaded(PlaceService.ListTimezonesResponse timeZoneResponse) {
        List<Map<String, Object>> timeZones = timeZoneResponse.getTimezones();
        List<TimeZoneModel> models = new ArrayList<>(timeZones.size() + 1);
        for (Map<String, Object> item : timeZones) {
            models.add(new TimeZoneModel(item));
        }

        Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        callback.loaded(models);
    }

    protected void callFailed(Throwable throwable) {
        Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        callback.failed(throwable);
    }
}
