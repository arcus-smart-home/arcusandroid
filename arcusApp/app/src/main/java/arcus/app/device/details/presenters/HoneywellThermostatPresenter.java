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
package arcus.app.device.details.presenters;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;

import com.iris.client.capability.HoneywellTCC;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.activities.FullscreenFragmentActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.banners.CloudAuthorizationErrorBanner;
import arcus.app.common.banners.CloudLoginErrorBanner;
import arcus.app.common.banners.HoneywellServiceLoginErrorBanner;
import arcus.app.common.banners.HoneywellServiceUnavailableBanner;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.popups.CloudCredentialsErrorPopup;
import arcus.app.common.utils.Range;
import arcus.app.device.details.HoneywellCloudThermostatCredentialsRemovedFragment;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class HoneywellThermostatPresenter extends BaseThermostatPresenter {

    private final int THROTTLE_PERIOD_MS = 3000;
    private final int QUIESCENT_PERIOD_MS = 20000;

    // Map of device address -> set of attribute names whose values are being changed
    private final Map<String, Set<String>> pendingValueChanges = new HashMap<>();

    @Override
    public void stopPresenting() {

        removeBanner(HoneywellServiceUnavailableBanner.class);
        removeBanner(HoneywellServiceLoginErrorBanner.class);
        removeBanner(CloudLoginErrorBanner.class);
        removeBanner(CloudAuthorizationErrorBanner.class);

        super.stopPresenting();
    }

    @Override
    public void updateView() {
        if (isAuthorized()) {
            removeBanner(CloudAuthorizationErrorBanner.class);
        } else {
            showBanner(buildCloudAuthorizationErrorBanner());
            removeBanner(HoneywellServiceUnavailableBanner.class, NoConnectionBanner.class);
        }

        if (isRequiringLogin()) {
            removeBanner(NoConnectionBanner.class, HoneywellServiceUnavailableBanner.class);
            showBanner(buildCloudLoginErrorBanner());
        } else {
            removeBanner(CloudLoginErrorBanner.class);
        }

        setWaitingIndicatorVisible(hasPendingValueChanges(), isControlDisabled());
        updateFooterState(showErrorColorBottomBanner());
        super.updateView();
    }

    @Override
    public void commitThrottled() {

        throttle.executeDelayed(new Runnable() {
            @Override
            public void run() {
                // Keep track of changes we're pushing to the cloud
                for (String thisPendingChange : getDeviceModel().getChangedValues().keySet()) {
                    addPendingValueChange(thisPendingChange);
                }

                setWaitingIndicatorVisible(true, isControlDisabled());
                getDeviceModel().commit();
            }
        });

        throttle.executeAfterQuiescence(new Runnable() {
            @Override
            public void run() {
                clearPendingValueChanges(null);     // Timeout; clear all pending changes
                setWaitingIndicatorVisible(false, isControlDisabled());
                updateView();
            }
        }, getQuiescentPeriodMs());

    }

    @Override
    Range<Integer> getRestrictedSetpointRange() {
        return new Range<>(null, null);
    }

    @Override
    boolean isLeafEnabled() {
        return false;
    }

    @Override
    boolean isCloudConnected() {
        return true;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        super.propertyChange(event);

        clearPendingValueChanges(event.getPropertyName());
        setWaitingIndicatorVisible(hasPendingValueChanges(), isControlDisabled());
        updateView();
    }

    protected int getThrottlePeriodMs() {
        return THROTTLE_PERIOD_MS;
    }

    protected int getQuiescentPeriodMs() {
        return QUIESCENT_PERIOD_MS;
    }

    private CloudLoginErrorBanner buildCloudLoginErrorBanner() {
        final CloudLoginErrorBanner errBanner = new CloudLoginErrorBanner(R.layout.cloud_credentials_attention_banner);
        final Fragment currentFragment = (Fragment) getPresentedView();

        errBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeBanner(CloudLoginErrorBanner.class);
                HoneywellCloudThermostatCredentialsRemovedFragment frag = HoneywellCloudThermostatCredentialsRemovedFragment.newInstance();
                frag.setCallback(new HoneywellCloudThermostatCredentialsRemovedFragment.IHoneywellCredentials() {
                    @Override
                    public void authComplete() {
                        BackstackManager.getInstance().navigateBackToFragment(currentFragment);
                    }

                    @Override
                    public void errorEncountered(int errorCode, String description, String failingUrl) {
                        BackstackManager.getInstance().navigateBackToFragment(currentFragment);
                        removeBanner(NoConnectionBanner.class);
                        showBanner(new HoneywellServiceLoginErrorBanner());
                    }
                });

                BackstackManager.getInstance().navigateToFragment(frag, true);
            }
        });

        return errBanner;
    }

    private CloudAuthorizationErrorBanner buildCloudAuthorizationErrorBanner() {
        final CloudAuthorizationErrorBanner errBanner = new CloudAuthorizationErrorBanner(R.layout.cloud_credentials_revoked_banner);
        errBanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity clickActivity = ArcusApplication.getArcusApplication().getForegroundActivity();
                if (clickActivity == null) { // Just to be extra sure we're not null, though we shouldn't be now.
                    return;
                }

                FullscreenFragmentActivity.launch(clickActivity, CloudCredentialsErrorPopup.class);
            }
        });

        return errBanner;
    }

    private void addPendingValueChange(String attribute) {
        logger.debug("Queueing pending value change: {}.", attribute);

        if (pendingValueChanges.get(getDeviceModel().getAddress()) == null) {
            pendingValueChanges.put(getDeviceModel().getAddress(), new HashSet<String>());
        }

        pendingValueChanges.get(getDeviceModel().getAddress()).add(attribute);
    }

    private void clearPendingValueChanges(String attribute) {
        logger.debug("Clearing pending value change: {}.", attribute == null ? "(all changes)" : attribute);

        if (pendingValueChanges.get(getDeviceModel().getAddress()) != null) {
            if (attribute == null) {
                pendingValueChanges.get(getDeviceModel().getAddress()).clear();
            } else {
                pendingValueChanges.get(getDeviceModel().getAddress()).remove(attribute);
            }
        }
    }

    private boolean hasPendingValueChanges() {
        return pendingValueChanges.get(getDeviceModel().getAddress()) != null &&
                pendingValueChanges.get(getDeviceModel().getAddress()).size() > 0;
    }

    @Override
    public boolean isControlDisabled() {
        return hasPendingValueChanges() || isRequiringLogin() || !isAuthorized() || !isDeviceConnected();
    }

    private boolean showErrorColorBottomBanner() {
        return isRequiringLogin() || !isAuthorized() || !isDeviceConnected();
    }

    private boolean isRequiringLogin() {
        return get(HoneywellTCC.class).getRequiresLogin();
    }

    private boolean isAuthorized() {
        return HoneywellTCC.AUTHORIZATIONSTATE_AUTHORIZED.equals(get(HoneywellTCC.class).getAuthorizationState());
    }
}
