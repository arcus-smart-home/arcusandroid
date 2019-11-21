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

import androidx.annotation.VisibleForTesting;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PagedRecordingModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.cameras.model.CameraModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.CapabilityInstances;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Camera;
import com.iris.client.capability.CameraStatus;
import com.iris.client.capability.CamerasSubsystem;
import com.iris.client.capability.CellBackupSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class CameraDeviceListController extends BaseSubsystemController<CameraDeviceListController.Callback> {
    private static final CameraDeviceListController INSTANCE;
    static {
        INSTANCE = new CameraDeviceListController(DeviceModelProvider.instance().getModels(Collections.emptyList()));
        INSTANCE.init();
    }

    private final Comparator<CameraModel> cameraModelComparator = (t1, t2) -> String.valueOf(t1.getCameraName()).compareToIgnoreCase(String.valueOf(t2.getCameraName()));

    @SuppressWarnings("FieldCanBeLocal")
    private Listener<List<DeviceModel>> updateViewTask = Listeners.runOnUiThread(deviceModels -> updateView());

    public interface Callback {
        void showDevices(List<CameraModel> cameraModels);
    }

    private AddressableListSource<DeviceModel> onlineCameraList;
    private ModelSource<SubsystemModel> cellBackupSubsystem;

    @SuppressWarnings("WeakerAccess")
    @VisibleForTesting
    CameraDeviceListController(AddressableListSource<DeviceModel> online) {
        this(online,
              SubsystemController.instance().getSubsystemModel(CamerasSubsystem.NAMESPACE),
              SubsystemController.instance().getSubsystemModel(CellBackupSubsystem.NAMESPACE)
        );
    }

    @SuppressWarnings("WeakerAccess")
    @VisibleForTesting
    CameraDeviceListController(
          AddressableListSource<DeviceModel> onlineCameraList,
          ModelSource<SubsystemModel> subsystem,
          ModelSource<SubsystemModel> cellBackupSubsystem) {
        super(subsystem);

        this.onlineCameraList = onlineCameraList;
        this.onlineCameraList.addListener(updateViewTask);

        this.cellBackupSubsystem = cellBackupSubsystem;
        this.cellBackupSubsystem.load();
        this.cellBackupSubsystem.addModelListener(Listeners.runOnUiThread(mce -> updateView()), ModelChangedEvent.class);
    }

    public static CameraDeviceListController instance() { return INSTANCE; }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);

        CamerasSubsystem subsystem = (CamerasSubsystem) getModel();
        this.onlineCameraList.setAddresses(list(subsystem.getCameras()));
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        Map<String, Object> changedAttrs = event.getChangedAttributes();
        if (changedAttrs == null) {
            return;
        }

        if (changedAttrs.containsKey(CamerasSubsystem.ATTR_CAMERAS) ||
                changedAttrs.containsKey(CameraStatus.ATTR_STATE) ||
                changedAttrs.containsKey(CamerasSubsystem.ATTR_OFFLINECAMERAS)) {
            CamerasSubsystem subsystem = (CamerasSubsystem) getModel();
            onlineCameraList.setAddresses(list(subsystem.getCameras()));
            updateView();
        } else {
            for(DeviceModel model : onlineCameraList.get()) {
                if(changedAttrs.containsKey(CameraStatus.ATTR_STATE + ":" + model.getId())) {
                    CamerasSubsystem subsystem = (CamerasSubsystem) getModel();
                    onlineCameraList.setAddresses(list(subsystem.getCameras()));
                    updateView();
                }
            }
        }
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        super.onSubsystemCleared(event);
        this.onlineCameraList.setAddresses(Collections.emptyList());
    }

    @Override
    protected void updateView(Callback callback) {
        if (!onlineCameraList.isLoaded() && PagedRecordingModelProvider.instance().isLoaded()) {
            onlineCameraList.load();
            PagedRecordingModelProvider.instance().load();
            return;
        }

        callback.showDevices(getCameraDevices(onlineCameraList.get()));
    }

    private List<CameraModel> getCameraDevices(List<DeviceModel> online) {
        if (online == null || online.isEmpty()) {
            return Collections.emptyList();
        }

        boolean onCellular = isOnCellular();

        List<CameraModel> cameraModels = new ArrayList<>();
        for (DeviceModel model : online) {
            Collection<String> caps = model.getCaps();
            if (caps != null && caps.contains(Camera.NAMESPACE)) {
                String recordingState = (String) CapabilityInstances.getAttributeValue(getModel(), model.getId(), CameraStatus.ATTR_STATE);

                CameraModel camera = new CameraModel(model, onCellular);
                camera.setCameraState(recordingState);
                cameraModels.add(camera);
            }
        }

        Collections.sort(cameraModels, cameraModelComparator);
        return cameraModels;
    }

    private boolean isOnCellular() {
        SubsystemModel subsystemModel = cellBackupSubsystem.get();
        return subsystemModel != null && CellBackupSubsystem.STATUS_ACTIVE.equals(subsystemModel.get(CellBackupSubsystem.ATTR_STATUS));
    }
}
