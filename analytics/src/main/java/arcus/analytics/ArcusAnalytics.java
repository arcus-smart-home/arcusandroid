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
package arcus.analytics;

import arcus.analytics.tag.CustomTag;
import arcus.analytics.tag.ArcusTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A facade for tagging interesting application events.
 */
public class ArcusAnalytics {
    private static final List<TagRoute> routeList = new ArrayList<>();

    public static void tag(String name, Map<String,Object> attributes) {
        tag(new CustomTag(name, attributes));
    }

    public static void tag(ArcusTag tag) {
        for (TagRoute thisRoute : routeList) {
            thisRoute.route(tag, GlobalTagAttributes.getInstance());
        }
    }

    public static void addRoute(TagRoute tagRoute) {
        routeList.add(tagRoute);
    }

    public static boolean deleteRoute(TagRoute tagRoute) {
        return routeList.remove(tagRoute);
    }

    public static void deleteAllRoutes() {
        routeList.clear();
    }
}
