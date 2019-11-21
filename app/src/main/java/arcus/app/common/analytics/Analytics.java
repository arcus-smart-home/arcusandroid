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
package arcus.app.common.analytics;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import android.util.Log;

import arcus.analytics.ArcusAnalytics;
import arcus.analytics.tag.ArcusTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents App-Level Analytics calls.
 */
public class Analytics {
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Log.ASSERT})
    public @interface LogLevel {}

    private static final Logger logger = LoggerFactory.getLogger(Analytics.class);
    protected Analytics() {}

    private static void logCustom(@NonNull String EventNames) {
        logCustom(new TaggingEvent(EventNames));
    }

    private static void logCustom(@NonNull ArcusTag event) {
        ArcusAnalytics.tag(event);
    }

    private static class TaggingEvent implements ArcusTag {

        private final String tagName;
        private final Map<String,Object> attributes = new HashMap<>();

        public TaggingEvent (String eventName) {
            this.tagName = eventName;
        }

        public TaggingEvent putCustomAttribute(String attributeName, Object attributeValue) {
            this.attributes.put(attributeName, attributeValue);
            return this;
        }

        @NonNull
        @Override
        public String getName() {
            return this.tagName;
        }

        @NonNull
        @Override
        public Map<String, Object> getAttributes() {
            return this.attributes;
        }
    }

    public static void log(@LogLevel int level, String tag, String message) {
        try {
            // Removed - Log.
        }
        catch (Exception ex) { // Don't crash over this...
            logger.debug("Failed logging update Level:{}, Tag:{}, Message:{}\n", level, tag, message, ex);
        }
    }

    public static void logException(Throwable throwable) {
        // Removed
    }
}
