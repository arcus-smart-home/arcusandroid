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

/**
 * Model of a state in a deterministic finite automata (state machine). Each state in a machine
 * should extend this class.
 *
 * Upon the machine's entry into a given state, the state's {@link #execute()} method will be fired.
 * From here, the state can request the machine to:
 *
 * 1. Transition to a new state using the {@link #transition(State)} method
 * 2. Terminate with this state as the terminal state, using {@link #terminate()}
 * 3. Retry the current state while retry attempts remain by calling {@link #handleException(StateException)}
 *
 */
public abstract class State {

    public final String name;

    private long msDelayBetweenRetry = 2000;
    private int retriesRemaining = 3;
    private StateMachine executor;

    /**
     * Construct a state with a name.
     * @param name Name of this state (used for logging/debugging)
     */
    public State(String name) {
        this.name = name;
    }

    /**
     * Construct a state with a name and number of retries.
     * @param name Name of this state
     * @param retryCount Number of times this state may be retried before giving up.
     */
    public State(String name, int retryCount) {
        this.retriesRemaining = retryCount;
        this.name = name;
    }

    /**
     * Construct a state with a name, retry count and delay between retries.
     * @param name Name of this state
     * @param retryCount Number of times this state may be retired before giving up.
     * @param msDelayBetweenRetry Number of milliseconds to wait before retrying
     */
    public State(String name, int retryCount, long msDelayBetweenRetry) {
        this.retriesRemaining = retryCount;
        this.name = name;
        this.msDelayBetweenRetry = msDelayBetweenRetry;
    }

    /**
     * Called when the machine enters this state. The machine will remain in this state until
     * this method requests a state change by calling {@link #transition(State)},
     * throwing {@link StateException}, invoking {@link #handleException(StateException)} or
     * calling {@link #terminate()}.
     *
     * @throws StateException Indication that a (synchronous) error occurred in this state.
     */
    public abstract void execute() throws StateException;

    public void handleExceptionWithoutRetry(StateException e) {
        retriesRemaining = 0;
        getExecutor().handleException(e);
    }

    public void handleException(StateException e) {
        getExecutor().handleException(e);
    }

    /**
     * Terminates the state machine with this state being the terminal state.
     */
    public void terminate () {
        getExecutor().execute(null);
    }

    /**
     * Transitions to the given next state.
     * @param nextState
     */
    public void transition(State nextState) {
        getExecutor().execute(nextState);
    }

    /**
     * @return Name of this state
     */
    public String getName() {
        return name;
    }

    /**
     * @return Number of retries remaining before the state "gives up"
     */
    public int getRetriesRemaining() {
        return retriesRemaining;
    }

    /**
     * @return Decrements the retry count and returns true if retries remain; false otherwise.
     */
    boolean retry () {
        return --retriesRemaining > 0;
    }

    /**
     * @return The delay between retry attempts for this state.
     */
    public long getMsDelayBetweenRetry() {
        return msDelayBetweenRetry;
    }

    StateMachine getExecutor() {
        return executor;
    }

    void setExecutor(StateMachine executor) {
        this.executor = executor;
    }

    @Override
    public String toString () {
        return name;
    }
}
