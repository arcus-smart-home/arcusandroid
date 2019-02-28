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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.ActionTemplate;
import com.iris.client.capability.SceneTemplate;
import com.iris.client.event.Listener;
import com.iris.client.model.Model;
import com.iris.client.model.SceneTemplateModel;
import com.iris.client.service.SceneService;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SceneActionsFragmentController extends FragmentController<SceneActionsFragmentController.Callbacks> {
    private static final SceneActionsFragmentController INSTANCE;
    static {
        INSTANCE = new SceneActionsFragmentController();
    }

    public interface Callbacks {
        void onActionsResolved (List<ActionTemplate> satisfiable, List<ActionTemplate> unsatisfiable);
        void onCorneaError (Throwable cause);
    }

    protected SceneActionsFragmentController() {}

    public static SceneActionsFragmentController getInstance() {
        return INSTANCE;
    }

    public void resolveActions(final String templateAddress) {

        String placeId = CorneaUtils.getIdFromAddress(PlaceModelProvider.getCurrentPlace().getAddress());
        CorneaClientFactory.getService(SceneService.class).listSceneTemplates(placeId).onSuccess(new Listener<SceneService.ListSceneTemplatesResponse>() {
            @Override
            public void onEvent(SceneService.ListSceneTemplatesResponse listSceneTemplatesResponse) {
                List<Model> sceneTemplates = CorneaClientFactory.getModelCache().addOrUpdate(listSceneTemplatesResponse.getSceneTemplates());
                for (Model sceneTemplate : sceneTemplates) {
                    if (templateAddress.equalsIgnoreCase(sceneTemplate.getAddress())) {
                        resolveActionsForTemplate((SceneTemplateModel) sceneTemplate);
                        return;
                    }
                }

                fireOnCorneaError(new IllegalArgumentException("Bug! No such scene template with address" + templateAddress));
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }



    private void resolveActionsForTemplate (final SceneTemplateModel sceneTemplateModel) {

        String placeId = CorneaUtils.getIdFromAddress(PlaceModelProvider.getCurrentPlace().getAddress());
        sceneTemplateModel.resolveActions(placeId).onSuccess(Listeners.runOnUiThread(new Listener<SceneTemplate.ResolveActionsResponse>() {
            @Override
            public void onEvent(SceneTemplate.ResolveActionsResponse resolveActionsResponse) {
                List<ActionTemplate> satisfiable = new ArrayList<>();
                List<ActionTemplate> unsatisfiable = new ArrayList<>();

                for (Map<String,Object> thisActionTemplateData : resolveActionsResponse.getActions()) {
                    ActionTemplate thisActionTemplate = new ActionTemplate(thisActionTemplateData);
                    if (thisActionTemplate.getSatisfiable()) {
                        satisfiable.add(thisActionTemplate);
                    } else {
                        unsatisfiable.add(thisActionTemplate);
                    }
                }

                fireOnActionsResolved(satisfiable, unsatisfiable);
            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

    private void fireOnActionsResolved (List<ActionTemplate> satisfiable, List<ActionTemplate> unsatisfiable) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onActionsResolved(satisfiable, unsatisfiable);
        }
    }
}
