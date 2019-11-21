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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.PagedRecordingModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Recording;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.RecordingModel;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ClipInteractionController {
    private final static String TOO_MANY_PINNED = "[request.invalid]: Exceeded max number of favorite videos";
    private static final Logger logger = LoggerFactory.getLogger(ClipInteractionController.class);
    private static final Map<String, Pair<Long, String>> previewURLs  = new ConcurrentHashMap<>(10);
    private static final Map<String, Download> downloadURLs = new ConcurrentHashMap<>(10);
    private static final String FILE_NAME_FORMAT = "%1$tY-%1$tm-%1$td-%1$tH-%1$tM";
    private ListenerRegistration changedLReg;
    private Reference<Callback> callbackRef = new WeakReference<>(null);
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            callOnError(throwable);
        }
    });
    private final Listener<ModelChangedEvent> modelChangedListener = new Listener<ModelChangedEvent>() {
        @Override public void onEvent(ModelChangedEvent mce) {
            final Model model = mce.getModel();
            if (model == null || !Boolean.TRUE.equals(model.get(Recording.ATTR_DELETED))) {
                return;
            }

            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override public void run() {
                    Callback callback = callbackRef.get();
                    if (callback != null) {
                        try {
                            callback.clipDeleted(model.getId());
                        }
                        catch (Exception ex) {
                            logger.error("Error dispatching deleted event.", ex);
                        }
                    }
                }
            });
        }
    };

    public interface Callback {
        void playbackURL(String recordingID, String url);
        void downloadURL(String recordingID, String fileName, long estimatedSize, String url);
        void clipDeleted(String recordingID);
        void onError(Throwable throwable);
        void exceededPinnedLimit(String recordingId);
    }

    public ClipInteractionController() {
    }

    public ListenerRegistration setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
        Listeners.clear(changedLReg);
        changedLReg = PagedRecordingModelProvider.instance().getStore().addListener(ModelChangedEvent.class, modelChangedListener);

        return new ListenerRegistration() {
            @Override public boolean isRegistered() {
                return callbackRef.get() != null;
            }

            @Override public boolean remove() {
                boolean registered = isRegistered();
                Listeners.clear(changedLReg);
                callbackRef.clear();
                return registered;
            }
        };
    }

    public void getPlaybackURL(@NonNull String recordingID) {
        Pair<Long, String> cache = previewURLs.get(recordingID);
        if (cache == null || isExpired(cache.first)) {
            refreshViewURL(recordingID);
            logger.debug("Looking up View URL for [{}]", recordingID);
        }
        else {
            playbackURLSuccess(recordingID, cache.second);
            logger.debug("Received View URL from Cache [{}]; Expiration -> [{}]", recordingID, dateLogFormat(cache.first));
        }
    }

    public void getDownloadURL(@NonNull String recordingID) {
        Download cache = downloadURLs.get(recordingID);
        if (cache == null || isExpired(cache.expireTime)) {
            doGetDownloadURL(recordingID);
            logger.debug("Looking up Download URL for [{}]", recordingID);
        }
        else {
            downloadURLSuccess(recordingID, cache.timestamp, cache.sizeEstimate , cache.url);
            logger.debug("Received Download URL from Cache [{}]; Expiration -> [{}]", recordingID, dateLogFormat(cache.expireTime));
        }
    }

    public void delete(String recordingID) {
        RecordingModel model = getRecordingModel(recordingID);
        if (model == null) {
            callOnError(new RuntimeException("Could not locate model in cache. Is this model loaded?"));
        }
        else {
            model.delete().onFailure(errorListener);
        }
    }

    protected void refreshViewURL(final String recordingID) {
        RecordingModel model = getRecordingModel(recordingID);
        if (model == null) {
            callOnError(new RuntimeException("Could not locate model in cache. Is this model loaded?"));
        }
        else {
            model.view()
                  .onFailure(errorListener)
                  .onSuccess(Listeners.runOnUiThread(new Listener<Recording.ViewResponse>() {
                      @Override public void onEvent(Recording.ViewResponse viewResponse) {
                          long time = getTime(viewResponse.getExpiration());
                          String hls = String.valueOf(viewResponse.getHls());

                          previewURLs.put(recordingID, Pair.create(time, hls));
                          playbackURLSuccess(recordingID, hls);
                      }
                  }));
        }
    }

    protected void doGetDownloadURL(final String recordingID) {
        final RecordingModel model = getRecordingModel(recordingID);
        if (model == null) {
            callOnError(new RuntimeException("Could not locate model in cache. Is this model loaded?"));
        }
        else {
            model.download()
                  .onFailure(errorListener)
                  .onSuccess(Listeners.runOnUiThread(new Listener<Recording.DownloadResponse>() {
                      @Override public void onEvent(Recording.DownloadResponse response) {
                          long expires = getTime(response.getExpiration());
                          long estimatedSize = getLong(response.getMp4SizeEstimate());
                          String mp4Url = String.valueOf(response.getMp4());

                          String cameraID = String.valueOf(model.getCameraid()) + "-";
                          String created  = String.format(Locale.getDefault(), FILE_NAME_FORMAT, getDate(model.getTimestamp()));
                          String fileName = created + "-" + cameraID.substring(0, cameraID.indexOf("-")) + ".mp4";

                          downloadURLs.put(recordingID, new Download(estimatedSize, fileName, mp4Url, expires));
                          downloadURLSuccess(recordingID, fileName, estimatedSize, mp4Url);
                      }
                  }));
        }
    }

    protected @Nullable RecordingModel getRecordingModel(String recordingID) {
        RecordingModel model = PagedRecordingModelProvider.instance().getStore().get(recordingID);
        if (model != null) {
            return model;
        }

        return (RecordingModel) CorneaClientFactory.getModelCache().get(Addresses.toObjectAddress(Recording.NAMESPACE, recordingID));
    }

    public @Nullable void updatePinState(String recordingID, boolean bPin) {
        RecordingModel model = PagedRecordingModelProvider.instance().getStore().get(recordingID);
        if (model == null) {
            model = (RecordingModel) CorneaClientFactory.getModelCache().get(Addresses.toObjectAddress(Recording.NAMESPACE, recordingID));
        }

        Collection<String> tags = model.getTags();
        if(bPin) {
            model.removeTags(tags);
            for(String tag : tags) {
                if(tag != null && tag.equals("FAVORITE")) {
                    //nothing to do here
                    return;
                }
            }
            tags.add("FAVORITE");
            model.addTags(tags).onFailure(event -> {
                if(TOO_MANY_PINNED.equals(event.getMessage())) {
                    tooManyPinned(recordingID);
                }
            });

        } else {
            model.removeTags(tags);
            for(String tag : tags) {
                if(tag != null && tag.equals("FAVORITE")) {
                    tags.remove(tag);
                    break;
                }
            }
            model.addTags(tags);
        }
        model.commit();
    }

    protected boolean isExpired(long timeStamp) {
        return System.currentTimeMillis() >= timeStamp;
    }

    protected long getTime(Date from) {
        if (from == null) {
            return System.currentTimeMillis();
        }
        else {
            return from.getTime() + TimeUnit.HOURS.toMillis(1);
        }
    }

    protected long getLong(Long item) {
        return item == null ? -1 : item;
    }

    private Date getDate(Date date) {
        return date == null ? new Date() : date;
    }

    protected String dateLogFormat(long time) {
        return DateFormatUtils.format(time, DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.getPattern());
    }

    protected void playbackURLSuccess(String recordingID, String hls) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.playbackURL(recordingID, hls);
        }
    }

    protected void downloadURLSuccess(String recordingID, String fileName, long estimatedSize, String url) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.downloadURL(recordingID, fileName, estimatedSize, url);
        }
    }

    protected void callOnError(Throwable throwable) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.onError(throwable);
        }
    }

    private void tooManyPinned(String recordingId){
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.exceededPinnedLimit(recordingId);
        }
    }

    public static class Pair<F, S> {
        public final F first;
        public final S second;

        public Pair(F first, S second) {
            this.first = first;
            this.second = second;
        }

        public static <A, B> Pair <A, B> create(A a, B b) {
            return new Pair<A, B>(a, b);
        }
    }

    class Download {
        public final long sizeEstimate;
        public final String timestamp;
        public final String url;
        public final long expireTime;

        public Download(long sizeEstimate, String timestamp, String url, long time) {
            this.sizeEstimate = sizeEstimate;
            this.timestamp = timestamp;
            this.url = url;
            this.expireTime = time;
        }
    }
}
