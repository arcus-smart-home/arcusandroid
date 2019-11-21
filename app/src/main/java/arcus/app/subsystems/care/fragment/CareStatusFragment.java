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
package arcus.app.subsystems.care.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;

import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.care.CareStatusController;
import arcus.cornea.subsystem.care.model.AlarmMode;
import arcus.cornea.subsystem.care.model.AlertTrigger;
import arcus.cornea.subsystem.care.model.CareStatus;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.DashboardRecyclerItemClickListener;
import arcus.app.subsystems.alarm.AlarmCallListFragment;
import arcus.app.subsystems.alarm.cards.AlarmActiveCard;
import arcus.app.subsystems.alarm.cards.AlarmInfoCard;
import arcus.app.subsystems.alarm.cards.AlarmStatusCard;
import arcus.app.subsystems.alarm.cards.AlarmTopCard;
import arcus.app.subsystems.alarm.cards.internal.AlarmInfoCardItemView;
import arcus.app.subsystems.alarm.security.adapters.SecurityFragmentRecyclerAdapter;



import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import static arcus.app.common.utils.GlobalSetting.AlertCardTags.*;

public class CareStatusFragment extends BaseFragment implements CareStatusController.Callback, IShowedFragment, IClosedFragment {

    private boolean isClosed = true;
    private boolean isAlert = false;
    private RecyclerView mListView;
    private ListenerRegistration statusListener;
    private SecurityFragmentRecyclerAdapter adapter;

    private AlarmTopCard mTopCard;
    private AlarmStatusCard mAlarmStatusCard;
    private AlarmStatusCard mStatusCard;
    private AlarmInfoCard mAllDevicesCard;
    private AlarmInfoCard mHistoryCard;
    private AlarmInfoCard mNoNotificationListCard;
    private AlarmInfoCard mNotificationListCard;
    private AlarmActiveCard mAlarmActiveCard;
    private View slidingTabLayout;

