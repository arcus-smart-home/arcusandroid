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

import androidx.fragment.app.Fragment;

import arcus.app.common.backstack.BackstackManager;

/**
 * A base sequence controller for controllers that present a sequence of floating fragments.
 *
 * Used to delegate to {@link BackstackManager#navigateToFloatingFragment(Fragment, String, boolean)}
 * instead of {@link BackstackManager#navigateToFragment(Fragment, boolean)} as performed by
 * {@link AbstractSequenceController}.
 */
public abstract class AbstractFloatingFragmentSequenceController extends AbstractSequenceController {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void pushFragmentToBackstack(SequencedFragment fragment, boolean addToBackstack) {
        BackstackManager.withoutAnimation().navigateToFloatingFragment(fragment, addToBackstack);
    }

}
