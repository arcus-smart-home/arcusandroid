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
package arcus.app.common.machine;

import android.os.Handler;
import android.os.Looper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A deterministic finite automata ("state machine") that supports per-state retries and
 * asynchronous transition paths.
 * <p/>
 * Each state is modeled as an instance of {@link State}; see this class for additional information
 * about modeling states.
 */
public class StateMachine {

    private static final Logger logger = LoggerFactory.getLogger(StateMachine.class);

    /**
     * A listener interface for clients interesting in being informed of state changes.
     */
    public interface StateMachineObserver {
        void onStateChanged(State lastState, State currentState);
        void onTerminated(State terminalState);
    }

    private final AtomicBoolean halted = new AtomicBoolean(false);
    private final State startState;

    private State currentState;
    private StateMachineObserver observer;
    private StateException lastException;

    /**
     * Constructs a state machine with the given start state.
     *
     * @param startState
     */
    public StateMachine(State startState) {
        this.startState = startState;
    }

    public void setObserver(StateMachineObserver observer) {
        this.observer = observer;
    }

    /**
     * Starts executing the state machine from the start state.
     */
    public void run() {
        execute(startState);
    }

    /**
     * Executes the given state. Package private to prevent clients from accessing this method
     * directly; typically only the state class should invoke this.
     *
     * @param state The state to be executed; null to indicate the machine has reached a terminal
     *              state.
     */
    void execute(final State state) {
        if (!halted.get()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (state == null) {
                        logger.debug("Terminal state {} reached. Machine has stopped.", currentState);
                        fireOnTerminated(currentState);
                        return;
                    }

                    logger.debug("Transitioning from state {} to {}", currentState, state);

                    fireOnStateChanged(currentState, state);
                    currentState = state;
                    currentState.setExecutor(StateMachine.this);

                    try {
                        state.execute();
                    } catch (StateException e) {
                        handleException(e);
                    }
                    }
            });
        }
    }

    /**
     * Handles a StateException case by retrying the current state as it is configured to do so and
     * transitioning to the exception's defined failed state if the retry count has been exceeded.
     *
     * @param e The exception to handle
     */
    void handleException(StateException e) {
        if (halted.get()) {
            logger.error("State machine received request to handleException {} after it has halted.", e.getMessage());
        }

        else {
            logger.debug("State {} failed with error: {}.", currentState, e.getMessage());
            this.lastException = e;

            if (currentState.retry()) {
                logger.debug("Waiting {}ms before retrying state {}. {} retries remaining.", currentState.getMsDelayBetweenRetry(), currentState, currentState.getRetriesRemaining());

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        execute(currentState);
                    }
                }, currentState.getMsDelayBetweenRetry());

            } else {
                execute(e.getFailState());
            }
        }
    }

    /**
     * Returns the last {@link StateException} encountered by this machine or null if no exceptions
     * have been handled. Useful for determining the cause of a transition to a terminal error
     * state.
     *
     * @return The last encountered {@link StateException} or null, if none has been encountered.
     */
    public StateException getLastException() {
        return this.lastException;
    }

    private void fireOnStateChanged(final State lastState, final State currentState) {
        if (observer != null) {
            observer.onStateChanged(lastState, currentState);
        }
    }

    private void fireOnTerminated(final State terminalState) {
        halted.set(true);

        if (observer != null) {
            observer.onTerminated(terminalState);
        }
    }
}
