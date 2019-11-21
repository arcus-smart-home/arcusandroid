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
package arcus.cornea.provider;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import com.google.common.base.Function;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.model.PagedRecordings;

import com.iris.client.IrisClient;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelCache;
import com.iris.client.model.RecordingModel;
import com.iris.client.model.Store;
import com.iris.client.service.VideoService;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class PagedRecordingModelProvider extends BaseModelProvider<RecordingModel> {
    private static final int DEFAULT_LIMIT = 10;
    private static final PagedRecordingModelProvider INSTANCE = new PagedRecordingModelProvider();

    public static PagedRecordingModelProvider instance() { return INSTANCE; }

    private final ModelCache cache;
    private final VideoService videoService;
    private final ListenerList<Throwable> partialFailedListeners = new ListenerList<>();
    private final ListenerList<PagedRecordings> partialLoadListeners = new ListenerList<>();
    private final AtomicReference<ClientFuture<PagedRecordings>> partialLoadRef = new AtomicReference<>();
    private Date endTime;
    private Date startTime;
    private Set<String> filterDevices;
    private Set<String> tags;

    @SuppressWarnings({"unchecked"})
    private final Function<VideoService.PageRecordingsResponse, PagedRecordings> pagedRecordings =
          new Function<VideoService.PageRecordingsResponse, PagedRecordings>() {
              @Override
              public PagedRecordings apply(VideoService.PageRecordingsResponse input) {
                  // Only return the responses back.
                  return new PagedRecordings((List) cache.addOrUpdate(input.getRecordings()), input.getNextToken());
              }
          };
    private final Function<PagedRecordings, List<RecordingModel>> loadTransform = new Function<PagedRecordings, List<RecordingModel>>() {
        @Override
        public List<RecordingModel> apply(@Nullable PagedRecordings input) {
            if (input != null && input.getRecordingModels() != null) {
                return input.getRecordingModels();
            }

            return Collections.emptyList();
        }
    };
    private final Listener<PagedRecordings> onPartialLoaded = new Listener<PagedRecordings>() {
        @Override
        public void onEvent(PagedRecordings recordings) {
            onPartialLoaded(recordings);
            partialLoadRef.set(null); // Allow new partial queries after a successful query.
        }
    };
    private final Listener<Throwable> onPartialFailure = new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onPartialFailed(throwable);
            partialLoadRef.set(null); // Allow new partial queries after an error.
        }
    };

    PagedRecordingModelProvider() {
        this(CorneaClientFactory.getClient(),
              CorneaClientFactory.getModelCache(),
              CorneaClientFactory.getStore(RecordingModel.class),
              CorneaClientFactory.getService(VideoService.class));
    }

    PagedRecordingModelProvider(IrisClient client, ModelCache cache, Store<RecordingModel> store, VideoService service) {
        super(client, cache, store);
        this.cache = cache;
        this.videoService = service;
    }

    @Override
    protected ClientFuture<List<RecordingModel>> doLoad(String placeId) {
        // This is no-op since all queries should use pagnaited version.
        return Futures.transform(loadLimited(DEFAULT_LIMIT, null, false), loadTransform);
    }

    public ClientFuture<PagedRecordings> loadLimited(@IntRange(from = 1) @Nullable Integer limit, @Nullable String nextToken, @Nullable Boolean all) {
        ClientFuture<PagedRecordings> current = partialLoadRef.get();
        if (current != null) {
            return current;
        }

        String placeID = getPlaceID();
        if (placeID == null) {
            return Futures.failedFuture(new IllegalStateException("Must select a place before data can be loaded"));
        }

        if (limit == null || limit < 1 || limit > Integer.MAX_VALUE) {
            limit = DEFAULT_LIMIT;
        }

        ClientFuture<PagedRecordings> response = doLoadPartial(placeID, limit, nextToken, Boolean.TRUE.equals(all));
        this.partialLoadRef.set(response);

        response.onSuccess(onPartialLoaded).onFailure(onPartialFailure);

        return response;
    }

    public ListenerRegistration addPartialLoadedListener(Listener<? super PagedRecordings> listener) {
        return partialLoadListeners.addListener(listener);
    }

    public ListenerRegistration addPartialFailedListener(Listener<? super Throwable> listener) {
        return partialFailedListeners.addListener(listener);
    }

    /**
     * Should be called to load the current streams.  Should only impact currently streaming cameras when you log in.
     * If the camera is not streaming when you login we should receive a value change event and update accordingly.
     * This will pull back the last {@code limit} devices which should be set to the number of cameras on the subsystem.
     *
     * @param limit number of cameras
     */
    public void loadLastStreams(Integer limit) {
        String placeID = getPlaceID();
        if (placeID == null) {
            return;
        }

        videoService.pageRecordings(placeID, limit, null, false, true, VideoService.PageRecordingsRequest.TYPE_STREAM, null, null, null, null);
    }

    protected ClientFuture<PagedRecordings> doLoadPartial(String placeID, Integer limit, String nextToken, Boolean all) {
        return Futures.transform(videoService.pageRecordings(placeID, limit, nextToken, all, false, VideoService.PageRecordingsRequest.TYPE_RECORDING, endTime, startTime, getFilterDevices(), getTags()), pagedRecordings);
    }

    protected void onPartialLoaded(PagedRecordings recordings) {
        firePartialLoaded(recordings);
    }

    private void firePartialLoaded(PagedRecordings recordings) {
        partialLoadListeners.fireEvent(recordings);
    }

    protected void onPartialFailed(Throwable throwable) {
        firePartialFailed(throwable);
    }

    private void firePartialFailed(Throwable throwable) {
        partialFailedListeners.fireEvent(throwable);
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Set<String> getFilterDevices() {
        if(filterDevices == null) {
            return null;
        }
        if(filterDevices.size() == 1) {
            for(String s : filterDevices) {
               if(s == null) {
                   return null;
               }
            }
        }
        return filterDevices;
    }

    public Set<String> getTags() {
        if(tags == null) {
            return null;
        }
        if(tags.size() == 1) {
            for(String s : tags) {
                if(s == null) {
                    return null;
                }
            }
        }
        return tags;
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean removeTag(@Nullable String tag) {
        return tag != null && tags != null && tags.remove(tag);
    }

    public void setFilterDevices(Set<String> filterDevices) {
        this.filterDevices = filterDevices;
    }
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }
}
