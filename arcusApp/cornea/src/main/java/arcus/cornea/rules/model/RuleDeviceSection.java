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
package arcus.cornea.rules.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RuleDeviceSection {
    private final List<RuleProxyModel> rules;

    public RuleDeviceSection() {
        rules = new ArrayList<>();
    }

    public RuleDeviceSection(@Nullable List<RuleProxyModel> initWithModels) {
        this.rules = (initWithModels == null) ? new ArrayList<RuleProxyModel>() : new ArrayList<>(initWithModels);
    }

    public void addRule(@NonNull RuleProxyModel ruleModel) {
        rules.add(ruleModel);
    }

    public void sortRules(boolean reverse) {
        if (!reverse) {
            Collections.sort(rules);
        }
        else {
            Collections.reverse(rules);
        }
    }

    public @NonNull List<RuleProxyModel> getRules() {
        return new ArrayList<>(rules);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RuleDeviceSection that = (RuleDeviceSection) o;

        return rules != null ? rules.equals(that.rules) : that.rules == null;

    }

    @Override public int hashCode() {
        return rules != null ? rules.hashCode() : 0;
    }

    @Override public String toString() {
        return "RuleDeviceSection{" +
              "rules=" + rules +
              '}';
    }
}
