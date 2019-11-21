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
package arcus.cornea.model.smartystreets;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

public class SmartyStreetsDeserializer {
    private static final Logger logger = LoggerFactory.getLogger(SmartyStreetsDeserializer.class);

    private SmartyStreetsDeserializer() {
        //no instance
    }

    public @NonNull static List<SmartyStreetsResponse> parseResponse(String response) {
        try {
            List<SmartyStreetsResponse> responses = new Gson().fromJson(response, new TypeToken<List<SmartyStreetsResponse>>(){}.getType());
            if (responses != null) {
                return responses;
            }

            logger.debug("GSON Returned null list. Returning an empty list in lieu of that.");
        }
        catch (Exception ex) {
            logger.debug("Failed to parse response. Returning empty list.", ex);
        }

        return Collections.emptyList();
    }
}
