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
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.security.model.AlarmDeviceModel;
import arcus.cornea.subsystem.security.model.AlarmDeviceSection;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Contact;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Motion;
import com.iris.client.capability.SecurityAlarmMode;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


public class SecurityDeviceStatusController extends BaseSecurityController<SecurityDeviceStatusController.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(SecurityDeviceStatusController.class);

    public static SecurityDeviceStatusController all() {
        return References.instance;
    }

    public static SecurityDeviceStatusController partial() {
        return References.partial;
    }

    // TODO should these move to some Android XML file?
    private static final String FMT_OFFLINE = "Offline";
    private static final String FMT_CONTACT_OPENED = "Opened %s";
    private static final String FMT_CONTACT_CLOSED = "Closed Since %s";
    private static final String FMT_MOTION = "Last Activity %s";
    private static final String FMT_ACTIVE = "Active";

    private static final String FMT_X_OFFLINE = "%d Offline";
    private static final String FMT_X_OPENED = "%d Opened";
    private static final String FMT_1_DEVICE = "1 Device";
    private static final String FMT_X_DEVICES = "%d Devices";
    private static final String FMT_1_MORE_DEVICE = "1 More Device";
    private static final String FMT_X_MORE_DEVICES = "%d More Devices";
    private static final String FMT_X_TRIGGERED_DEVICES = "Triggered & Offline Devices";

    private final String devicesAttribute;
    private final WeakReference<Callback> callbackRef = new WeakReference<Callback>(null);

    // lazy-load
    private static class References {
        private static final SecurityDeviceStatusController instance;
        private static final SecurityDeviceStatusController partial;

        static {
            ModelSource<SubsystemModel> source = SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE);
            instance = new SecurityDeviceStatusController(SecuritySubsystem.ALARMMODE_ON, source, null);
            partial = new SecurityDeviceStatusController(SecuritySubsystem.ALARMMODE_PARTIAL, source, null);
            instance.init();
            partial.init();
        }
    }

    private AddressableListSource<DeviceModel> devices;

    SecurityDeviceStatusController(String mode) {
        this(mode, null, null);
    }

    SecurityDeviceStatusController(String mode, ModelSource<SubsystemModel> source, AddressableListSource<DeviceModel> devices) {
        super(source);
        String mode1 = mode;
        this.devicesAttribute = SecurityAlarmMode.ATTR_DEVICES + ":" + mode;
        this.devices = devices == null ?
                DeviceModelProvider.instance().getModels(ImmutableList.<String>of()) :
                devices;

        Listener<List<DeviceModel>> onDevicesChanged = Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> models) {
                onDevicesChanged(models);
            }
        });
        this.devices.addListener(onDevicesChanged);
        Listener<ModelChangedEvent> onDeviceChanged = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
            @Override
            public void onEvent(ModelChangedEvent event) {
                onDeviceChanged((DeviceModel) event.getModel());
            }
        });
        this.devices.addModelListener(onDeviceChanged, ModelChangedEvent.class);
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        SubsystemModel subsystem = getModel();
        this.devices.setAddresses( list((Collection<String>) subsystem.get(devicesAttribute)) );
        updateView();
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        SubsystemModel subsystem = getModel();
        Set<String> keys = event.getChangedAttributes().keySet();
        if(keys.contains(devicesAttribute)) {
            this.devices.setAddresses( list((Collection<String>) subsystem.get(devicesAttribute)) );
            updateView();
        }
        else if(
            keys.contains(SecuritySubsystem.ATTR_BYPASSEDDEVICES) ||
            keys.contains(SecuritySubsystem.ATTR_OFFLINEDEVICES)  ||
            keys.contains(SecuritySubsystem.ATTR_ALARMMODE)
        ) {
            updateView();
        }
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        this.devices.setAddresses(ImmutableList.<String>of());
    }

    protected void onDevicesChanged(List<DeviceModel> models) {
        Callback cb = getCallback();
        if(cb != null) {
            updateView(cb);
        }
    }

    protected void onDeviceChanged(DeviceModel model) {
        // TODO target this event better
        Callback cb = getCallback();
        if(cb != null) {
            updateView(cb);
        }
    }

    protected void updateView(Callback callback) {
        SecuritySubsystem subsystem = getSecuritySubsystem();
        if(subsystem == null) {
            logger.debug("Not updating view, subsystem not loaded");
            return;
        }

        List<DeviceModel> devices = this.devices.get();
        if(devices == null) {
            logger.debug("Not updating view, devices not loaded");
            return;
        }

        callback.updateSections(getSections(subsystem, devices));
    }

    protected List<AlarmDeviceSection> getSections(SecuritySubsystem subsystem, List<DeviceModel> devices) {
        List<AlarmDeviceModel> offline_open = new ArrayList<>();
        List<AlarmDeviceModel> more = new ArrayList<>();

        for(DeviceModel device: devices) {
            AlarmDeviceModel alarmModel = convert(subsystem, device);
            if(!alarmModel.isOnline()) {
                offline_open.add(alarmModel);
            }
            else if(alarmModel.isOpened()) {
                offline_open.add(alarmModel);
            }
            else {
                more.add(alarmModel);
            }
        }

        List<AlarmDeviceSection> sections = new ArrayList<>(2);
        if(!offline_open.isEmpty()) {
            AlarmDeviceSection section = new AlarmDeviceSection();
            section.setId("open");
            section.setTitle(FMT_X_TRIGGERED_DEVICES);
            section.setDevices(offline_open);
            sections.add(section);
        }
        if(!more.isEmpty()) {
            AlarmDeviceSection section = new AlarmDeviceSection();
            section.setId("all");
            String format = getMoreTitle(!offline_open.isEmpty(), more.size());
            section.setTitle(String.format(format, more.size()));
            section.setDevices(more);
            sections.add(section);
        }
        return sections;
    }

    protected AlarmDeviceModel convert(SecuritySubsystem subsystem, DeviceModel model) {
        AlarmDeviceModel alarmModel = new AlarmDeviceModel();
        alarmModel.setDeviceId(model.getId());
        alarmModel.setOnline( !DeviceStatus.isOffline(model) );
        alarmModel.setBypassed(set(subsystem.getBypassedDevices()).contains(model.getAddress()));
        alarmModel.setOpened( DeviceStatus.isOpen(model) );
        alarmModel.setName(model.getName());
        alarmModel.setDescription( getDescription(model) );
        alarmModel.setIcon(model.getDevtypehint());
        return alarmModel;
    }

    protected String getMoreTitle(boolean hasOpen, int count) {
        if(hasOpen) {
            if(count > 1) {
                return String.format(FMT_X_MORE_DEVICES, count);
            }
            else {
                return FMT_1_MORE_DEVICE;
            }
        }
        else {
            if(count > 1) {
                return String.format(FMT_X_DEVICES, count);
            }
            else {
                return FMT_1_DEVICE;
            }
        }
    }

    public String getDescription(DeviceModel model) {
        if( DeviceStatus.isOffline(model) ) {
            return FMT_OFFLINE;
        }
        // TODO can we use device type hint here?
        if(model instanceof Contact) {
            return getContactDescription((Contact) model);
        }
        if(model instanceof Motion) {
            return getMotionDescription((Motion) model);
        }

        // keypads and other
        return FMT_ACTIVE;
    }

    protected  String getContactDescription(Contact sensor) {
        String message = Contact.CONTACT_OPENED.equals(sensor.getContact()) ?
                FMT_CONTACT_OPENED :
                FMT_CONTACT_CLOSED;
        return String.format(message, DateUtils.format(date(sensor.getContactchanged())));
    }

    protected  String getMotionDescription(Motion sensor) {
        return String.format(FMT_MOTION, DateUtils.format(date(sensor.getMotionchanged())));
    }

    protected List<AlarmDeviceModel> getAlarmDevices(SecuritySubsystem subsystem) {
        List<DeviceModel> devices = this.devices.get();
        if(devices == null) {
            return ImmutableList.of();
        }

        List<AlarmDeviceModel> models = new ArrayList<AlarmDeviceModel>();
        for(DeviceModel device: devices) {
            AlarmDeviceModel model = new AlarmDeviceModel();
            model.setBypassed(subsystem.getBypassedDevices().contains(device.getAddress()));
            model.setName(device.getName());
            // TODO figure out description
            model.setDeviceId( device.getId() );
            model.setOnline( DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE)) );
            models.add(model);
        }
        return models;
    }

    public interface Callback {

        /**
         * Called when the callback is initially registered or when
         * there are a set of changes across multiple sections.
         * @param sections
         */
        void updateSections(List<AlarmDeviceSection> sections);

        /**
         * Called when there is a change in a single section, use
         * AlarmDeviceSection#getId() to match to the changed
         * section.
         * @param section
         */
        void updateSection(AlarmDeviceSection section);
    }
}
