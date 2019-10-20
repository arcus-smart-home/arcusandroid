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
package arcus.cornea.subsystem.security;

import com.google.common.collect.ImmutableList;
import arcus.cornea.provider.BaseModelProvider;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.ProductModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.security.model.ConfigDeviceModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.ProductCatalogUtils;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.SecurityAlarmMode;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ProductModel;
import com.iris.client.model.SubsystemModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class SecurityDeviceConfigController extends BaseSecurityController<SecurityDeviceConfigController.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(SecurityDeviceConfigController.class);
    private static final SecurityDeviceConfigController instance;

    private static final String TXT_PARTIAL_AND_ON = "On & Partial";
    private static final String TXT_PARTIAL = "Partial";
    private static final String TXT_ON = "On";
    private static final String TXT_NOT_PARTICIPATING = "Not Participating";

    private static final String ATTR_ON_DEVICES = SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON;
    private static final String ATTR_PARTIAL_DEVICES = SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL;

    static {
        instance = new SecurityDeviceConfigController(
                SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE),
                DeviceModelProvider.instance().getModels(ImmutableList.<String>of()),
                ProductModelProvider.instance()
        );
        instance.init();
    }

    public static SecurityDeviceConfigController instance() {
        return instance;
    }

    private final AddressableListSource<DeviceModel> devices;
    private final BaseModelProvider<ProductModel> products;
    private final Listener<Throwable> onFailure = Listeners.runOnUiThread(
            new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable cause) {
                    onFailure(cause);
                }
            }
    );

    private DeviceModel selectedModel;
    private WeakReference<SelectedDeviceCallback> selectedDeviceCallbackRef = new WeakReference<SelectedDeviceCallback>(null);

    SecurityDeviceConfigController(
            ModelSource<SubsystemModel> subsystem,
            AddressableListSource<DeviceModel> devices,
            BaseModelProvider<ProductModel> products
    ) {
        super(subsystem);
        this.devices = devices;
        Listener<List<DeviceModel>> onDevicesChanged = Listeners.runOnUiThread(
                new Listener<List<DeviceModel>>() {
                    @Override
                    public void onEvent(List<DeviceModel> models) {
                        onDevicesChanged(models);
                    }
                }
        );
        this.devices.addListener(onDevicesChanged);
        Listener<ModelChangedEvent> onDeviceChanged = Listeners.runOnUiThread(
                new Listener<ModelChangedEvent>() {
                    @Override
                    public void onEvent(ModelChangedEvent model) {
                        onDeviceChanged(model);
                    }
                }
        );
        this.devices.addModelListener(onDeviceChanged, ModelChangedEvent.class);
        this.products = products;
    }

    protected void onDevicesChanged(List<DeviceModel> models) {
        updateView();
    }

    protected void onDeviceChanged(ModelChangedEvent event) {
        Set<String> keys = event.getChangedAttributes().keySet();
        if(
                keys.contains(Device.ATTR_NAME) ||
                keys.contains(Device.ATTR_PRODUCTID)
        ) {
            updateDevice(event.getModel().getAddress());
        }
    }

    protected void onFailure(Throwable cause) {
        logger.warn("Unable to complete request", cause);
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);
        SecuritySubsystem security = getSecuritySubsystem();
        this.devices.setAddresses(BaseSubsystemController.list(security.getSecurityDevices()));
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        SecuritySubsystem security = getSecuritySubsystem();
        this.devices.setAddresses(BaseSubsystemController.list(security.getSecurityDevices()));
    }

    @Override
    protected void updateView(Callback callback) {
        SecuritySubsystem security = getSecuritySubsystem();
        if(security == null) {
            logger.debug("Security subsystem not loaded, not updating view");
            return;
        }

        List<DeviceModel> devices = this.devices.get();
        if(devices == null) {
            logger.debug("Devices not loaded, not updating view");
            return;
        }

        List<ConfigDeviceModel> config = getConfiguration(security, devices);
        callback.updateDevices(config);
    }

    protected void updateDevice(String address) {
        Callback cb = getCallback();
        if(cb == null) {
            logger.debug("No callback, not updating view");
            return;
        }

        SecuritySubsystem security = getSecuritySubsystem();
        if(security == null) {
            logger.debug("Security subsystem not loaded, not updating view");
            return;
        }

        DeviceModel device = getDevice(address);
        if(device == null) {
            logger.debug("Can't load device " + address + ", not updating view");
            return;
        }

        ConfigDeviceModel config = convert(security, device);
        cb.updateDevice(config);
    }

    protected DeviceModel getDevice(String address) {
        List<DeviceModel> devices = this.devices.get();
        if(devices == null) {
            return null;
        }

        for(DeviceModel device: devices) {
            if(address.equals(device.getAddress())) {
                return device;
            }
        }
        return null;
    }

    protected List<ConfigDeviceModel> getConfiguration(SecuritySubsystem security, List<DeviceModel> devices) {
        List<ConfigDeviceModel> configuration = new ArrayList<>();
        for(DeviceModel model: devices) {
            ConfigDeviceModel config = convert(security, model);
            if(config != null) {
                configuration.add(config);
            }
        }
        return configuration;
    }

    protected ConfigDeviceModel convert(SecuritySubsystem security, DeviceModel device) {
        ConfigDeviceModel config = new ConfigDeviceModel();
        config.setId(device.getId());
        config.setIcon(device.getDevtypehint());
        config.setName(device.getName());
        config.setDescription(getDescription(device));
        config.setLinkText(getLinkText((SubsystemModel) security, device));
        return config;
    }

    protected String getDescription(DeviceModel device) {
        String description = ProductCatalogUtils.getProductNameForDevice(device);
        return StringUtils.isEmpty(description) ? "" : description;
    }

    protected String getLinkText(SubsystemModel subsystem, DeviceModel device) {
        String address = device.getAddress();
        boolean isPartial = set( (Collection<String>) subsystem.get(ATTR_PARTIAL_DEVICES) ).contains(address);
        boolean isOn = set( (Collection<String>) subsystem.get(ATTR_ON_DEVICES) ).contains(address);

        if(isPartial && isOn) {
            return TXT_PARTIAL_AND_ON;
        }
        else if(isPartial) {
            return TXT_PARTIAL;
        }
        else if(isOn) {
            return TXT_ON;
        }
        else {
            return TXT_NOT_PARTICIPATING;
        }
    }

    protected void updateSelected() {
        SelectedDeviceCallback callback = selectedDeviceCallbackRef.get();
        if(callback == null) {
            return;
        }

        DeviceModel selected = this.selectedModel;
        if(selected == null) {
            return;
        }

        SubsystemModel subsystem = getModel();
        if(subsystem == null) {
            return;
        }

        updateSelected(selected, subsystem, callback);
    }

    protected void updateSelected(DeviceModel device, SubsystemModel subsystem, SelectedDeviceCallback callback) {
        boolean isPartial = set( (Collection<String>) subsystem.get(ATTR_PARTIAL_DEVICES) ).contains(device.getAddress());
        boolean isOn = set( (Collection<String>) subsystem.get(ATTR_ON_DEVICES) ).contains(device.getAddress());
        if(isPartial && isOn) {
            callback.updateSelected(device.getName(), Mode.ON_AND_PARTIAL);
        }
        else if(isOn) {
            callback.updateSelected(device.getName(), Mode.ON_ONLY);
        }
        else if(isPartial) {
            callback.updateSelected(device.getName(), Mode.PARTIAL_ONLY);
        }
        else {
            callback.updateSelected(device.getName(), Mode.NOT_PARTICIPATING);
        }
    }

    public ListenerRegistration setSelectedDeviceCallback(String id, SelectedDeviceCallback callback) {
        if(selectedDeviceCallbackRef.get() != null) {
            logger.warn("Replacing selected device callback");
        }
        selectedDeviceCallbackRef = new WeakReference<SelectedDeviceCallback>(callback);
        this.selectedModel = getDevice("DRIV:dev:" + id);
        updateSelected();
        return Listeners.wrap(selectedDeviceCallbackRef);
    }

    public void setMode(String id, Mode mode) {
        SubsystemModel model = getModel();
        if(model == null) {
            // uh?
            logger.warn("Unable to change mode, the subsystem is not loaded");
            return;
        }

        String address = "DRIV:" + Device.NAMESPACE + ":" + id;
        Set<String> onDevices = new HashSet<>( set((Collection<String>) model.get(ATTR_ON_DEVICES)) );
        Set<String> partialDevices = new HashSet<>( set((Collection<String>) model.get(ATTR_PARTIAL_DEVICES)) );
        switch(mode) {
        case ON_AND_PARTIAL:
            onDevices.add(address);
            partialDevices.add(address);
            break;

        case ON_ONLY:
            onDevices.add(address);
            partialDevices.remove(address);
            break;

        case PARTIAL_ONLY:
            onDevices.remove(address);
            partialDevices.add(address);
            break;

        case NOT_PARTICIPATING:
            onDevices.remove(address);
            partialDevices.remove(address);
            break;
        }

        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(ATTR_ON_DEVICES, onDevices);
        request.setAttribute(ATTR_PARTIAL_DEVICES, partialDevices);
        model
            .request(request)
            .onFailure(onFailure)
            ;
    }

    public interface Callback {

        void updateDevices(List<ConfigDeviceModel> models);

        // May deprecate this
        void updateDevice(ConfigDeviceModel model);

    }

    public interface SelectedDeviceCallback {

        void updateSelected(String name, Mode mode);
    }

    public enum Mode {
        ON_ONLY, PARTIAL_ONLY, ON_AND_PARTIAL, NOT_PARTICIPATING
    }
}
