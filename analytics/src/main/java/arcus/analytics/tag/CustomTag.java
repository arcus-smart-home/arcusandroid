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
package arcus.analytics.tag;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.Map;

/**
 * A base implementation of {@link ArcusTag}.
 */
public class CustomTag implements ArcusTag {

    private final String name;
    private final Map<String,Object> attributes;

    public CustomTag(@NonNull String name) {
        this(name, null);
    }

    @SuppressWarnings("ConstantConditions")
    public CustomTag(@NonNull String name, @Nullable Map<String,Object> attributes) {
        if (name == null) {
            this.name = "null_tag_name";
        } else {
            this.name = name;
        }

        this.attributes = attributes == null ? Collections.emptyMap() : attributes;
    }

    @NonNull
    @Override
    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
}
