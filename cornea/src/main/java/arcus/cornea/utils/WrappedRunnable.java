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
package arcus.cornea.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WrappedRunnable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(WrappedRunnable.class);

    abstract public void onRun() throws Exception;

    @Override public final void run() {
        try {
            onRun();
        }
        catch (Exception ex) {
            logger.error("Could not process runnable.", ex);
        }
    }
}
