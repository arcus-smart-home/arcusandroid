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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.ModelSource;
import okhttp3.Call;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.iris.client.IrisClient;
import com.iris.client.capability.CamerasSubsystem;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraPreviewGetter extends BaseSubsystemController<CameraPreviewGetter.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(CameraPreviewGetter.class);
    private static final String IMAGES_DIRECTORY = "PIM";
    private static final String AUTHORIZATION = "Authorization";
    private static final String PREVIEW = "/preview/";
    private static final String URL_FORMAT = "%s%s%s/%s";
    private static final int REFRESH_TIME_SECONDS = 15;
    private static final int REQUEST_TIMEOUT = 30_000;

    private static final CameraPreviewGetter INSTANCE;
    private final OkHttpClient okHttpClient;
    private final IrisClient irisClient;
    private final AtomicBoolean shouldPoll = new AtomicBoolean(false);
    private final AddressableListSource<DeviceModel> cameras;
    private final Map<String, WeakReference<Callback>> callbackRefs;
    private DiskCache diskCache;

    // Callbacks will be emitted on main thread (below) while work will be sent to bg thread from OkHttp
    private final Handler ioHandler = new Handler(Looper.getMainLooper());

    public interface Callback {
        void imageUpdated();
    }

    static {
        INSTANCE = new CameraPreviewGetter(
              CorneaClientFactory.getClient(),
              SubsystemController.instance().getSubsystemModel(CamerasSubsystem.NAMESPACE),
              DeviceModelProvider.instance().newModelList()
        );
        INSTANCE.init();
    }

    protected CameraPreviewGetter(
          IrisClient client,
          ModelSource<SubsystemModel> subsystem,
          AddressableListSource<DeviceModel> cameras) {
        super(subsystem);

        Preconditions.checkNotNull(client);
        this.irisClient = client;
        this.cameras = cameras;
        this.callbackRefs = new HashMap<>();
        this.okHttpClient = new OkHttpClient
                .Builder()
                .dispatcher(new Dispatcher())
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS)
                .build();
    }

    public static CameraPreviewGetter instance() {
        return INSTANCE;
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        CamerasSubsystem cs = (CamerasSubsystem) getModel();
        cameras.setAddresses(Lists.newArrayList(cs.getCameras()));
        start();
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(changes.contains(CamerasSubsystem.ATTR_CAMERAS)) {
            CamerasSubsystem cs = (CamerasSubsystem) getModel();
            cameras.setAddresses(Lists.newArrayList(cs.getCameras()));
        }
        start();
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        stop();
        callbackRefs.clear();
    }

    public void addCallback(String deviceID, Callback callback) {
        if (callbackRefs.get(deviceID) != null) {
            logger.trace("Replacing callback for [{}]", deviceID);
        }

        callbackRefs.put(deviceID, new WeakReference<>(callback));
    }

    public void clearCallbacks(String deviceId) {
        try {
            callbackRefs.remove(deviceId);
        }
        catch (Exception ex) {
            logger.error("Caught ex trying to remove callback.", ex);
        }
    }

    @Nullable
    public File getForID(String deviceID) {
        if (Strings.isNullOrEmpty(deviceID) || diskCache == null) {
            return null;
        }

        return diskCache.getExistingFileRef(deviceID);
    }

    public void pauseUpdates() {
        stop();
    }

    public void resumeUpdates() {
        start();
    }

    public void setContext(Context context) {
        if (CorneaClientFactory.isConnected() && irisClient.getActivePlace() != null) {
            if (this.diskCache != null) {
                logger.trace("Updating cache molecules to use Context [{}]", context);
            }

            this.diskCache = new DiskCache(context, IMAGES_DIRECTORY + irisClient.getActivePlace().toString());
        }
        else {
            logger.warn("Client is not connected/Active place was null, Context was not set.");
        }
    }

    private void start() {
        if (!shouldPoll.getAndSet(true)) {
            fetchImages();
        }
    }

    private void stop() {
        shouldPoll.set(false);
        ioHandler.removeCallbacksAndMessages(null);
    }

    private void postNewTask() {
        postNewTask(getNormalRetryTime());
    }

    private void postNewTask(int returnIn) {
        if (!shouldPoll.get()) {
            logger.info("Stopping poll - logged out or watching video?");
            return;
        }

        ioHandler.postDelayed(new Runnable() {
            @Override public void run() {
                fetchImages();
            }
        }, returnIn);
    }

    private void fetchImages() {
        if (!isLoaded() || !cameras.isLoaded()) {
            cameras.load();
            postNewTask();
            logger.debug("Camera models not loaded, called load() and retrying");
            return;
        }

        int queueingDevice = 0;
        int deviceCount = cameras.get().size();
        if (deviceCount == 0) {
            shouldPoll.set(false);
            return;
        }

        int running = okHttpClient.dispatcher().runningCallsCount();
        int active  = okHttpClient.dispatcher().queuedCallsCount();
        if (running >= deviceCount || active >= deviceCount) {
            postNewTask(getBackoffTime()); // We've started to run-away; Backoff a little bit.
            return;
        }

        for (DeviceModel devID : cameras.get()) {
            String deviceID = devID.getId();
            if (!CorneaClientFactory.isConnected() || diskCache == null) {
                shouldPoll.set(false);
                return;
            }

            try {
                String url = String.format(URL_FORMAT,
                      irisClient.getSessionInfo().getPreviewBaseUrl(), PREVIEW,
                      irisClient.getActivePlace().toString(), deviceID);
                Request request = new Request.Builder()
                      .url(url)
                      .get()
                      .addHeader(AUTHORIZATION, irisClient.getSessionInfo().getSessionToken())
                      .build();
                OkHttpCallback downloadCB = new OkHttpCallback(++queueingDevice == deviceCount, deviceID);
                okHttpClient.newCall(request).enqueue(downloadCB);
            }
            catch (Exception ex) {
                // If we barf on the last item being scheduled, submit another task to come back and update using backoff.
                if (queueingDevice == deviceCount) {
                    postNewTask(getBackoffTime());
                }
                logger.debug("Cannot download clip images for [{}]", deviceID, ex);
            }
        }
    }

    private int getBackoffTime() {
        return getNormalRetryTime() * 2;
    }

    private int getNormalRetryTime() {
        return REFRESH_TIME_SECONDS * 1000;
    }

    private class OkHttpCallback implements okhttp3.Callback {
        private final boolean isLast;
        private final String id;

        public OkHttpCallback(boolean isLastItem, String deviceID) {
            isLast = isLastItem;
            this.id = deviceID;
        }

        @Override
        public void onFailure(Call call, IOException e) {
            logger.debug("Failed to execute " + call.request(), e);
            rePost();
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            try {
                if (response.isSuccessful()) {
                    Bitmap bmd = BitmapFactory.decodeStream(response.body().byteStream());
                    if (bmd != null) {
                        diskCache.saveImage(Bitmap.createScaledBitmap(bmd, 370, 370, false), id);
                        updateCallbackOfNewImage(id);
                        logger.trace("Saved Preview for: [{}]", id);
                    }
                }
                else {
                    logger.error("Received [{}] attempting to get preview image for [{}]", response.code(), id);
                }
            }
            finally {
                // This throws an IOE but the way it's wrapped it'll be caught by {@link Call.AsyncCall#execute()}
                response.body().close();
                rePost();
            }
        }

        private void rePost() {
            if (isLast) {
                postNewTask();
            }
        }

        private void updateCallbackOfNewImage(String deviceID) {
            if (callbackRefs.get(deviceID) == null) {
                return;
            }

            Callback callback = callbackRefs.get(deviceID).get();
            if (callback != null) {
                try {
                    logger.debug("Callback updated that new image is available.");
                    callback.imageUpdated();
                }
                catch (Exception ex) {
                    logger.debug("Caught ex trying to update callback.", ex);
                }
            }
        }
    }
}
