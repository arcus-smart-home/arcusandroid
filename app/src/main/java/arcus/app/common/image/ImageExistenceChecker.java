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
package arcus.app.common.image;

import androidx.annotation.NonNull;

import arcus.app.common.utils.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ImageExistenceChecker {

    @NonNull public static Map<String, Boolean> cache = new ConcurrentHashMap<>();

    public static boolean onServer (@NonNull String url) {

        if (StringUtils.isEmpty(url)) {
            return false;
        }

        if (cache.containsKey(url)) {
            return cache.get(url);
        }

        else {
            boolean exists = existsOnServer(url);
            cache.put(url, exists);
            return exists;
        }
    }

    private static boolean existsOnServer (@NonNull String url){
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            return (connection.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            return false;
        }
    }
}
