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

public class Errors {
    private Errors() {
    }

    public interface Hub {
        String MISSING_ARGUMENT = "error.register.missingargument";
        String ALREADY_REGISTERED = "error.register.alreadyregistered";
        String NOT_FOUND = "error.register.hubnotfound";
        String SERVER_ERROR = "server.error";
        String FWUPGRADE_FAILED = "error.fwupgrade.failed";
        String INSTALL_TIMEDOUT = "error.install.timed.out";
        String ORPHANED_HUB = "error.register.orphanedhub";

    }

    public interface Process {
        String NULL_MODEL_FOUND = "null.models";
        String CANCELLED = "user.cancelled";
        String RETRIES_EXCEEDED = "retries.exceeded";
        String DEBUG_MESSAGE = "debug.message";
    }
}
