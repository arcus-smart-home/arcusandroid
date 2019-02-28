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
package arcus.cornea.utils;

import com.iris.client.model.Model;

import org.eclipse.jdt.annotation.Nullable;

import java.util.Map;


public class CapabilityInstances {

    public static String getAttributeName(String instanceName, String attributeName) {
        return attributeName + ":" + instanceName;
    }

    @Nullable
    public static Object getAttributeValue(Model model, String instanceName, String attributeName) {
        return model.get( getAttributeName(instanceName, attributeName) );
    }

    @Nullable
    public static Object getAttributeValue(Map<String, Object> values, String instanceName, String attributeName) {
        return values.get( getAttributeName(instanceName, attributeName) );
    }

    public static CapabilityUtils wrap(Model model) {
        return new CapabilityUtils(model);
    }
}
