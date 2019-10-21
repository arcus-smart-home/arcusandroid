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
package arcus.app.subsystems.scenes.active.controller;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.capability.Scene;
import com.iris.client.event.Listener;
import com.iris.client.model.Model;
import com.iris.client.model.SceneModel;
import com.iris.client.service.SceneService;
import arcus.app.subsystems.scenes.active.model.SceneListModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SceneListController {
    private static final SceneListController INSTANCE;
    static {
        INSTANCE = new SceneListController();
        INSTANCE.clearCallbacks();
    }

    private WeakReference<Callback> callbackRef;
    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onRequestError(throwable);
        }
    });
    private static final Comparator<SceneListModel> sceneModelComparator = new Comparator<SceneListModel>() {
        @Override
        public int compare(SceneListModel lhs, SceneListModel rhs) {
            if(lhs.getNameOfScene() == null) {
                return rhs.getNameOfScene() == null ? 0 : -1;
            }
            if(rhs.getNameOfScene() == null) {
                return 1;
            }

            return lhs.getNameOfScene().compareToIgnoreCase(rhs.getNameOfScene());
        }
    };

    public interface Callback {
        void scenesLoaded(List<SceneListModel> scenes);
        void modelDeleted(String addressDeleted);
        void onError(Throwable throwable);
    }

    protected SceneListController() {}

    public static SceneListController instance() {
        return INSTANCE;
    }

    public void setCallback(Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
    }

    public void clearCallbacks() {
        this.callbackRef = new WeakReference<>(null);
    }

    /**
     *
     * Calls platform to get a list of scenes created by the user.
     *
     */
    public void listScenes() {
        if (!CorneaClientFactory.isConnected()) {
            notConnectedError();
            return;
        }

        String placeID = CorneaClientFactory.getClient().getActivePlace().toString();
        SceneService sceneService = CorneaClientFactory.getService(SceneService.class);
        sceneService.listScenes(placeID)
              .onFailure(onErrorListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<SceneService.ListScenesResponse>() {
                  @Override
                  public void onEvent(SceneService.ListScenesResponse listScenesResponse) {
                      List<Model> models = CorneaClientFactory.getModelCache().addOrUpdate(listScenesResponse.getScenes());
                      List<SceneListModel> sceneListModels = new ArrayList<>();
                      for (Model model : models) {
                          sceneListModels.add(new SceneListModel((SceneModel) model));
                      }

                      Collections.sort(sceneListModels, sceneModelComparator);
                      Callback callback = callbackRef.get();
                      if (callback != null) {
                          callback.scenesLoaded(sceneListModels);
                      }
                  }
              }));
    }

    /**
     *
     * Calls platform to delete the selected scene (by address)
     *
     * @param address Address of scene.
     */
    public void deleteScene(final String address) {
        if (!CorneaClientFactory.isConnected()) {
            notConnectedError();
            return;
        }

        Model model = CorneaClientFactory.getModelCache().get(address);
        if (model == null || !model.getCaps().contains(Scene.NAMESPACE)) {
            Callback callback = callbackRef.get();
            if (callback != null) {
                callback.onError(new RuntimeException("Model not found in cache."));
            }
            return;
        }

        SceneModel sceneModel = (SceneModel) model;
        sceneModel.delete()
              .onFailure(onErrorListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<Scene.DeleteResponse>() {
                  @Override
                  public void onEvent(Scene.DeleteResponse deleteResponse) {
                      Callback callback = callbackRef.get();
                      if (callback != null) {
                          callback.modelDeleted(address);
                      }
                  }
              }));
    }

    private void onRequestError(final Throwable throwable) {
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

    private void notConnectedError() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.onError(new RuntimeException("Client not connected."));
        }
    }
}
