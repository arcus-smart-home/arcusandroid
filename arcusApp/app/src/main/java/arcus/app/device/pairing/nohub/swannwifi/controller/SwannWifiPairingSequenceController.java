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
package arcus.app.device.pairing.nohub.swannwifi.controller;

import android.Manifest;
import android.app.Activity;

import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.model.DeviceModel;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.pairing.catalog.controller.ProductCatalogSequenceController;
import arcus.app.device.pairing.nohub.swannwifi.SwannAccessPointSelectionFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannAirplaneModeFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannDaysFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannHomeNetworkSelectionFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannRequestPermissionFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannScheduleFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannSuccessFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannTurnOffTimeFragment;
import arcus.app.device.pairing.nohub.swannwifi.SwannTurnOnTimeFragment;
import arcus.app.device.pairing.post.AddToFavoritesFragment;
import arcus.app.device.pairing.post.NameDeviceFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;


public class SwannWifiPairingSequenceController extends AbstractSequenceController {

    private final static Logger logger = LoggerFactory.getLogger(SwannWifiPairingSequenceController.class);

    private final static int DEFAULT_HOUR_ON = 17;
    private final static int DEFAULT_MINUTE_ON = 0;
    private final static int DEFAULT_SECOND_ON = 0;

    private final static int DEFAULT_HOUR_OFF = 23;
    private final static int DEFAULT_MINUTE_OFF = 0;
    private final static int DEFAULT_SECOND_OFF = 0;

    private String homeNetworkSsid;
    private String homeNetworkPassword;
    private String swannApSsid;

    private DeviceModel deviceModel;

    private TimeOfDay scheduledTimeOn = new TimeOfDay(DEFAULT_HOUR_ON, DEFAULT_MINUTE_ON, DEFAULT_SECOND_ON);
    private TimeOfDay scheduledTimeOff = new TimeOfDay(DEFAULT_HOUR_OFF, DEFAULT_MINUTE_OFF, DEFAULT_SECOND_OFF);
    private EnumSet<DayOfWeek> scheduledDays = EnumSet.noneOf(DayOfWeek.class);

    private AtomicBoolean pairingInProgress = new AtomicBoolean(false);
    private boolean didShowPermissionScreen;

