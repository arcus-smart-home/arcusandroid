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
package arcus.app.dashboard;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import arcus.cornea.SessionController;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.PersonModel;
import arcus.app.BuildConfig;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.activities.LaunchActivity;
import arcus.app.common.models.RegistrationContext;
import arcus.app.common.utils.LoginUtils;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.view.ScleraTextView;
import arcus.app.dashboard.adapter.NavDrawerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NavigationDrawerFragment extends Fragment implements View.OnClickListener{
    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    @Nullable private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private Version1TextView mPersonNameTextView;
    private View mFragmentContainerView;
    transient PersonModel mPersonModel;

    private int mCurrentSelectedPosition = 0;

    protected ListenerRegistration logoutListener;
    private final SessionController.LogoutCallback logoutCallback = new SessionController.LogoutCallback() {
        @Override public void logoutSuccess() {
            logoutComplete();
        }
        @Override public void onError(Throwable throwable) {
            logoutComplete();
        }
    };

    public Logger logger = LoggerFactory.getLogger(NavigationDrawerFragment.class);

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            boolean mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
        selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(
              R.layout.fragment_navigation_drawer, container, false);
        mPersonNameTextView = (Version1TextView) view.findViewById(R.id.person_name);
        mDrawerListView = (ListView) view.findViewById(R.id.sidenav_listview);

        View footerView = inflater.inflate(R.layout.side_nav_logout,null,false);
        mDrawerListView.addFooterView(footerView);

        if (footerView != null) {
            ScleraTextView version = footerView.findViewById(R.id.version_number);
            version.setText(getString(R.string.version_format, BuildConfig.VERSION_NAME));
        }

        View logout = footerView.findViewById(R.id.sidenav_logout);
        logout.setOnClickListener(this);

        //Don't do anything if patent pending is touched
        footerView.findViewById(R.id.patent_pending).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        final Resources res = getResources();
        String[] mSideNavTitles = res.getStringArray(R.array.navDrawer_title_array);
        String[] mSideNavSubTitles = res.getStringArray(R.array.navDrawer_sub_title_array);
        NavDrawerAdapter adapter = new NavDrawerAdapter(getActivity(), mSideNavTitles, mSideNavSubTitles);

        mDrawerListView.setAdapter(adapter);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

        populatePersonBanner();
        return view;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.addDrawerListener(mDrawerToggle);
    }

    public void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    public void closeDrawer(){
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            if (mDrawerLayout != null) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }

                return true;
            }
        }

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    @Nullable
    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id){
            case R.id.sidenav_logout:
                if(!PreferenceUtils.getUsesFingerPrint()) {
                    logoutListener = SessionController.instance().setCallback(logoutCallback);
                    SessionController.instance().logout();
                    break;
                }
                else { // fake logout
                    SessionController.instance().close();
                    Activity activity = getActivity();
                    if (activity != null) {
                        closeDrawer();
                        LaunchActivity.startLoginScreen(activity);
                        activity.finish();
                    }
                    break;
                }
        }
    }

    protected void logoutComplete() {
        Listeners.clear(logoutListener);
        LoginUtils.completeLogout();

        RegistrationContext registrationContext = ArcusApplication.getRegistrationContext();
        if (registrationContext != null) {
            registrationContext.setHubID(null);
        }

        Activity activity = getActivity();
        if (activity != null) {
            closeDrawer();
            LaunchActivity.startLoginScreen(activity);
            activity.finish();

            // This is a total hack. It was introduced to fix "weird" caching issues when logging
            // out of one account and into another and seeing models from the old account available
            // in the new...

            // TODO: Determine root cause and fix; remove .exit() call
            Runtime.getRuntime().exit(0);
        }
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    private void populatePersonBanner(){
        mPersonModel = SessionController.instance().getPerson();
        mPersonNameTextView.setTypeface(null, Typeface.BOLD_ITALIC);

        if (mPersonModel == null) {
            mPersonNameTextView.setText("Hello!");
            return;
        }

        String firstName = String.format("Hello, %s", mPersonModel.getFirstName
                ());
        mPersonNameTextView.setText(firstName);
    }

}
