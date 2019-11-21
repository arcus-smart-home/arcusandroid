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
package arcus.app.dashboard.popups;

import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import arcus.cornea.subsystem.DashboardSubsystemController;
import arcus.cornea.subsystem.model.DashboardState;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.dashboard.HomeFragment;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;



public abstract class PopupResponsibility {

    protected final Logger logger = LoggerFactory.getLogger(PopupResponsibility.class);
    private final AtomicBoolean hasFired = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // TODO: Make this value configurable?
    private final long timeoutMs = 5000;

    /**
     * Determines if this popup is "qualified" to be displayed in the current context.
     *
     * Be aware of the following:
     *
     * 1. This method may be called more than once in the process of determining whether or not the
     * popup should be displayed. Implementers should not set a one-shot flag or implement other
     * logic that assumes this method needs to only return 'true' once. If one-shot behavior is
     * desired, set/clear flags in the {@link #showPopup()} method.
     *
     * 2. This method must return synchronously. Subclasses requiring async processing in
     * qualification should override {@link #isAsynchronouslyQualified()} and instead throw
     * IllegalStateException from this method.
     *
     * @return True if the popup should be displayed; false otherwise
     */
    protected abstract boolean isQualified();

    /**
     * Display the popup.
     * @return True if the popup was displayed successfully, false otherwise.
     */
    protected abstract void showPopup();

    /**
     * Determine if the popup represented by this responsibility is currently visible.
     *
     * WARNING: The application has no "reliable" mechanism for determining this. Most subclasses
     * will delegate to {@link BackstackManager} which will, in turn, delegate to {@link FragmentManager}.
     * FragmentManager is "eventually consistent" and will not immediately report that a popup has been shown. You
     * may to manually track visibility of your popups if this is problematic.
     *
     * @return True if visible; false otherwise
     */
    protected abstract boolean isVisible();

    /**
     * Delay to apply before attempting to display popup.
     * @return Delay, in milliseconds
     */
    protected int executionDelayMs() {
        return 0;
    }

    Future<Boolean> qualify() {

        final String responsibilityName = this.getClass().getSimpleName();

        return executor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                logger.debug("{} is being qualified... Giving up in {} ms", responsibilityName, timeoutMs);
                boolean qualified = baseQualify() && isAsynchronouslyQualified().get(timeoutMs, TimeUnit.MILLISECONDS) && (!isOneShot() || !hasFired.get());

                if (qualified) {
                    logger.debug("{} qualified for execution.", responsibilityName);
                } else {
                    logger.debug("{} not qualified for execution. Base qualification: {}. One shot: {}, Has fired: {}", responsibilityName, baseQualify(), isOneShot(), hasFired.get());
                }

                return qualified;
            }
        });
    }

    void execute() {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                logger.debug("Executing responsibility {}", this.getClass().getSimpleName());
                showPopup();
                hasFired.set(true);
            }
        });
    }

    /**
     * Returns a future indicating whether this responsibility is qualified for execution. Subclasses
     * which require asynchronous processing (like making a network call) to determine if they're
     * qualified should override this method and throw an IllegalStateException from within
     * {@link #isQualified()} (which should never be called).
     *
     * @return A future boolean indicating whether the responsibility is qualified.
     */
    public Future<Boolean> isAsynchronouslyQualified() {
        return ConcurrentUtils.constantFuture(isQualified());
    }

    /**
     * When true, this responsibility will execute only once (per manager instance; for one-shot
     * behavior that persists across app launches, use {@link arcus.app.common.utils.PreferenceUtils}).
     * @return True to implement one-shot behavior; false to allow this responsibility to execute
     * multiple times.
     */
    public boolean isOneShot() {
        return true;
    }

    /**
     * A common set of conditions that should be met in order to qualify the responsibility. Override
     * in a subclass to allow a group of responsibilities to inherit a common set of "base" qualifications.
     * @return True if the responsibility is qualified for execution, false otherwise.
     */
    public boolean baseQualify() {
        // No popups whenever dashboard is alerting
        return !isDashboardInAlertingState();
    }

    public <T extends Fragment> boolean isFragmentVisible(Class<T> clazz) {
        Fragment fragment = BackstackManager.getInstance().getCurrentFragment();
        return  (fragment.getClass().isAssignableFrom(clazz)) && fragment.isVisible();
    }

    /**
     * Determines if the dashboard fragment is presently visible on the screen.
     * @return True if visible; false otherwise
     */
    protected boolean isDashboardVisible() {
        return isFragmentVisible(HomeFragment.class);
    }

    protected boolean isDashboardInAlertingState() {
        return DashboardSubsystemController.instance().getDashboardState() == null ||
                !(DashboardSubsystemController.instance().getDashboardState().getState() == DashboardState.State.NORMAL);
    }

    public void resetHasFired() {
        hasFired.set(false);
    }

}
