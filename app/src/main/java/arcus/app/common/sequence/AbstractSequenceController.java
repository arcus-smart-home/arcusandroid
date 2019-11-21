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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import arcus.app.common.backstack.BackstackManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a sequence of Fragments.
 * Provides base navigation functions and convenience methods for SequenceController subclasses.
 */
public abstract class AbstractSequenceController implements SequenceController {

    private static Logger logger = LoggerFactory.getLogger(AbstractSequenceController.class);

    // The presently displayed fragment, or null
    private SequencedFragment activeFragment;

    /**
     * Gets the SequencedFragment currently active in the sequence (i.e., the one displayed if
     * the sequence is, itself, active.
     *
     * @return The active SequencedFragment
     */
    @Nullable
    public SequencedFragment getActiveFragment() {
        return activeFragment;
    }

    /**
     * Navigate backwards to the next Sequenceable element. This method differs from
     * {@link #navigateForward(Activity, Sequenceable, Object...)} only in the means by which it
     * transitions:
     *
     * 1. For SequencedFragments, it uses {@link BackstackManager#navigateBackToFragment(Fragment)}
     * rather than {@link BackstackManager#navigateToFragment(Fragment, boolean)}.
     *
     * 2. For SequenceControllers, it uses {@link BackstackManager#navigateBackToFragment(Fragment)}
     * to transition to that controller's active fragment ({@link #getActiveFragment()})
     *  @param activity
     * @param next
     * @param data
     */
    protected void navigateBack(@NonNull Activity activity, @NonNull Sequenceable next, Object... data) {

        if (next instanceof SequenceController) {
            logger.warn("Navigating back to SequenceController {} with data {}.", next, data);
            if (((SequenceController) next).getActiveFragment() != null) {
                BackstackManager.getInstance().navigateBackToFragment(((SequenceController) next).getActiveFragment());
            } else {
                next.endSequence(activity, true, data);
            }
        }

        else if (next instanceof SequencedFragment) {
            logger.warn("Navigating back to SequencedFragment {} with data {}.", next, data);

            this.activeFragment = (SequencedFragment) next;
            ((SequencedFragment) next).setController(this);

            BackstackManager.getInstance().navigateBackToFragment((SequencedFragment) next);
        }
    }

    /**
     * Navigate forwards to the next Sequenceable element. This method differs from
     * {@link #navigateBack(Activity, Sequenceable, Object...)} only in the means by which it
     * transitions:
     *
     * 1. For SequencedFragments, it uses {@link BackstackManager#navigateToFragment(Fragment, boolean)}
     * rather than {@link BackstackManager#navigateBackToFragment(Fragment)} and injects this
     * controller into the fragment.
     *
     * 2. For SequenceControllers, it invokes {@link Sequenceable#startSequence(android.app.Activity, Sequenceable, Object...)}
     * rather than navigating to the controller's active fragment.
     *  @param activity
     * @param next
     * @param data
     */
    protected void navigateForward(@NonNull Activity activity, @NonNull Sequenceable next, Object... data) {

        if (next instanceof SequenceController) {
            logger.warn("Navigating forward to SequenceController {} with data {}.", next, data);
            next.startSequence(activity, this, data);
        }

        else if (next instanceof SequencedFragment) {
            logger.warn("Navigating forward to SequencedFragment {} with data {}.", next, data);

            this.activeFragment = (SequencedFragment) next;
            ((SequencedFragment) next).setController(this);

            pushFragmentToBackstack((SequencedFragment) next, true);
        }
    }

    /**
     * Delegates to BackstackManager to navigate to the given fragment. Override in subclasses to
     * provide custom navigation behavior.
     *
     * @param fragment The fragment to navigate to
     * @param addToBackstack True if the fragment should be added to the backstack
     */
    protected void pushFragmentToBackstack(SequencedFragment fragment, boolean addToBackstack) {
        BackstackManager.getInstance().navigateToFragment(fragment, addToBackstack);
    }

    /**
     * A convenience method to unpack an argument buried in the data varargs. If the argument does
     * not exist, or is not of the expected type an IllegalArgumentException is thrown.
     *
     * @param index The index of the argument to unpack
     * @param asType The expected type of the argument
     * @param data The array of Object data
     * @param <T>
     * @return
     */
    protected <T> T unpackArgument (int index, @NonNull Class<T> asType, Object... data) {
        if (data == null || index >= data.length) {
            throw new IllegalArgumentException("Argument expected in position " + index);
        }

        if (data[index] == null || !asType.isAssignableFrom(data[index].getClass())) {
            throw new IllegalArgumentException("Argument expected in position " + index + " to be of type " + asType.getSimpleName() + ", but found " + data[index]);
        }

        return (T) data[index];
    }

    /**
     * A convenience method to unpack an argument buried in the data varargs
     *
     * @param index
     * @param asType
     * @param defaultValue
     * @param data
     * @param <T>
     * @return
     */
    protected <T> T unpackArgument (int index, @NonNull Class<T> asType, T defaultValue, Object... data) {
        try {
            return unpackArgument(index, asType, data);
        } catch (Throwable e) {
            return defaultValue;
        }
    }

}
