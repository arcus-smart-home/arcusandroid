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
package arcus.app.subsystems.camera.controllers;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.event.ListenerRegistration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class ClipDownloadController {
    public static final int DOWNLOAD_IN_PROGRESS = 0x01;
    public static final int ON_METERED_NETWORK = 0x02;
    public static final int INSUFFICIENT_STORAGE_SPACE = 0x03;
    public static final int STORAGE_NOT_AVAILABLE = 0x04;

    public static final int STATUS_DOWNLOAD_RUNNING = 0x05;
    public static final int STATUS_DOWNLOAD_CANCELED = 0x06;
    public static final int STATUS_DOWNLOAD_COMPLETE = 0x07;
    public static final int STATUS_SIZE_UNKNOWN = 0x08;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
          DOWNLOAD_IN_PROGRESS,
          ON_METERED_NETWORK,
          INSUFFICIENT_STORAGE_SPACE,
          STORAGE_NOT_AVAILABLE
    })
    public @interface DownloadErrorStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
          STATUS_DOWNLOAD_RUNNING,
          STATUS_DOWNLOAD_CANCELED,
          STATUS_DOWNLOAD_COMPLETE,
          STATUS_SIZE_UNKNOWN
    })
    public @interface DownloadProgressStatus {
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
          STATUS_DOWNLOAD_CANCELED,
          STATUS_DOWNLOAD_COMPLETE
    })
    public @interface DownloadCompleteStatus {
    }

    public interface Callback {
        /**
         * Called if there is an error outside of a normal failed http request, this is typically not recoverable.
         *
         * @param throwable
         */
        void onDownloadFatalError(Throwable throwable);

        /**
         * Called when there is an error locally that can typically be recoverable by some user interaction.
         *
         * @param errorType
         */
        void onDownloadError(@DownloadErrorStatus int errorType);

        /**
         * Called when there is information on the status of a download.  This is called when a download is in progress
         * and also as a result of manually calling getCurrentDownloadStatus()
         *
         * @param recordingID
         * @param newProgress
         * @param status
         */
        void onDownloadProgressChanged(
              @Nullable String recordingID,
              int newProgress,
              @DownloadProgressStatus int status
        );

        /**
         * Called when a download is complete with the associated status.
         *
         * @param recordingID
         * @param status
         */
        void onDownloadComplete(@Nullable String recordingID, @DownloadCompleteStatus int status);
    }

    /**
     * Attempts to download a clip. Checks to see if:
     * - Already downloading a clip.
     * - Have enough free space
     * - Is on a metered (3G, 4G, etc) connection & meteredOK is not true
     *
     * - - Any one of the above will produce an error to {@linkplain Callback#onDownloadError(int)} if they are true
     * - - If they are not true, a download will be attempted
     *
     * @param recordingID
     * @param recordingNotificationTitle
     * @param expectedSize
     * @param url
     * @param meteredOk
     * @return True if the request to download succeeds; false if a condition exists that prevents
     * the download from occurring (i.e., concurrent download, not enough space, etc.)
     */
    public abstract boolean downloadClip(
          @NonNull String recordingID,
          @NonNull String recordingNotificationTitle,
          long expectedSize,
          @NonNull String url,
          boolean meteredOk
    );

    /**
     * Cancels the current download in progress
     */
    public abstract void cancelCurrentDownload();

    /**
     *
     * Manually used to get the current download status
     * - Should invoke this after registering a new callback via {@linkplain #setCallback(Callback)}
     *
     */
    public abstract void getCurrentDownloadStatus();

    /**
     * Sets the callback to emit status/errors on.
     *
     * @param callback
     * @return
     */
    public abstract ListenerRegistration setCallback(@Nullable Callback callback);
}
