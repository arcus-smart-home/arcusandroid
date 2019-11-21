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

public enum TimeWindowSupport {
    REQUIRED,
    OPTIONAL,
    NODURATION,
    NONE;

    public static TimeWindowSupport from(@Nullable String string) {
        if (TextUtils.isEmpty(string)) {
            return NONE;
        }

        try {
            return valueOf(string);
        }
        catch (Exception ex) {
            LoggerFactory.getLogger(TimeWindowSupport.class).debug("Unable to parse TimeWindowSupport of {}", string);
            return NONE;
        }
    }
}
