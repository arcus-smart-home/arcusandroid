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
package arcus.cornea.subsystem.lightsnswitches;

import androidx.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesDevice;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Color;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Halo;
import com.iris.client.capability.Light;
import com.iris.client.capability.LightsNSwitchesSubsystem;
import com.iris.client.capability.Switch;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SubsystemModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LightsNSwitchesDevListController extends BaseSubsystemController<LightsNSwitchesDevListController.Callback> {
    public interface Callback {
        /**
         * Called to show the list of devices.  These will be sorted by type followed by name. This
         * method indicates the list of devices itself has changed; either this is the first callback
         * issued by the controller; a new devices has been added or an existing device deleted.
         *
         * This is the controller equivalent of "notifyDatasetChanged".
         *
         * @param devices Lights And Switches Devices.
         */
        void showDevices(List<LightsNSwitchesDevice> devices);

        /**
         * Called to indicate a state change in one ore more previously reported devices. That is, a
         * device that was previously offered as a parameter in the {@link #showDevices(List)}
         * callback method.
         *
         * This is the controller equivalent of "notifyItemChanged."
         * @param devices List of one or more devices whose state has changed.
         */
        void updateDevices(List<LightsNSwitchesDevice> devices);
    }

    private static final LightsNSwitchesDevListController INSTANCE;
    private final AddressableListSource<DeviceModel> deviceModels;
    private final Listener<List<DeviceModel>> onDeviceListChange = Listeners.runOnUiThread(
          new Listener<List<DeviceModel>>() {
              @Override
              public void onEvent(List<DeviceModel> deviceModels) {
                  onDevicesChanged(deviceModels);
              }
          });
    private final Listener<ModelEvent> modelsChangedEventListener = new Listener<ModelEvent>() {
        @Override public void onEvent(ModelEvent modelEvent) {
            if (!(modelEvent instanceof ModelChangedEvent)) {
                return;
            }

            final ModelChangedEvent mce = (ModelChangedEvent) modelEvent;
            Set<String> changes = mce.getChangedAttributes().keySet();
            Set<String> intersection = Sets.intersection(changes, UPDATE_DEVICE_ON);
            if (!intersection.isEmpty()) {
                LooperExecutor.getMainExecutor().execute(new Runnable() {
                    @Override public void run() {
                        updateDevices(getCallback(), mce.getModel());
                    }
                });
            }
        }
    };

    private static final Set<String> UPDATE_ON = ImmutableSet.of(
        LightsNSwitchesSubsystem.ATTR_SWITCHDEVICES
    );
    private static final Set<String> UPDATE_DEVICE_ON = ImmutableSet.of(
        Device.ATTR_NAME,
        Device.ATTR_CAPS,
        DeviceConnection.ATTR_STATE,
        Switch.ATTR_STATE,
        Dimmer.ATTR_BRIGHTNESS,
        ColorTemperature.ATTR_COLORTEMP,
        Color.ATTR_HUE,
        Color.ATTR_SATURATION,
        Light.ATTR_COLORMODE,
        DeviceAdvanced.ATTR_ERRORS,
        DevicePower  .ATTR_SOURCE // Halo

    );

    private final Comparator<DeviceModel> deviceSorter = new Comparator<DeviceModel>() {
        @Override
        public int compare(DeviceModel d1, DeviceModel d2) {
            String d1Name = String.valueOf(d1.getName());
            String d2Name = String.valueOf(d2.getName());
            return d1Name.compareToIgnoreCase(d2Name);
        }
    };

    private final Function<DeviceModel, LightsNSwitchesDevice> transform = new Function<DeviceModel, LightsNSwitchesDevice>() {
        @Override
        public LightsNSwitchesDevice apply(DeviceModel deviceModel) {

            LightsNSwitchesDevice device = new LightsNSwitchesDevice();
            device.setDeviceId(deviceModel.getId());
            device.setDeviceName(deviceModel.getName());
            device.setAddress(deviceModel.getAddress());
            device.setDeviceType(getType(deviceModel));

            Collection<String> caps = deviceModel.getCaps() == null ? Collections.<String>emptySet() : deviceModel.getCaps();
            device.setDimmable(caps.contains(Dimmer.NAMESPACE));
            device.setColorChangeable(caps.contains(Color.NAMESPACE));
            device.setColorTempChangeable(caps.contains(ColorTemperature.NAMESPACE));
            device.setSwitchable(caps.contains(Switch.NAMESPACE));
            device.setIsOffline(DeviceConnection.STATE_OFFLINE.equals(deviceModel.get(DeviceConnection.ATTR_STATE)));

            Number dim = (Number) deviceModel.get(Dimmer.ATTR_BRIGHTNESS);
            Number colorTemp = (Number) deviceModel.get(ColorTemperature.ATTR_COLORTEMP);
            Number colorMaxTemp = (Number) deviceModel.get(ColorTemperature.ATTR_MAXCOLORTEMP);
            Number colorMinTemp = (Number) deviceModel.get(ColorTemperature.ATTR_MINCOLORTEMP);
            Number colorHue = (Number) deviceModel.get(Color.ATTR_HUE);
            Number colorSaturation = (Number) deviceModel.get(Color.ATTR_SATURATION);
            String colorMode = (String) deviceModel.get(Light.ATTR_COLORMODE);

            device.setDimPercent(dim != null ? dim.intValue() : 0);
            device.setColorTemp(colorTemp != null ? colorTemp.intValue() : 0);
            device.setColorMaxTemp(colorMaxTemp != null ? colorMaxTemp.intValue() : 0);
            device.setColorMinTemp(colorMinTemp != null ? colorMinTemp.intValue() : 0);
            device.setOn(Switch.STATE_ON.equals(String.valueOf(deviceModel.get(Switch.ATTR_STATE))));
            device.setColorHue(colorHue != null ? colorHue.intValue() : 0);
            device.setColorSaturation(colorSaturation!= null ? colorSaturation.intValue() : 0);
            device.setColorMode(colorMode);
            device.setOnBattery(DevicePower.SOURCE_BATTERY.equals(deviceModel.get(DevicePower.ATTR_SOURCE)));

            if(deviceModel instanceof DeviceAdvanced) {
                DeviceAdvanced advanced = (DeviceAdvanced) deviceModel;
                Map<String, String> errorMap = advanced.getErrors();
                if(errorMap != null) {
                    String errorType = "";
                    String errorDesc;
                    if(errorMap.size() == 1) {
                        Map.Entry<String, String> entry = errorMap.entrySet().iterator().next();
                        errorType = entry.getKey();
                        errorDesc = entry.getValue();
                    }
                    else if(errorMap.size() == 0){
                        errorDesc = "";
                    }
                    else {
                        //TODO: HALO - add appropriate error text
                        errorDesc = "Multiple Errors";
                    }
                    device.setErrorType(errorType);
                    device.setErrorText(errorDesc);
                }
            }

            return device;
        }
    };

    static {
        INSTANCE = new LightsNSwitchesDevListController(LightsNSwitchesSubsystem.NAMESPACE);
        INSTANCE.init();
    }

    protected LightsNSwitchesDevListController(String namespace) {
        this(SubsystemController.instance().getSubsystemModel(namespace),
              DeviceModelProvider.instance().newModelList());
    }

    protected LightsNSwitchesDevListController(ModelSource<SubsystemModel> subsystem, AddressableListSource<DeviceModel> devices) {
        super(subsystem);
        this.deviceModels = devices;
    }

    public static LightsNSwitchesDevListController instance() {
        return INSTANCE;
    }

    @Override
    public void init() {
        this.deviceModels.addListener(onDeviceListChange);
        this.deviceModels.addModelListener(modelsChangedEventListener);
        super.init();
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        LightsNSwitchesSubsystem deviceAddresses = (LightsNSwitchesSubsystem) getModel();
        deviceModels.setAddresses(list(deviceAddresses.getSwitchDevices()));
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        Set<String> intersection = Sets.intersection(changes, UPDATE_ON);
        if(intersection.isEmpty()) {
            return;
        }

        LightsNSwitchesSubsystem subsystem = (LightsNSwitchesSubsystem) getModel();
        deviceModels.setAddresses(list(subsystem.getSwitchDevices()));
    }

    @Override
    public boolean isLoaded() {
        if (super.isLoaded()) {
            List<DeviceModel> devices = getDevices();
            return devices != null;
        }

        return false;
    }

    private void onDevicesChanged(List<DeviceModel> devices) {
        updateView();
    }

    public @Nullable List<DeviceModel> getDevices() {
        return deviceModels.get();
    }

    protected void updateDevices(Callback callback, Model model) {
        if (!isLoaded() || callback == null) {
            return;
        }

        List<LightsNSwitchesDevice> devices = buildDevices((LightsNSwitchesSubsystem) getModel());
        List<LightsNSwitchesDevice> updatedDevices = new ArrayList<>();

        for (LightsNSwitchesDevice thisDevice : devices) {
            if (thisDevice.getDeviceId().equals(model.getId())) {
                updatedDevices.add(thisDevice);
            }
        }

        callback.updateDevices(updatedDevices);
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            return;
        }

        List<LightsNSwitchesDevice> devices = buildDevices((LightsNSwitchesSubsystem) getModel());
        callback.showDevices(devices);
    }

    private List<LightsNSwitchesDevice> buildDevices(LightsNSwitchesSubsystem subsystem) {
        List<DeviceModel> deviceModels = getDevices();
        if (deviceModels == null) {
            deviceModels = new ArrayList<>();
        }

        List<DeviceModel> copy = new ArrayList<>(deviceModels);
        Collections.sort(copy, deviceSorter);
        return Lists.newArrayList(Iterables.transform(copy, transform));
    }

    private boolean isSwitch(DeviceModel d) {
        return d.getCaps().contains(Switch.NAMESPACE);
    }

    private boolean isDimmer(DeviceModel d) {
        return d.getCaps().contains(Dimmer.NAMESPACE);
    }

    private boolean isLightBulb(DeviceModel d) {
        return d.getCaps().contains(Light.NAMESPACE);
    }

    private boolean isHalo(DeviceModel d) {
        return d.getCaps().contains(Halo.NAMESPACE);
    }

    private LightsNSwitchesDevice.Type getType(DeviceModel d) {
        if (isHalo(d)) {
            return LightsNSwitchesDevice.Type.HALO;
        }
        else if (isLightBulb(d)) {
            return LightsNSwitchesDevice.Type.LIGHT;
        }
        else if (isDimmer(d)) {
            return LightsNSwitchesDevice.Type.DIMMER;
        }
        else if(isSwitch(d)) {
            return LightsNSwitchesDevice.Type.SWITCH;
        }
        else {
            return LightsNSwitchesDevice.Type.UNKNOWN;
        }
    }
}
