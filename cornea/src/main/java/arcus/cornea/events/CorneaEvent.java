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
package arcus.cornea.events;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

public abstract class CorneaEvent {
    private static final String EMPTY_MESSAGE = "Message was empty.";
    private final String code;
    private final String message;
    private Throwable cause;

    CorneaEvent(@NonNull String code) {
        this(code, EMPTY_MESSAGE);
    }

    CorneaEvent(@NonNull String code, @NonNull String message) {
        Preconditions.checkNotNull(code, "Code cannot be null");
        Preconditions.checkNotNull(message, "Message cannot be null");

        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Nullable
    public Throwable getCause() {
        return this.cause;
    }

    @Override
    public String toString() {
        return "CorneaEvent{" +
              "code='" + code + '\'' +
              ", message='" + message + '\'' +
              ", cause=" + cause +
              '}';
    }
}
