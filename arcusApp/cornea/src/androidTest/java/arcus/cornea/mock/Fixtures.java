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
package arcus.cornea.mock;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientMessage;
import com.iris.client.capability.Capability;
import com.iris.client.model.Model;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Fixtures {
    private static final Gson gson = new GsonBuilder().create();
    private static final TypeToken<Map<String, Object>> object = new TypeToken<Map<String, Object>>() {};
    private static final TypeToken<List<Map<String, Object>>> collection = new TypeToken<List<Map<String, Object>>>() {};

    public static void resetModels() {
        CorneaClientFactory.getModelCache().clearCache();
    }

    public static List<Model> loadModels(String path) {
        List<Map<String, Object>> models = loadJson(path, collection);
        return CorneaClientFactory.getModelCache().addOrUpdate(models);
    }

    public static Model loadModel(String path) {
        Map<String, Object> model = loadJson(path, object);
        return CorneaClientFactory.getModelCache().addOrUpdate(model);
    }

    public static Model updateModel(String address, String attribute, Object value) {
        return CorneaClientFactory.getModelCache().addOrUpdate(ImmutableMap.of(
                Capability.ATTR_ADDRESS, address,
                attribute, value
        ));
    }

    public static Model updateModel(String address, Map<String, Object> attributes) {
        Map<String, Object> copy = new HashMap<>(attributes);
        copy.put(Capability.ATTR_ADDRESS, address);
        return CorneaClientFactory.getModelCache().addOrUpdate(copy);
    }

    public static boolean deleteModel(String address) {
        Model model = CorneaClientFactory.getModelCache().get(address);

        CorneaClientFactory.getModelCache().onEvent(
                ClientMessage
                    .builder()
                    .withSource(address)
                    .withType(Capability.EVENT_DELETED)
                    .create()
        );
        return model != null;
    }

    public static JsonElement loadJson(String path) {
        return loadJson(path, JsonElement.class);
    }

    public static <T> T loadJson(String path, Class<T> type) {
        return loadJson(path, type);
    }

    public static <T> T loadJson(String path, TypeToken<T> token) {
        return loadJson(path, token.getType());
    }

    private static <T> T loadJson(String path, Type type) {
        Reader r = new InputStreamReader( loadResource(path) );
        try {
            return gson.fromJson(r, type);
        }
        finally {
            try { r.close(); }
            catch(Exception e) { /* ignore */ }
        }
    }

    public static InputStream loadResource(String path) {
        return loadResource("/" + path, Fixtures.class);
    }

    public static InputStream loadResource(String path, Class<?> relativeTo) {
        InputStream is = relativeTo.getResourceAsStream(path);
        if(is == null) {
            throw new IllegalArgumentException("Could not find resource classpath:" + path);
        }
        return is;
    }
}
