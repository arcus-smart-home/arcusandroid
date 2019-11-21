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
package arcus.app.common.fragments;

import android.app.Activity;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.ActionBar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.CorneaService;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubPower;
import com.iris.client.model.HubModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.banners.FirmwareUpdatingBanner;
import arcus.app.common.banners.InvitationBanner;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.banners.NoHubConnectionBanner;
import arcus.app.common.banners.PoorConnectionBanner;
import arcus.app.common.banners.RunningOnBatteryBanner;
import arcus.app.common.banners.TextOnlyBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.details.ArcusProductFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class BaseFragment extends Fragment {
    public static RegistrationContext registrationContext = ArcusApplication.getRegistrationContext();

    //'this' will resolve to whatever the sub-class is at runtime.
    public Logger logger = LoggerFactory.getLogger(this.getClass());
    private BaseFragmentInterface callback;

    public interface BaseFragmentInterface {
        void backgroundUpdated();
    }

    public void setBaseFragmentCallback(BaseFragmentInterface callback) {
        this.callback = callback;
    }
    @Nullable
    public abstract String getTitle();
    public abstract Integer getLayoutId();

    @Override
    public void onAttach(@NonNull Activity activity) {
        logger.debug("Attaching fragment " + this.getClass().getSimpleName());

        super.onAttach(activity);
        activity.invalidateOptionsMenu();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        try {
            if(getActivity() instanceof BaseActivity) {
                ((BaseActivity) getActivity()).showTitle(true);
            }
        } catch (NullPointerException npe){
            logger.error("No title to set", npe);
        }
        return view;
    }

    public boolean isHomeFragment() {
        return this instanceof HomeFragment;
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public String getResourceString(@StringRes int resId) {
        String resourceString = "";
        if (getActivity() != null && isAdded()) {
            resourceString = getString(resId);
        }
        return resourceString;
    }

    public String getResourceString(@StringRes int resId, @Nullable Object... formatArgs) {
        String resourceString = "";
        if (getActivity() != null && isAdded()) {
            resourceString = getString(resId, formatArgs);
        }
        return resourceString;
    }



    @Nullable
    public Integer getMenuId() {
        return null;
    }

    public boolean submit() {
        return true;
    }

    public boolean validate() {
        return Boolean.TRUE;
    }

    public boolean onBackPressed() {

        if (this instanceof Sequenceable) {
            ((Sequenceable) this).goBack(getActivity(), (Sequenceable) this, null);
            return true;
        }

        return false;
    }

    public CorneaService getCorneaService() {
        final BaseActivity base = (BaseActivity) getActivity();
        return base.getCorneaService();
    }

    /**
     * Gets the view to be shown/hidden as the "progress bar". Override in subclasses to allow
     * fragments to specify their own custom progress indicators/spinners.
     *
     * @return The view to be hidden/shown to indicate progress is pending. Return null to use the
     * BaseActivity's view.
     */
    public View getIndeterminateProgressView() {
        return null;
    }

    public void showProgressBarAndDisable(View... views) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            ((BaseActivity) activity).showProgressIndicator();
        }

        enableViews(false, views);
    }

    public void hideProgressBarAndEnable(View... views) {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            ((BaseActivity) activity).hideProgressIndicator();
        }

        enableViews(true, views);
    }

    public void showProgressBar() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            ((BaseActivity) activity).showProgressIndicator();
        }
    }

    public void hideProgressBar() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            ((BaseActivity) activity).hideProgressIndicator();
        }
    }

    public boolean isProgressBarVisible () {
        Activity activity = getActivity();
        if (activity != null && activity instanceof BaseActivity) {
            return ((BaseActivity) activity).isProgressIndicatorVisible();
        }

        return false;
    }

    private void enableViews(boolean shouldEnable, @NonNull View... views) {
        for (View singleView : views) {
            try {
                singleView.setEnabled(shouldEnable);
            } catch (Exception ex) {
                logger.debug("Exception trying to {} view.", shouldEnable ? "enable" : "disable", ex);
            }
        }
    }

    protected void instancePropertyUpdated(String instanceName, @NonNull PropertyChangeEvent event) {
        logger.debug("Received update for instance [{}], Key::Value [{}]::[{}]", instanceName, event.getPropertyName(), event.getNewValue());
    }

    //TODO: NOT TESTED YET
    public void displayRunOnBatteryBanner() {
        BannerManager.in(getActivity()).showBanner(new RunningOnBatteryBanner());
    }

    public void deviceReconnected() {}

    public void displayNoStreamingBanner() {
        BannerManager.in(getActivity()).removeBanner(TextOnlyBanner.class);
        BannerManager.in(getActivity()).showBanner(new TextOnlyBanner(getString(R.string.streaming_disabled_description)));
    }

    public void displayNoConnectionBanner() {

        if(this.getClass().getSimpleName().equals("HubFragment")){
            BannerManager.in(getActivity()).showBanner(new NoConnectionBanner(true));
        }else{
            BannerManager.in(getActivity()).showBanner(new NoConnectionBanner());
        }

    }

    public void displayHubNoConnectionBanner(){
        BannerManager.in(getActivity()).showBanner(new NoHubConnectionBanner());
    }

    public void displayDeviceFirmwareUpdatingBanner() {
        BannerManager.in(getActivity()).showBanner(new FirmwareUpdatingBanner());
    }

    public void displayPoorConnectionBanner() {
        BannerManager.in(getActivity()).showBanner(new PoorConnectionBanner());
    }


    /**
     * update background color based on the current device connectivity
     */
    public void updateBackground(boolean isConnected) {
        try {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            final ColorFilter filter = new ColorMatrixColorFilter(cm);
            final ColorFilter filterOnline = getOnlineColorFilter();
            final View bgView = ImageManager.getWallpaperView();
            if (bgView != null) {
                final Drawable bgDrawable = bgView.getBackground();
                if (bgDrawable != null) {
                    bgDrawable.setColorFilter(isConnected ? filterOnline : filter);
                }
            }
            if(this instanceof ArcusProductFragment) {
                final ArcusProductFragment fragment = (ArcusProductFragment) this;
                fragment.setEnabled(isConnected);
            }
            if(callback != null) {
                callback.backgroundUpdated();
            }
        }catch (Exception e){
            logger.error("Can't change background color filter: {}", e);
        }
    }

    protected ColorFilter getOnlineColorFilter() {
        return null;
    }

    public int getColorFilterValue() { return -1; }

    public void updateHubConnection(@Nullable final String hubState) {
        Activity activity = getActivity();
        if (activity == null) {
            return; // If we've become detached, called before we're attached, or activity is destroyed.
        }

        BannerManager.in(activity).removeBanner(NoConnectionBanner.class);
        BannerManager.in(activity).removeBanner(NoHubConnectionBanner.class);
        BannerManager.in(activity).removeBanner(InvitationBanner.class);
        BannerManager.in(activity).removeBanner(RunningOnBatteryBanner.class);

        if (Hub.STATE_DOWN.equals(hubState)) {
            if (this instanceof HomeFragment) {
                displayHubNoConnectionBanner();
            }
            else {
                displayNoConnectionBanner();
                updateBackground(false);
            }
        } else if (Hub.STATE_NORMAL.equals(hubState)) {
            // If the hub just came back online, check power source in case
            // line power was lost while it was down, or line power
            // was restored while the hub went offline while on battery
            checkHubPower();

        } else if (HubPower.SOURCE_BATTERY.equals(hubState)){
            displayRunOnBatteryBanner();
        } else {
            if (!(this instanceof HomeFragment)) {
                updateBackground(true);
            }
        }
    }

    public void checkHubConnection(){
        try {
            final HubModel hubModel = SessionModelManager.instance().getHubModel();
            if (hubModel != null) {
                String hubState = String.valueOf(hubModel.get(Hub.ATTR_STATE));
                updateHubConnection(hubState);
            }
        } catch (Exception e){
            logger.error("Can't get hub capability: {}", e);
        }
    }

    public void checkHubPower(){
        try {
            final HubModel hubModel = SessionModelManager.instance().getHubModel();
            if (hubModel != null) {
                String hubConn = String.valueOf(hubModel.get(HubPower.ATTR_SOURCE));
                updateHubConnection(hubConn);
            }
        } catch (Exception e){
            logger.error("Can't get hub capability: {}", e);
        }
    }

    protected void setEmptyTitle() {
        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.setTitle("");
        ActivityCompat.invalidateOptionsMenu(activity);
    }

    protected void setTitle() {
        String title = getTitle();
        if (TextUtils.isEmpty(title)) {
            return;
        }

        Activity activity = getActivity();
        if (activity == null) {
            return;
        }

        activity.setTitle(title);
        activity.invalidateOptionsMenu();
    }

    public void showActionBar() {
        toggleActionBarVisibility(false);
    }

    public void hideActionBar() {
        toggleActionBarVisibility(true);
    }

    protected boolean isActionBarVisible() {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            return false;
        }

        ActionBar actionBar = ((BaseActivity) activity).getSupportActionBar();
        if (actionBar != null) {
            return actionBar.isShowing();
        }

        return false;
    }

    private void toggleActionBarVisibility(boolean hide) {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            return;
        }

        ActionBar actionBar = ((BaseActivity) activity).getSupportActionBar();
        if (actionBar != null) {
            if (hide) {
                actionBar.hide();
            }
            else {
                actionBar.show();
            }
        }
    }

    @IntDef({View.VISIBLE, View.GONE, View.INVISIBLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewVisibility {}
}
