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

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.event.ListenerRegistration;
import arcus.app.ArcusApplication;
import arcus.app.common.utils.PreferenceCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClipDownloadControllerImpl extends ClipDownloadController {
    private static final Logger logger = LoggerFactory.getLogger(ClipDownloadControllerImpl.class);

    private final String DOWNLOAD_CLIP_DOWNLOAD_MGR_ID  = "DOWNLOAD_CLIP_DOWNLOAD_MGR_ID";
    private final String DOWNLOAD_CLIP_RECORDING_ID     = "DOWNLOAD_CLIP_RECORDING_ID";
    private final String DOWNLOAD_CLIP_EXPECTED_SIZE    = "DOWNLOAD_CLIP_EXPECTED_SIZE";
    private final Uri    DOWNLOAD_CONTENT_URI           = Uri.parse("content://downloads/my_downloads");
    private final IntentFilter DOWNLOAD_COMPLETE_FILTER = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

    private Reference<Callback> callbackRef = new WeakReference<>(null);

    public static final ClipDownloadControllerImpl INSTANCE = new ClipDownloadControllerImpl();
    private final Object LOCK = new Object();
    private final AtomicBoolean receiversRegistered = new AtomicBoolean(false);
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ContentObserver contentObserver = new ContentObserver(handler) {
        @Override public void onChange(boolean selfChange) {
            long downloadId = getDownloadID();
            if (downloadId != -1) {
                doGetStatus(downloadId);
            }
        }
    };
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            long downloadId = getDownloadID();
            if (downloadId != -1) {
                doGetStatus(downloadId);
            }
        }
    };

    private ClipDownloadControllerImpl() {
        //no instance
    }

    public static ClipDownloadControllerImpl instance() {
        return INSTANCE;
    }

    @Override public ListenerRegistration setCallback(@Nullable final Callback callback) {
        callbackRef = new WeakReference<>(callback);
        return new ListenerRegistration() {
            @Override public boolean isRegistered() {
                return callbackRef.get() != null;
            }

            @Override public boolean remove() {
                boolean registered = isRegistered();
                removeObservers();
                callbackRef.clear();
                return registered;
            }
        };
    }


    @Override public boolean downloadClip(
          @NonNull String recordingID, @NonNull String fileName, long expectedSize, @NonNull String url, boolean meteredOk
    ) {
        synchronized (LOCK) {
            if (downloadInProgress()) {
                onDownloadError(DOWNLOAD_IN_PROGRESS);
                return false;
            }
            else if (!isStorageAvailable()) {
                onDownloadError(STORAGE_NOT_AVAILABLE);
                return false;
            }
            else if (!hasAvailableStorage(expectedSize)) {
                onDownloadError(INSUFFICIENT_STORAGE_SPACE);
                return false;
            }
            else {
                doDownloadClip(recordingID, fileName, expectedSize, url);
                return true;
            }
        }
    }

    @Override public void cancelCurrentDownload() {
        long downloadId = getDownloadID();
        if (downloadId == -1) {
            return;
        }

        doCancelDownload(downloadId);
    }

    @Override public void getCurrentDownloadStatus() {
        long downloadId = getDownloadID();
        if (downloadId == -1) {
            return;
        }

        reapplyObservers();
        doGetStatus(downloadId);
    }

    protected void doDownloadClip(String recordingID, String fileName, long expectedSize, String url) {
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                  .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                  .setAllowedOverMetered(true)
                  .setVisibleInDownloadsUi(true);
            request.allowScanningByMediaScanner();

            final long downloadID = getDownloadManager().enqueue(request);
            putDownloadID(downloadID);
            putRecordingID(recordingID);
            putRecordingExpectedSize(expectedSize);
            reapplyObservers();
        }
        catch (Exception ex) {
            removeCurrentDownloadReferences();
            onDownloadFatalError(ex);
        }
    }

    protected void doGetStatus(long downloadId) {
        try {
            switch (getDownloadManagerStatus(downloadId)) {
                case DownloadManager.STATUS_FAILED:
                    onDownloadComplete(getRecordingID(), STATUS_DOWNLOAD_CANCELED);
                    removeCurrentDownloadReferences();
                    break;

                case DownloadManager.STATUS_SUCCESSFUL:
                    onDownloadComplete(getRecordingID(), STATUS_DOWNLOAD_COMPLETE);
                    removeCurrentDownloadReferences();
                    break;

                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_RUNNING:
                    int progress = doGetDownloadProgress();
                    if (progress == 100) {
                        onDownloadComplete(getRecordingID(), STATUS_DOWNLOAD_COMPLETE);
                        removeCurrentDownloadReferences();
                    }
                    else {
                        onDownloadProgressChanged(progress, STATUS_DOWNLOAD_RUNNING);
                    }
                    break;
            }
        }
        catch (Exception ex) {
            removeCurrentDownloadReferences();
            onDownloadFatalError(ex);
        }
    }

    protected void doCancelDownload(long downloadId) {
        int downloadsRemoved = getDownloadManager().remove(downloadId);
        logger.debug("Removed [{}] downloads from the download manager.", downloadsRemoved);

        onDownloadComplete(getRecordingID(), STATUS_DOWNLOAD_CANCELED);
        removeCurrentDownloadReferences();
    }

    protected int doGetDownloadProgress() {
        long downloadId = getDownloadID();
        long toDownload = getRecordingExpectedSize();
        if (downloadId == -1 || toDownload < 1) {
            return 0;
        }

        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = getDownloadManager().query(query);
        try {
            if (cursor != null && cursor.moveToNext()) {
                int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                logger.debug("Got [{}] bytes for [{}] with a total of [{}]", bytesDownloaded, downloadId, toDownload);
                Number total = (bytesDownloaded * 100L) / toDownload;
                return total.intValue();
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return 0;
    }

    protected void onDownloadError(@DownloadErrorStatus int status) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            try {
                callback.onDownloadError(status);
            }
            catch (Exception ex) {
                logger.error("Could not dispatch callback for download error.", ex);
            }
        }
    }

    protected void onDownloadComplete(@Nullable String recordingID, @DownloadCompleteStatus int status) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            try {
                callback.onDownloadComplete(recordingID, status);
            }
            catch (Exception ex) {
                logger.error("Could not dispatch callback for download status.", ex);
            }
        }
    }

    protected void onDownloadProgressChanged(int newProgress, @DownloadProgressStatus int status) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            try {
                callback.onDownloadProgressChanged(getRecordingID(), newProgress, status);
            }
            catch (Exception ex) {
                logger.error("Could not dispatch callback for download status.", ex);
            }
        }
    }

    protected void onDownloadFatalError(final Throwable throwable) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            try {
                callback.onDownloadFatalError(throwable);
            }
            catch (Exception ex) {
                logger.error("Could not dispatch fatal error about a download.", ex);
            }
        }
    }

    protected boolean downloadInProgress() {
        long downloadId = getDownloadID();
        if (downloadId == -1) {
            return false;
        }

        try {
            switch (getDownloadManagerStatus(downloadId)) {
                case DownloadManager.STATUS_FAILED:
                    removeCurrentDownloadReferences();
                    return false;

                case DownloadManager.STATUS_SUCCESSFUL:
                    removeCurrentDownloadReferences();
                    return false;

                case DownloadManager.STATUS_PENDING:
                case DownloadManager.STATUS_PAUSED:
                case DownloadManager.STATUS_RUNNING:
                    return true;

                default:
                case -1:
                    return false;
            }
        }
        catch (Exception ex) {
            logger.error("Could not process download ID [{}]", downloadId, ex);
        }

        return false;
    }

    protected int getDownloadManagerStatus(long downloadId) {
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = getDownloadManager().query(query);
        try {
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
            }
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return -1;
    }

    protected void removeCurrentDownloadReferences() {
        PreferenceCache.getInstance().removeKey(DOWNLOAD_CLIP_DOWNLOAD_MGR_ID);
        PreferenceCache.getInstance().removeKey(DOWNLOAD_CLIP_RECORDING_ID);
        PreferenceCache.getInstance().removeKey(DOWNLOAD_CLIP_EXPECTED_SIZE);
        removeObservers();
    }

    protected @Nullable String getRecordingID() {
        return PreferenceCache.getInstance().getString(DOWNLOAD_CLIP_RECORDING_ID, null);
    }

    protected long getRecordingExpectedSize() {
        return PreferenceCache.getInstance().getLong(DOWNLOAD_CLIP_EXPECTED_SIZE, -1);
    }

    protected long getDownloadID() {
        return PreferenceCache.getInstance().getLong(DOWNLOAD_CLIP_DOWNLOAD_MGR_ID, -1);
    }

    protected void putRecordingID(String recordingID) {
        PreferenceCache.getInstance().putString(DOWNLOAD_CLIP_RECORDING_ID, recordingID);
    }

    protected void putRecordingExpectedSize(long value) {
        PreferenceCache.getInstance().getLong(DOWNLOAD_CLIP_EXPECTED_SIZE, value);
    }

    protected void putDownloadID(long value) {
        PreferenceCache.getInstance().putLong(DOWNLOAD_CLIP_DOWNLOAD_MGR_ID, value);
    }

    protected @NonNull File getContentFile() {
        File external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!external.exists()) {
            external.mkdir();
        }

        return external;
    }

    protected boolean isStorageAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    protected boolean hasAvailableStorage(long expectedSize) {
        File content = getContentFile();
        double totalSpace = (double) content.getTotalSpace();
        double freeSpace  = (double) content.getFreeSpace();
        double percentFull = 100 - ((freeSpace * 100) / totalSpace);
        double afterSavedSize = (totalSpace - freeSpace) + expectedSize;
        logger.debug("Reporting -> %[{}] Full (download size of [{}] bytes to be added)", percentFull, expectedSize);

        // See: https://developer.android.com/training/basics/data-storage/files.html#GetFreeSpace
        // If the number returned is a few MB more than the size of the data you want to save, or if the file system is
        // less than 90% full, then it's probably safe to proceed. Otherwise, you probably shouldn't write to storage.
        return afterSavedSize < totalSpace && percentFull <= 90D;
    }

    protected void removeObservers() {
        removeContentObserver();

        if (receiversRegistered.getAndSet(false)) {
            ArcusApplication.getContext().unregisterReceiver(receiver);
        }
    }

    protected void reapplyObservers() {
        removeObservers();
        registerContentObserver();

        if (!receiversRegistered.getAndSet(true)) {
            ArcusApplication.getContext().registerReceiver(receiver, DOWNLOAD_COMPLETE_FILTER);
        }
    }

    protected void removeContentObserver() {
        ArcusApplication.getContext().getContentResolver().unregisterContentObserver(contentObserver);
    }

    protected void registerContentObserver() {
        ArcusApplication.getContext().getContentResolver().registerContentObserver(DOWNLOAD_CONTENT_URI, true, contentObserver);
    }

    protected @NonNull DownloadManager getDownloadManager() {
        return (DownloadManager) ArcusApplication.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
    }
}
