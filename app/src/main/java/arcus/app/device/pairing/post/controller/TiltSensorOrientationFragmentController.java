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

import com.google.common.collect.ImmutableSet;
import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.ClientEvent;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.GlobalSetting;


public class TiltSensorOrientationFragmentController extends FragmentController<TiltSensorOrientationFragmentController.Callbacks> {

    public interface Callbacks {
        void onDeviceModelLoaded (DeviceModel model);
        void onSuccess();
        void onFailure(Throwable cause);
    }

    private Activity activity;
    private String deviceAddress;

    private static final TiltSensorOrientationFragmentController instance = new TiltSensorOrientationFragmentController();
    private TiltSensorOrientationFragmentController() {}
    public static TiltSensorOrientationFragmentController getInstance () { return instance; }

    public void loadDeviceModel (Activity activity, String deviceAddress) {
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

    public void setClosedOnVertical (final boolean isClosedOnVeritical) {

        DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                if (isClosedOnVeritical) {
                    deviceModel.addTags(ImmutableSet.of(GlobalSetting.VERTICAL_TILT_TAG));
                } else {
                    deviceModel.removeTags(ImmutableSet.of(GlobalSetting.VERTICAL_TILT_TAG));
                }
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

    private void fireOnDeviceModelLoaded(final DeviceModel deviceModel) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onDeviceModelLoaded(deviceModel);
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

    private void fireOnFailure(final Throwable cause) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onFailure(cause);
                    }
                }
            });
        }
    }

}
