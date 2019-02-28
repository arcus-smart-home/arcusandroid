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
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.provider.PersonModelProvider;
import com.iris.client.ClientEvent;
import com.iris.client.capability.Presence;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.PersonModel;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;

import java.util.List;


public class NameDeviceFragmentController extends FragmentController<NameDeviceFragmentController.Callbacks> {

    public interface Callbacks {
        void onDeviceLoaded (DeviceModel deviceModel, boolean supportsPresence, PersonModel currentAssignment, List<PersonModel> people);
        void onHubLoaded (HubModel hubModel);
        void onSuccess();

        void onCorneaError(Throwable cause);
    }

    private static NameDeviceFragmentController instance = new NameDeviceFragmentController();
    private Activity activity;
    private String deviceAddress;

    private NameDeviceFragmentController() {
    }

    public static NameDeviceFragmentController getInstance() {
        return instance;
    }

    public void loadModel(Activity activity, final String deviceAddress) {
        this.activity = activity;
        this.deviceAddress = deviceAddress;

        if (CorneaUtils.isHubAddress(deviceAddress)) {
            loadHubModel(deviceAddress);
        } else {
            PersonModelProvider.instance().reload().onSuccess(new Listener<List<PersonModel>>() {
                @Override
                public void onEvent(List<PersonModel> personModels) {
                    loadDeviceModel(deviceAddress, personModels);
                }
            }).onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    fireOnCorneaError(throwable);
                }
            });
        }
    }

    public void setName(String newName) {
        if (CorneaUtils.isHubAddress(deviceAddress)) {
            setHubName(newName);
        } else {
            setDeviceName(newName);
        }
    }

    public void unassignDevice (DeviceModel device) {
        Presence presenceCap = CorneaUtils.getCapability(device, Presence.class);
        if (presenceCap != null) {
            presenceCap.setPerson("UNSET");
            presenceCap.setUsehint(Presence.USEHINT_UNKNOWN);
            device.commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    fireOnCorneaError(throwable);
                }
            });
        }
    }

    public void assignPersonToDevice (DeviceModel device, String personAddress) {
        Presence presenceCap = CorneaUtils.getCapability(device, Presence.class);

        if (presenceCap != null) {
            presenceCap.setPerson(personAddress);
            presenceCap.setUsehint(Presence.USEHINT_PERSON);
            device.commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    fireOnCorneaError(throwable);
                }
            });
        }
    }

    private void setHubName(final String newHubName) {

        HubModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<HubModel>() {
            @Override
            public void onEvent(HubModel hubModel) {
                commitHubName(hubModel, newHubName);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    private void setDeviceName(final String newDeviceName) {

        DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                commitDeviceName(deviceModel, newDeviceName);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });

    }

    private void loadHubModel(String deviceAddress) {
        HubModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<HubModel>() {
            @Override
            public void onEvent(HubModel hubModel) {
                fireOnHubLoaded(hubModel);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    private void loadDeviceModel(String deviceAddress, final List<PersonModel> people) {
        DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                PersonModel currentAssignment = null;
                Presence presence = CorneaUtils.getCapability(deviceModel, Presence.class);

                // If device supports presence, attempt to locate the person the device is assigned to
                if (presence != null && !StringUtils.isEmpty(presence.getPerson())) {
                    for (PersonModel thisPerson : people) {
                        if (presence.getPerson().equals(thisPerson.getAddress())) {
                            currentAssignment = thisPerson;
                        }
                    }
                }

                fireOnDeviceLoaded(deviceModel, CorneaUtils.hasCapability(deviceModel, Presence.class), currentAssignment, people);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    private void commitDeviceName(DeviceModel deviceModel, String newDeviceName) {

        deviceModel.setName(newDeviceName);
        deviceModel.commit().onSuccess(new Listener<ClientEvent>() {
            @Override
            public void onEvent(ClientEvent clientEvent) {
                fireOnSuccess();
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    private void commitHubName(HubModel hubModel, String newHubName) {

        hubModel.setName(newHubName);
        hubModel.commit().onSuccess(new Listener<ClientEvent>() {
            @Override
            public void onEvent(ClientEvent clientEvent) {
                fireOnSuccess();
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        });
    }

    private void fireOnHubLoaded(final HubModel hubModel) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onHubLoaded(hubModel);
                    }
                }
            });
        }
    }

    private void fireOnDeviceLoaded (final DeviceModel deviceModel, final boolean supportsPresence, final PersonModel currentAssignment, final List<PersonModel> people) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onDeviceLoaded(deviceModel, supportsPresence, currentAssignment, people);
                    }
                }
            });
        }
    }

    private void fireOnSuccess() {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }
            });
        }
    }

    private void fireOnCorneaError(final Throwable cause) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onCorneaError(cause);
                    }
                }
            });
        }
    }

}
