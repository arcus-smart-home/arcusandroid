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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



public abstract class PopupManager {

    private Logger logger = LoggerFactory.getLogger(PopupManager.class);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future pendingPopup = null;
    private Handler delayedHandler = new Handler();

    /**
     * Gets the priority-ordered list of popups managed by this PopupManager.
     *
     * @return
     */
    public abstract List<PopupResponsibility> getPopupResponsibilities();

    /**
     * Display the highest-priority popup currently qualified to be shown; has no effect if no
     * popups are presently qualified, or if a popup (managed by this module) is presently displayed.
     *
     * @return True if one or more popups were qualified to execute; false otherwise
     */
    public void triggerPopups() {

        // Do not attempt to trigger popups if we're already in the process of doing so
        if (pendingPopup == null || pendingPopup.isDone()) {

            pendingPopup = executor.submit(new Runnable() {
                @Override
                public void run() {
                    if (!isExecutionPending()) {

                        for (final PopupResponsibility thisPopup : getPopupResponsibilities()) {

                            if (getFutureBoolean(thisPopup.qualify(), false)) {

                                // Popup requests delayed execution
                                if (thisPopup.executionDelayMs() > 0) {
                                    logger.debug("Popup {} requests delayed execution for {} ms", thisPopup.getClass().getSimpleName(), thisPopup.executionDelayMs());

                                    delayedHandler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            logger.debug("Popup {} is being re-evaluated after delay.", thisPopup.getClass().getSimpleName());

                                            // Make sure we're still qualified to display after delay
                                            if (getFutureBoolean(thisPopup.qualify(), false)) {
                                                logger.debug("Popup {} is being executed.", thisPopup.getClass().getSimpleName());
                                                thisPopup.execute();
                                            } else {
                                                logger.debug("Popup {} has been canceled; was initially qualified for execution, but wasn't after {}ms delay.", thisPopup.getClass().getSimpleName(), thisPopup.executionDelayMs());
                                            }

                                        }
                                    }, thisPopup.executionDelayMs());
                                }

                                // Okay to execute immediately
                                else {
                                    thisPopup.execute();
                                }

                                return;
                            }
                        }
                    }
                }
            });
        }
    }

    private boolean isExecutionPending() {
        for (PopupResponsibility thisPopup : getPopupResponsibilities()) {
            if (thisPopup.isVisible()) {
                return true;
            }
        }
        return false;
    }

    private boolean getFutureBoolean(Future<Boolean> future, boolean dflt) {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            return dflt;
        }
    }

    public <T extends PopupResponsibility> void resetHasFired(Class<T> clazz) {
        for (PopupResponsibility responsibility : getPopupResponsibilities()) {
            if (clazz.isInstance(responsibility)) {
                responsibility.resetHasFired();
            }
        }
    }
}
