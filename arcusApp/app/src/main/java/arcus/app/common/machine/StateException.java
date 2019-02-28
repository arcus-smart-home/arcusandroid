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
 * Represents an error condition occuring within a State, thereby informing the state machine to
 * retry the current state in accordance with its retry policy. If no retries remain in the
 * current state, then the machine will transition to the fail state indicated in this exception.
 */
public class StateException extends Exception {

    private final State failState;

    public StateException(State failState) {
        super();
        this.failState = failState;
    }

    public StateException(State failState, String reason) {
        super(reason);
        this.failState = failState;
    }

    public StateException(State failState, Throwable reason) {
        super(reason);
        this.failState = failState;
    }

    public State getFailState() {
        return failState;
    }
}
