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
package arcus.app.common.utils;

import android.os.Handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A utility class for throttling the execution of a repeating task so that it its frequency of
 * execution does not exceed a provided period.
 */
public class ThrottledExecutor {

    private static final Logger logger = LoggerFactory.getLogger(ThrottledExecutor.class);

    private final Handler quiescenceHandler = new Handler();
    private final Handler handler = new Handler();

    private Runnable quiescenceTask;
    private Runnable throttleTask;
    private long quiescenceDelay;
    private final int throttlePeriodMs;
    private long lastExecution = 0;

    public ThrottledExecutor(int throttlePeriodMs) {
        this.throttlePeriodMs = throttlePeriodMs;
    }

    /**
     * Delays the execution of the last-enqueued throttle task a duration equal to the throttle
     * period. Equivalent to calling {@link #executeDelayed(Runnable)} with the last Runnable
     * provided to it.
     */
    public void delayExecution() {
        if (throttleTask != null) {
            executeDelayed(throttleTask);
        }
    }

    /**
     * Executes the given task no more frequently than the throttle period allows for, but assures
     * that the next execution is delayed by the throttle period.
     *
     * @param task The task to execute; replaces a previous task passed to this method or
     * {@link #execute(Runnable)} if a task is pending.
     */
    public void executeDelayed(final Runnable task) {
        lastExecution = System.currentTimeMillis();
        execute(task);
    }

    /**
     * Executes the given task after this object's throttle period has been reached. This enables
     * the caller to repeatedly invoke this method (typically with the same task object), but
     * assure that the given task executes no more than once per throttle period, regardless of how
     * frequently this method is invoked.
     *
     * If this is the first time this method is being called since creating the object, then the
     * given task will execute immediately.
     *
     * If this method has been called previously, but the previous invocation occurred within the
     * throttle period, then the previously provided task will be discarded and the given task will
     * execute at the same time the previous task would have. That is, calling this method does not
     * simply "reset" the delay before execution.
     *
     * @param task The task to execute at the next throttle period boundary.
     */
    public void execute (Runnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        this.throttleTask = task;

        final long msSinceLastExecution = System.currentTimeMillis() - lastExecution;
        final long msUntilNextExecution = throttlePeriodMs - msSinceLastExecution;

        if (msUntilNextExecution <= 0) {
            handler.removeCallbacksAndMessages(null);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    logger.trace("Executing task {} immediately.");
                    lastExecution = System.currentTimeMillis();
                    fireQuiescenceDelay();
                    try {
                        throttleTask.run();
                    } catch (Exception e) {
                        logger.error("Caught exception while immediately executing throttled task.", e);
                    }
                }
            });
        } else {
            handler.removeCallbacksAndMessages(null);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    logger.trace("Executing task now; was delayed {} ms", msSinceLastExecution);
                    lastExecution = System.currentTimeMillis();
                    fireQuiescenceDelay();
                    try {
                        throttleTask.run();
                    } catch (Exception e) {
                        logger.error("Caught exception while delayed-executing throttled task.", e);
                    }
                }
            }, msUntilNextExecution);
        }
    }

    /**
     * Executes the given task after the executor has been quiescent for the requested duration.
     * This allows the caller to specify some action to occur only after the action specified
     * in {@link #execute(Runnable)} has completed and no further calls to {@link #execute(Runnable)}
     * have been made within the period defined by quiescenceMs.
     *
     * This enables the caller to define some routine to fire after the primary, repetitive task
     * has completed.
     *
     * If the primary task (that which executes via {@link #execute(Runnable)}) has not fired within
     * the provided delay, then the given task will fire immediately.
     *
     * Note that the task provided to this method is *not* throttled. That is, the given task will
     * execute each time this method is called (regardless of the frequency it is invoked), provided
     * the executor meets the specified quiescence.
     *
     * @param task The task to execute once quiescent.
     * @param quiescenceMs The time (in milliseconds) that must elapse after the last execution of
     *                     the task provided to {@link #execute(Runnable)} before the system is
     *                     said to be quiescent.
     */
    public void executeAfterQuiescence(final Runnable task, int quiescenceMs) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null.");
        }

        this.quiescenceTask = task;
        this.quiescenceDelay = quiescenceMs;

        fireQuiescenceDelay();
    }

    public int getThrottlePeriodMs() {
        return throttlePeriodMs;
    }

    private void fireQuiescenceDelay() {
        if (quiescenceTask != null) {
            final long msSinceLastExecution = System.currentTimeMillis() - lastExecution;
            final long msUntilQuiescence = quiescenceDelay - msSinceLastExecution;

            if (msUntilQuiescence <= 0) {
                quiescenceHandler.removeCallbacksAndMessages(null);
                quiescenceHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        logger.trace("System is quiescent; executing quiescence task now.");
                        try {
                            quiescenceTask.run();
                        } catch (Exception e) {
                            logger.error("Caught exception while executing quiescent task.", e);
                        }
                    }
                });
            }

            else {
                quiescenceHandler.removeCallbacksAndMessages(null);
                quiescenceHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        logger.trace("Quiescence reached; executing quiescence task now.");
                        try {
                            quiescenceTask.run();
                        } catch (Exception e) {
                            logger.error("Caught exception while executing quiescent task.", e);
                        }
                    }
                }, msUntilQuiescence);
            }
        }
    }
}
