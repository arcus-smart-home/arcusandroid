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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.model.PagedRecordings;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PagedRecordingModelProvider;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.cameras.model.ClipModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.CamerasSubsystem;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.Recording;
import com.iris.client.capability.Rule;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.RecordingModel;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ClipListingController extends BaseSubsystemController<ClipListingController.Callback> {


    public interface Callback {
        void addClips(List<ClipModel> clips);

        void onError(Throwable error);
    }

    private static final Logger logger = LoggerFactory.getLogger(ClipListingController.class);
    private static final ClipListingController instance;
    private static final int QUERY_LIMIT = 15;

    private ListenerRegistration partialLoadListenerReg;
    private ListenerRegistration partialLoadFailedReg;
    private static final String TODAY = "Today";
    private static final String YESTERDAY = "Yesterday";
    private static final String HEADER_FORMAT = "%1$ta, %1$tb %1$te";
    private static final String NOT_TODAY_FORMAT = "%1$tm/%1$te/%1$ty %1$tl:%1$tM %1$Tp";
    private static final long MILLISECONDS_IN_24_HRS = TimeUnit.HOURS.toMillis(24);

    private static final String[] UNITS = {"B", "KB", "MB"};

    private String filterDeviceAddress = null;
    private Date filterStartTime = null;
    private Date filterEndTime = null;
    private int filterByTimeValue = 0;
    private Map<String, String> cameraNames = new ConcurrentHashMap<>(5);
    private AddressableListSource<DeviceModel> onlineCameraList;

    static {
        instance = new ClipListingController(DeviceModelProvider.instance().getModels(Collections.emptyList()));
        instance.init();
    }

    public static ClipListingController instance() {
        return instance;
    }

    private AtomicReference<String> nextToken = new AtomicReference<>("");
    private List<String> headerDates;
    private final Predicate<RecordingModel> filterDone = recordingModel -> recordingModel.getDuration() != null &&
            Boolean.FALSE.equals(recordingModel.get(Recording.ATTR_DELETED)) &&
            Recording.TYPE_RECORDING.equals(recordingModel.get(Recording.ATTR_TYPE));

    private final Listener<PagedRecordings> pagedRecordingsLoaded = recordings -> {
        List<RecordingModel> recordingModels = Lists.newArrayList(Iterables.filter(recordings.getRecordingModels(), filterDone));
        List<ClipModel> clips = new ArrayList<>(recordingModels.size() + 1);

        nextToken.set(recordings.getToken());
        for (RecordingModel recording : recordingModels) {

            if(!SubscriptionController.isPremiumOrPro() && isOlderThan24Hours(recording.getTimestamp())) {
                continue;
            }

            String headerDateTime = headerString(recording.getTimestamp());

            if (headerDateTime != null) {
                clips.add(ClipModel.asHeader(headerDateTime));
            }

            ClipModel clip = new ClipModel(String.valueOf(recording.getId()));
            clip.setCameraName(getCameraName(recording.getCameraid()));
            clip.setTimeString(getDateString(recording.getTimestamp()));
            clip.setTime(recording.getTimestamp());
            clip.setDeleteTime(recording.getDeleteTime());
            clip.setDurationString(getDurationString(recording.getDuration()));
            clip.setSizeInBytesString(getSizeString(recording.getSize()));
            clip.setCachedClipFile(ClipPreviewImageGetter.instance().getClipForID(recording.getId()));
            clip.setActorName(getActor(recording.getPersonid()));
            clip.setPinned(false);
            Collection<String> tags = recording.getTags();
            if (tags != null) {
                for (String tag : tags) {
                    if (tag.equals("FAVORITE")) {
                        clip.setPinned(true);
                        break;
                    }
                }
            }
            clips.add(clip);
        }

        addClips(clips);
    };


    private boolean isOlderThan24Hours(Date date) {
        if(date == null) {
            return true;
        }
        long currentMilliseconds = System.currentTimeMillis();
        long recordingDateMillis = date.getTime();
        long millisSinceRecording = Math.max(0, currentMilliseconds - recordingDateMillis);

        return  millisSinceRecording > MILLISECONDS_IN_24_HRS;
    }


    public void refresh() {
        headerDates.clear();
        nextToken = new AtomicReference<>("");
        loadMoreClips();
    }

    private final Listener<Throwable> errorListener = throwable -> {
        nextToken.set(null);
        addClips(Collections.emptyList());
        logger.error("Received an error on getting clips. Returning empty list.", throwable);
    };

    ClipListingController(AddressableListSource<DeviceModel> devices) {
        this(devices,
                SubsystemController.instance().getSubsystemModel(CamerasSubsystem.NAMESPACE)
        );
    }

    ClipListingController(
            AddressableListSource<DeviceModel> onlineCameraList,
            ModelSource<SubsystemModel> model) {
        super(model);
        this.onlineCameraList = onlineCameraList;
    }

    public void loadMoreClips() {
        String nextQueryStart = nextToken.get();
        if (nextQueryStart != null) {
            PagedRecordingModelProvider.instance().loadLimited(QUERY_LIMIT, nextQueryStart, false);
        }
    }

    @Override
     protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);

        CamerasSubsystem subsystem = (CamerasSubsystem) getModel();
        onlineCameraList.setAddresses(list(subsystem.getCameras()));

        getCameraDevices();
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        super.onSubsystemCleared(event);
        onlineCameraList.setAddresses(Collections.emptyList());
    }

    @Override
    public ListenerRegistration setCallback(Callback callback) {
        headerDates = new ArrayList<>();
        super.setCallback(callback);
        return new ListenerRegistration() {
            @Override
            public boolean isRegistered() {
                return getCallback() != null;
            }

            @Override
            public boolean remove() {
                boolean registered = isRegistered();
                Listeners.clear(partialLoadListenerReg);
                Listeners.clear(partialLoadFailedReg);
                clearCallback();
                return registered;
            }
        };
    }

    @Override
    protected void updateView(Callback callback) {
        if (isLoaded()) {

            RuleModelProvider.instance()
                             .load()
                             .onFailure(errorListener)
                             .onSuccess(Listeners.runOnUiThread(ruleModels -> loadInitialModels()));
        }
    }

    private void getCameraDevices() {
        onlineCameraList
                .load()
                .onSuccess(Listeners.runOnUiThread(devices -> {
                    for (DeviceModel model : devices) {
                        cameraNames.put(
                                String.valueOf(model.get(Capability.ATTR_ID)),
                                (String) model.get(Device.ATTR_NAME)
                        );
                    }
                }));
    }

    protected void loadInitialModels() {
        partialLoadListenerReg = PagedRecordingModelProvider.instance().addPartialLoadedListener(pagedRecordingsLoaded);
        partialLoadFailedReg = PagedRecordingModelProvider.instance().addPartialFailedListener(errorListener);
        PagedRecordingModelProvider.instance().loadLimited(QUERY_LIMIT, null, false);
    }

    protected void addClips(final List<ClipModel> clips) {
        LooperExecutor.getMainExecutor().execute(() -> {
            Callback callback = getCallback();
            if (callback != null) {
                try {
                    callback.addClips(clips);
                }
                catch (Exception ex) {
                    logger.error("Could not dispatch callback", ex);
                }
            }
        });
    }

    public void addTag(String tag) {
        Set<String> tags = PagedRecordingModelProvider.instance().getTags();
        if(tags == null) {
            tags = new HashSet<>();
        }
        for(String item : tags) {
            if(item != null && item.equals(tag)) {
                //no need to add one twice
                return;
            }
        }
        tags.add(tag);
        PagedRecordingModelProvider.instance().setTags(tags);
    }

    public void removeTag(String tag) {
        PagedRecordingModelProvider.instance().removeTag(tag);
    }

    public void setFilterByDevice(String filterDeviceAddress) {
        this.filterDeviceAddress = filterDeviceAddress;
        PagedRecordingModelProvider.instance().setFilterDevices(new HashSet<>(Arrays.asList(filterDeviceAddress)));
    }

    public void setFilterByTime(Date filterStartTime, Date filterEndTime) {
        this.filterStartTime = filterStartTime;
        this.filterEndTime = filterEndTime;
        PagedRecordingModelProvider.instance().setStartTime(filterStartTime);
        PagedRecordingModelProvider.instance().setEndTime(filterEndTime);
    }

    public void setFilterByTimeValue(int value) {
        this.filterByTimeValue = value;
    }

    public int getFilterByTimeValue() {
        return this.filterByTimeValue;
    }

    public String getFilterDeviceAddress() {
        return filterDeviceAddress;
    }

    public Date getFilterStartTime() {
        return filterStartTime;
    }

    public Date getFilterEndTime() {
        return filterEndTime;
    }

    protected String getActor(String uuidString) {
        try {
            UUID uuid = UUID.fromString(uuidString);
            if (uuid != null && uuid.getMostSignificantBits() == 0) { // It was recorded due to a Rule.
                String ruleID = String.format("%s%s.%s",
                      Addresses.toServiceAddress(Rule.NAMESPACE),
                      SessionController.instance().getActivePlace(),
                      uuid.getLeastSignificantBits()
                );

                Model m = CorneaClientFactory.getModelCache().get(ruleID);
                return m != null ? (String) m.get(Rule.ATTR_NAME) : "By Rule";
            }
        }
        catch (Exception ex) {
            logger.error("Could not convert string to UUID [String: {}]", uuidString, ex);
        }

        return null;
    }

    protected String getDateString(Date date) {
        if (date == null) {
            return null;
        }

        boolean isToday = DateUtils.Recency.TODAY.equals(DateUtils.getRecency(date));
        //return String.format(Locale.getDefault(), isToday ? TODAY_FORMAT : NOT_TODAY_FORMAT, date);
        return String.format(Locale.getDefault(), NOT_TODAY_FORMAT, date);
    }

    protected String getSizeString(Long s) {
        if (s == null || s <= 0) {
            return null;
        }

        double size = s;

        int unitIndex = 0, maxUnits = UNITS.length;
        // This follows android's formatter pattern android.text.format.Formatter.formatShortFileSize
        while (size > 900 && unitIndex < maxUnits) {
            size /= 1024;
            unitIndex++;
        }

        String sizeCount = (unitIndex > 1) ? String.format("%.1f", size) : String.valueOf(Math.round(size));

        return String.format("%s %s", sizeCount, UNITS[unitIndex]);
    }

    protected String headerString(Date date) {
        if (date == null) {
            return null;
        }

        if (DateUtils.Recency.TODAY.equals(DateUtils.getRecency(date))) {
            if (!headerDates.contains(TODAY)) {
                headerDates.add(TODAY);
                return TODAY;
            }
        } else if (DateUtils.Recency.YESTERDAY.equals(DateUtils.getRecency(date))) {
            if (!headerDates.contains(YESTERDAY)) {
                headerDates.add(YESTERDAY);
                return YESTERDAY;
            }
        }
        else {
            String time = String.format(Locale.US, HEADER_FORMAT, date);
            if (!headerDates.contains(time)) {
                headerDates.add(time);
                return time;
            }
        }

        return null;
    }

    protected String getDurationString(Double durationSecs) {
        if (durationSecs != null && durationSecs > 0) {
            int duration = durationSecs.intValue();
            int hours = duration / 3600;
            int minutes = (duration % 3600) / 60;
            int seconds = duration % 60;

            String h = hours > 0 ? hours + "h " : "";
            String m = minutes > 0 ? minutes + "m " : "";
            String s = seconds > 0 ? seconds + "s" : "";

            return String.format("%s%s%s", h, m, s).trim();
        }

        return null;
    }

    private String getCameraName(String cameraId) {
        String name = cameraNames.get(String.valueOf(cameraId));
        if (name != null) {
            return name;
        } else {
            return "";
        }
    }
}
