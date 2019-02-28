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
package arcus.app.subsystems.scenes.catalog.controller;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import com.iris.client.model.Model;
import com.iris.client.model.SceneTemplateModel;
import com.iris.client.service.SceneService;
import arcus.app.common.controller.FragmentController;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SceneCatalogFragmentController extends FragmentController<SceneCatalogFragmentController.Callbacks> {
    private static final Logger logger = LoggerFactory.getLogger(SceneCatalogFragmentController.class);
    private static final SceneCatalogFragmentController instance = new SceneCatalogFragmentController();

    public interface Callbacks {
        void onCorneaError(Throwable throwable);
        void onCategoriesLoaded (List<SceneCategory> categories);
    }

    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            fireOnCorneaError(throwable);
        }
    });

    private SceneCatalogFragmentController () {}

    public static SceneCatalogFragmentController instance () { return instance; }

    public void loadCategories() {
                CorneaClientFactory.getService(SceneService.class)
                .listSceneTemplates(getPlaceID())
                .onFailure(onErrorListener)
                .transform(input -> {
                    if (input != null && input.getSceneTemplates() != null) {
                        logger.debug("Got scene templates; total of {}.", input.getSceneTemplates().size());
                       return getVisibleCategoriesFromTemplates(
                               CorneaClientFactory
                                       .getModelCache()
                                       .addOrUpdate(input.getSceneTemplates())
                       );
                    } else {
                       return Collections.<SceneCategory>emptyList();
                    }
                })
                .onSuccess(Listeners.runOnUiThread(categories -> {
                    logger.debug("Got scene categories; total of {}.", categories.size());
                    fireOnCategoriesLoaded(categories);
                }));
    }

    private List<SceneCategory> getVisibleCategoriesFromTemplates (List<Model> sceneTemplateModels) {

        List<SceneCategory> categories = new ArrayList<>();

        for (Model thisModel : sceneTemplateModels) {
            SceneTemplateModel templateModel = (SceneTemplateModel) thisModel;

            if (templateModel.getAvailable()) {
                categories.add(SceneCategory.fromSceneTemplate((SceneTemplateModel) thisModel));
            }
        }

        return categories;
    }

    private void fireOnCategoriesLoaded(final List<SceneCategory> categories) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCategoriesLoaded(categories);
        }
    }

    private void fireOnCorneaError(final Throwable throwable) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(throwable);
        }
    }

    private String getPlaceID() {
        if (CorneaClientFactory.isConnected()) {
            return CorneaClientFactory.getClient().getActivePlace().toString();
        }

        return "";
    }
}
