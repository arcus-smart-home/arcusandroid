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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import arcus.app.R;
import arcus.app.common.controller.BackstackPopListener;
import arcus.app.common.utils.PreferenceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BackstackManager {

    @NonNull
    private static BackstackManager instance = new BackstackManager();
    private static Logger logger = LoggerFactory.getLogger(BackstackManager.class);

    private FragmentController fragmentController;
    private TransitionEffect animation = TransitionEffect.DEFAULT;

    private BackstackManager() {
    }

    public static BackstackManager localInstance(FragmentActivity forActivity) {
        BackstackManager thisInstance = new BackstackManager();
        thisInstance.setBaseActivity(forActivity);
        return thisInstance;
    }

    @NonNull
    public static BackstackManager getInstance() {
        instance.animation = TransitionEffect.DEFAULT;
        return instance;
    }

    public static BackstackManager withAnimation(TransitionEffect animation) {
        instance.animation = animation;
        return instance;
    }

    public static BackstackManager withoutAnimation() {
        instance.animation = TransitionEffect.NONE;
        return instance;
    }

    public void setBaseActivity(@NonNull FragmentActivity activity) {
        logger.debug("Setting base activity to " + activity.getClass().getSimpleName());
        fragmentController = new FragmentController(activity.getSupportFragmentManager());
    }

    public boolean navigateBack() {
        assertInitialized();
        Fragment previousFragment = fragmentController.getPreviousFragment();

        logger.debug("Navigating back to last fragment on stack: {}", previousFragment);

        if (previousFragment != null && BackstackPopListener.class.isAssignableFrom(previousFragment.getClass())) {
            logger.debug("Notifying fragment it was popped: {}", previousFragment.getClass().getSimpleName());
            ((BackstackPopListener) previousFragment).onPopped();
        }

        return fragmentController.navigateBack();
    }

    /**
     * Attempts to find an element in the back stack matching the provided name and activates it.
     * Behaves like a pop operation that allows the user to pop an element not at the top of the
     * stack.
     * <p>
     * Traverses up the back stack and activates the first element matching the provided tag. All
     * elements above the found element are removed from the stack.
     *
     * @param fragment
     */
    public boolean navigateBackToFragment(@NonNull Class fragment) {
        assertInitialized();

        logger.debug("Attempting to navigate back to {}", fragment.getSimpleName());

        // Corner case: Cannot navigate back to self; attempting to do so will crash FragmentController
        if (isCurrentFragment(fragment)) {
            logger.warn("Cannot navigate back to self {}; nothing to do.", fragment.getSimpleName());
            return true;
        }

        Fragment previousFragment = fragmentController.getFragmentByClassName(fragment.getName());

        if (previousFragment != null && BackstackPopListener.class.isAssignableFrom(previousFragment.getClass())) {
            logger.debug("Notifying fragment {} it was popped", previousFragment.getClass().getSimpleName());
            ((BackstackPopListener) previousFragment).onPopped();
        }

        fragmentController.navigateBackToFragmentTag(fragment.getName());
        return true;
    }

    /**
     * If an instance of the given fragment already exists on the stack, navigate back to it; otherwise
     * navigate "forward" to the fragment instance provided and it to the backstack.
     *
     * @param fragment
     */
    public void navigateBackToFragment(@NonNull Fragment fragment) {
        assertInitialized();

        if (isFragmentOnStack(fragment.getClass())) {
            logger.debug("Navigating back to fragment {}", fragment.getClass().getSimpleName());
            navigateBackToFragment(fragment.getClass());
        } else {
            logger.debug("Instance of fragment {} not on stack; navigating to new instance of fragment instead.", fragment.getClass().getSimpleName());
            navigateToFragment(fragment, true);
        }
    }

    public boolean navigateToFragment(@NonNull Fragment fragment, boolean addToBackStack) {
        assertInitialized();

        logger.debug("Navigating to fragment {} with animation {}; pushing to stack: {}", fragment.getClass().getSimpleName(), animation, addToBackStack);
        fragmentController.navigateToFragment(fragment, fragment.getClass().getName(), addToBackStack, getAnimation(TransitionEffect.PUSH_LEFT, true), R.id.container);

        return true;
    }

    public boolean dismissFloatingFragments() {
        assertInitialized();
        return fragmentController.popAllFloatingFragments();
    }

    public boolean rewindToFragment(@NonNull Fragment fragment) {
        assertInitialized();

        logger.debug("Rewinding to fragment {}", fragment.getClass().getSimpleName());
        fragmentController.replaceAfterInitial(fragment);

        return true;
    }

    public void popAllFragments() {
        assertInitialized();
        fragmentController.popAllFragments();
    }

    public boolean isFragmentOnStack(@NonNull Class fragment) {
        assertInitialized();
        return fragmentController.getFragmentByClassName(fragment.getName()) != null;
    }

    public Fragment getFragmentOnStack(@NonNull Class fragment) {
        assertInitialized();
        return fragmentController.getFragmentByClassName(fragment.getName());
    }

    private boolean isCurrentFragment(@NonNull Class fragment) {
        assertInitialized();
        return fragmentController.getCurrentFragmentName().equals(fragment.getName());
    }

    public Fragment getCurrentFragment() {
        assertInitialized();
        return fragmentController.getCurrentFragment();
    }

    public boolean navigateToFloatingFragment(Fragment fragment, boolean addToBackStack) {
        return navigateToFloatingFragment(fragment, fragment.getClass().getCanonicalName(), addToBackStack);
    }

    public boolean navigateToFloatingFragment(Fragment fragment, String tag, boolean addToBackStack) {
        assertInitialized();

        logger.debug("Navigating to floating fragment {}; pushing to stack: {}", tag, addToBackStack);
        fragmentController.navigateToFragment(fragment, tag, addToBackStack, getAnimation(TransitionEffect.SLIDE_OVER, false), R.id.floating);

        return true;
    }

    public boolean navigateToFragmentSlideAnimation(Fragment fragment, String tag, boolean addToBackStack) {
        assertInitialized();

        logger.debug("Navigating to floating fragment {}; pushing to stack: {}", tag, addToBackStack);
        fragmentController.navigateToFragment(fragment, tag, addToBackStack, getAnimation(TransitionEffect.SLIDE_OVER, false), R.id.floating_under);

        return true;
    }

    private void assertInitialized() {
        if (fragmentController == null) {
            throw new IllegalStateException("Not initialized. Please call setBaseActivity() first.");
        }
    }

    private TransitionEffect getAnimation(TransitionEffect defaultAnimation, boolean honorAnimationEnabledSetting) {

        if (honorAnimationEnabledSetting && !PreferenceUtils.isAnimationEnabled()) {
            return TransitionEffect.NONE;
        }

        if (animation == TransitionEffect.DEFAULT) {
            return defaultAnimation;
        }

        return animation;
    }

}
