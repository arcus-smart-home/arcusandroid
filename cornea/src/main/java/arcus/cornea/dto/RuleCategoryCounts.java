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
package arcus.cornea.dto;

import com.iris.client.service.RuleService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RuleCategoryCounts {
    private static final Logger logger = LoggerFactory.getLogger(RuleCategoryCounts.class);
    private List<RuleCountInstance> counts = new ArrayList<>();
    private Comparator<RuleCountInstance> DESC_COUNT_COMPARATOR = new Comparator<RuleCountInstance>() {
        @Override
        public int compare(RuleCountInstance lhs, RuleCountInstance rhs) {
            return rhs.getCount().compareTo(lhs.getCount());
        }
    };
    private Comparator<RuleCountInstance> ASC_COUNT_COMPARATOR = new Comparator<RuleCountInstance>() {
        @Override
        public int compare(RuleCountInstance lhs, RuleCountInstance rhs) {
            return lhs.getCount().compareTo(rhs.getCount());
        }
    };

    public enum SortOrder {
        ASC_COUNT,
        DESC_COUNT
    }

    public RuleCategoryCounts(RuleService.GetCategoriesResponse response) {
        if (response != null && response.getCategories() != null) {
            for (Map.Entry<String, Integer> item : response.getCategories().entrySet()) {
                try {
                    Double number = Double.parseDouble(String.valueOf(item.getValue()));
                    counts.add(new RuleCountInstance(item.getKey(), number.intValue()));
                }
                catch (Exception ex) {
                    logger.debug("Skipping over [{}] cannot parse count from [{}]", item.getKey(), item.getValue(), ex);
                }
            }
        }
    }

    public List<RuleCountInstance> getCategoryList() {
        return getCategoryList(SortOrder.DESC_COUNT);
    }

    public List<RuleCountInstance> getCategoryList(SortOrder sortOrder) {
        if (sortOrder == SortOrder.DESC_COUNT) {
            Collections.sort(counts, DESC_COUNT_COMPARATOR);
        }
        else {
            Collections.sort(counts, ASC_COUNT_COMPARATOR);
        }

        return Collections.unmodifiableList(counts);
    }

    @Override
    public String toString() {
        return getCategoryList().toString();
    }

    public static class RuleCountInstance extends ProductCounts {
        public RuleCountInstance(String name, Integer count) {
            super(name, count);
        }
    }
}
