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
package arcus.app.device.details;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.HoneywellTCC;
import com.iris.client.capability.Thermostat;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.activities.FullscreenFragmentActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.CloudAuthorizationErrorBanner;
import arcus.app.common.banners.CloudLoginErrorBanner;
import arcus.app.common.banners.HoneywellServiceUnavailableBanner;
import arcus.app.common.banners.HoneywellServiceLoginErrorBanner;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.CloudCredentialsErrorPopup;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.utils.EventTimeoutController;
import arcus.app.common.utils.ThrottledDelayedExecutor;
import arcus.cornea.subsystem.climate.EventMessageMonitor;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class HoneywellCloudThermostatFragment extends ThermostatFragment implements IShowedFragment, IClosedFragment, View.OnClickListener,
        HoneywellCloudThermostatCredentialsRemovedFragment.IHoneywellCredentials, EventTimeoutController.EventTimeoutCallback {
    protected static final int THROTTLE_PERIOD_MS = 3000;
    protected static final int PROCESS_THROTTLE_PERIOD_MS = 1000;
    protected static final int WAIT_THROTTLE_PERIOD_MS = 120000;

    private EventMessageMonitor eventMessageMonitor;
    private EventTimeoutController eventTimeoutController;

    protected boolean bEnabledByProduct = true;

    protected Fragment currentFragment;
    protected final ThrottledDelayedExecutor setPointDelayThrottle = new ThrottledDelayedExecutor(THROTTLE_PERIOD_MS);
    protected final ThrottledDelayedExecutor modeDelayThrottle = new ThrottledDelayedExecutor(THROTTLE_PERIOD_MS);
    protected ThrottledDelayedExecutor waitDelayThrottle = new ThrottledDelayedExecutor(WAIT_THROTTLE_PERIOD_MS);
    protected ThrottledDelayedExecutor processUpdatesDelayThrottle = new ThrottledDelayedExecutor(PROCESS_THROTTLE_PERIOD_MS);

    private final HoneywellCloudThermostatCredentialsRemovedFragment.IHoneywellCredentials callback =
            new HoneywellCloudThermostatCredentialsRemovedFragment.IHoneywellCredentials() {
                @Override
                public void authComplete() {
                    BackstackManager.getInstance().navigateBackToFragment(currentFragment);
                }

                @Override
                public void errorEncountered(int errorCode, String description, String failingUrl) {
                    BackstackManager.getInstance().navigateBackToFragment(currentFragment);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                showHoneywellLoginErrorBanner();
                            } catch (Exception ignored) {}
                        }
                    },500);
                }
            };


    protected int getThrottleValue() {
        return THROTTLE_PERIOD_MS;
    }

    @NonNull
    public static HoneywellCloudThermostatFragment newInstance() {
        return new HoneywellCloudThermostatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        return view;
    }
    @Override
    public void doBottomSection() {
        super.doBottomSection();
        cloudIcon.setVisibility(View.VISIBLE);
    }

    @Override
    public void setEnabled (boolean isEnabled) {
        bEnabledByProduct = isEnabled;
        if(shouldDisableControls()) {
            super.setEnabled(false);
        }
        else {
            super.setEnabled(isEnabled);
        }
        //this doesn't look right yet.
        waitingLabel.setEnabled(true);
        justAMoment.setEnabled(true);
        updateBottomView();
        //checkDeviceConnection();
    }

    @Override
    public void onResume() {
        super.onResume();
        currentFragment = BackstackManager.getInstance().getCurrentFragment();
        eventMessageMonitor = EventMessageMonitor.getInstance();
        eventTimeoutController = EventTimeoutController.getInstance();
        if (getDeviceModel() != null) {
            eventTimeoutController.setCallback(getDeviceModel().getId(), this);
            checkForOnGoingMessages();
        }
    }

    private boolean shouldDisableControls() {
        if(getDeviceModel() != null) {
            HoneywellTCC tcc = (HoneywellTCC) getDeviceModel();
            if(tcc.getRequiresLogin() || tcc.getAuthorizationState().equals(HoneywellTCC.AUTHORIZATIONSTATE_DEAUTHORIZED)) {
                return true;
            }
        }
        else if(checkForOnGoingMessages()) {
            return true;
        }
        return false;
    }

    @Override
    public void onShowedFragment() {
        super.onShowedFragment();
        checkConnection();
        eventMessageMonitor = EventMessageMonitor.getInstance();
        eventTimeoutController = EventTimeoutController.getInstance();
        if(getDeviceModel() != null) {
            HoneywellTCC tcc = (HoneywellTCC) getDeviceModel();
            if (tcc.getRequiresLogin()) {
                showHoneywellLoginInformationBanner();
            } else if (tcc.getAuthorizationState().equals(HoneywellTCC.AUTHORIZATIONSTATE_DEAUTHORIZED)) {
                showHoneywellAccountRevokedBanner();
            }
            eventTimeoutController.setCallback(getDeviceModel().getId(), this);
        }
        setControlPoints();
        updateUI();
    }

    private void updateUI() {
        if (getDeviceModel() != null) {
            HoneywellTCC tcc = (HoneywellTCC) getDeviceModel();
            if(tcc.getRequiresLogin()) {
                showHoneywellLoginInformationBanner();
            }
            else if(tcc.getAuthorizationState().equals(HoneywellTCC.AUTHORIZATIONSTATE_DEAUTHORIZED)) {
                showHoneywellAccountRevokedBanner();
            }
            if(!checkForOnGoingMessages()) {
                hideWaitingText();
            }
            else {
                showWaitingText();
                waitDelayThrottle = eventTimeoutController.getTimer(getDeviceModel().getId(), "WAIT_TIMER"+getDeviceModel().getId());
                if(waitDelayThrottle == null || !waitDelayThrottle.hasTask()) {
                    waitDelayThrottle = new ThrottledDelayedExecutor(WAIT_THROTTLE_PERIOD_MS);
                    waitDelayThrottle.execute(new Runnable() {
                        @Override
                        public void run() {
                            EventTimeoutController.timedOut(getDeviceModel().getId());
                        }
                    });
                    eventTimeoutController.setTimer(getDeviceModel().getId(), "WAIT_TIMER"+getDeviceModel().getId(), waitDelayThrottle);
                }
            }
        }
    }

    protected void checkTimeout() {
        eventMessageMonitor.clearStale(getDeviceModel().getId());
        hideWaitingText();
    }

    protected void showWaitingText() {
        waitingLabel.setVisibility(View.VISIBLE);
        justAMoment.setVisibility(View.VISIBLE);
        setEnabledNoLoad(false);
        updateBottomView();
    }

    protected void hideWaitingText() {
        waitingLabel.setVisibility(View.GONE);
        justAMoment.setVisibility(View.GONE);
        if(shouldDisableControls()) {
            setEnabledNoLoad(false);
        }
        else {
            setEnabledNoLoad(bEnabledByProduct);
        }
        updateBottomView();
    }

    private void showHoneywellServiceUnavailableBanner() {
        BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
        BannerManager.in(getActivity()).showBanner(new HoneywellServiceUnavailableBanner());
        updateBottomView();
    }

    private void showHoneywellLoginErrorBanner() {
        BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
        BannerManager.in(getActivity()).showBanner(new HoneywellServiceLoginErrorBanner());
        updateBottomView();
    }

    private void showHoneywellLoginInformationBanner() {
        BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
        BannerManager.in(getActivity()).removeBanner(HoneywellServiceUnavailableBanner.class);
        final CloudLoginErrorBanner errBanner = new CloudLoginErrorBanner(R.layout.cloud_credentials_attention_banner);
        errBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BannerManager.in(getActivity()).removeBanner(CloudLoginErrorBanner.class);
                HoneywellCloudThermostatCredentialsRemovedFragment frag = HoneywellCloudThermostatCredentialsRemovedFragment
                      .newInstance();
                frag.setCallback(callback);
                BackstackManager.getInstance().navigateToFragment(frag, true);
            }
        });
        BannerManager.in(getActivity()).showBanner(errBanner);
        updateBottomView();
    }

    private void showHoneywellAccountRevokedBanner() {
        // If you would go Card -> Details -> Back to Card -> Details -> Click on this banner, it'd crash 100% of the time
        // because of a old reference to the activity.
        // Adding in a remove, before we add this banner onto the view seems to solve using a stale activity;
        // However, added in a check (just to be on the safe side as well).
        BannerManager.in(getActivity()).removeBanner(CloudAuthorizationErrorBanner.class);
        final CloudAuthorizationErrorBanner errBanner = new CloudAuthorizationErrorBanner(R.layout.cloud_credentials_revoked_banner);
        errBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity clickActivity = getActivity();
                if (clickActivity == null) { // Just to be extra sure we're not null, though we shouldn't be now.
                    return;
                }

                FullscreenFragmentActivity.launch(clickActivity, CloudCredentialsErrorPopup.class);
            }
        });
        BannerManager.in(getActivity()).showBanner(errBanner);
        updateBottomView();

        BannerManager.in(getActivity()).removeBanner(HoneywellServiceUnavailableBanner.class);
        BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
    }

    @Override
    public void onClosedFragment() {
        removeBanners();
    }

    private void removeBanners() {
        BannerManager.in(getActivity()).removeBanner(HoneywellServiceUnavailableBanner.class);
        BannerManager.in(getActivity()).removeBanner(HoneywellServiceLoginErrorBanner.class);
        BannerManager.in(getActivity()).removeBanner(CloudLoginErrorBanner.class);
        BannerManager.in(getActivity()).removeBanner(CloudAuthorizationErrorBanner.class);
        updateBottomView();
    }

    @Override public void onDestroy() {
        super.onDestroy();
        onClosedFragment();
    }

    @Override public void deviceReconnected() {
        BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
        BannerManager.in(getActivity()).removeBanner(HoneywellServiceUnavailableBanner.class);
    }

    @Override public void displayNoConnectionBanner() {
        showHoneywellServiceUnavailableBanner();
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        if(eventTimeoutController.getTimer(getDeviceModel().getId(), "HONEYWELLTCC_PROCESS_TIMER") != null) {
            return;
        }
        switch (event.getPropertyName()) {
            case Thermostat.ATTR_COOLSETPOINT:
                eventMessageMonitor.removeScheduledEvent(getDeviceModel().getId(), Thermostat.ATTR_COOLSETPOINT);
            case Thermostat.ATTR_HEATSETPOINT:
                eventMessageMonitor.removeScheduledEvent(getDeviceModel().getId(), Thermostat.ATTR_HEATSETPOINT);
                break;
            case Thermostat.ATTR_HVACMODE:
                eventMessageMonitor.removeScheduledEvent(getDeviceModel().getId(), Thermostat.ATTR_HVACMODE);
                break;
            default:
                break;
        }

        //do the switch above before super so it can call to reset the timers in its execution, so the UI updates at the same time we enable everything.
        super.propertyUpdated(event);

        if(event.getPropertyName().equals(HoneywellTCC.ATTR_AUTHORIZATIONSTATE)) {
            if(event.getNewValue().equals(HoneywellTCC.AUTHORIZATIONSTATE_DEAUTHORIZED)) {
                showHoneywellAccountRevokedBanner();
            }
            else {
                BannerManager.in(getActivity()).removeBanner(CloudAuthorizationErrorBanner.class);
            }
        }
        if(event.getPropertyName().equals(HoneywellTCC.ATTR_REQUIRESLOGIN)) {
            if((boolean) event.getNewValue()) {
                showHoneywellLoginInformationBanner();
            }
            else {
                BannerManager.in(getActivity()).removeBanner(CloudLoginErrorBanner.class);
            }
        }
        if(!checkForOnGoingMessages()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub

                    hideWaitingText();
                }
            });
        }

    }

    @Override
    protected void requestUpdatePlatformSetPoints() {
        final int heatProgress = seekArc.getProgress(DeviceSeekArc.THUMB_LOW);
        final int coolProgress = seekArc.getProgress(DeviceSeekArc.THUMB_HIGH);
        Thermostat device = getCapability(Thermostat.class);
        if (device != null) {
            if(device.getCoolsetpoint()!= null && TemperatureUtils.celsiusToFahrenheit(device.getCoolsetpoint()) != coolProgress) {
                eventMessageMonitor.scheduleEvent(device.getId(), Thermostat.ATTR_COOLSETPOINT);
            }
            if(device.getHeatsetpoint()!= null && TemperatureUtils.celsiusToFahrenheit(device.getHeatsetpoint()) != heatProgress) {
                eventMessageMonitor.scheduleEvent(device.getId(), Thermostat.ATTR_HEATSETPOINT);
            }
        }

        setPointDelayThrottle.execute(new Runnable() {
            @Override
            public void run() {
                addProcessTimer();
                setPlatformSetPoints(heatProgress, coolProgress);
            }
        });
    }

    private void addProcessTimer() {
        processUpdatesDelayThrottle.execute(new Runnable() {
            @Override
            public void run() {
                eventTimeoutController.removeTimer(getDeviceModel().getId(), "HONEYWELLTCC_PROCESS_TIMER");
            }
        });
        eventTimeoutController.setTimer(getDeviceModel().getId(), "HONEYWELLTCC_PROCESS_TIMER", processUpdatesDelayThrottle);
    }
    @Override
    protected void requestUpdateHvacMode (final String mode) {
        Thermostat device = getCapability(Thermostat.class);
        if (device != null) {
            eventMessageMonitor.scheduleEvent(device.getId(), Thermostat.ATTR_HVACMODE);
        }
        modeDelayThrottle.execute(new Runnable() {
            @Override
            public void run() {
                addProcessTimer();
                setPlatformHvacMode(mode);
            }
        });
    }

    @Override
    protected void setPlatformSetPoints(double heat, double cool) {
        final Thermostat device = getCapability(Thermostat.class);
        if (device != null) {
            waitDelayThrottle = new ThrottledDelayedExecutor(WAIT_THROTTLE_PERIOD_MS);
            waitDelayThrottle.execute(new Runnable() {
                @Override
                public void run() {
                    EventTimeoutController.timedOut(device.getId());
                }
            });
            eventTimeoutController.setTimer(getDeviceModel().getId(), "WAIT_TIMER"+getDeviceModel().getId(), waitDelayThrottle);

            device.setHeatsetpoint(TemperatureUtils.fahrenheitToCelsius(heat));
            device.setCoolsetpoint(TemperatureUtils.fahrenheitToCelsius(cool));
            showWaitingText();
            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Error updating thermostat set points.", throwable);
                }
            });
        } else {
            logger.error("Ignoring request to set platform heat set point on null device model.");
        }
    }

    protected void setPlatformHvacMode(String mode) {
        final Thermostat device = getCapability(Thermostat.class);
        if (device != null) {
            waitDelayThrottle = new ThrottledDelayedExecutor(WAIT_THROTTLE_PERIOD_MS);
            waitDelayThrottle.execute(new Runnable() {
                @Override
                public void run() {
                    EventTimeoutController.timedOut(device.getId());
                }
            });
            eventTimeoutController.setTimer(getDeviceModel().getId(), "WAIT_TIMER"+getDeviceModel().getId(), waitDelayThrottle);
            device.setHvacmode(mode);
            showWaitingText();
            getDeviceModel().commit().onFailure(new Listener<Throwable>() {
                @Override
                public void onEvent(Throwable throwable) {
                    logger.error("Error updating thermostat mode.", throwable);
                }
            });
        } else {
            logger.error("Ignoring request to set HVAC mode on null device model.");
        }
    }

    private void updateBottomView() {
        DeviceModel device = getDeviceModel();
        if(BannerManager.in(getActivity()).containsBanner(CloudLoginErrorBanner.class) ||
                BannerManager.in(getActivity()).containsBanner(CloudAuthorizationErrorBanner.class) ||
                BannerManager.in(getActivity()).containsBanner(HoneywellServiceUnavailableBanner.class) ||
                BannerManager.in(getActivity()).containsBanner(HoneywellServiceLoginErrorBanner.class) ||
                (device !=null && !DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE)))) {
            bottomView.setBackgroundColor(getResources().getColor(R.color.pink_banner));
            bottomView.getBackground().setColorFilter(null);
            bottomView.setEnabled(true);
        }
        else {
            bottomView.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));
        }
        //some crap to make the bottom bar look right here.
        setEnabledRecursively(bottomView, true);
        setEnabledRecursively(justAMoment, true);
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.thermostat_mode_auto:
                Thermostat therm = (Thermostat)getDeviceModel();
                if(!therm.getSupportsAuto()) {
                    alertDialog.dismiss();
                    showAutoModeError();
                }
                else {
                    setUiHvacMode(Thermostat.HVACMODE_AUTO);
                    requestUpdateHvacMode(Thermostat.HVACMODE_AUTO);
                    alertDialog.dismiss();
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    public void showAutoModeError() {
        AlertPopup popup = AlertPopup.newInstance(getString(R.string.device_more_auto_mode),
                getString(R.string.device_more_honeywell_c2c_device_information), null, null, new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return false;
                    }

                    @Override
                    public void close() {
                        BackstackManager.getInstance().navigateBack();
                        getActivity().invalidateOptionsMenu();
                    }
                });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override
    protected void updateScheduleText(boolean show) {
        nextEventLabel.setVisibility(View.INVISIBLE);
        nextEventDescription.setVisibility(View.INVISIBLE);
    }

    @Override
    public void authComplete() {
        BackstackManager.getInstance().navigateBackToFragment(this);
    }

    @Override
    public void errorEncountered(int errorCode, String description, String failingUrl) {

    }

    protected void resetWaitTimeoutCheck() {
        if(!checkForOnGoingMessages()) {
            /*waitDelayThrottle.execute(new Runnable() {
                @Override
                public void run() {
                    //no-op
                }
            });*/
            hideWaitingText();
        }
    }

    private boolean checkForOnGoingMessages() {
        //we need to check and see if there are messages in progress
        HashMap<String, Long> deviceEventMap = eventMessageMonitor.getScheduleForDevice(getDeviceModel().getId());
        if(deviceEventMap != null) {
            for(Iterator<Map.Entry<String, Long>> it = deviceEventMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Long> entry = it.next();
                if (entry.getValue() < (System.currentTimeMillis()+WAIT_THROTTLE_PERIOD_MS)) {
                    //disable screen
                    showWaitingText();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void timeoutReached() {
        checkTimeout();
    }
}
