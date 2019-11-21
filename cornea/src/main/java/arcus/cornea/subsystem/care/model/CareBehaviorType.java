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
package arcus.cornea.subsystem.care.model;

import androidx.annotation.Nullable;
import android.text.TextUtils;

import org.slf4j.LoggerFactory;

public enum CareBehaviorType {
    INACTIVITY,
    OPEN,
    OPEN_COUNT,
    TEMPERATURE,
    PRESENCE,
    UNSUPPORTED;

    public static CareBehaviorType from(@Nullable String type) {
        if (TextUtils.isEmpty(type)) {
            LoggerFactory.getLogger(CareBehaviorType.class).debug("Unsupported Template Type of Null/Empty");
            return UNSUPPORTED;
        }

        try {
            return valueOf(type);
        }
        catch (Exception ex) {
            LoggerFactory.getLogger(CareBehaviorType.class).debug("Unsupported Template Type: {}", type);
            return UNSUPPORTED;
        }
    }
}
