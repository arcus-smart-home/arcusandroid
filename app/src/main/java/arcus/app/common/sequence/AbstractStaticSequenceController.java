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

import android.app.Activity;
import androidx.annotation.NonNull;

/**
 * Represents a sequence of fragments whose ordering is static, that is, the visited order of
 * fragments can be determined at the time the sequence starts. For example, the account creation
 * process is a static sequence (the set and order of "pages" is known at the start). The pairing
 * process is not (one cannot determine the set of "pages" displayed until the user has begun
 * pairing one or more devices).
 *
 * This class provides navigation and convenience methods for SequenceControllers that employ a
 * static sequence.
 */
public abstract class AbstractStaticSequenceController extends AbstractSequenceController {

    protected boolean navigateToPreviousSequenceable(Activity activity, @NonNull StaticSequence sequence, Class<? extends Sequenceable> from, Object... data) {
        Class<? extends Sequenceable> prevFragmentType = sequence.getPrevious(from);
        if (prevFragmentType != null) {
            navigateForward(activity, getInstanceOf(prevFragmentType, data), data);
            return true;
        }

        // Not able to determine 'next'
        return false;
    }

    protected boolean navigateToNextSequenceable(Activity activity, @NonNull StaticSequence sequence, Class<? extends Sequenceable> from, Object... data) {
        Class<? extends Sequenceable> nextFragmentType = sequence.getNext(from);
        if (nextFragmentType != null) {
            navigateForward(activity, getInstanceOf(nextFragmentType, data), data);
            return true;
        }

        // Not able to determine 'next'
        return false;
    }

    protected Sequenceable getInstanceOf(@NonNull Class<? extends Sequenceable> clazz, Object... data) {
        return newInstanceOf(clazz, data);
    }

    protected Sequenceable newInstanceOf(@NonNull Class<? extends Sequenceable> clazz, Object... data)  {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot instantiate fragment " + clazz.getSimpleName() + ". Does it have a no-arg constructor?");
        }
    }
}
