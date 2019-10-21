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
package arcus.cornea.provider;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.IrisClient;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.ModelCache;
import com.iris.client.model.SceneTemplateModel;
import com.iris.client.model.Store;
import com.iris.client.service.SceneService;

import java.util.List;

public class SceneTemplateProvider extends BaseModelProvider<SceneTemplateModel> {
    private static final SceneTemplateProvider INSTANCE = new SceneTemplateProvider();
    private final IrisClient client;
    private final ModelCache cache;
    private final Function<SceneService.ListSceneTemplatesResponse, List<SceneTemplateModel>> getSceneTemplates =
          new Function<SceneService.ListSceneTemplatesResponse, List<SceneTemplateModel>>() {
              @Override
              public List<SceneTemplateModel> apply(SceneService.ListSceneTemplatesResponse input) {
                  return Lists.newArrayList(getStore().values());
              }
          };

    public static SceneTemplateProvider instance() {
        return INSTANCE;
    }

    public SceneTemplateProvider() {
        this(
              CorneaClientFactory.getClient(),
              CorneaClientFactory.getModelCache(),
              CorneaClientFactory.getStore(SceneTemplateModel.class)
        );
    }

    protected SceneTemplateProvider(IrisClient client, ModelCache cache, Store<SceneTemplateModel> store) {
        super(client, cache, store);
        this.client = client;
        this.cache = cache;
    }

    @Override
    protected ClientFuture<List<SceneTemplateModel>> doLoad(String placeId) {
        ClientFuture<SceneService.ListSceneTemplatesResponse> request =
              CorneaClientFactory.getService(SceneService.class).listSceneTemplates(placeId);

        return Futures.transform(request, getSceneTemplates);
    }
}
