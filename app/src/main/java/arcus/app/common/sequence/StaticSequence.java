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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * Represents an ordered list of Sequenceable elements. Useful for sequences in which the visited
 * set and order of Fragments is known and the start of the sequence.
 */
public class StaticSequence extends ArrayList<Class<? extends Sequenceable>> {

    @NonNull
    public static StaticSequence from (@NonNull Class<? extends Sequenceable>... elements) {
        StaticSequence sequence = new StaticSequence();

        for (Class<? extends Sequenceable> thisElement : elements) {
            sequence.add(thisElement);
        }

        return sequence;
    }

    @Nullable
    public Class<? extends Sequenceable> getPrevious (Class<? extends Sequenceable> from) {

        for (int index = 0; index < size(); index++) {
            if (get(index) == from && index > 0) {
                return get(index - 1);
            }
        }

        return null;
    }

    @Nullable
    public Class<? extends Sequenceable> getNext (Class<? extends Sequenceable> from) {

        for (int index = 0; index < size(); index ++ ) {
            if (get(index) == from && index < (size() - 1)) {
                return get(index + 1);
            }
        }

        return null;
    }
}