    @Override
    public void goNext(final Activity activity, final Sequenceable from, Object... data) {

        if (from instanceof SwannRequestPermissionFragment) {
            navigateForward(activity, SwannAirplaneModeFragment.newInstance(getPairingStepOffset()), data);
        }

        else if (from instanceof SwannAirplaneModeFragment) {
            navigateForward(activity, SwannHomeNetworkSelectionFragment.newInstance(getPairingStepOffset() + 1));
        }

        else if (from instanceof SwannHomeNetworkSelectionFragment) {
            navigateForward(activity, SwannAccessPointSelectionFragment.newInstance(getPairingStepOffset() + 2), data);
        }

        else if (from instanceof SwannAccessPointSelectionFragment) {
            deviceModel = unpackArgument(0, DeviceModel.class, data);
            navigateForward(activity, NameDeviceFragment.newInstance(NameDeviceFragment.ScreenVariant.DEVICE_PAIRING, deviceModel.getName(), deviceModel.getAddress()));
        }

        else if (from instanceof NameDeviceFragment) {
            navigateForward(activity, SwannScheduleFragment.newInstance());
        }

        else if (from instanceof SwannScheduleFragment) {
            if (unpackArgument(0, Boolean.class, data)) {
                if (data[0].equals(true)) {
                    navigateForward(activity, SwannTurnOnTimeFragment.newInstance());
                }
            }
            else {
                navigateForward(activity, AddToFavoritesFragment.newInstance(deviceModel.getAddress()));
            }
        }

        else if (from instanceof SwannTurnOnTimeFragment) {
            navigateForward(activity, SwannTurnOffTimeFragment.newInstance());
        }

        else if (from instanceof SwannTurnOffTimeFragment) {
            navigateForward(activity, SwannDaysFragment.newInstance());
        }

        else if (from instanceof SwannDaysFragment) {
            ((SwannDaysFragment) from).showProgressBar();
            SwannScheduleController.getInstance().saveSchedule(new SwannScheduleController.Callbacks() {
                @Override
                public void onSuccess() {
                    ((SwannDaysFragment) from).hideProgressBar();

                    if (SubscriptionController.isPremiumOrPro()) {
                        navigateForward(activity, AddToFavoritesFragment.newInstance(deviceModel.getAddress()));
                    } else {
                        navigateForward(activity, SwannSuccessFragment.newInstance());
                    }
                }

                @Override
                public void onFailure(Throwable cause) {
                    ((SwannDaysFragment) from).hideProgressBar();
                    ErrorManager.in(activity).showGenericBecauseOf(cause);
                }
            }, deviceModel.getAddress(), getScheduledTimeOn(), getScheduledTimeOff(), getScheduledDays());
        }

        else if (from instanceof AddToFavoritesFragment) {
            navigateForward(activity, SwannSuccessFragment.newInstance());
        }

        else if (from instanceof SwannSuccessFragment) {
            endSequence(activity, true);
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof SwannRequestPermissionFragment || from instanceof SwannAirplaneModeFragment) {
            endSequence(activity, false);
        }

        else if (from instanceof SwannHomeNetworkSelectionFragment) {
            navigateBack(activity, SwannAirplaneModeFragment.newInstance(getPairingStepOffset()));
        }

        else if (from instanceof SwannAccessPointSelectionFragment) {
            // Don't allow navigation out of this fragment while pairing is in process
            if (!pairingInProgress.get()) {
                navigateBack(activity, SwannHomeNetworkSelectionFragment.newInstance(getPairingStepOffset() + 1));
            }
        }

        else if (from instanceof NameDeviceFragment) {
            // Can't go back into the pairing process; user must go forward
        }

        else if (from instanceof SwannScheduleFragment) {
            navigateBack(activity, NameDeviceFragment.newInstance(NameDeviceFragment.ScreenVariant.DEVICE_PAIRING, deviceModel.getName(), deviceModel.getAddress()));
        }

        else if (from instanceof SwannTurnOnTimeFragment) {
            navigateBack(activity, SwannScheduleFragment.newInstance());
        }

        else if (from instanceof SwannTurnOffTimeFragment) {
            navigateBack(activity, SwannTurnOnTimeFragment.newInstance());
        }

        else if (from instanceof SwannDaysFragment) {
            navigateBack(activity, SwannTurnOffTimeFragment.newInstance());
        }

        else if (from instanceof AddToFavoritesFragment) {
            if (BackstackManager.getInstance().isFragmentOnStack(SwannDaysFragment.class)) {
                navigateBack(activity, SwannDaysFragment.newInstance());
            } else {
                navigateBack(activity, SwannScheduleFragment.newInstance());
            }
        }

        else if (from instanceof SwannSuccessFragment) {
            // Nothing to do; user cannot back out of success
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        if (activity instanceof BaseActivity) {
            logger.debug("Clearing keep-screen-on flag.");
            ((BaseActivity) activity).setKeepScreenOn(false);
        }
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        if (activity instanceof BaseActivity) {
            logger.debug("Setting keep-screen-on flag during Swann pairing process.");
            ((BaseActivity) activity).setKeepScreenOn(true);

            if (((BaseActivity) activity).hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                didShowPermissionScreen = false;
                navigateForward(activity, SwannAirplaneModeFragment.newInstance(getPairingStepOffset()), null);
            } else {
                didShowPermissionScreen = true;
                navigateForward(activity, SwannRequestPermissionFragment.newInstance());
            }
        }
    }

    public void abortToFirstStep(Activity activity) {
        navigateForward(activity, SwannAirplaneModeFragment.newInstance(getPairingStepOffset()), null);
    }

    public void abortToProductCatalog(Activity activity) {
        // Abort sequence and return to product catalog
        new ProductCatalogSequenceController().startSequence(activity, null);
    }

    public String getHomeNetworkSsid() {
        return homeNetworkSsid;
    }

    public void setHomeNetworkSsid(String homeNetworkSsid) {
        this.homeNetworkSsid = homeNetworkSsid;
    }

    public String getHomeNetworkPassword() {
        return homeNetworkPassword;
    }

    public void setHomeNetworkPassword(String homeNetworkPassword) {
        this.homeNetworkPassword = homeNetworkPassword;
    }

    public String getSwannApSsid() {
        return swannApSsid;
    }

    public void setSwannApSsid(String swannApSsid) {
        this.swannApSsid = swannApSsid;
    }

    public DeviceModel getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(DeviceModel deviceModel) {
        this.deviceModel = deviceModel;
    }

    public void setPairingInProgress(boolean pairingInProgress) {
        this.pairingInProgress.set(pairingInProgress);
    }

    public TimeOfDay getScheduledTimeOn() {
        return scheduledTimeOn;
    }

    public void setScheduledTimeOn(TimeOfDay scheduledTimeOn) {
        this.scheduledTimeOn = scheduledTimeOn;
    }

    public TimeOfDay getScheduledTimeOff() {
        return scheduledTimeOff;
    }

    public void setScheduledTimeOff(TimeOfDay scheduledTimeOff) {
        this.scheduledTimeOff = scheduledTimeOff;
    }

    public EnumSet<DayOfWeek> getScheduledDays() {
        return scheduledDays;
    }

    public void setScheduledDays(EnumSet<DayOfWeek> scheduledDays) {
        this.scheduledDays = scheduledDays;
    }

    /**
     * When displaying the Android permission screen for Swann pairing, we have to offset the rest
     * of the step icons to compensate for the extra pairing step.
     * @return The step index of the first custom pairing screen (i.e., the home network selection
     * screen).
     */
    private int getPairingStepOffset() {
        return didShowPermissionScreen ? 3 : 2;
    }
}
