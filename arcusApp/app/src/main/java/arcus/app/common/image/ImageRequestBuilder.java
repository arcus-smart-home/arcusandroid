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
package arcus.app.common.image;

import android.content.Context;
import android.os.AsyncTask;


/**
 * An executable image request builder.
 */
public class ImageRequestBuilder extends PicassoRequestBuilder implements ImageRequestExecutor {

    public ImageRequestBuilder(Context context, ViewBackgroundTarget wallpaperTarget, ImageCategory category, Object locationHint) {
        super(context, wallpaperTarget, category, locationHint);
    }

    public void execute () {
        if (category == null) {
            throw new IllegalStateException("Must invoke .putXXX() method before .into()");
        }

        if (locationHint == null) {
            return;
        }

        requestExecutionStartTime = System.currentTimeMillis();
        log("Executing image request on AsyncTask executor: {}", this);

        // Do not use .execute() here... some version of Android use a single threaded executor
        // and under some circumstances the thread can block for 30-60 seconds causing no images
        // to be visible in the app during this period.
        new ImageRequestExecutionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this);
    }
}
