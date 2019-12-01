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
package arcus.app.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.LayoutRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.CorneaService;
import arcus.cornea.events.LogoutEvent;
import arcus.cornea.events.NetworkConnectedEvent;
import arcus.cornea.events.NetworkLostEvent;
import arcus.cornea.model.PlaceLostEvent;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.account.registration.AccountSecurityQuestionsFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.popup.NoNetworkConnectionPopup;
import arcus.app.common.error.type.NoNetworkConnectionErrorType;
import arcus.app.common.events.PlaceChangeRequestedEvent;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.utils.LoginUtils;
import arcus.app.dashboard.HomeFragment;
import arcus.app.launch.InvitationFragment;
import arcus.app.subsystems.debug.KonamiCodeDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.greenrobot.event.EventBus;


public abstract class BaseActivity extends PermissionsActivity {
    public static final Logger logger = LoggerFactory.getLogger(BaseActivity.class);
    private boolean showingNetworkLostPopup = false;

    private boolean doubleBackToExitPressedOnce;
    private static boolean noNetworkErrorSupressed;
    private static boolean isDeepLink = false;

    public Toolbar toolbar;
    public TextView toolBarTitle;
    public Menu menu;
    private ImageView toolbarImage;
    private ScaleGestureDetector konamiCodeDetector;
    private FragmentManager fragmentManager;
    private Set<ActivityResultListener> activityResultListeners = new HashSet<>();

    // Progress spinner
    private View indeterminateProgressView;
    private Timer indeterminateProgressTimeout = new Timer();
    private final static int PROGRESS_TIMEOUT = 45000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackstackManager.getInstance().setBaseActivity(this);
        fragmentManager = getSupportFragmentManager();

