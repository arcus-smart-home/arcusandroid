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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PagedRecordingModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.cameras.model.DashboardCameraModel;
import arcus.cornea.subsystem.cameras.model.DashboardCardModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Camera;
import com.iris.client.capability.CamerasSubsystem;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Recording;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.RecordingModel;
import com.iris.client.model.Store;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class CameraDashboardCardController extends BaseSubsystemController<CameraDashboardCardController.Callback> {

    public interface Callback {
        void showLearnMore();
        void showSummary(DashboardCardModel model);
    }

    private static final Logger logger = LoggerFactory.getLogger(CameraDashboardCardController.class);
    private static final CameraDashboardCardController instance;

    static {
        instance = new CameraDashboardCardController();
        instance.init();
    }

    public static CameraDashboardCardController instance() {
        return instance;
    }

    private ListenerRegistration recordingStoreListener;
    private final Function<DeviceModel, DashboardCameraModel> transform = new Function<DeviceModel, DashboardCameraModel>() {
        @Override
        public DashboardCameraModel apply(DeviceModel deviceModel) {
            DashboardCameraModel m = new DashboardCameraModel();
            m.setDeviceId(deviceModel.getId());
            m.setName(deviceModel.getName());
            return m;
        }
    };

    private final Listener<List<DeviceModel>> onCameraListChange = Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
        @Override
        public void onEvent(List<DeviceModel> deviceModels) {
            onCameraListChange();
        }
    });

    private final Listener<ModelAddedEvent> onRecordingAdded = Listeners.runOnUiThread(new Listener<ModelAddedEvent>() {
        @Override
        public void onEvent(ModelAddedEvent modelAddedEvent) {
            Model model = modelAddedEvent.getModel();
            if (model == null) {
                return;
            }

            if (!model.getCaps().contains(Recording.NAMESPACE)) {
                return;
            }

            boolean isRecording = RecordingModel.TYPE_RECORDING.equals(model.get(Recording.ATTR_TYPE));
            boolean notDeleted  = Boolean.FALSE.equals(model.get(Recording.ATTR_DELETED));
            if (isRecording && notDeleted) {
                onRecordingAdded();
            }
        }
    });

    private final Comparator<RecordingModel> recordingSorter = new Comparator<RecordingModel>() {
        @Override
        public int compare(RecordingModel recordingModel, RecordingModel t1) {
            return t1.getTimestamp().compareTo(recordingModel.getTimestamp());
        }
    };

    private final Predicate<RecordingModel> streamFilter = new Predicate<RecordingModel>() {
        @Override
        public boolean apply(RecordingModel recordingModel) {
            return Boolean.FALSE.equals(recordingModel.get(Recording.ATTR_DELETED)) &&
                   Recording.TYPE_RECORDING.equals(recordingModel.get(Recording.ATTR_TYPE)) &&
                   recordingModel.get(Recording.ATTR_DURATION) != null;
        }
    };

    private final Listener<List<RecordingModel>> onStoreLoadedListener = Listeners.runOnUiThread(new Listener<List<RecordingModel>>() {
        @Override
        public void onEvent(List<RecordingModel> recordingModels) {
            logger.debug("Loaded [{}] models", recordingModels.size());
            recordingStoreListener = recordings.addListener(ModelAddedEvent.class, onRecordingAdded);
            onRecordingAdded();
        }
    });

    private final Listener<ModelChangedEvent> onModelAttributesChanged = Listeners.runOnUiThread(
        new Listener<ModelChangedEvent>() {
            @Override
            public void onEvent(ModelChangedEvent modelEvent) {
                Set<String> changed = modelEvent.getChangedAttributes().keySet();
                if (changed.contains(DeviceConnection.ATTR_STATE)
                  || changed.contains(Device.ATTR_NAME)) {
                updateView();
                }
            }
        });

    private AddressableListSource<DeviceModel> cameras;
    private Store<RecordingModel> recordings;

    CameraDashboardCardController() {
        this(SubsystemController.instance().getSubsystemModel(CamerasSubsystem.NAMESPACE),
             DeviceModelProvider.instance().newModelList(),
             PagedRecordingModelProvider.instance().getStore());
    }

    CameraDashboardCardController(ModelSource<SubsystemModel> subsystemModel,
                                  AddressableListSource<DeviceModel> cameras,
                                  Store<RecordingModel> recordings) {
        super(subsystemModel);
        this.cameras = cameras;
        this.recordings = recordings;
    }

    @Override
    public void init() {
        this.cameras.addModelListener(onModelAttributesChanged, ModelChangedEvent.class);
        this.cameras.addListener(onCameraListChange);
        PagedRecordingModelProvider.instance().addStoreLoadListener(onStoreLoadedListener);
        PagedRecordingModelProvider.instance().load();
        super.init();
    }

    @Override
    protected boolean isLoaded() {
        return cameras.isLoaded() && PagedRecordingModelProvider.instance().isLoaded();
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        CamerasSubsystem cs = (CamerasSubsystem) getModel();
        cameras.setAddresses(Lists.newArrayList(cs.getCameras()));
        int cameras = cs.getCameras() == null ? 0 : cs.getCameras().size();
        if (cameras > 0) {
            // This will preload currently streaming cameras for the detail views when we first load up.
            // Otherwise we will handle based off of valueChanged events.
            PagedRecordingModelProvider.instance().loadLastStreams(cameras);
        }

        super.onSubsystemLoaded(event);
    }

    @Override protected void onSubsystemCleared(ModelDeletedEvent event) {
        super.onSubsystemCleared(event);
        Listeners.clear(recordingStoreListener);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(changes.contains(CamerasSubsystem.ATTR_CAMERAS)) {
            CamerasSubsystem cs = (CamerasSubsystem) getModel();
            cameras.setAddresses(Lists.newArrayList(cs.getCameras()));
        }
        super.onSubsystemChanged(event);
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            logger.debug("ignoring update view because the subsystem, cameras or recordings are not loaded");
            return;
        }

        DashboardCardModel model = buildModel();
        if(model.getCameras().isEmpty()) {
            callback.showLearnMore();
        } else {
            callback.showSummary(model);
        }
    }

    private void onCameraListChange() {
        updateView();
    }

    private void onRecordingAdded() {
        updateView();
    }

    private DashboardCardModel buildModel() {
        DashboardCardModel model = new DashboardCardModel();
        model.setCameras(buildCameras());
        model.setLastRecording(getLatestRecording());
        return model;
    }

    private List<DashboardCameraModel> buildCameras() {
        List<DeviceModel> devs = cameras.get();
        if(devs == null) {
            return Collections.emptyList();
        }

        List<DeviceModel> newList = new ArrayList<>();
        for (DeviceModel m : devs) {
            if (m.getCaps().contains(Camera.NAMESPACE)) {
                newList.add(m);
            }
        }
        return Lists.newArrayList(Iterables.transform(newList, transform));
    }

    private Date getLatestRecording() {
        List<RecordingModel> onlyRecordings = Lists.newArrayList(Iterables.filter(recordings.values(), streamFilter));
        if(onlyRecordings.isEmpty()) {
            return null;
        }
        Collections.sort(onlyRecordings, recordingSorter);
        return onlyRecordings.get(0).getTimestamp();

    }
}
