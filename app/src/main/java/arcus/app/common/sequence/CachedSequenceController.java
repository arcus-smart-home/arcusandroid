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
package arcus.app.common.sequence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an {@link AbstractStaticSequenceController} whose sequence fragment instances
 * should be held in cache, rather than being recreated on each transition.
 *
 * This useful for "wizard-like" forms (i.e., account creation) where data entered on a previous
 * fragment should persist (and be available upon a backpress) for as long as the sequence is
 * active.
 */
public abstract class CachedSequenceController extends AbstractStaticSequenceController {

    private final Map<Class, Sequenceable> cachedInstances = new HashMap<>();
    private final List<Class> cacheExclusions = new ArrayList<>();

    public void addCacheExclusion (Class<? extends SequencedFragment> clazz) {
        cacheExclusions.add(clazz);
    }

    @Override
    public Sequenceable getInstanceOf(Class<? extends Sequenceable> clazz, Object... data) {

        if (cachedInstances.containsKey(clazz)) {
            return cachedInstances.get(clazz);
        }

        Sequenceable newInstance = newInstanceOf(clazz, data);

        if (!cacheExclusions.contains(clazz)) {
            cachedInstances.put(clazz, newInstance);
        }

        return newInstance;
    }
}