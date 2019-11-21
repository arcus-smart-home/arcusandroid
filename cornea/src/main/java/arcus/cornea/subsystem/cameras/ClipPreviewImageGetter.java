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
package arcus.cornea.subsystem.cameras;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.common.base.Strings;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.PagedRecordingModelProvider;
import arcus.cornea.utils.Listeners;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.iris.client.capability.Recording;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.RecordingModel;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClipPreviewImageGetter {
    public interface Callback {
        // In the event watching clips page as these load.
        void addedImageToCache(String recordingID);
    }

    private static final Logger logger = LoggerFactory.getLogger(ClipPreviewImageGetter.class);
    private static final String IMAGES_DIRECTORY = "RIM";
    private static final int REQUEST_TIMEOUT = 30_000;
    private static final ClipPreviewImageGetter INSTANCE;
    static {
        INSTANCE = new ClipPreviewImageGetter();
        INSTANCE.init();
    }

    private DiskCache diskCache;
    private WeakReference<Callback> callbackRef = new WeakReference<>(null);
    private final AtomicBoolean cacheInited = new AtomicBoolean(false);
    private final Listener<List<RecordingModel>> storeLoadedListener = new Listener<List<RecordingModel>>() {
        @Override
        public void onEvent(List<RecordingModel> recordingModels) {
            initCache();
            PagedRecordingModelProvider.instance().getStore().addListener(ModelAddedEvent.class, modelAddedEventListener);
        }
    };

    private final Listener<ModelAddedEvent> modelAddedEventListener = new Listener<ModelAddedEvent>() {
        @Override
        public void onEvent(ModelAddedEvent mae) {
            updateImages((RecordingModel) mae.getModel());
        }
    };
    private final OkHttpClient okHttpClient;

    public ClipPreviewImageGetter() {
        this.okHttpClient = new OkHttpClient
                .Builder()
                .dispatcher(new Dispatcher())
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    public static ClipPreviewImageGetter instance() {
        return INSTANCE;
    }

    public void setContext(Context context, String placeID) {
        if (this.diskCache != null) {
            logger.trace("Replacing diskCache location with [{}]", context);
        }

        this.diskCache = new DiskCache(context, IMAGES_DIRECTORY + placeID);
    }

    public ListenerRegistration setCallback(Callback callback) {
        if (callbackRef.get() != null) {
            logger.trace("Replacing existing callback ref");
        }

        callbackRef = new WeakReference<>(callback);
        return Listeners.wrap(callbackRef);
    }

    protected void init() {
        PagedRecordingModelProvider.instance().addStoreLoadListener(storeLoadedListener);
        PagedRecordingModelProvider.instance().load();
        CorneaClientFactory.getClient().addSessionListener(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent sessionEvent) {
                if (sessionEvent instanceof SessionActivePlaceSetEvent) {
                    cacheInited.set(false);
                }
            }
        });
    }

    protected void initCache() {
        if (!shouldInitCache()) {
            return;
        }

        for (RecordingModel model : PagedRecordingModelProvider.instance().getStore().values()) {
            if (shouldDelete(model)) {
                deleteFromCache(model.getId());
            }
            else {
                downloadNewImages(model);
            }
        }
    }

    /**
     * Clears disk cache. This setting is exposed in the Debug Menu and is intended to be used for testing.
     *
     * @return an int array where [0] = total files deleted and [1] = total number of files tried to delete, but could not
     * Check logcat for the delete failure reason(s)
     *
     */
    @VisibleForTesting public int[] clearDiskCache() {
        if (diskCache == null) {
            logger.error("Tried to clear disk cache, but it was null");
            return new int[]{-1, -1}; // To help identify error conditions.
        }

        return diskCache.clearCache();
    }

    protected void deleteFromCache(String fileName) {
        logger.trace("Attempting to delete ({}). No longer referenced?", fileName);
        diskCache.delete(fileName);
    }

    protected boolean shouldInitCache() {
        if (diskCache == null) {
            logger.trace("Cannot load/save cache - diskCache is not loaded. Did you set the context?");
            return false;
        }
        else if (cacheInited.getAndSet(true)) {
            logger.trace("Cache already setup initialized for this place.");
            return false;
        }

        return true;
    }

    protected boolean shouldDownload(RecordingModel model) {
        if (model == null) {
            return false;
        }

        // Is deleted or NOT a recording.
        if (model.getDeleted() || !RecordingModel.TYPE_RECORDING.equals(model.get(Recording.ATTR_TYPE))) {
            return false;
        }

        // If it's not cached, download it.
        return !isCached(model.getId());
    }

    protected boolean shouldDelete(RecordingModel model) {
        if (model == null) {
            return false;
        }

        // If it's marked deleted or it's a stream image - if we've cached it; delete it.
        return (model.getDeleted() || RecordingModel.TYPE_STREAM.equals(model.get(Recording.ATTR_TYPE))) && isCached(model.getId());
    }

    protected boolean isCached(String fileName) {
        if (Strings.isNullOrEmpty(fileName)) {
            logger.trace("Did you really mean to check to see if an empty string was cached?");
            return true;
        }

        List<String> filesSaved = Arrays.asList(diskCache.listFileNames());
        return filesSaved.contains(fileName);
    }

    protected void downloadNewImages(final RecordingModel recordingModel) {
        if (diskCache == null || !shouldDownload(recordingModel)) {
            return;
        }

        logger.debug("Decided we needed to download [{}] getting URL.", recordingModel.getId());
        recordingModel.view()
              .onSuccess(new Listener<Recording.ViewResponse>() {
                  @Override
                  public void onEvent(Recording.ViewResponse viewResponse) {
                      downloadClipImage(recordingModel.getId(), viewResponse.getPreview());
                  }
              });
    }

    protected void updateImages(RecordingModel model) {
        if (diskCache == null) {
            return;
        }

        if (shouldDelete(model)) {
            deleteFromCache(model.getId());
        }
        else {
            downloadNewImages(model);
        }
    }

    public File getClipForID(String recordingID) {
        if (Strings.isNullOrEmpty(recordingID) || diskCache == null) {
            return null;
        }

        return diskCache.getExistingFileRef(recordingID);
    }

    private void downloadClipImage(String recordingID, String previewURL) {
        if (diskCache == null) {
            return;
        }

        Request request = new Request.Builder().url(previewURL).get().build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new OkHttpCallback(recordingID, previewURL));
    }

    private class OkHttpCallback implements okhttp3.Callback {
        private final String recordingID;
        private final String previewURL;

        public OkHttpCallback(String recID, String prevURL) {
            recordingID = recID;
            previewURL = prevURL;
            logger.trace("Scheduling [{}]", recID);
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            logger.error("--FAILED-- Trying to download a clip image. [{}]", call.request(), e);
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            logger.trace("Response For [{}]", recordingID);
            try {
                Bitmap bmd = BitmapFactory.decodeStream(response.body().byteStream());
                if (bmd != null) {
                    if (diskCache.saveImage(Bitmap.createScaledBitmap(bmd, 370, 370, false), recordingID)) {
                        notifyCallback();
                        logger.trace("Saved [{}]", recordingID);
                    }
                }
            }
            finally {
                response.body().close();
            }
        }

        private void notifyCallback() {
            try {
                Callback callback = callbackRef.get();
                if (callback != null) {
                    callback.addedImageToCache(recordingID);
                    logger.trace("Notifying callback of clip image download for [{}]", recordingID);
                }
            }
            catch (Exception ex) {
                logger.debug("Error notifying callback.", ex);
            }
        }
    }
}
