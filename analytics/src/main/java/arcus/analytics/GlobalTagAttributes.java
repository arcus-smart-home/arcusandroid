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

import java.util.HashMap;
import java.util.Map;

/**
 * A set of tag attributes that should be applied to each and every tag. This class eliminates the
 * boilerplate of having to repeatedly specify common attributes each time a tag is generated.
 *
 * Global attributes come in two flavors:
 *
 * 1. Static attributes whose value remains constant (useful for things for device identifiers, app
 * versions, etc).
 *
 * 2. Dynamic attributes whose value is evaluated each time the tag is committed to an endpoint.
 * Useful for runtime contextual values (timestamps, memory use, etc.)
 */
public class GlobalTagAttributes {

    private final static GlobalTagAttributes instance = new GlobalTagAttributes();

    private final Map<String,Object> staticAttributes = new HashMap<>();
    private final Map<String,DynamicAttributeProvider> dynamicAttributes = new HashMap<>();

    private GlobalTagAttributes() {}

    public static GlobalTagAttributes getInstance() {
        return instance;
    }

    public Map<String,Object> get() {
        Map<String,Object> globalAttributes = new HashMap<>();

        for (String thisStaticAttribute : staticAttributes.keySet()) {
            globalAttributes.put(thisStaticAttribute, staticAttributes.get(thisStaticAttribute));
        }

        for (String thisDynamicAttribute : dynamicAttributes.keySet()) {
            globalAttributes.put(thisDynamicAttribute, dynamicAttributes.get(thisDynamicAttribute).getValueForAttribute(thisDynamicAttribute));
        }

        return globalAttributes;
    }

    public void put (String attribute, Object withValue) {
        staticAttributes.put(attribute, withValue);
    }

    public void put (String attribute, DynamicAttributeProvider provider) {
        dynamicAttributes.put(attribute, provider);
    }
}
