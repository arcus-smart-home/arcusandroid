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
package arcus.app.subsystems.lightsnswitches;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import arcus.app.R;
import arcus.app.common.fragments.HeaderNavigationViewPagerFragment;
import arcus.app.subsystems.lightsnswitches.model.EditModeChangeListener;

import java.util.ArrayList;
import java.util.List;


public class LightsNSwitchesParentFragment extends HeaderNavigationViewPagerFragment {

    private boolean isEditMode = false;
    private boolean showEditMenu = false;

    private EditModeChangeListener editModeChangeListener;

    public static LightsNSwitchesParentFragment newInstance () {
        return new LightsNSwitchesParentFragment();
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        // Hide or show the "device/schedule" tab bar based on edit mode
        setSlidingTabLayoutVisibility(isEditMode ? View.GONE : View.VISIBLE);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.lightsnswitches_title);
    }

    @NonNull
    @Override
    protected List<Fragment> getFragments() {
        ArrayList fragments = new ArrayList<>();
        fragments.add(LightsNSwitchesDevicesFragment.newInstance());
        fragments.add(LightsNSwitchesScheduleFragment.newInstance());
        return fragments;
    }

    @NonNull
    @Override
    protected String[] getTitles() {
        return new String[] {getString(R.string.lightsnswitches_tab_devices), getString(R.string.lightsnswitches_tab_schedule)};
    }

    @Override
    public Integer getMenuId() {
        if (showEditMenu) {
            return R.menu.menu_edit_done_toggle;
        } else {
            return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isEditMode = !isEditMode;

        if (isEditMode) {
            item.setTitle(getString(R.string.card_menu_done));
        } else {
            item.setTitle(getString(R.string.card_menu_edit));
        }

        if (editModeChangeListener != null) {
            editModeChangeListener.onEditModeChanged(isEditMode);
        }

        return true;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (menu == null || menu.size() ==0){
            return;
        }
        // Nothing to do if edit menu is hidden
        if (showEditMenu ) {
            menu.getItem(0).setTitle(getString(isEditMode ? R.string.card_menu_done : R.string.card_menu_edit));
        }
    }



    public void setEditModeChangeListener (EditModeChangeListener listener) {
        this.editModeChangeListener = listener;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMenuVisible (boolean editMenuVisible) {
        this.showEditMenu = editMenuVisible;
        getActivity().invalidateOptionsMenu();
    }
}