        konamiCodeDetector = new ScaleGestureDetector(this, new KonamiCodeDetector());
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);

        ImageManager.setWallpaperView(this, findViewById(layoutResID));
        View v = findViewById(R.id.my_toolbar);

        indeterminateProgressView = findViewById(R.id.indeterminate_progress);

        if(v!=null){
            toolbar = (Toolbar) v;
            setSupportActionBar(toolbar);
            toolBarTitle = (TextView) v.findViewById(R.id.toolbar_title);
            if(toolBarTitle!=null){
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openNavigationDrawer();
                    }
                });
            }
            toolbarImage = (ImageView) v.findViewById(R.id.app_image);
        }
    }

    protected void openNavigationDrawer() {
    }

    public void setToolbarTitle(CharSequence title){
        if(toolBarTitle!=null){
            toolBarTitle.setText(title);
        }
    }

    public void setToolbarOverflowMenuIcon(Drawable drawable) {
        toolbar.setOverflowIcon(drawable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ArcusApplication.shouldReload()) {
            reloadToLaunchActivity();
            return;
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        dismissNoNetworkPopup();
    }

    protected boolean isDeepLinkIntent() {
        return false;
    }

    protected void consumeDeepLinkFlag() {
        isDeepLink = false;
    }

    /**
     * If we're in the background for 30 seconds say.. signing up for an account in the web....
     * When we come back to the app we restart altogether (historically this was to clear singletons and
     * ensure we didn't have weird states or partially loaded subsystems - I'm not convinced this is still needed).
     * This causes us to lose the deep link that was dispatched.
     *
     * The order this comes in, is new intent is dispatched ->
     * Launch activity will see and toggle this flag to true,
     * Other activities will call onResume and stop the restart if this is true.
     */
    protected void reloadToLaunchActivity() {
        if (!isDeepLink) { // We haven't seen a deep link
            isDeepLink = isDeepLinkIntent(); // Check again just to be sure
        }

        if (!isDeepLink) { // if it's still not a deep link enter this if block.
            Intent intent = new Intent(this, LaunchActivity.class);
            startActivity(intent);
            finish();
            Runtime.getRuntime().exit(0);
        }
    }

    public void onEvent(NetworkLostEvent event) {
        if (!showingNetworkLostPopup && !isNoNetworkErrorSupressed() && findViewById(R.id.floating) != null) {
            ErrorManager.in(this).show(NoNetworkConnectionErrorType.NO_NETWORK_CONNECTION);
            showingNetworkLostPopup = true;
        }
    }

    public void onEvent(NetworkConnectedEvent event) {
        if (showingNetworkLostPopup && CorneaClientFactory.isConnected()) {
            BackstackManager.getInstance().navigateBack();
        }

        showingNetworkLostPopup = false;
    }

    protected void dismissNoNetworkPopup() {
        if (hasActiveNetwork() && CorneaClientFactory.isConnected()) {
            Fragment fragment = BackstackManager.getInstance().getCurrentFragment();
            // Check the top fragment and rewind if it's the NoNetworkConnectionPopup
            if (fragment != null && fragment instanceof NoNetworkConnectionPopup) {
                try {
                    BackstackManager.getInstance().navigateBack();
                    showingNetworkLostPopup = false;
                }
                catch (Exception ex) {
                    reloadToLaunchActivity();
                }
            }
        }
    }

    /**
     * This event is generated by the SessionController to indicate the current place has been
     * 'lost' (i.e, either deleted or our access has been revoked). Event contains the next place
     * we should transition user to.
     *
     * If the user has access to no other places this event will not be generated and instead the
     * SessionController will forcibly logout the user.
     *
     * @param event
     */
    public void onEvent(PlaceLostEvent event) {
        logger.debug("Got PlaceLostEvent, forcibly navigating back to dashboard.");
        BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
        EventBus.getDefault().post(new PlaceChangeRequestedEvent(event.getSwitchingToPlaceID()));
    }

    /**
     * User was forcibly logged out (typically because their only place was deleted remotely)
     * @param event
     */
    public void onEvent(LogoutEvent event) {
        LoginUtils.completeLogout(); // Clear any previous session data.

        LaunchActivity.startLoginScreen(this);
        finish();
        Runtime.getRuntime().exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {

        final Fragment fragment = fragmentManager.findFragmentById(R.id.container);
        if (this instanceof InvitationActivity && fragment instanceof InvitationFragment) {
            ((InvitationFragment) fragment).handleBackPress();
            return;
        }
        if (fragment instanceof AccountSecurityQuestionsFragment) {
            ((AccountSecurityQuestionsFragment) fragment).handleBackPress();
            return;
        }
        if (doubleBackToExitPressedOnce) {
            finishAffinity();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        try {
            this.konamiCodeDetector.onTouchEvent(event);
            return super.dispatchTouchEvent(event);
        }
        catch (Exception ex) {
            logger.debug("Touch event was touchy....", ex);
            return true;
        }
    }

    public CorneaService getCorneaService() {
        return ArcusApplication.getArcusApplication().getCorneaService();
    }

    protected boolean hasActiveNetwork() {
        ConnectivityManager cm = (ConnectivityManager) ArcusApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public boolean isNoNetworkErrorSupressed() {
        return noNetworkErrorSupressed;
    }

    public void setNoNetworkErrorSupressed(boolean noNetworkErrorSupressed) {
        this.noNetworkErrorSupressed = noNetworkErrorSupressed;
    }

    public void setKeepScreenOn (boolean keepScreenOn) {
        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    public void showTitle(boolean show) {
        if(toolbarImage == null || toolBarTitle == null) {
            return;
        }
//        if(show) {
        // No image right now
            toolbarImage.setVisibility(View.GONE);
            toolBarTitle.setVisibility(View.VISIBLE);
//        } else {
//            toolbarImage.setVisibility(View.VISIBLE);
//            toolBarTitle.setVisibility(View.GONE);
//        }
    }

    public void setTitleImage(int resId) {
        toolbarImage.setImageResource(resId);
    }

    public View getProgressIndicator() {
        Fragment current = BackstackManager.getInstance().getCurrentFragment();

        if (current != null && current instanceof BaseFragment) {
            BaseFragment baseFragment = (BaseFragment) current;
            View fragmentProgressView = baseFragment.getIndeterminateProgressView();

            if (fragmentProgressView != null) {
                return fragmentProgressView;
            }
        }

        return indeterminateProgressView;
    }

    public boolean isProgressIndicatorVisible() {
        return getProgressIndicator() != null && getProgressIndicator().getVisibility() == View.VISIBLE;
    }

    public void showProgressIndicator() {
        showProgressIndicator(PROGRESS_TIMEOUT);
    }

    public void showProgressIndicator(long forMs) {

        if (getProgressIndicator() != null) {
            getProgressIndicator().setVisibility(View.VISIBLE);

            indeterminateProgressTimeout.cancel();
            indeterminateProgressTimeout = new Timer();

            indeterminateProgressTimeout.schedule(new TimerTask() {
                @Override
                public void run() {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressIndicator();
                        }
                    });
                }
            }, forMs);
        }
    }

    public void hideProgressIndicator() {
        if (getProgressIndicator() != null) {
            getProgressIndicator().setVisibility(View.GONE);
        }
    }
}