    @NonNull public static CareStatusFragment newInstance(){
        return new CareStatusFragment();
    }

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mListView = (RecyclerView) view.findViewById(R.id.material_listview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mListView.setLayoutManager(layoutManager);

        slidingTabLayout = getActivity().findViewById(R.id.fragment_header_navigation_sliding_tabs);
        mListView.addOnItemTouchListener(new DashboardRecyclerItemClickListener(getActivity(), new DashboardRecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView.ViewHolder view, int position) {
                if (view instanceof AlarmInfoCardItemView) {
                    AlarmInfoCardItemView item = (AlarmInfoCardItemView) view;
                    switch (item.getCardType()) {
                        case ALL_DEVICES_CARD:
                            CareBehaviorsFragment careBehaviorsFragment = new CareBehaviorsFragment();
                            BackstackManager.getInstance().navigateToFragment(careBehaviorsFragment,true);
                            break;
                        case HISTORY_CARD:
                            CareAlarmHistory careAlarmHistory = new CareAlarmHistory();
                            BackstackManager.getInstance().navigateToFragment(careAlarmHistory, true);
                            break;
                        case NOTIFICATIONS_CARD:
                            BackstackManager.getInstance().navigateToFragment(AlarmCallListFragment.newInstance(AlarmCallListFragment.SUBSYSTEM_CARE), true);
                            break;
                        default:
                            break;
                    }
                }

            }

            @Override
            public void onDragRight(RecyclerView.ViewHolder view, int position) {

            }

            @Override
            public void onDragLeft(RecyclerView.ViewHolder view, int position) {

            }
        }));

        return view;
    }

    @Override public void onResume() {
        super.onResume();
        populateCards();

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
        statusListener = CareStatusController.instance().setCallback(this);
    }

    @Override public void onPause() {
        super.onPause();
        hideProgressBar();
        Listeners.clear(statusListener);
    }

    private void populateCards(){
        if(adapter == null) {
            adapter = new SecurityFragmentRecyclerAdapter(new ArrayList<SimpleDividerCard>());
        }
        adapter.removeAll();

        // Card order is in order of first added to last
        if (mTopCard == null) {
            mTopCard = new AlarmTopCard(getActivity());
            mTopCard.setCareCard(true);
            mTopCard.setCenterTopText(getStringSpan(R.string.care_behaviors_title_status));
            mTopCard.setTag(TOP_CARD);
        }
        adapter.add(mTopCard);

        if (mAllDevicesCard == null) {
            mAllDevicesCard = new AlarmInfoCard(getActivity());
            mAllDevicesCard.setTag(ALL_DEVICES_CARD);
            mAllDevicesCard.setTitle("CARE BEHAVIORS");
            mAllDevicesCard.setDescription("ADD CARE BEHAVIORS");
            mAllDevicesCard.showChevron();
            mAllDevicesCard.setImageResource(R.drawable.service_care_icon);
        }
        adapter.add(mAllDevicesCard);

        if (mHistoryCard == null) {
            mHistoryCard = new AlarmInfoCard(getActivity());
            mHistoryCard.setTag(HISTORY_CARD);
            mHistoryCard.setTitle("CARE ALARM HISTORY");
            mHistoryCard.showChevron();
            mHistoryCard.setImageResource(R.drawable.icon_alert);
            mHistoryCard.showDivider();
        }
        adapter.add(mHistoryCard);

        if (mNotificationListCard == null) {
            mNotificationListCard = new AlarmInfoCard(getActivity());
            mNotificationListCard.setTag(NOTIFICATIONS_CARD);
            mNotificationListCard.setTitle(getString(R.string.security_alarm_notification_list));
            mNotificationListCard.setImageResource(R.drawable.notification_icon);
            mNotificationListCard.showChevron();
            mNotificationListCard.showDivider();
        }

        if (mStatusCard == null) {
            mStatusCard = new AlarmStatusCard(getActivity());
            mStatusCard.setTag(STATUS_CARD);
            mStatusCard.showDivider();
        }
        adapter.add(mNotificationListCard);
        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @NonNull @Override public String getTitle() {
        return getString(R.string.card_care_title);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_safety_alarm;
    }

    @Override public void showError(Throwable cause) {
        hideProgressBar();
        if (mTopCard != null) {
            mTopCard.setToggleEnabled(true);
        }

        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override public void showSummary(CareStatus careStatus) {
        hideProgressBar();
        logger.debug("Should show {}", careStatus);
        isAlert = false;
        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
            activity.invalidateOptionsMenu();
        }

        mTopCard.setDeviceImage(null);
        if (slidingTabLayout != null) {
            slidingTabLayout.setVisibility(View.VISIBLE);
        }
        mListView.setBackgroundColor(Color.TRANSPARENT);
        mTopCard.setTotalDevices(careStatus.getTotalBehaviors());

        String lastAlert = careStatus.getLastAlertString();
        mHistoryCard.setDescription(TextUtils.isEmpty(lastAlert) ? getString(R.string.care_no_alam_history) : lastAlert);
        List<String> callTree = careStatus.getNotificationList();
        loadPersons(callTree);

        if (careStatus.getTotalBehaviors() == 0) {
            mTopCard.setCenterTopText(getStringSpan(R.string.care_behaviors_title_status_none_added));
            mTopCard.setCenterBottomText(null);
            mTopCard.setShowCareOnOff(false);
            mTopCard.setCareToggleListener(null);
            mTopCard.setAlarmState(AlarmTopCard.AlarmState.OFF);
            mAllDevicesCard.setDescription(getString(R.string.care_add_care_behaviors));
            populateCards();
            return;
        }

        mTopCard.setCareToggleListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressBar();
                mTopCard.setToggleEnabled(false);
                CareStatusController.instance().setAlarmOn(((ToggleButton) v).isChecked());
            }
        });

        mTopCard.setShowCareOnOff(true);
        mTopCard.setToggleEnabled(true); // Alarm On/Off toggle....
        if (AlarmMode.ON.equals(careStatus.getAlarmMode())) {
            mTopCard.setAlarmState(AlarmTopCard.AlarmState.ON);
            mTopCard.setCenterTopText(getStringSpan(R.string.care_behaviors_title_status));
            mTopCard.setCenterBottomText(StringUtils.getSuperscriptSpan(
                  String.valueOf(careStatus.getActiveBehaviors()),
                  " / " + String.valueOf(careStatus.getTotalBehaviors())));
            mTopCard.setActiveDevices(careStatus.getActiveBehaviors());
            mTopCard.setBypassDevices(careStatus.getTotalBehaviors() - careStatus.getActiveBehaviors());
            mAllDevicesCard.setDescription(String.format("%s of %s Active", careStatus.getActiveBehaviors(), careStatus
                  .getTotalBehaviors()));
            mTopCard.setToggleOn(true); // Alarm On/Off toggle....
        }
        else {
            mTopCard.setAlarmState(AlarmTopCard.AlarmState.OFF);
            mTopCard.setCenterTopText(getStringSpan(R.string.care_behaviors_title_status));
            mTopCard.setCenterBottomText(getStringSpan("Off"));
            mAllDevicesCard.setDescription("Disabled");
            mTopCard.setToggleOn(false); // Alarm On/Off toggle....
        }

        populateCards();
    }

    protected String getDateFormat(Date date) {
        String format;
        if (StringUtils.isDateToday(date)) {
            format = "Today h:mm a";
        }
        else if (StringUtils.isDateYesterday(date)) {
            format = "Yesterday h:mm a";
        }
        else {
            format = "MMM d, h:mm a";
        }

        return format;
    }

    protected SpannableString getStringSpan(@StringRes int stringToShow) {
        return getStringSpan(getString(stringToShow));
    }

    protected SpannableString getStringSpan(String stringToShow) {
        return new SpannableString(stringToShow);
    }

    @Override public void showAlerting(CareStatus careStatus) {
        hideProgressBar();
        if(adapter == null) {
            adapter = new SecurityFragmentRecyclerAdapter(new ArrayList<SimpleDividerCard>());
        }
        adapter.removeAll();
        isAlert = true;
        logger.debug("Should show {}", careStatus);
        AlertTrigger trigger = careStatus.getAlertTriggeredBy();
        if (trigger == null) {
            return;
        }
        if (!isClosed) { // Currently being viewed.
            hideTabs();
        }

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getString(R.string.care_alarm_triggered));
            activity.invalidateOptionsMenu();
        }
        mListView.setBackgroundColor(Color.WHITE);
        mTopCard.setAlarmState(AlarmTopCard.AlarmState.ALERT);
        mTopCard.setCenterTopText(getStringSpan(trigger.getTriggerDescription().toUpperCase()));
        mTopCard.setDeviceImage(getImageRes(trigger.getTriggerType()));
        mTopCard.setAlarmType(AlarmTopCard.AlarmType.CARE);

        mStatusCard.setAlarmState(AlarmStatusCard.AlarmState.ALERT);
        mStatusCard.setSinceDate(trigger.getTriggerTime());
        mStatusCard.setAlarmType(AlarmStatusCard.AlarmType.CARE);
        mStatusCard.setLeftButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CareStatusController.instance().disarm();
            }
        });

        adapter.add(mTopCard);
        adapter.add(mStatusCard);

        List<AlertTrigger> allTriggers = careStatus.getAllAlertTriggers();
        if (allTriggers == null || allTriggers.isEmpty()) {
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Iterator<AlertTrigger> triggerIterator = allTriggers.iterator();
        int i = 0;
        while (triggerIterator.hasNext()) {
            AlertTrigger currentTrigger = triggerIterator.next();
            if(i!=0){
                mAlarmActiveCard.showDivider();
            }
            mAlarmActiveCard = new AlarmActiveCard(getActivity());
            mAlarmActiveCard.setImageResource(getImageRes(currentTrigger.getTriggerType()));
            mAlarmActiveCard.setTitle(currentTrigger.getTriggerTitle());
            mAlarmActiveCard.setDescription(currentTrigger.getTriggerDescription());
            mAlarmActiveCard.setAlertTime(sdf.format(currentTrigger.getTriggerTime()));
            mAlarmActiveCard.setIconRes(getImageRes(currentTrigger.getTriggerType()));
            adapter.add(mAlarmActiveCard);
            i++;
        }

        mListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    protected Integer getImageRes(@Nullable AlertTrigger.TriggerType triggerType) {
        if (AlertTrigger.TriggerType.PANIC.equals(triggerType)) {
            return R.drawable.side_menu_rules;
        }
        else {
            return R.drawable.service_care_icon;
        }
    }

    protected void hideTabs() {
        if (isAlert && slidingTabLayout != null) {
            slidingTabLayout.setVisibility(View.GONE);
        }
    }

    @Override public void onShowedFragment() {
        isClosed = false;
        hideTabs();
    }

    @Override public void onClosedFragment() {
        isClosed = true;
    }

    private void loadPersons(@NonNull final List<String> personIds){
        if(personIds.size() == 0) {
            mNotificationListCard.setDescription(getResources().getString(R.string.security_alarm_notification_list_desc));
        }
        else {
            PersonModelProvider.instance().getModels(personIds).load()
                    .onSuccess(Listeners.runOnUiThread(new Listener<List<PersonModel>>() {
                        @Override public void onEvent(List<PersonModel> people) {
                            if(people.size() > 0) {
                                PersonModel person = people.get(0);
                                String display = person.getFirstName();
                                if(people.size() > 1) {
                                    display+= String.format(getResources().getString(R.string.notification_list_description), people.size()-1);
                                }
                                mNotificationListCard.setDescription(display);
                            }

                        }
                    }));
        }
    }
}
