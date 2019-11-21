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
package arcus.app.subsystems.doorsnlocks;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.doorsnlocks.DoorsNLocksAccessController;
import arcus.cornea.subsystem.doorsnlocks.model.AccessState;
import arcus.cornea.subsystem.doorsnlocks.model.AccessSummary;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.adapters.personlist.AbstractPersonSelectionListAdapter;
import arcus.app.common.adapters.personlist.AccessStateAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import java.util.List;

public class DoorsNLocksAccessListFragment extends BaseFragment implements DoorsNLocksAccessController.SelectedDeviceCallback, AbstractPersonSelectionListAdapter.OnCheckboxCheckedListener {

    public static final String ACCESS_NAME_KEY = "ACCESS NAME KEY";
    public static final String ACCESS_DEVICE_ID_KEY = "ACCESS DEVICE ID KEY";

    private int maxAccessorsAllowed = Integer.MAX_VALUE;
    private boolean isEditMode = false;
    @Nullable private String mAccessName;
    @Nullable private String mSelectedDeviceId;
    private boolean mIsBasic = false;

    private AccessStateAdapter mAdapter;
    private Version1TextView alarmTitle;
    private Version1TextView alarmSubTitle;
    private Version1TextView addPeopleText;
    private ListenerRegistration mCallbackListener;

    @NonNull
    public static DoorsNLocksAccessListFragment newInstance(AccessSummary subsystem) {
        DoorsNLocksAccessListFragment fragment = new DoorsNLocksAccessListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(ACCESS_NAME_KEY,subsystem.getName());
        bundle.putString(ACCESS_DEVICE_ID_KEY, subsystem.getDeviceId());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mAccessName = arguments.getString(ACCESS_NAME_KEY);
            mSelectedDeviceId = arguments.getString(ACCESS_DEVICE_ID_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_alarm_call_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        alarmTitle = (Version1TextView) view.findViewById(R.id.fragment_alarm_call_list_title);
        alarmSubTitle = (Version1TextView) view.findViewById(R.id.fragment_alarm_call_list_subtitle);
        addPeopleText = (Version1TextView) view.findViewById(R.id.call_tree_sub_text);

        alarmTitle.setText(getString(R.string.doors_and_locks_access_title));
        alarmSubTitle.setText(getString(R.string.doors_and_locks_access_des));

        addPeopleText.setText(R.string.doors_and_locks_access_no_people);
        addPeopleText.setVisibility(View.VISIBLE);

        // drag & drop manager
        RecyclerViewDragDropManager mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.material_shadow_z3));

        mAdapter = new AccessStateAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter));
        //mRecyclerView.setItemAnimator(new RefactoredDefaultItemAnimator());

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (DoorsNLocksAccessController.instance().hasPendingChanges()) {
            showPendingChanges();
            return true;
        }

        if (BackstackManager.getInstance().isFragmentOnStack(InfoTextPopup.class)) {
            // Ensure the user closes the popup before we go edit/done again
            return true;
        }

        isEditMode = !isEditMode;
        mAdapter.setIsEditable(isEditMode);

        if (isEditMode) {
            item.setTitle(getResources().getString(R.string.card_menu_done));
            DoorsNLocksAccessController.instance().edit();
        } else {
            item.setTitle(getResources().getString(R.string.card_menu_edit));
            DoorsNLocksAccessController.instance().save(mAdapter.getAccessStateEntries());
        }

        return true;
    }

    protected void showPendingChanges() {
        InfoTextPopup infoTextPopup = InfoTextPopup.newInstance(
              R.string.doors_n_locks_pins_updating_desc,
              R.string.doors_n_locks_pins_updating_title,
              false
        );
        String stackName = infoTextPopup.getClass().getCanonicalName();
        BackstackManager.getInstance().navigateToFloatingFragment(infoTextPopup, stackName, true);
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().setTitle(mAccessName);
        getActivity().invalidateOptionsMenu();

        mCallbackListener = DoorsNLocksAccessController.instance().setSelectedDeviceCallback(mSelectedDeviceId, this);
    }

    @Override
    public void onPause() {
        super.onPause();

        mCallbackListener = Listeners.clear(mCallbackListener);
    }

    @Override
    public String getTitle() {
        return getString(R.string.safety_alarm_notification_list_title);
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        return mIsBasic ? null : isEditMode ? R.menu.menu_done : R.menu.menu_edit_done_toggle;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_alarm_call_list;
    }

    @Override
    public void showActiveAccess(List<AccessState> access) {
        mAdapter.setAccessStateEntries(translateRelationship(access));
    }

    @Override
    public void showEditableAccess(int maxPeople, List<AccessState> access) {
        maxAccessorsAllowed = maxPeople;

        mAdapter.setOnCheckboxCheckedListener(this);
        mAdapter.setAccessStateEntries(translateRelationship(access));
    }

    @Override
    public void updateAccessState(AccessState access) {
        // Nothing to do; platform update handled when user clicks "DONE" menu item
    }

    @Override
    public void showSaving() {
        if (mAdapter != null) {
            mAdapter.setIsEditable(false);
            mAdapter.notifyDataSetChanged();
        }
        showPendingChanges();
    }

    @Override
    public void showError(ErrorModel error) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(new RuntimeException(error.getMessage()));
    }

    @Override
    public boolean onCheckboxChecked(int position) {

        if (mAdapter.getCheckedItemsCount() >= maxAccessorsAllowed) {
            String errorTile = ArcusApplication.getContext().getString(R.string.doors_permissions_exceeded);
            String errorDescription = ArcusApplication.getContext().getString(R.string.doors_permissions_exceeded_desc, maxAccessorsAllowed);

            BackstackManager.getInstance().navigateToFloatingFragment(AlertFloatingFragment.newInstance(errorTile, errorDescription, null, null, null), AlertFloatingFragment.class.getSimpleName(), true);
            return false;
        }

        return true;
    }

    //utility method to translate the relationship using PersonTag
    private List<AccessState> translateRelationship(List<AccessState> accessStates){
        for (AccessState entry : accessStates) {
            entry.setRelationship(CorneaUtils.getPersonRelationship(entry.getPersonId()));
        }

        return accessStates;
    }

}
