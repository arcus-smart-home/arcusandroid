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
package arcus.app.account.settings.notifications;

import android.app.Activity;

import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import com.iris.client.capability.MobileDevice;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.model.MobileDeviceModel;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.PreferenceUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;


public class SettingsPushNotificationsFragmentController extends FragmentController<SettingsPushNotificationsFragmentController.Callbacks> {

    private static SettingsPushNotificationsFragmentController instance = new SettingsPushNotificationsFragmentController();

    public interface Callbacks {
        void onMobileDevicesLoaded(MobileDeviceModel thisDevice, List<MobileDeviceModel> otherDevices);
        void onDeviceRemoved(MobileDeviceModel removedDevice);
        void onCorneaError(Throwable cause);
    }

    private SettingsPushNotificationsFragmentController() {}
    public static SettingsPushNotificationsFragmentController getInstance() {
        return instance;
    }

    public void removeMobileDevice (final Activity activity, final MobileDeviceModel mobileDeviceModel) {
        SessionController.instance().getPerson().removeMobileDevice(mobileDeviceModel.getDeviceIndex()).onSuccess(new Listener<Person.RemoveMobileDeviceResponse>() {
            @Override
            public void onEvent(Person.RemoveMobileDeviceResponse removeMobileDeviceResponse) {
                fireOnDeviceRemoved(activity, mobileDeviceModel);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(activity, throwable);
            }
        });
    }

    public void loadMobileDevices (final Activity activity) {
        SessionController.instance().getPerson().listMobileDevices().onSuccess(new Listener<Person.ListMobileDevicesResponse>() {
            @Override
            public void onEvent(Person.ListMobileDevicesResponse listMobileDevicesResponse) {
                CorneaClientFactory.getModelCache().retainAll(MobileDevice.NAMESPACE, listMobileDevicesResponse.getMobileDevices());

                List<MobileDeviceModel> mobileDeviceModels = Lists.newArrayList(CorneaClientFactory.getStore(MobileDeviceModel.class).values());
                MobileDeviceModel thisDevice = removeCurrentDeviceFromList(mobileDeviceModels);

                fireOnMobileDevicesLoaded(activity, thisDevice, mobileDeviceModels);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(activity, throwable);
            }
        });
    }

    private MobileDeviceModel removeCurrentDeviceFromList(List<MobileDeviceModel> mobileDeviceModels) {
        String gcmToken = PreferenceUtils.getGcmNotificationToken();
        MobileDeviceModel currentDevice = null;

        // Corner case: Device is not accepting push notifications or is not registered
        if (StringUtils.isEmpty(gcmToken)) {
            return null;
        }

        for (MobileDeviceModel thisDeviceModel : mobileDeviceModels) {
            if (gcmToken.equalsIgnoreCase(thisDeviceModel.getNotificationToken())) {
                currentDevice = thisDeviceModel;
            }
        }

        mobileDeviceModels.remove(currentDevice);
        return currentDevice;
    }

    private void fireOnMobileDevicesLoaded (Activity activity, final MobileDeviceModel thisDevice, final List<MobileDeviceModel> otherDevices) {
        if (getListener() != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onMobileDevicesLoaded(thisDevice, otherDevices);
                    }
                }
            });
        }
    }

    private void fireOnCorneaError (Activity activity, final Throwable cause) {
        if (getListener() != null) {
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

    private void fireOnDeviceRemoved (Activity activity, final MobileDeviceModel removedDevice) {
        if (getListener() != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onDeviceRemoved(removedDevice);
                    }
                }
            });
        }
    }

}
