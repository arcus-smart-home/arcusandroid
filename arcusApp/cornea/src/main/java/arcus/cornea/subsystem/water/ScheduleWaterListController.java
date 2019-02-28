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
package arcus.cornea.subsystem.water;

import com.google.common.collect.ImmutableList;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.ScheduleGenericStateModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.WaterSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class ScheduleWaterListController extends BaseWaterController<ScheduleWaterListController.Callback> implements ScheduleWaterStateController.Callback {
    private static final ScheduleWaterListController instance = new ScheduleWaterListController();

    private WeakReference<ScheduleWaterStateController> mScheduleStateController;

    public static ScheduleWaterListController instance() {
        return instance;
    }

    private AddressableListSource<DeviceModel> waterDevices;

    private String placeId;

    ScheduleWaterListController() {
        super();
        this.waterDevices = DeviceModelProvider.instance().newModelList();
        init();
    }

    ScheduleWaterListController(ModelSource<SubsystemModel> subsystem, AddressableListSource<DeviceModel> thermostats) {
        super(subsystem);
        this.waterDevices = thermostats;
        init();
    }

    public ListenerRegistration selectAll(String placeId, Callback callback) {
        this.placeId = placeId;
        return this.setCallback(callback);
    }

    public void init() {
        super.init();
        this.waterDevices.addListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                updateView();
            }
        }));

        if (mScheduleStateController == null) {
            this.mScheduleStateController = new WeakReference<>(ScheduleWaterStateController.instance());
        }
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        ArrayList<String> devices = new ArrayList<>(getWaterSubsystem().getWaterDevices());
        waterDevices.setAddresses(devices);
        waterDevices.load();
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        if(event.getChangedAttributes().containsKey(WaterSubsystem.ATTR_WATERDEVICES)) {
            waterDevices.setAddresses(list(getWaterSubsystem().getWaterDevices()), true);
        }

        super.onSubsystemChanged(event);
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        waterDevices.setAddresses(ImmutableList.<String>of());
        super.onSubsystemCleared(event);
    }

    @Override
    protected void updateView(Callback callback, WaterSubsystem subsystem) {
        if(!waterDevices.isLoaded()) {
            return;
        }

        List<DeviceModel> climateDeviceModels = waterDevices.get();
        if(climateDeviceModels.isEmpty()) {
            callback.showNoSchedulableDevices();
        }
        else {
            mScheduleStateController.get().selectAll(this.placeId, climateDeviceModels, this);
        }
    }

    @Override
    public void showScheduleStates(List<ScheduleGenericStateModel> models) {
        if (getCallback() != null) {
            getCallback().showSchedules(models);
        }
    }

    @Override
    public void onError(ErrorModel error) {

    }


    public interface Callback {

        void showNoSchedulableDevices();

        void showSchedules(List<ScheduleGenericStateModel> devices);
    }
}
