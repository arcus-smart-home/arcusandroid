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
package arcus.app.common.backstack;

import android.app.FragmentTransaction;

import arcus.app.R;


public enum TransitionEffect {
    PUSH_LEFT(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right),
    PUSH_RIGHT(R.anim.enter_from_left, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_left),
    SLIDE_OVER(R.anim.slide_in_from_bottom, R.anim.slide_in_from_bottom, R.anim.slide_out_through_bottom, R.anim.slide_out_through_bottom),
    FADE(FragmentTransaction.TRANSIT_FRAGMENT_FADE),
    DEFAULT,
    NONE;

    public Integer enter, exit, popEnter, popExit, transitionId;

    TransitionEffect() {}

    TransitionEffect(int effectId) {
        this.transitionId = effectId;
    }

    TransitionEffect(int enter, int exit, int popEnter, int popExit) {
        this.enter = enter;
        this.exit = exit;
        this.popEnter = popEnter;
        this.popExit = popExit;
    }

    boolean isTransition() {
        return transitionId != null;
    }

    boolean isAnimation() {
        return enter != null && exit != null && popEnter != null && popExit != null;
    }
}
