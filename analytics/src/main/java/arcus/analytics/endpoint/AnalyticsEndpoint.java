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
package arcus.analytics.endpoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a data sink for analytics tag messages.
 */
public abstract class AnalyticsEndpoint {

    /**
     * Commit (write) the given tag data to this endpoint.
     *
     * @param name Name of the tagged event
     * @param attributes Map of all attributes and values associated with this tag
     */
    public abstract void commitTag(String name, Map<String,Object> attributes);


    /**
     * Renders a map of String->Object values into a map of String->String values. The intent of
     * this method is to format attribute values in a manner most useful to the endpoint. This
     * implementation merely returns the String.valueOf() of each value object, but could be
     * overridden to properly format dates, numbers or other custom objects.
     *
     * @param attributes Map of String->Object attributes to be rendered.
     * @return A rendered map of String->String
     */
    protected Map<String,String> renderAttributeValues(Map<String,Object> attributes) {
        Map<String,String> renderedAttributes = new HashMap<>();

        for (String thisAttribute : attributes.keySet()) {
            renderedAttributes.put(thisAttribute, String.valueOf(attributes.get(thisAttribute)));
        }

        return renderedAttributes;
    }
}
