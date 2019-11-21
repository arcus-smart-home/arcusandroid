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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.ViewGroup;

import arcus.app.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FragmentController {

    private final FragmentManager fragmentManager;
    private final static Logger logger = LoggerFactory.getLogger(FragmentController.class.getName());

    public FragmentController(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    public void replaceAfterInitial(@NonNull Fragment fragment) {
        try {
            while (fragmentManager.getBackStackEntryCount() != 1) {
                fragmentManager.popBackStackImmediate();
            }

            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(fragment.getClass().getName());
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            logger.error("An error occurred replacing fragment after initial.", e);
        }
    }

    public void popAllFragments() {
        try {
            while (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStackImmediate();
            }
        } catch (Exception e) {
            logger.error("An error occurred popping all fragments.", e);
        }
    }

    public boolean popAllFloatingFragments() {
        Fragment f = getCurrentFragment();
        boolean success = false;
        while (fragmentManager.getBackStackEntryCount() > 0 &&
                f.getView() != null &&
                f.getView().getParent() != null &&
                (R.id.floating == ((ViewGroup) f.getView().getParent()).getId()))
        {
            try {
                fragmentManager.popBackStackImmediate();
                f = getCurrentFragment();
                success = true;
            } catch (Exception e) {
                logger.error("An error occurred popping all floating fragments.", e);
                return false;
            }
        }

        return success;
    }

    // Add the fragment to the top of the stack
    // Will replace the current fragment with the new fragment to navigate forward
    public void navigateToFragment(Fragment fragment, String tag, boolean addToBackStack, TransitionEffect animation, int containerResId) {

        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (animation != null && animation.isAnimation()) {
            transaction.setCustomAnimations(animation.enter, animation.exit, animation.popEnter, animation.popExit);
        }

        if (animation != null && animation.isTransition()) {
            transaction.setTransition(animation.transitionId);
        }

        transaction.replace(containerResId, fragment, fragment.getClass().getName());
        if (addToBackStack)
            transaction.addToBackStack(tag);

        try {
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            logger.error("An error occurred while navigating to fragment.", e);
        }
    }

    // Pop all fragments up to the specified fragment
    public boolean navigateBackToFragmentTag(String tag) {

        try {
            if (fragmentManager.getBackStackEntryCount() == 1)
                return false;
            else {
                int index = -1;
                for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                    String name = fragmentManager.getBackStackEntryAt(i).getName();
                    if (name.equals(tag)) {
                        index = i + 1;
                        break;
                    }
                }

                if (index == -1) {
                    // Error - We could potentially pop the whole stack and move forward
                    // to the needed fragment but still you'd be in an unknown state.
                    return false;
                } else {
                    fragmentManager.popBackStack(fragmentManager.getBackStackEntryAt(index).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
                }
                return true;
            }
        } catch (Exception e) {
            logger.error("An error occurred navigating back to fragment.", e);
            return false;
        }
    }

    // Pop the current fragment to the previous fragment
    public boolean navigateBack() {
        try {
            if (fragmentManager.getBackStackEntryCount() == 1)
                return false;
            else {
                fragmentManager.popBackStack();
                return true;
            }
        } catch (Exception e) {
            logger.error("An error occurred while popping the backstack.", e);
            return false;
        }
    }

    public String getCurrentFragmentName() {
        return fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
    }

    public Fragment getCurrentFragment () {
        try {
            if (fragmentManager.getBackStackEntryCount() == 0)
                return null;

            String name = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getName();
            return getFragmentByClassName(name);
        } catch (Exception e) {
            logger.error("An error occurred getting the current fragment", e);
            return null;
        }
    }

    @Nullable
    public Fragment getPreviousFragment() {
        try {
            if (fragmentManager.getBackStackEntryCount() == 1)
                return null;

            String name = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 2).getName();
            return getFragmentByClassName(name);
        } catch (Exception e) {
            logger.error("An error occurred getting previous fragment.", e);
            return null;
        }
    }

    @Nullable
    public Fragment getFragmentByClassName(String name) {
        return fragmentManager.findFragmentByTag(name);
    }

    public void remove(String tag) {

        try {
            Fragment fragmentByTag = fragmentManager.findFragmentByTag(tag);
            if (fragmentByTag != null) {
                fragmentManager.beginTransaction().remove(fragmentByTag).commitAllowingStateLoss();
            }
        } catch (Exception e) {
            logger.error("An error occurred removing fragment.", e);
        }
    }
}
