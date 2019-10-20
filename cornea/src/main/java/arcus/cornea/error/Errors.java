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
package arcus.cornea.error;

import com.iris.client.exception.ConnectionException;

public class Errors {
    private static final String TXT_TITLE_BAD_CONNECTION = "BAD CONNECTION";
    private static final String TXT_MESSAGE_BAD_CONNECTION = "Unable to reach platform, please check your internet connection and try again.";
    private static final String TXT_TITLE_GENERIC = "OOPS";
    private static final String TXT_MESSAGE_GENERIC = "Sorry, didn't quite get that, please try again later.";


    public static ErrorModel translate(Throwable cause) {
        ErrorModel error = new ErrorModel();
        error.setCause(cause);

        if(cause instanceof ConnectionException) {
            error.setTitle(TXT_TITLE_BAD_CONNECTION);
            error.setMessage(TXT_MESSAGE_BAD_CONNECTION);
        }
        // TODO handle the other client exceptions
        else {
            error.setTitle(TXT_TITLE_GENERIC);
            error.setMessage(TXT_MESSAGE_GENERIC);
        }
        return error;
    }

    public static ErrorModel translateWithRetry(Throwable cause, String retryDescription, Runnable retry) {
        ErrorModel error = translate(cause);
        error.setRetry(retry);
        return error;
    }

}
