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

import arcus.app.common.fragments.BaseFragment;

/**
 * Represents an Fragment that can exist as part of sequence of fragments, "pages" or "screens".
 *
 * Useful when comprising multiple fragments that work together to form a process or sequence (for
 * example, the set of fragments that make up the account creation, pairing, or button assignment
 * sequences).
 *
 * Any fragment that needs to participate in a sequence should extend this class.
 *
 * The class has two very trivial goals:
 * 1. It maintains a reference to the {@link SequenceController} that is responsible for
 * encapsulating the logic used to determine next/back/start/end behavior for the sequence.
 * 2. It provides a set of convenience methods that delegate to the controller (to eliminate
 * boilerplate calls to {@link #getController()} subtypes can simply invoke {@link #goNext(Object...)} )
 *
 * When creating a fragment that participates in a sequence, it's important to delegate to the
 * sequence controller for navigating back and forth. This implies that SequencedFragment subclasses
 * should never use {@link arcus.app.common.backstack.BackstackManager} for
 * transitioning between fragments.
 *
 * The generic type K is used to optionally specify the class of SequenceController used to manage
 * this fragment. This enables the {@link #getController()} method to return a specific subtype
 * useful to the fragment (rather than requiring the client to cast the return value). Do not
 * specify a generic if the fragment can be controlled by multiple types controllers.
 */
public abstract class SequencedFragment<K extends SequenceController> extends BaseFragment implements Sequenceable {

    private SequenceController controller = new ReturnToSenderSequenceController();

    public void goBack (Object... data) {
        if (controller != null) {
            controller.goBack(getActivity(), this, data);
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        if (controller != null) {
            controller.goBack(activity, from, data);
        }
    }

    public void goNext (Object... data) {
        if (controller != null) {
            controller.goNext(getActivity(), this, data);
        }
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        if (controller != null) {
            controller.goNext(activity, from, data);
        }
    }

    public void endSequence(boolean isSuccess, Object... data) {
        endSequence(getActivity(), isSuccess, data);
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        if (controller != null) {
            controller.endSequence(activity, isSuccess, data);
        }
    }

    public void startSequence(Object... data) {
        startSequence(getActivity(), data);
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        if (controller != null) {
            controller.startSequence(activity, from, data);
        }
    }

    /**
     * Sets the controller that will be used to manage this sequence of fragments. Note that most
     * users should never need to invoke this method; controllers that extend {@link AbstractSequenceController}
     * will automatically inject themselves before transitioning.
     *
     * If you find yourself needing to regularly invoke this method, you're controller is not properly
     * architected.
     *
     * @param controller
     */
    public void setController (K controller) {
        this.controller = controller;
    }

    /**
     * Gets the controller responsible for managing transitions in this sequence.
     * @return
     */
    public K getController() {
        return (K) controller;
    }
}
