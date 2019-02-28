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
package arcus.app.subsystems.alarm.promonitoring;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.subsystem.calllist.CallListEntry;
import arcus.cornea.utils.Listeners;
import com.iris.client.IrisClientFactory;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import com.iris.client.service.ProMonitoringService;
import arcus.app.R;
import arcus.app.common.adapters.personlist.CallListEntryAdapter;
import arcus.app.common.adapters.personlist.PersonListItemModel;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.CallTreeErrorType;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.LinkBuilder;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.utils.RecyclerViewItemClickListener;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmCallTreeContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmCallTreePresenter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ProMonitoringAlarmCallListFragment extends BaseFragment implements AlarmCallTreeContract.AlarmCallListView, ArcusFloatingFragment.OnCloseHandler {

    private AlarmCallTreePresenter presenter = new AlarmCallTreePresenter();
    private boolean isEditMode = false;
    private CallListEntryAdapter mAdapter;
    private View goToSettingsCopy;
    private MenuItem editDoneMenu;
    private Version1TextView addMonitoringStationContact;

    @NonNull
    public static ProMonitoringAlarmCallListFragment newInstance() {
        return new ProMonitoringAlarmCallListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.fragment_alarm_call_list);
        Version1TextView alarmTitle = (Version1TextView) view.findViewById(R.id.fragment_alarm_call_list_title);
        Version1TextView alarmSubTitle = (Version1TextView) view.findViewById(R.id.fragment_alarm_call_list_subtitle);

        addMonitoringStationContact = (Version1TextView) view.findViewById(R.id.add_monitoring_station_contact);
        goToSettingsCopy = view.findViewById(R.id.call_tree_sub_text);

        if (SubscriptionController.isProfessional()) {
            alarmTitle.setText(R.string.alarm_calltree_pro);
            alarmSubTitle.setText(R.string.alarm_calltree_water_desc);
            alarmSubTitle.setVisibility(View.VISIBLE);

            // Create link text to add monitoring station contact popup
            new LinkBuilder(addMonitoringStationContact)
                    .startLinkSpan(new LinkBuilder.OnLinkClickListener() {
                        @Override
                        public void onLinkClicked(TextView view) {
                            IrisClientFactory.getService(ProMonitoringService.class).getMetaData().onSuccess(Listeners.runOnUiThread(new Listener<ProMonitoringService.GetMetaDataResponse>() {
                                @Override
                                public void onEvent(ProMonitoringService.GetMetaDataResponse event) {
                                    ProMonitoringAddUccContactFragment popup = ProMonitoringAddUccContactFragment.newInstance(new ArrayList<>(event.getMetadata()));
                                    popup.setOnCloseHandler(ProMonitoringAlarmCallListFragment.this);
                                    BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
                                }
                            })).onFailure(new Listener<Throwable>() {
                                @Override
                                public void onEvent(Throwable event) {
                                    ErrorManager.in(getActivity()).showGenericBecauseOf(event);
                                }
                            });
                        }
                    })
                    .appendText(R.string.ucc_add_contact_link)
                    .endLinkSpan()
                    .build();

        } else if (SubscriptionController.isPremiumOrPro()) {
            alarmTitle.setText(R.string.alarm_calltree_premium);
            alarmSubTitle.setVisibility(View.GONE);
        } else {
            alarmTitle.setText(R.string.alarm_calltree_basic);
            alarmSubTitle.setVisibility(View.GONE);
        }

        // drag & drop manager
        RecyclerViewDragDropManager mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.material_shadow_z3));

        mAdapter = new CallListEntryAdapter();
        RecyclerView.Adapter mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);

        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);

        // Handle clicks on "Add up to 6 people" cells...
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (mAdapter.getItem(position).hasDisclosure()) {
                    BackstackManager.getInstance().navigateToFragment(ProMonitoringCallListRecommendation.newInstance(), true);
                }
            }
        }));

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        this.editDoneMenu = item;

        if (!isEditMode) {
            presenter.edit();
        } else {
            presenter.save(mAdapter.getCallListEntries(), mAdapter.getEnabledCallListEntries());
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        presenter.startPresenting(this);
        presenter.show();
    }

    @Override
    public void onPause() {
        super.onPause();

        presenter.stopPresenting();
    }

    @Override
    public String getTitle() {
        return getString(R.string.alarm_calltree);
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        return SubscriptionController.isPremiumOrPro() ? R.menu.menu_edit_done_toggle : null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_promon_call_list;
    }

    protected void populateCalltree(CallListEntryAdapter adapter, List<CallListEntry> entries) {
        if (SubscriptionController.isPremiumOrPro()) {
            populatePremiumCalltree(adapter, entries);
        } else {
            populateBasicCalltree(adapter);
        }
    }

    private void populatePremiumCalltree(final CallListEntryAdapter adapter, List<CallListEntry> entries) {
        List<PersonListItemModel> editedList = new ArrayList<>();

        for (int index = 0; index < entries.size(); index++) {
            CallListEntry entry = entries.get(index);
            PersonListItemModel thisListItem;

            if (index == 0) {
                thisListItem = PersonListItemModel.forDisabledPerson(
                        StringUtils.upperCase(entry.getFirstName()),
                        StringUtils.upperCase(entry.getLastName()),
                        CorneaUtils.getPersonRelationship(entry.getId()),
                        entry.getId(),
                        entry.isEnabled());
            } else {
                thisListItem = PersonListItemModel.forEnabledPerson(
                        StringUtils.upperCase(entry.getFirstName()),
                        StringUtils.upperCase(entry.getLastName()),
                        CorneaUtils.getPersonRelationship(entry.getId()),
                        entry.getId(),
                        entry.isEnabled(),
                        true);
            }
            editedList.add(thisListItem);
        }

        if (editedList.size() < 2 && !isEditMode) {
            editedList.add(PersonListItemModel.forDummyItem(getString(R.string.alarm_calltree_add_six_people), null, R.drawable.dashed_person, SubscriptionController.isProfessional(), false));
        }

        adapter.setPersonListEntries(editedList);
    }

    private void populateBasicCalltree(CallListEntryAdapter adapter) {
        List<PersonListItemModel> editedList = new ArrayList<>();
        PersonModel accountHolder = CorneaUtils.getAccountHolder();

        if (accountHolder != null) {
            editedList.add(PersonListItemModel.forEnabledPerson(
                    StringUtils.upperCase(accountHolder.getFirstName()),
                    StringUtils.upperCase(accountHolder.getLastName()),
                    CorneaUtils.getPersonRelationship(accountHolder),
                    accountHolder.getId(),
                    true,
                    false
            ));
        }

        editedList.add(PersonListItemModel.forDummyItem(getString(R.string.alarm_calltree_add_six_people), getString(R.string.alarm_calltree_add_six_people_desc), R.drawable.dashed_person, SubscriptionController.isProfessional(), false));
        adapter.setPersonListEntries(editedList);
    }

    @Override
    public void onCallTreeTooBigError() {
        ErrorManager.in(getActivity()).show(CallTreeErrorType.TOO_BIG);
    }

    @Override
    public void onCallTreeEntryMissingPinError(PersonModel personMissingPin) {
        ErrorManager.in(getActivity()).show(CallTreeErrorType.NO_PIN);
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        // Nothing to do
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    @Override
    public void updateView(@NonNull List<CallListEntry> model) {
        isEditMode = false;
        updateMenuTitle();

        addMonitoringStationContact.setVisibility(SubscriptionController.isProfessional() && !PreferenceUtils.hasAddedUccContact() ? View.VISIBLE : View.GONE);
        goToSettingsCopy.setVisibility(View.GONE);
        populateCalltree(mAdapter, model);
        mAdapter.setIsEditable(isEditMode);
    }

    @Override
    public void updateViewWithEditableCallTree(List<CallListEntry> contacts) {
        isEditMode = true;
        updateMenuTitle();

        addMonitoringStationContact.setVisibility(View.GONE);
        goToSettingsCopy.setVisibility(View.VISIBLE);
        populateCalltree(mAdapter, contacts);
        mAdapter.setIsEditable(isEditMode);

        for (PersonListItemModel thisItem : mAdapter.getItems()) {
            thisItem.setOnPersonCheckedChangeListener(new PersonListItemModel.OnPersonCheckedChangeListener() {
                @Override
                public void onPersonCheckedChange(boolean isChecked) {
                }
            });
        }
    }

    private void updateMenuTitle() {
        if (this.editDoneMenu != null) {
            this.editDoneMenu.setTitle(isEditMode ? getString(R.string.card_menu_done) : getString(R.string.card_menu_edit));
        }
    }

    /**
     * Fired when Add UCC Contact popup is closed
     */
    @Override
    public void onClose() {
        addMonitoringStationContact.setVisibility(SubscriptionController.isProfessional() && !PreferenceUtils.hasAddedUccContact() ? View.VISIBLE : View.GONE);
    }
}