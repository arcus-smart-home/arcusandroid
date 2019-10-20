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
package arcus.cornea.device.contactsensor;

import arcus.cornea.device.DeviceController;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Contact;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Temperature;
import com.iris.client.model.DeviceModel;

public class ContactSensorController extends DeviceController<ContactSensorProxyModel> {

    public static ContactSensorController newController(String deviceId, DeviceController.Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        ContactSensorController controller = new ContactSensorController(source);
        controller.setCallback(callback);
        return controller;
    }

    ContactSensorController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
                Contact.ATTR_CONTACT,
                Temperature.ATTR_TEMPERATURE,
                DeviceConnection.ATTR_STATE
        );
    }

    @Override
    protected ContactSensorProxyModel update(DeviceModel device) {
        ContactSensorProxyModel model = new ContactSensorProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());

        Contact contact = (Contact) device;

        model.setLastStateChange(contact.getContactchanged());

        if(contact.getContact() != null) {
            model.setState(ContactSensorProxyModel.State.valueOf(contact.getContact()));
        }

        if(contact.getUsehint() != null) {
            model.setUseHint(ContactSensorProxyModel.UseHint.valueOf(contact.getUsehint()));
        }

        model.setOnline(!DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        if (device.getCaps().contains(Temperature.NAMESPACE)) {
            Temperature temperature = (Temperature) device;
            model.setTemperature(temperature.getTemperature());
        }
        return model;
    }
}
