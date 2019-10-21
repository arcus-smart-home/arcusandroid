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

import arcus.analytics.endpoint.AnalyticsEndpoint;
import arcus.analytics.tag.ArcusTag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a route, consisting of zero or more {@link AnalyticsEndpoint} destinations that tags
 * should be delivered two, provided they meet a set of specified conditions.
 */
public class TagRoute implements Predicate<ArcusTag> {
    private boolean matchAll;
    private final List<RoutePredicate> conditions;
    private final List<AnalyticsEndpoint> destinations;

    TagRoute(boolean matchAll, List<RoutePredicate> conditions, List<AnalyticsEndpoint> destinations) {
        this.matchAll = matchAll;
        this.conditions = conditions;
        this.destinations = destinations;
    }

    void route(ArcusTag tag, GlobalTagAttributes globalTagAttributes) {
        if (apply(tag)) {
            for (AnalyticsEndpoint thisEndpoint : destinations) {
                thisEndpoint.commitTag(tag.getName(), mergeAttributes(tag.getAttributes(), globalTagAttributes));
            }
        }
    }

    /**
     * Determines if the given tag should be delivered to this route's endpoint based on whether it
     * matches the route's conditions.
     */
    @Override
    public boolean apply(ArcusTag arcusTag) {

        boolean matchesAny = false;
        boolean matchesAll = true;

        for (RoutePredicate thisCondition : conditions) {
            if (thisCondition.apply(arcusTag)) {
                matchesAny = true;
            } else {
                matchesAll = false;
            }
        }

        return matchAll ? matchesAll : matchesAny;
    }

    private Map<String,Object> mergeAttributes(Map<String,Object> localAttributes, GlobalTagAttributes globalTagAttributes) {
        Map<String,Object> mergedAttributes = new HashMap<>();

        mergedAttributes.putAll(globalTagAttributes.get());
        mergedAttributes.putAll(localAttributes);

        return mergedAttributes;
    }
}
