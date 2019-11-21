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

import androidx.fragment.app.Fragment;



public class ScleraTransitionManager {

    /**
     * Navigates to a fragment representing a new mode or section of the app (rather than a screen
     * in a linear sequence of screens).
     *
     * Use {@link #navigateForward(Fragment)} or {@link #navigateBackward(Fragment)} if this
     * fragment represents the next/previous screen in a logical sequence, or
     * {@link #displaySheet(Fragment)} if the fragment represents a popup.
     *
     * Displays the given fragment in the 'container' view group of the current activity using a fade
     * transition animation. Useful when navigating from one mode of the app to another, for
     * example, from the '+' menu to the product catalog.
     *
     * @param fragment The fragment to be displayed
     */
    public static void navigateAway(Fragment fragment) {
        BackstackManager
                .withAnimation(TransitionEffect.FADE)
                .navigateToFragment(fragment, true);
    }

    /**
     * Navigates to a fragment representing the previous logical screen in a linear sequence of
     * screens.
     *
     * Use {@link #navigateAway(Fragment)} if the fragment is not logically in sequence with the
     * current screen, or {@link #displaySheet(Fragment)} if the fragment represents a popup.
     *
     * Displays the given fragment in the 'container' view group of the current activity using a
     * push-right animation. Useful only when "forcing" the back animation; most users will find
     * navigating backwards is better accomplished with {@link #pop()}.
     *
     * @param fragment The fragment to be displayed.
     */
    public static void navigateBackward(Fragment fragment) {
        BackstackManager
                .withAnimation(TransitionEffect.PUSH_RIGHT)
                .navigateToFragment(fragment, true);
    }

    /**
     * Navigates to a fragment representing the next logical screen in a linear sequence of screens.
     *
     * Use {@link #navigateAway(Fragment)} if the fragment is not logically in sequence with the
     * current screen, or {@link #displaySheet(Fragment)} if the fragment represents a popup.
     *
     * Displays the given fragment in the 'container' view group of the current activity using a
     * push-left animation.
     *
     * @param fragment The fragment to be displayed.
     */
    public static void navigateForward(Fragment fragment) {
        BackstackManager
                .withAnimation(TransitionEffect.PUSH_LEFT)
                .navigateToFragment(fragment, true);
    }

    /**
     * Displays an overlay atop the current screen.
     *
     * Use one of {@link #navigateBackward(Fragment)}, {@link #navigateForward(Fragment)} or
     * {@link #navigateAway(Fragment)} to move between screens. This method is useful only for
     * presenting slide-over sheets, popups and pickers.
     *
     * Displays the given fragment in the 'floating' view group of the current activity using a
     * slide-up animation.
     *
     * @param sheet
     */
    public static void displaySheet(Fragment sheet) {
        BackstackManager
                .withAnimation(TransitionEffect.SLIDE_OVER)
                .navigateToFloatingFragment(sheet, sheet.getClass().getCanonicalName(), true);
    }

    /**
     * Pops the backstack, removing the last fragment navigated to or displayed.
     *
     * For consistency, callers should use {@link #dismissSheet()} when "closing" a fragment that
     * was displayed using {@link #displaySheet(Fragment)}.
     *
     * Note that the animation used to "pop" a fragment is determined when it was pushed.
     */
    public static void pop() {
        BackstackManager.getInstance().navigateBack();
    }

    /**
     * Closes/dismisses all active sheets / pop-overs.
     *
     * Removes fragments presently displayed in the 'floating' view group of the current activity.
     * Has no effect if there are no fragments on the backstack displayed in the 'floating' view
     * group.
     *
     * @return True if one or more sheets were dismissed; false if no sheets were present to
     * dismiss.
     */
    public static boolean dismissSheet() {
        return BackstackManager.getInstance().dismissFloatingFragments();
    }

}
