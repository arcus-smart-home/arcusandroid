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
package arcus.app.subsystems.alarm.safety;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dexafree.materialList.controller.RecyclerItemClickListener;
import com.dexafree.materialList.model.CardItemView;
import com.dexafree.materialList.view.MaterialListView;
import arcus.cornea.subsystem.calllist.CallListEntry;
import arcus.cornea.subsystem.safety.SafetyStatusController;
import arcus.cornea.subsystem.safety.model.Alarm;
import arcus.cornea.subsystem.safety.model.DeviceCounts;
import arcus.cornea.subsystem.safety.model.SensorSummary;
import arcus.cornea.utils.DateUtils;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.LeakH2O;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.SlidingTabLayout;
import arcus.app.subsystems.alarm.AlarmCallListFragment;
import arcus.app.subsystems.alarm.cards.AlarmActiveCard;
import arcus.app.subsystems.alarm.cards.AlarmInfoCard;
import arcus.app.subsystems.alarm.cards.AlarmStatusCard;
import arcus.app.subsystems.alarm.cards.AlarmTopCard;
import arcus.app.subsystems.alarm.safety.cards.SafetyStatusCard;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class SafetyAlarmFragment extends BaseFragment implements SafetyStatusController.Callback{

    private static final String SDF_TRIGGERS = "h:mm aa";
    private MaterialListView mListView;

    private SafetyStatusController statusController;

    private ListenerRegistration mAlarmListener;

    private enum CardTags {
        TOP_CARD,
        STATUS_CARD,
        ALARM_STATUS_CARD,
        ALL_DEVICES_CARD,
        HISTORY_CARD,
        NOTIFICATIONS_CARD,
        ALARM_ACTIVE
    }

    private AlarmTopCard mTopCard;
    private SafetyStatusCard mStatusCard;
    private AlarmStatusCard mAlarmStatusCard;
    private AlarmInfoCard mAllDevicesCard;
    private AlarmInfoCard mHistoryCard;
    private AlarmInfoCard mNotificationListCard;
    private AlarmActiveCard mAlarmActiveCard;

    private boolean mBasicContact = true;
    private boolean mIsAlarmOn = false;

    SlidingTabLayout mSlidingTabLayout;


    public interface DevicesCountUpdateListener{
        void updateOffline(int offline);
        void updateBypassed(int bypassed);
        void updateActive(int active);
    }

    @NonNull
    public static SafetyAlarmFragment newInstance(){
        SafetyAlarmFragment fragment = new SafetyAlarmFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        mListView = (MaterialListView)view.findViewById(R.id.material_listview);
        mSlidingTabLayout = (SlidingTabLayout) getActivity().findViewById(R.id.fragment_header_navigation_sliding_tabs);

        mListView.addOnItemTouchListener(new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(@NonNull CardItemView view, int position) {
                if (view.getTag() == null) return;

                switch ((CardTags) view.getTag()) {
                    case TOP_CARD:
                        break;
                    case STATUS_CARD:
                        break;
                    case ALARM_STATUS_CARD:
                        break;
                    case ALL_DEVICES_CARD:
                        BackstackManager.getInstance().navigateToFragment(SafetyAlarmDevicesFragment.newInstance(),true);
                        break;
                    case HISTORY_CARD:
                        BackstackManager.getInstance().navigateToFragment(SafetyHistory.newInstance(),true);
                        break;
                    case NOTIFICATIONS_CARD:
                        BackstackManager.getInstance().navigateToFragment(AlarmCallListFragment.newInstance(AlarmCallListFragment.SUBSYSTEM_SAFETY),true);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onItemLongClick(CardItemView view, int position) {
            }
        });

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        populateCards();

        if(statusController ==null){
            statusController = SafetyStatusController.instance();
        }

        mAlarmListener = statusController.setCallback(this);

    }

    @Override
    public void onPause() {
        super.onPause();
        removeAlarmListener();
    }

    public boolean isAlarmOn() {
        return mIsAlarmOn;
    }

    private void removeAlarmListener(){
        if(mAlarmListener ==null || !mAlarmListener.isRegistered()){
            return;
        }
        mAlarmListener.remove();
    }

    private void populateCards(){
        if (mIsAlarmOn) {
            return;
        }

        mSlidingTabLayout.setVisibility(View.VISIBLE);
        mListView.setBackgroundColor(Color.TRANSPARENT);
        mListView.clear();

        // Card order is in order of first added to last
        if (mTopCard == null) {
            mTopCard = new AlarmTopCard(getActivity());
            mTopCard.setCenterTopText(new SpannableString("SAFETY\nALARM"));
            mTopCard.setCenterBottomText(new SpannableString("OFF"));
            mTopCard.setTag(CardTags.TOP_CARD);
        }
        mListView.add(mTopCard);

        if (mStatusCard == null) {
            mStatusCard = new SafetyStatusCard(getActivity());
            mStatusCard.setTag(CardTags.STATUS_CARD);
            mStatusCard.showDivider();
        }

        if(mAlarmStatusCard == null){
            mAlarmStatusCard = new AlarmStatusCard(getActivity());
            mAlarmStatusCard.setTag(CardTags.ALARM_STATUS_CARD);
            mAlarmStatusCard.showDivider();
        }

        mListView.add(mStatusCard);

        if (mAllDevicesCard == null) {
            mAllDevicesCard = new AlarmInfoCard(getActivity());
            mAllDevicesCard.setTag(CardTags.ALL_DEVICES_CARD);
            mAllDevicesCard.setTitle("SAFETY ALARM DEVICES");
            mAllDevicesCard.showChevron();
            mAllDevicesCard.setImageResource(R.drawable.icon_security);
            mAllDevicesCard.showDivider();
        }
        mListView.add(mAllDevicesCard);

        if (mHistoryCard == null) {
            mHistoryCard = new AlarmInfoCard(getActivity());
            mHistoryCard.setTag(CardTags.HISTORY_CARD);
            mHistoryCard.setTitle("ALARM HISTORY");
            mHistoryCard.showChevron();
            mHistoryCard.setImageResource(R.drawable.icon_alert);
            mHistoryCard.showDivider();
        }
        mListView.add(mHistoryCard);

        if (mNotificationListCard == null) {
            logger.error("Creating notification list card...");
            mNotificationListCard = new AlarmInfoCard(getActivity());
            mNotificationListCard.setTag(CardTags.NOTIFICATIONS_CARD);
            mNotificationListCard.setTitle(getString(R.string.security_alarm_notification_list));
            mNotificationListCard.setImageResource(R.drawable.notification_icon);
            mNotificationListCard.showChevron();
            mNotificationListCard.showDivider();
        }

        mListView.add(mNotificationListCard);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResources().getString(R.string.safety_alarm_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_safety_alarm;
    }

    @Override
    public void showUnsatisfiableCopy() {
        //todo: no safety alarm devices
    }


    //alarm system is online and show summary
    @Override
    public void showSummary(SensorSummary summary) {
        mIsAlarmOn = false;
        mTopCard.setAlarmState(AlarmTopCard.AlarmState.ON);
        mTopCard.setCenterTopText(new SpannableString("DEVICES"));

        mStatusCard.setSummary(summary);

        populateCards();
    }

    @Override
    public void showCounts(@NonNull DeviceCounts counts) {
        mTopCard.setActiveDevices(counts.getActiveDevices());
        mTopCard.setBypassDevices(0);
        mTopCard.setOfflineDevices(counts.getOfflineDevices());

        if(counts.getOfflineDevices() > 0) {
            mAllDevicesCard.setDescription(counts.getOfflineDevices() + " Offline");
            mTopCard.setCenterBottomText(StringUtils.getSuperscriptSpan(String.valueOf(counts.getActiveDevices()), "/" + String.valueOf(counts.getTotalDevices())));
        } else {
            mAllDevicesCard.setDescription(counts.getActiveDevices() + " Devices");
            mTopCard.setCenterBottomText(new SpannableString(String.valueOf(counts.getActiveDevices())));
        }

        populateCards();
    }

    //alarm is triggered
    @Override
    public void showAlarm(List<Alarm> alarm) {
        if (alarm == null || alarm.isEmpty()) {
            populateCards();
            return;
        }

        final boolean isCO = alarm.get(0).getMessage().contains("CO");
        mIsAlarmOn = true;
        // Update Top Card
        mTopCard.setAlarmState(AlarmTopCard.AlarmState.ALERT);
        final DeviceModel deviceModel = SessionModelManager.instance().getDeviceWithId(alarm.get(0).getDevId(), false);
        mTopCard.setDeviceModel(deviceModel);
        if (deviceModel != null && mTopCard != null) {
            mTopCard.setCenterTopText(new SpannableString(deviceModel.getName()));
        }
        // update status card
        mAlarmStatusCard.setAlarmState(AlarmStatusCard.AlarmState.ALERT);
        mAlarmStatusCard.setStatus(alarm.get(0).getMessage());
        mAlarmStatusCard.setSinceDate(alarm.get(0).getTime());

        mListView.clear();
        mListView.setBackgroundColor(Color.WHITE);
        mListView.add(mTopCard);
        mListView.add(mAlarmStatusCard);
        mSlidingTabLayout.setVisibility(View.GONE);

        mAlarmStatusCard.setLeftButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                statusController.cancel();
                if (deviceModel instanceof LeakH2O) {
                    return;
                }
                if (isCO) {
                    BackstackManager.getInstance().navigateToFloatingFragment(StillSoundingFloatingFragment.newInstance(StillSoundingFloatingFragment.SoundDevice.CO), StillSoundingFloatingFragment.class.getName(), true);
                } else {
                    BackstackManager.getInstance().navigateToFloatingFragment(StillSoundingFloatingFragment.newInstance(StillSoundingFloatingFragment.SoundDevice.SMOKE), StillSoundingFloatingFragment.class.getName(), true);
                }
                logger.debug("Safety alarm system is closed: {}", statusController.get());
            }
        });

        Collections.reverse(alarm); // So the sort is newest -> oldest
        SimpleDateFormat sdf = new SimpleDateFormat(SDF_TRIGGERS, Locale.getDefault());
        for (Alarm triggeredCard : alarm) {
            if(!alarm.get(0).equals(triggeredCard)){
                mAlarmActiveCard.showDivider();
            }
            String activeTime = sdf.format(triggeredCard.getTime());
            DeviceModel activeDeviceModel = SessionModelManager.instance().getDeviceWithId(triggeredCard.getDevId(), false);
            mAlarmActiveCard = new AlarmActiveCard(getActivity());
            mAlarmActiveCard.setDeviceModel(activeDeviceModel);
            mAlarmActiveCard.setTag(CardTags.ALARM_ACTIVE);
            mAlarmActiveCard.setTitle(triggeredCard.getName());
            mAlarmActiveCard.setDescription(triggeredCard.getMessage());
            mAlarmActiveCard.setAlertTime(activeTime);
            mListView.add(mAlarmActiveCard);
        }
    }

    @Override
    public void showHistory(HistoryLog event) {
        String prefix = getString(R.string.last_alarm);
        mHistoryCard.setDescription(String.format("%s%s", prefix, DateUtils.format(event.getTimestamp())));
    }

    @Override
    public void showBasicCallList(List<CallListEntry> callList) {
        logger.debug("Show basic  call list: {}", callList);
        mBasicContact = true;
        loadPersons(callList);
        populateCards();
    }

    @Override
    public void showPremiumCallList(@NonNull List<CallListEntry> callList) {
        logger.debug("Show premium call list: {}", callList);

        if (callList.size() == 0) {
            mBasicContact = true;
        } else {
            mBasicContact = false;
            List<String> list = new ArrayList<>();

            for (CallListEntry callListEntry : callList) {
                list.add(callListEntry.getId());
            }
        }
        loadPersons(callList);
        populateCards();
    }

    private void loadPersons(@NonNull final List<CallListEntry> callList) {
        if (callList.size() > 0) {
            CallListEntry person = callList.get(0);
            String display = person.getFirstName();
            if (callList.size() > 1) {
                if (callList.size() == 2) {
                    display += String.format(" " + getResources().getString(R.string.notification_list_description), callList.size() - 1);
                } else {
                    display += String.format(" " + getResources().getString(R.string.notification_list_description_plural), callList.size() - 1);
                }
            }
            mNotificationListCard.setDescription(display);
        }
        else {
            mNotificationListCard.setDescription(getResources().getString(R.string.security_alarm_notification_list_desc));
        }
    }
}
