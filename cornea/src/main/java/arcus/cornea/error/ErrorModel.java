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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorModel {
    private static final Logger logger = LoggerFactory.getLogger(ErrorModel.class);

    private String title;
    private String message;
    private String retryDescription = "Retry";
    private Runnable retry;
    private Throwable cause;

    public ErrorModel() {

    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRetrySupported() {
        return retry != null;
    }

    public String getRetryDescription() {
        return retryDescription;
    }

    public void setRetryDescription(String retryDescription) {
        this.retryDescription = retryDescription;
    }

    public Runnable getRetry() {
        return retry;
    }

    public void setRetry(Runnable retry) {
        this.retry = retry;
    }

    public void retry() {
        Runnable retry = getRetry();
        if(retry == null) {
            logger.warn("Attempting to retry an error that can't be retried");
        }
        else {
            retry.run();
        }
    }

}
