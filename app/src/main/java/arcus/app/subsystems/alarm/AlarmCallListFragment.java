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
package arcus.app.subsystems.alarm;

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

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.calllist.CallListController;
import arcus.cornea.subsystem.calllist.CallListEntry;
import arcus.cornea.subsystem.care.CareCallListController;
import arcus.cornea.subsystem.safety.SafetyCallListController;
import arcus.cornea.subsystem.security.SecurityCallListController;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.adapters.personlist.CallListEntryAdapter;

import java.util.ArrayList;
import java.util.List;


public class AlarmCallListFragment extends BaseFragment implements CallListController.Callback{

    public static final String SUBSYSTEM_KEY = "sub system key";
    public static final String SUBSYSTEM_SAFETY = "sub system safety";
    public static final String SUBSYSTEM_SECURITY = "sub system security";
    public static final String SUBSYSTEM_CARE     = "sub system care";

    @Nullable
    private String mSubsystem;
    private boolean mIsBasic = false;
    private boolean isEditMode = false;
    private CallListEntryAdapter mAdapter;

    private Version1TextView alarmTitle;
    private Version1TextView alarmSubTitle;
    private CallListController callListController;
    private ListenerRegistration mCallbackListener;
    private View callTreeSubText;

    @NonNull
    public static AlarmCallListFragment newInstance(String subsystem){
        AlarmCallListFragment fragment = new AlarmCallListFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SUBSYSTEM_KEY,subsystem);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle arguments = getArguments();
        if (arguments != null) {
            mSubsystem = arguments.getString(SUBSYSTEM_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_alarm_call_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());

        alarmTitle = (Version1TextView) view.findViewById(R.id.fragment_alarm_call_list_title);
        alarmSubTitle = (Version1TextView) view.findViewById(R.id.fragment_alarm_call_list_subtitle);

        callTreeSubText = view.findViewById(R.id.call_tree_sub_text);
        if(SUBSYSTEM_SAFETY.equals(mSubsystem)) {
            alarmTitle.setText(getString(R.string.safety_alarm_premium_title));
        }
        else if (SUBSYSTEM_CARE.equals(mSubsystem)) {
            alarmTitle.setText(getString(R.string.care_alarm_premium_title));
            if (callTreeSubText != null) {
                callTreeSubText.setVisibility(View.VISIBLE);
            }
        }else{
            alarmTitle.setText(getString(R.string.security_alarm_premium_title));
        }

        alarmSubTitle.setText(getString(R.string.safety_alarm_premium_sub_title));

        // drag & drop manager
        RecyclerViewDragDropManager mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.material_shadow_z3));

        mAdapter = new CallListEntryAdapter();
        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        //mRecyclerView.setItemAnimator(animator);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isEditMode = !isEditMode;
        mAdapter.setIsEditable(isEditMode);

        if (isEditMode) {
            item.setTitle(getResources().getString(R.string.card_menu_done));
            callListController.edit();
        } else {
            item.setTitle(getResources().getString(R.string.card_menu_edit));
            callListController.save(mAdapter.getCallListEntries());
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        if(SUBSYSTEM_SAFETY.equals(mSubsystem)) {
            callListController = SafetyCallListController.instance();
        }
        else if (SUBSYSTEM_CARE.equals(mSubsystem)) {
            callListController = CareCallListController.instance();
        }else{
            callListController = SecurityCallListController.instance();
        }

        mCallbackListener = callListController.setCallback(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Listeners.clear(mCallbackListener);
        callListController = null;
    }

    @Override
    public String getTitle() {
        return getString(R.string.safety_alarm_notification_list_title);
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        return mIsBasic ? null : R.menu.menu_edit_done_toggle;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_alarm_call_list;
    }

    @Override
    public void showActiveCallList(List<CallListEntry> contacts) {
        logger.debug("Got active call list :{}", contacts);
        List<CallListEntry> newEntries = updateCallListEntries(contacts);
        mAdapter.setCallListEntries(newEntries);
    }

    @Override
    public void showEditableCallList(List<CallListEntry> contacts) {
        logger.debug("Got editable call list :{}", contacts);
        List<CallListEntry> newEntries = updateCallListEntries(contacts);
        mAdapter.setCallListEntries(newEntries);
    }

    @Override
    public void showSaving() {
        logger.debug("Showing saving");
    }

    @Override
    public void showError(ErrorModel error) {
        logger.debug("Error saving call list :{}", error);
    }

    @Override
    public void showUpgradePlanCopy() {
        mIsBasic = true;
        logger.debug("Show upgrade plan copy","to do upgrade copy");
        alarmTitle.setText(getString(R.string.basic_alarm_call_list_title));
        if(SUBSYSTEM_SAFETY.equals(mSubsystem)) {
            alarmSubTitle.setText(getString(R.string.safety_alarm_call_list_sub_title));
        }
        else if (SUBSYSTEM_CARE.equals(mSubsystem)) {
            alarmSubTitle.setText(getString(R.string.care_alarm_call_list_sub_title));
        }else{
            alarmSubTitle.setText(getString(R.string.security_alarm_call_list_sub_title));
        }
        if(callTreeSubText != null) {
            callTreeSubText.setVisibility(View.GONE);
        }
    }

    protected List<CallListEntry> updateCallListEntries(List<CallListEntry> entries) {
        List<CallListEntry> editedList = new ArrayList<>(entries.size() + 1);

        for (CallListEntry entry : entries) {
            editedList.add(new CallListEntry(
                  entry.getId(),
                  entry.getFirstName(),
                  entry.getLastName(),
                  CorneaUtils.getPersonRelationship(entry.getId()),
                  entry.isEnabled()
            ));
        }

        return editedList;
    }
}
