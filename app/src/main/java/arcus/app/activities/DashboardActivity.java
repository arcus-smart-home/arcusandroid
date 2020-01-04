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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import arcus.app.common.fragments.NoViewModelFragment;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.DashboardSubsystemController;
import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import arcus.cornea.subsystem.model.DashboardState;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.Hub;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.HubModel;
import arcus.app.R;
import arcus.app.account.creation.CreateAccountSuccessFragment;
import arcus.app.account.settings.SettingsWalkthroughFragment;
import arcus.app.account.settings.list.SideNavSettingsFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.backstack.TransitionEffect;
import arcus.app.common.banners.core.BannerActivity;
import arcus.app.common.banners.core.BannerAdapter;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.WebViewFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.utils.ToolbarColorizeHelper;
import arcus.app.dashboard.HomeFragment;
import arcus.app.dashboard.NavigationDrawerFragment;
import arcus.app.dashboard.popups.responsibilities.dashboard.DashboardPopupManager;
import arcus.app.device.list.DeviceListingFragment;
import arcus.app.subsystems.alarm.DeprecatedAlertFragment;
import arcus.app.subsystems.alarm.safety.EarlySmokeWarningFragment;
import arcus.app.subsystems.care.CareParentFragment;
import arcus.app.subsystems.rules.RuleListFragment;
import arcus.app.subsystems.scenes.list.SceneListFragment;
import arcus.app.subsystems.weather.WeatherWarningFragment;

import de.greenrobot.event.EventBus;

public class DashboardActivity extends BaseActivity implements BannerActivity, NavigationDrawerFragment.NavigationDrawerCallbacks, DashboardSubsystemController.Callback {

    public static final String TAG_ADDRESS = "address-tag";
    public static final String TAG_ACCOUNT_CONFETTI = "account-confetti";
    public static final String ARG_SHOW_HOME_FRAGMENT = "ARG_SHOW_HOME_FRAGMENT";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private boolean isHub;

    public View root;
    private ListenerRegistration mSubsystemListener;
    private BannerAdapter banners;
    private DashboardState state;
    private DrawerLayout mDrawerLayout;

    private boolean navigatedToAlarmSegment, navigatedToCareSegment, navigatedToPresmokeSegment;

    private enum ToolbarColor {
        WHITE, PINK, PURPLE, EARLY_WARNING, WEATHER, LIGHT_BLUE, BLUE, RED, GREY
    }

    private ToolbarColor mToolbarColor = ToolbarColor.WHITE;
    private ToolbarColor mPreviousToolbarColor = ToolbarColor.WHITE;

    /**
     * Start this activity and immediately begin "new account confetti" sequence. Intended for use
     * after a user has created a new account and been redirected to the mobile app from the web.
     *
     * @param activity The current activity
     */
    public static void startActivityForAccountConfetti(Activity activity) {
        Intent intent = new Intent(activity, DashboardActivity.class);
        intent.putExtra(TAG_ACCOUNT_CONFETTI, true);
        activity.startActivity(intent);
    }

    /**
     * Start this activity and use the {@link DeepLinkDispatcher} to determine where the user should
     * land based on the context of given the Arcus platform address. Takes the user to the
     * dashboard if no address is given or the dispatcher has no defined route for the address.
     *
     * @param activity The current activity
     * @param address An Arcus platform address (SERV:person:123456789abcdef) to dispatch.
     */
    public static void startActivityForAddress(Activity activity, String address) {
        Intent intent = new Intent(activity, DashboardActivity.class);
        intent.putExtra(TAG_ADDRESS, address);
        activity.startActivity(intent);
    }

    /**
     * Get the launch intent for this activity, directing the user to the home fragment.
     * Note: this also flags to CLEAR_TOP
     *
     * @param context the current context.
     */
    public static Intent getHomeFragmentIntent(Context context) {
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(ARG_SHOW_HOME_FRAGMENT, true);

        return intent;
    }

