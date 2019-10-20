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
package arcus.app.subsystems.scenes.editor.controller;

import com.google.common.collect.ImmutableSet;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Scene;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SceneModel;
import com.iris.client.model.SceneTemplateModel;
import com.iris.client.service.SceneService;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.GlobalSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SceneEditorFragmentController extends FragmentController<SceneEditorFragmentController.Callbacks> {

    public interface Callbacks {
        /**
         * Called to indicate that a scene or template has been loaded and is ready for editing.
         *
         * @param name Default or current name of the scene
         * @param notificationsEnabled Default or current state of notifications
         * @param isFavorite Default or current state of favorite tag
         */
        void onSceneLoaded (String modelTemplate, String sceneAddress, String name, boolean notificationsEnabled, boolean isFavorite, boolean hasActions);

        /**
         * Called to indicate that the active scene was successfully deleted.
         */
        void onSceneDeleted ();

        /**
         * Called to indicate a platform call failed.
         * @param cause
         */
        void onCorneaError (Throwable cause);
    }

    private static SceneEditorFragmentController instance = new SceneEditorFragmentController();

    private ListenerRegistration sceneAddedListener;
    private SceneModel scene;

    private SceneEditorFragmentController () {}
    public static SceneEditorFragmentController getInstance () { return instance; }

    /**
     * Begin editing ("adding") a new scene from a template.
     * @param sceneTemplateAddress
     */
    public void createScene (String sceneTemplateAddress) {
        createSceneFromTemplateAddress(sceneTemplateAddress);
    }

    /**
     * Begin editing an existing scene from the address of that scene.
     * @param sceneModelAddress
     */
    public void editScene (String sceneModelAddress) {
        loadSceneFromAddress(sceneModelAddress);
    }


    /**
     * Deletes the scene currently being operated on.
     */
    public void deleteScene () {
        if (scene != null) {
            scene.delete().onSuccess(Listeners.runOnUiThread(new Listener<Scene.DeleteResponse>() {
                @Override
                public void onEvent(Scene.DeleteResponse deleteResponse) {
                    fireOnSceneDeleted();
                }
            })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    fireOnCorneaError(throwable);
                }
            }));
        }
    }

    /**
     * Enable or disable notifications for this scene.
     *
     * Must be called after {@link #editScene(String)} or {@link #createScene(String)} have fired
     * the {@link Callbacks#onSceneLoaded(String, String, String, boolean, boolean)} method indicating that
     * the scene/template was correctly loaded.
     *
     * @param notificationEnabled
     */
    public void setNotificationEnabled (boolean notificationEnabled) {
        if (scene == null) {
            fireOnCorneaError(new IllegalStateException("Can't set notifications before the scene has been loaded."));
        }

        else {
            scene.setNotification(notificationEnabled);
            scene.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    fireOnCorneaError(throwable);
                }
            }));
        }
    }

    /**
     * Tag or untag this scene as a favorite.
     *
     * Must be called after {@link #editScene(String)} or {@link #createScene(String)} have fired
     * the {@link Callbacks#onSceneLoaded(String, String, String, boolean, boolean)} method indicating that
     * the scene/template was correctly loaded.
     *
     * @param isFavorite
     */
    public void setFavorite (boolean isFavorite) {
        if (scene == null) {
            fireOnCorneaError(new IllegalStateException("Can't set favorite tag before the scene has been loaded."));
        }

        else {
            if (isFavorite) {
                scene.addTags(ImmutableSet.of(GlobalSetting.FAVORITE_TAG)).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        fireOnCorneaError(throwable);
                    }
                }));
            } else {
                scene.removeTags(ImmutableSet.of(GlobalSetting.FAVORITE_TAG)).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        fireOnCorneaError(throwable);
                    }
                }));
            }
        }
    }

    /**
     * Sets the name of this scene.
     *
     * Must be called after {@link #editScene(String)} or {@link #createScene(String)} have fired
     * the {@link Callbacks#onSceneLoaded(String, String, String, boolean, boolean)} method indicating that
     * the scene/template was correctly loaded.
     *
     * @param name
     */
    public void setName (String name) {
        if (scene == null) {
            fireOnCorneaError(new IllegalStateException("Can't set name before the scene has been loaded."));
        }

        else {
            scene.setName(name);
            scene.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    fireOnCorneaError(throwable);
                }
            }));
        }
    }

    private void loadSceneFromAddress (final String sceneAddress) {

        final String placeId = CorneaUtils.getIdFromAddress(PlaceModelProvider.getCurrentPlace().getAddress());
        CorneaClientFactory.getService(SceneService.class).listScenes(placeId).onSuccess(Listeners.runOnUiThread(new Listener<SceneService.ListScenesResponse>() {
            @Override
            public void onEvent(SceneService.ListScenesResponse listScenesResponse) {
                List<Model> scenes = CorneaClientFactory.getModelCache().addOrUpdate(listScenesResponse.getScenes());
                for (Model thisScene : scenes) {
                    if (sceneAddress.equalsIgnoreCase(thisScene.getAddress())) {
                        scene = (SceneModel) thisScene;
                        fireOnSceneLoaded((SceneModel) thisScene);
                        return;
                    }
                }

                // None found; this shouldn't be possible...
                fireOnCorneaError(new IllegalArgumentException("Bug! No scene model found with address " + sceneAddress));
            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void createSceneFromTemplateAddress(final String templateAddress) {

        final String placeId = CorneaUtils.getIdFromAddress(PlaceModelProvider.getCurrentPlace().getAddress());
        CorneaClientFactory.getService(SceneService.class).listSceneTemplates(placeId).onSuccess(new Listener<SceneService.ListSceneTemplatesResponse>() {
            @Override
            public void onEvent(SceneService.ListSceneTemplatesResponse listSceneTemplatesResponse) {
                List<Model> sceneTemplates = CorneaClientFactory.getModelCache().addOrUpdate(listSceneTemplatesResponse.getSceneTemplates());

                // Look for a template matching the category's address
                for (Model thisScene : sceneTemplates) {
                    if (thisScene.getAddress().equals(templateAddress)) {
                        createSceneFromTemplateModel((SceneTemplateModel) thisScene);
                        return;
                    }
                }

                // None found; this shouldn't be possible...
                fireOnCorneaError(new IllegalArgumentException("Bug! No template model found with address " + templateAddress));
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void createSceneFromTemplateModel(final SceneTemplateModel templateModel) {
        final String defaultName = templateModel.getName();
        final String currentPlace = CorneaUtils.getIdFromAddress(PlaceModelProvider.getCurrentPlace().getAddress());

        // Start listening for new scene models to be added ...
        Listeners.clear(sceneAddedListener);
        sceneAddedListener = CorneaClientFactory.getStore(SceneModel.class).addListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelAddedEvent) {
                    scene = (SceneModel) modelEvent.getModel();
                    fireOnSceneLoaded(scene);
                }
            }
        }));

        // ... then create our scene
        templateModel.create(currentPlace, defaultName, new ArrayList<Map<String, Object>>()).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void fireOnSceneLoaded (SceneModel model) {
        Callbacks listener = getListener();
        if (listener != null) {
            Boolean isFavorite = model.getTags().contains(GlobalSetting.FAVORITE_TAG);
            listener.onSceneLoaded(model.getTemplate(), model.getAddress(), model.getName(), model.getNotification(), isFavorite, model.getActions().size() > 0);
        }
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

    private void fireOnSceneDeleted () {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onSceneDeleted();
        }
    }

}
