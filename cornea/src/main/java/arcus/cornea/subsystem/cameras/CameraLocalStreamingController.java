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
package arcus.cornea.subsystem.cameras;

import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.LooperExecutor;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.IpInfo;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;

public final class CameraLocalStreamingController {
    public interface Callback {
        void onSuccess(String username, String password, String ip);
        void onError(Throwable error);
    }

    private static final Logger logger = LoggerFactory.getLogger(CameraLocalStreamingController.class);
    private String username, ip, deviceAddress;
    private int expectedLength = 8;
    private final Reference<Callback> callbackRef;
    private final Listener<Throwable> errorListener = new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    };

    public CameraLocalStreamingController(Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
    }

    public void loadCameraCredentials(String deviceID) {
        if (deviceID == null) {
            onError(new RuntimeException("Cannot locate an empty/null model ID"));
        }
        else {
            deviceAddress = Addresses.toObjectAddress(Device.NAMESPACE, Addresses.getId(deviceID));
            doLoadCameraCredentials();
        }
    }

    protected void doLoadCameraCredentials() {
        CachedModelSource.<DeviceModel>get(deviceAddress)
              .load()
              .onFailure(errorListener)
              .onSuccess(new Listener<DeviceModel>() {
                  @Override public void onEvent(DeviceModel deviceModel) {
                      ip = (String) deviceModel.get(IpInfo.ATTR_IP);
                      loadHub();
                  }
              });
    }

    protected void loadHub() {
        HubModelProvider.instance()
              .load()
              .onFailure(errorListener)
              .onSuccess(new Listener<List<HubModel>>() {
                  @Override public void onEvent(List<HubModel> hubModels) {
                      if (hubModels == null || hubModels.isEmpty()) {
                          onError(new RuntimeException("Could not load correct models; cannot update credentials."));
                          return;
                      }

                      // the below block is meant to run so long as a hub is not a sercomm hub.
                      // since we no longer control sercomm hubs return this error in all cases
                      onError(new RuntimeException("Model did not contain proper capabilities; cannot update credentials."));
                      return;
                  }
              });
    }


    protected void dispatchSuccess(final String password) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                Callback callback = callbackRef.get();
                if (callback != null) {
                    callback.onSuccess(username, password, ip);
                }
            }
        });
    }

    protected void onError(final Throwable error) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                try {
                    Callback callback = callbackRef.get();
                    if (callback != null) {
                        callback.onError(error);
                        logger.error("Dispatched ->", error);
                    }
                }
                catch (Exception ex) {
                    logger.error("Could not dispatch error", ex);
                }
            }
        });
    }
}
