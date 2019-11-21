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
package arcus.cornea.subsystem.favorites;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.SceneModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.WrappedRunnable;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Device;
import com.iris.client.capability.Scene;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SceneModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;


public class FavoritesController {

    public static final String FAVORITE_TAG = "FAVORITE";

    public static final String DEVICE_MODEL = "DEVICE_MODEL";
    public static final String SCENE_MODEL = "SCENE_MODEL";
    public static final String UNKNOWN_MODEL = "UNKNOWN_MODEL";
    public static final String INVALID_ADDRESS = "INVALID_ADDRESS";

    public static final int DEBOUNCE_DELAY_MS = 100;

    private static final FavoritesController instance = new FavoritesController();
    private static final Logger logger = LoggerFactory.getLogger(FavoritesController.class);

    private ListenerRegistration devicesListener;
    private ListenerRegistration scenesListener;

    private WeakReference<Callback> callbackRef = new WeakReference<>(null);

    private final Listener<ModelEvent> modelEventListener = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent event) {
            if (event instanceof ModelChangedEvent) {
                if (((ModelChangedEvent)event).getChangedAttributes().containsKey(Device.ATTR_TAGS)) {
                    debounceGetFavorites();
                }
            } else {
                debounceGetFavorites();
            }
        }
    });
    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onRequestError(throwable);
        }
    });

    private final Handler handler = new Handler(Looper.myLooper());
    private final Runnable getFavoritesRunnable = new Runnable() {
        @Override public void run() {
            if (DeviceModelProvider.instance().isLoaded()) {
                devicesLoaded(Lists.newLinkedList(DeviceModelProvider.instance().getStore().values()));
            }
            else {
                DeviceModelProvider.instance().load()
                      .onFailure(onErrorListener)
                      .onSuccess(new Listener<List<DeviceModel>>() {
                          @Override
                          public void onEvent(List<DeviceModel> deviceModels) {
                              devicesLoaded(deviceModels);
                          }
                      });
            }
        }
    };

    private FavoritesController() {

    }

    public static FavoritesController getInstance() {
        return instance;
    }

    public ListenerRegistration setCallback(final Callback callback) {
        if (callbackRef.get() != null) {
            logger.debug("Updating FavoritesController callbacks with [{}].", callback);
        }

        this.callbackRef = new WeakReference<>(callback);

        debounceGetFavorites();

        return new ListenerRegistration() {
            @Override public boolean isRegistered() {
                return callbackRef.get() != null;
            }

            @Override public boolean remove() {
                boolean removed = callbackRef.get() != null;
                callbackRef.clear();
                Listeners.clear(devicesListener);
                Listeners.clear(scenesListener);
                return removed;
            }
        };
    }

    void debounceGetFavorites() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(getFavoritesRunnable, DEBOUNCE_DELAY_MS);
    }

    void devicesLoaded(List<DeviceModel> deviceModels) {
        final List<Model> favorites = new LinkedList<>();

        for (DeviceModel model : deviceModels) {
            if (model.getTags() != null && model.getTags().contains(FAVORITE_TAG)) {
                favorites.add(model);
            }
        }

        Listeners.clear(devicesListener);
        devicesListener = DeviceModelProvider.instance().getStore().addListener(modelEventListener);

        // If there are no devices, nothing can be favorited. Don't bother loading un-runnable scenes.
        if(deviceModels.isEmpty()) {
            fireFavoritesCallback(null);
        } else {
            loadScenes(favorites);
        }
    }

    void loadScenes(@NonNull final List<Model> favorites) {
        if (SceneModelProvider.instance().isLoaded()) {
            scenesLoaded(Lists.newLinkedList(SceneModelProvider.instance().getStore().values()), favorites);
        }
        else {
            SceneModelProvider.instance().reload()
                  .onFailure(onErrorListener)
                  .onSuccess(new Listener<List<SceneModel>>() {
                      @Override public void onEvent(List<SceneModel> sceneModels) {
                          scenesLoaded(sceneModels, favorites);
                      }
                  });
        }
    }

    void scenesLoaded(@NonNull List<SceneModel> sceneModels, List<Model> favorites) {
        for (Model model : sceneModels) {
            if (model.getTags() != null && model.getTags().contains(FAVORITE_TAG))
                favorites.add(model);
        }

        Listeners.clear(scenesListener);
        scenesListener = SceneModelProvider.instance().getStore().addListener(modelEventListener);

        fireFavoritesCallback(favorites);
    }

    void fireFavoritesCallback(final List<Model> favorites) {
        final Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        LooperExecutor.getMainExecutor().execute(new WrappedRunnable() {
            @Override
            public void onRun() throws Exception {
             if(favorites == null) {
                 callback.showNoItemsToFavorite();
             } else if (favorites.isEmpty()) {
                 callback.showAddFavorites();
             } else {
                 callback.showFavorites(favorites);
             }
            }
        });
    }

    void onRequestError(final Throwable throwable) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Callback callback = callbackRef.get();
                if (callback != null) {
                    callback.onError(throwable);
                }
            }
        });
    }

    public interface Callback {
        void showAddFavorites();

        void showNoItemsToFavorite();

        void showFavorites(List<Model> favoriteModels);

        void onError(Throwable throwable);
    }

    public static String determineModelType(String address) {
        Model model = CorneaClientFactory.getModelCache().get(address);

        if (model == null) {
            return INVALID_ADDRESS;
        }

        if (model.getCaps().contains(Device.NAMESPACE)) {
            return DEVICE_MODEL;
        } else if (model.getCaps().contains(Scene.NAMESPACE)) {
            return SCENE_MODEL;
        }

        return UNKNOWN_MODEL;
    }

    public static Model getModelWithAddress(String address) {
        SceneModel scene = CorneaClientFactory.getStore(SceneModel.class).get(Addresses.getId(address));
        if (scene != null)
            return scene;

        DeviceModel device = CorneaClientFactory.getStore(DeviceModel.class).get(Addresses.getId(address));
        if (device != null)
            return device;

        return null;
    }
}