    /**
     * Start this activity, directing the user to the dashboard.
     * @param activity The current activity
     */
    public static void startActivity (Activity activity) {
        Intent intent = new Intent(activity, DashboardActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        root = findViewById(R.id.container);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.icon_side_menu);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                mDrawerLayout);

        ImageManager.setWallpaperView(this, root);
        ImageManager.with(this).setWallpaper(Wallpaper.ofCurrentPlace().darkened());

        boolean accountConfetti = getIntent().getBooleanExtra(TAG_ACCOUNT_CONFETTI, false);
        
        if (accountConfetti) {
            BackstackManager.getInstance().navigateToFragment(CreateAccountSuccessFragment.newInstance(), true);
        } else {
            // Send the user to screen targeted by the given address or do nothing if no address target exists
            DeepLinkDispatcher.dispatchToAddress(getIntent().getStringExtra(TAG_ADDRESS));
        }
    }

    @Override
    protected void openNavigationDrawer() {
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            return true;
        }
        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.container);
        if (frag instanceof NoViewModelFragment) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
            restoreActionBar();
            return true;
        } else if(!(frag instanceof BaseFragment)){
            return true;
        }else{
            final BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.container);
            if (fragment != null) {
                final Integer menuId = fragment.getMenuId();
                if (menuId != null) {
                    getMenuInflater().inflate(menuId, menu);
                    fragment.setHasOptionsMenu(true);
                }
                if(fragment instanceof HomeFragment) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    toolbar.setNavigationIcon(R.drawable.icon_side_menu);
                } else {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

                    if (mToolbarColor != ToolbarColor.PINK && mToolbarColor != ToolbarColor.PURPLE
                          && mToolbarColor != ToolbarColor.EARLY_WARNING && mToolbarColor != ToolbarColor.WEATHER
                          && mToolbarColor != ToolbarColor.BLUE && mToolbarColor != ToolbarColor.LIGHT_BLUE
                          && mToolbarColor != ToolbarColor.RED && mToolbarColor != ToolbarColor.GREY) {
                        mPreviousToolbarColor = mToolbarColor;
                        mToolbarColor = ToolbarColor.WHITE;
                    }
                }

                updateOptionsMenu();

                restoreActionBar();
                return true;
            }
            return super.onCreateOptionsMenu(menu);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getBooleanExtra(ARG_SHOW_HOME_FRAGMENT, false)) {
            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!CorneaClientFactory.isConnected()) {
            reloadToLaunchActivity();
            return;
        }

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        // Subsystem Callback Registration
        Listeners.clear(mSubsystemListener);
        mSubsystemListener = DashboardSubsystemController.instance().setCallback(this);

        // Reset the actionbar title
        updateOptionsMenu();
        if(this.state != null) {
            showState(this.state);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragmentsfragmentManager
        Bundle arguments = new Bundle();
        WebViewFragment webViewFragment;
        Intent intent;
        Uri uri;

        switch (position) {
            case 0:  //  Home
                BackstackManager.withAnimation(TransitionEffect.FADE).navigateBackToFragment(HomeFragment.newInstance());
                setTitle(R.string.sidenav_home_title);
                break;
            case 1: // Scenes
//                if (SubscriptionController.isPremiumOrPro()) {
                    BackstackManager.withAnimation(TransitionEffect.FADE).navigateBackToFragment(SceneListFragment.newInstance());
                    setTitle(R.string.scenes_scenes);
/*                } else {
                    if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.SCENES_WALKTHROUGH_DONT_SHOW_AGAIN, true)) {
                        WalkthroughBaseFragment scenesWalkthroughFragment = WalkthroughBaseFragment.newInstance(WalkthroughType.SCENES);
                        BackstackManager.getInstance().navigateToFloatingFragment(scenesWalkthroughFragment, scenesWalkthroughFragment.getClass().getName(), true);
                    } else {
                        FullscreenFragmentActivity.launch(this, ScenesPremiumRequired.class);
                    }
                }*/
                break;
            case 2:  //  Rules
                BackstackManager.withAnimation(TransitionEffect.FADE).navigateBackToFragment(RuleListFragment.newInstance());
                setTitle(R.string.rules_rules);

                break;
            case 3:  // Devices
                BackstackManager.withAnimation(TransitionEffect.FADE).navigateBackToFragment(new DeviceListingFragment());
                setTitle(R.string.sidenav_devices_title);
                isHub = false;
                break;
            case 4:  //  Settings
                BackstackManager.withAnimation(TransitionEffect.FADE).navigateBackToFragment(SideNavSettingsFragment.newInstance());
                setTitle(getString(R.string.sidenav_settings_title));
                isHub = false;
                break;
            case 5:  //  support
                BackstackManager.withAnimation(TransitionEffect.FADE).navigateBackToFragment(SettingsWalkthroughFragment.newInstance());
                setTitle(getString(R.string.support));
                isHub = false;
                break;
        }
    }

    @Override
    public void onBackPressed() {

        if (mNavigationDrawerFragment.isDrawerOpen()){
            mNavigationDrawerFragment.closeDrawer();
            return;
        }
        boolean eventConsumed = false;
        Fragment fragment = BackstackManager.getInstance().getCurrentFragment();

        if (isBaseFragment(fragment)) {
            eventConsumed = ((BaseFragment) fragment).onBackPressed();
        } else if (fragment instanceof NoViewModelFragment) {
            eventConsumed = ((NoViewModelFragment) fragment).onBackPressed();
        }

        if (!eventConsumed) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragment = fragmentManager.findFragmentById(R.id.floating);
            if (isBaseFragment(fragment)) {
                eventConsumed = ((BaseFragment) fragment).onBackPressed();
            }

            if (!eventConsumed) {
                int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                if (backStackEntryCount > 1) {
                    BackstackManager.getInstance().navigateBack();
                } else if (backStackEntryCount == 1) {
                    fragment = fragmentManager.findFragmentById(R.id.container);
                    if (isBaseFragment(fragment) && ((BaseFragment) fragment).isHomeFragment()) {
                        super.onBackPressed();
                    } else {
                        BackstackManager.getInstance().navigateBack();
                    }
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            logger.trace("back press event consumed", "");
        }
    }

    protected boolean isBaseFragment(Fragment fragment) {
        return fragment != null && fragment instanceof BaseFragment;
    }

    public void restoreActionBar() {
        setToolbarTitle(getTitle());
    }

    public boolean isHub() {
        return isHub;
    }

    public void setIsHub(boolean result) {
        this.isHub = result;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);
    }

    private void updateOptionsMenu() {
        switch (mToolbarColor) {
            case WHITE:
                toolbar.setBackgroundColor(Color.WHITE);
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.BLACK, this);
                toolBarTitle.setTextColor(Color.BLACK);
                break;
            case PINK:
                toolbar.setBackgroundColor(getResources().getColor(R.color.pink_banner));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, this);
                toolBarTitle.setTextColor(Color.WHITE);
                break;
            case PURPLE:
                toolbar.setBackgroundColor(getResources().getColor(R.color.care_alarm_purple));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, this);
                toolBarTitle.setTextColor(Color.WHITE);
                break;
            case EARLY_WARNING:
                toolbar.setBackgroundColor(getResources().getColor(R.color.early_warning_color));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.BLACK, this);
                toolBarTitle.setTextColor(Color.BLACK);
                break;
            case WEATHER:
                toolbar.setBackgroundColor(getResources().getColor(R.color.weather_alert_color));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, this);
                toolBarTitle.setTextColor(Color.WHITE);
                break;
            case LIGHT_BLUE:
                toolbar.setBackgroundColor(getResources().getColor(R.color.waterleak_color));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, this);
                toolBarTitle.setTextColor(Color.WHITE);
                break;
            case BLUE:
                toolbar.setBackgroundColor(getResources().getColor(R.color.security_color));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, this);
                toolBarTitle.setTextColor(Color.WHITE);
                break;
            case RED:
                toolbar.setBackgroundColor(getResources().getColor(R.color.safety_color));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, this);
                toolBarTitle.setTextColor(Color.WHITE);
                break;
            case GREY:
                toolbar.setBackgroundColor(getResources().getColor(R.color.panic_color));
                ToolbarColorizeHelper.colorizeToolbar(toolbar, Color.WHITE, this);
                toolBarTitle.setTextColor(Color.WHITE);
                break;
        }
    }

    public void setToPreviousToolbarColor() {
        if(state.isAlarmAlertActivated() || state.isAlarmPreAlertActivated()) {
            setToolbarColorForActiveAlert(state.getPrimaryActiveAlarm());
        } else if(state.isCareAlarmActivated()) {
            mToolbarColor = ToolbarColor.PURPLE;
        } else {
            mToolbarColor = ToolbarColor.WHITE;
        }
        invalidateOptionsMenu();
    }

    public void setToolbarNormal() {
        applyToolbarColor(ToolbarColor.WHITE);
    }

    public void setToolbarError() {
        applyToolbarColor(ToolbarColor.PINK);
    }

    public void setToolbarPanic() {
        applyToolbarColor(ToolbarColor.GREY);
    }

    public void setToolbarSafety() {
        applyToolbarColor(ToolbarColor.RED);
    }

    public void setToolbarSecurity() {
        applyToolbarColor(ToolbarColor.BLUE);
    }

    public void setToolbarWaterLeak() {
        applyToolbarColor(ToolbarColor.LIGHT_BLUE);
    }

    public void setToolbarCare() {
        applyToolbarColor(ToolbarColor.PURPLE);
    }

    public void setToolbarEarlyWarningColor() {
        applyToolbarColor(ToolbarColor.EARLY_WARNING);
    }

    public void setToolbarWeatherWarningColor() {
        applyToolbarColor(ToolbarColor.WEATHER);
    }

    private void setToolbarColorForActiveAlert(String activeAlert) {
        if (!StringUtils.isEmpty(activeAlert)) {
            switch (activeAlert) {
                case AlarmSubsystem.ACTIVEALERTS_CO:
                case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                    setToolbarSafety();
                    break;
                case AlarmSubsystem.ACTIVEALERTS_PANIC:
                    setToolbarPanic();
                    break;
                case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                    setToolbarSecurity();
                    break;
                case AlarmSubsystem.ACTIVEALERTS_WATER:
                    setToolbarWaterLeak();
                    break;
            }
        }
    }

    protected void applyToolbarColor(ToolbarColor color) {
        mPreviousToolbarColor = mToolbarColor;
        mToolbarColor = color;
        invalidateOptionsMenu();
    }


    @Override
    public void showDeprecatedState(@Nullable DashboardState state) {
        Fragment frag = BackstackManager.getInstance().getFragmentOnStack(DeprecatedAlertFragment.class);
        if(state.getSafetyActivated()) {
            if(frag == null) {
                setToolbarSafety();
                BackstackManager.getInstance().navigateToFragment(DeprecatedAlertFragment.newInstance(DeprecatedAlertFragment.SAFETY_ALARM), true);
            }
        } else if(state.getSecurityActivated()) {
            if(frag == null) {
                setToolbarSecurity();
                BackstackManager.getInstance().navigateToFragment(DeprecatedAlertFragment.newInstance(DeprecatedAlertFragment.SECURITY_ALARM), true);
            }
        } else {
            showState(state);
            if(frag != null && frag.isVisible()) {
                setToPreviousToolbarColor();
                BackstackManager.getInstance().navigateBack();
            }
            AlarmSubsystemController.getInstance().activate();
        }
    }


    /**
     * Subsystem Callbacks
     * TODO: This is a total nightmare; needs to be re-written
     */
    @Override
    public void showState(@Nullable DashboardState state) {
        Fragment currentFragment = BackstackManager.getInstance().getCurrentFragment();

        logger.debug("Dashboard state change in fragment {}, new state: {}.", currentFragment == null ? "null" : currentFragment.getClass().getSimpleName(), state);

        if (state == null) return;
        this.state = state;

        if(!state.isCareAlarmActivated()){
            navigatedToCareSegment = false;
        }
        if (!state.isAlarmAlertActivated() && !state.isAlarmPreAlertActivated()) {
            navigatedToAlarmSegment = false;
        }
        if (!state.isPresmokeAlertActivated()) {
            navigatedToPresmokeSegment = false;
        }

        if (currentFragment == null) {
            if(state.getState().equals(DashboardState.State.NORMAL)) {
                setToolbarNormal();
            }

            return;
        }

        if(state.getState().equals(DashboardState.State.NORMAL)){
            Fragment frag = BackstackManager.getInstance().getFragmentOnStack(EarlySmokeWarningFragment.class);
            popOverlay(frag);

            Fragment fragWeather = BackstackManager.getInstance().getFragmentOnStack(WeatherWarningFragment.class);
            popOverlay(fragWeather);

            setToolbarNormal();

            DashboardPopupManager.getInstance().triggerPopups();
        } else {
            // When we hit these...
            // If multiple were actually going off we'd end up navigating to each one
            // weather -> care -> safety -> security (lowest to highest priority) where each one
            // is "rewinding the stack" so it's the last one showing...
            if (state.isWeatherAlertActivated()) {
                // We show weather alerts until they're hushed. but don't manipulate the toolbar.
                BackstackManager.getInstance().navigateToFragment(WeatherWarningFragment.newInstance(), true); // !Bam
            }
            if(state.isCareAlarmActivated() && !navigatedToCareSegment){
                navigatedToCareSegment=true;

                if (currentFragment instanceof CareParentFragment) {
                    ((CareParentFragment) currentFragment).setVisiblePageToAlarmTab();
                }
                else {
                    BackstackManager.getInstance().navigateBackToFragment(CareParentFragment.newInstance(1));
                }
            }
            if((state.isAlarmAlertActivated() || state.isAlarmPreAlertActivated())) {
                navigatedToAlarmSegment = true;
                String hubConnState = "";
                String alarmProvider = "";

                HubModel hubModel = SessionModelManager.instance().getHubModel();
                if (hubModel != null) {
                    hubConnState = (String) hubModel.get(Hub.ATTR_STATE);
                }

                AlarmSubsystemController alarmSubsystemController = AlarmSubsystemController.getInstance();
                if (alarmSubsystemController != null) {
                    alarmProvider = alarmSubsystemController.getAlarmProvider();
                }

                setToolbarColorForActiveAlert(state.getPrimaryActiveAlarm());

                Fragment frag = BackstackManager.getInstance().getFragmentOnStack(EarlySmokeWarningFragment.class);
                popOverlay(frag);

            }
            if (state.isPresmokeAlertActivated() && !navigatedToPresmokeSegment) {
                navigatedToPresmokeSegment = true;

                if(!navigatedToCareSegment && !navigatedToAlarmSegment) {
                    BackstackManager.getInstance().navigateBackToFragment(EarlySmokeWarningFragment.newInstance());
                }
            }

            if(state.isCareAlarmActivated()){
                setToolbarCare();
            }
        }
    }

    @Override
    public void showAlerting() {
        logger.debug("Dashboard in alert state; dismissing interactions.");
    }

    private void popOverlay(Fragment frag) {
        if(frag != null) {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction trans = manager.beginTransaction();
            trans.remove(frag);
            trans.commit();
            manager.popBackStack();
        }
    }

    public BannerAdapter getBanners() {
        // Lazy initialization
        if (banners == null) {
            banners = new BannerAdapter(this);
        }

        return banners;
    }

}
