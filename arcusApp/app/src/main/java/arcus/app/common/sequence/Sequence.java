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

import arcus.app.common.backstack.BackstackManager;
import arcus.app.dashboard.HomeFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Sequence extends AbstractSequenceController {

    private List<Sequenceable> sequence = new ArrayList<>();
    private final Sequenceable uponCompletion;

    public Sequence() {
        this.uponCompletion = null;
    }

    public Sequence (Sequenceable uponCompletion) {
        this.uponCompletion = uponCompletion;
    }

    public Sequence (Sequenceable uponCompletion, Sequenceable... sequenceables) {
        this.uponCompletion = uponCompletion;
        sequence = Arrays.asList(sequenceables);
    }

    public void add (Sequenceable sequenceable) {
        if (sequence.contains(sequenceable)) {
            throw new IllegalArgumentException("Sequence can only contain one instance of Sequenceable " + sequenceable);
        }

        sequence.add(sequenceable);
    }

    public int size() {
        return sequence.size();
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {

        // From is not in sequence...
        if (sequence.indexOf(from) < 0) {
            startSequence(activity, from, data);
        }

        // We're at the last element in the sequence
        else if (sequence.indexOf(from) == sequence.size() - 1) {
            endSequence(activity, true, data);
        }

        // Transition to next Sequenceable
        else {
            navigateForward(activity, sequence.get(sequence.indexOf(from) + 1), data);
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {

        // We're at the first element in the sequence or from is not in sequence
        if (sequence.indexOf(from) < 1) {
            endSequence(activity, false, data);
        }

        // Transition to previous Sequenceable
        else {
            navigateForward(activity, sequence.get(sequence.indexOf(from) - 1), data);
        }

    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        if (uponCompletion == null) {
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
        }

        if (isSuccess) {
            navigateForward(activity, uponCompletion, data);
        } else {
            navigateBack(activity, uponCompletion, data);
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        // Corner case: Sequence is empty
        if (sequence.size() == 0) {
            endSequence(activity, true);
        } else {
            navigateForward(activity, sequence.get(0), data);
        }
    }
}
