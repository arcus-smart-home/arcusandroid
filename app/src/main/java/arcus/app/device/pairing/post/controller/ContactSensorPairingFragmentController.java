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
package arcus.app.device.pairing.post.controller;

import android.app.Activity;

import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.ClientEvent;
import com.iris.client.capability.Contact;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.common.controller.FragmentController;


public class ContactSensorPairingFragmentController extends FragmentController<ContactSensorPairingFragmentController.Callbacks> {

    public interface Callbacks {
        void onDeviceModelLoaded (DeviceModel model);
        void onSuccess();
        void onFailure(Throwable cause);
    }

    private Activity activity;
    private String deviceAddress;

    private static final ContactSensorPairingFragmentController instance = new ContactSensorPairingFragmentController();
    private ContactSensorPairingFragmentController () {}
    public static ContactSensorPairingFragmentController getInstance() { return instance; }

    public void loadDevice (Activity activity, String deviceAddress) {
        this.activity = activity;
        this.deviceAddress = deviceAddress;

        DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                fireOnDeviceModelLoaded(deviceModel);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnFailure(throwable);
            }
        });
    }

    public void setContactSensorHint (final String hint) {
        DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                deviceModel.set(Contact.ATTR_USEHINT, hint);
                commitDeviceModelChanges(deviceModel);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnFailure(throwable);
            }
        });
    }

    private void commitDeviceModelChanges (DeviceModel deviceModel) {
        deviceModel.commit().onSuccess(new Listener<ClientEvent>() {
            @Override
            public void onEvent(ClientEvent clientEvent) {
                fireOnSuccess();
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnFailure(throwable);
            }
        });
    }

    private void fireOnDeviceModelLoaded (final DeviceModel model) {
        if (activity != null) {
            final Callbacks listener = getListener();
            if (listener != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onDeviceModelLoaded(model);
                    }
                });
            }
        }
    }

    private void fireOnSuccess () {
        if (activity != null) {
            final Callbacks listener = getListener();
            if (listener != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSuccess();
                    }
                });
            }
        }
    }

    private void fireOnFailure (final Throwable cause) {
        if (activity != null) {
            final Callbacks listener = getListener();
            if (listener != null) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onFailure(cause);
                    }
                });
            }
        }
    }
}
