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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;


public class Responses {
    private static final Gson gson = new GsonBuilder().create();
    private static final TypeToken<Map<String, Object>> token = new TypeToken<Map<String, Object>>() {};

    public static <E extends ClientEvent> E loadResponse(Class<E> type, String path) {
        ClientMessage message = load(path).create();
        return instantiate(type, message.getEvent());
    }

    public static <E extends ClientEvent> E loadResponse(Class<E> type, String path, Class<?> relativeTo) {
        ClientMessage message = load(path, relativeTo).create();
        return instantiate(type, message.getEvent());
    }

    public static ClientMessage.Builder load(String path) {
        InputStream is = Responses.class.getClassLoader().getResourceAsStream(path);
        if(is == null) {
            throw new IllegalArgumentException("No file on classpath found at " + path);
        }
        try {
            return load(is);
        }
        finally {
            try { is.close(); }
            catch(Exception ex) { /* ignore */ }
        }
    }

    public static ClientMessage.Builder load(String path, Class<?> relativeTo) {
        InputStream is = relativeTo.getResourceAsStream(path);
        if(is == null) {
            throw new IllegalArgumentException("No file on classpath found at " + path);
        }
        try {
            return load(is);
        }
        finally {
            try { is.close(); }
            catch(Exception ex) { /* ignore */ }
        }
    }

    public static ClientMessage.Builder load(InputStream is) {
        JsonObject json = gson.fromJson(new InputStreamReader(is), JsonObject.class);
        Map<String, Object> attributes = gson.fromJson(json.get("payload").getAsJsonObject().get("attributes"), token.getType());
        return
                ClientMessage
                        .builder()
                        .withType(json.get("type").getAsString())
                        .withSource(json.get("source").getAsString())
                        .withDestination(json.get("destination").getAsString())
                        .withAttributes(attributes)
                        ;
    }

    private static <E extends ClientEvent> E instantiate(Class<E> type, ClientEvent event) {
        if(type.isAssignableFrom(event.getClass())) {
            return (E) event;
        }

        try {
            return type.getConstructor(ClientEvent.class).newInstance(event);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Invalid type " + type +". Must have a public constructor(ClientEvent)", e);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid type " + type +". Must have a public constructor(ClientEvent)", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Invalid type " + type +". Must have a public constructor(ClientEvent)", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Invalid type " + type +". Must have a public constructor(ClientEvent)", e);
        }
    }
}
