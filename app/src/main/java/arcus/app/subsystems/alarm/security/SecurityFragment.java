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
package arcus.app.subsystems.alarm.security;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.security.SecurityStatusController;
import arcus.cornea.subsystem.security.model.ArmedModel;
import arcus.cornea.subsystem.security.model.ArmingModel;
import arcus.cornea.subsystem.security.model.PromptUnsecuredModel;
import arcus.cornea.subsystem.security.model.Trigger;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PersonModel;
import arcus.app.R;
import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.SecurityWalkthroughFragment;
import arcus.app.account.settings.walkthroughs.WalkthroughBaseFragment;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.PreferenceCache;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.SlidingTabLayout;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.subsystems.alarm.cards.AlarmActiveCard;
import arcus.app.subsystems.alarm.cards.AlarmInfoCard;
import arcus.app.subsystems.alarm.cards.AlarmStatusCard;
import arcus.app.subsystems.alarm.cards.AlarmTopCard;
import arcus.app.subsystems.alarm.security.adapters.SecurityFragmentRecyclerAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;



public class SecurityFragment extends BaseFragment implements SecurityStatusController.AlarmCallback, SecurityStatusController.ButtonCallback {
    private static final String SDF_TRIGGERS = "h:mm aa";
    private RecyclerView mListView;

    private SecurityStatusController mStatusController;
    private SecurityFragmentRecyclerAdapter adapter;

    private AlarmTopCard mTopCard;
    private AlarmStatusCard mStatusCard;
    private AlarmInfoCard mAllDevicesCard;
    private AlarmInfoCard mPartialDevicesCard;
    private AlarmInfoCard mHistoryCard;
    private AlarmActiveCard mAlarmActiveCard;
    private AlarmInfoCard mNotificationListCard;

    ListenerRegistration mAlarmListener;
    ListenerRegistration mButtonListener;

    Boolean mAlarmTriggered = false;
    SlidingTabLayout mslidingTabLayout;

    private boolean mBasicContact = true;
    private boolean walkthroughTriggered = false;
    private boolean bAlerting = false;

    @NonNull
    public static SecurityFragment newInstance() {
        SecurityFragment fragment = new SecurityFragment();

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        setHasOptionsMenu(true);

        mListView = (RecyclerView) view.findViewById(R.id.material_listview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mListView.setLayoutManager(layoutManager);

        mslidingTabLayout = (SlidingTabLayout) getActivity().findViewById(R.id.fragment_header_navigation_sliding_tabs);

        /*mListView.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new ArrayList<Integer>(), new RecyclerItemClickListener.OnItemClickListener() {

            @Override
            public void onItemClick(RecyclerView.ViewHolder view, int position) {
                if (view instanceof AlarmInfoCardItemView) {
                    AlarmInfoCardItemView item = (AlarmInfoCardItemView) view;
                    switch (item.getCardType()) {
                        case ALL_DEVICES_CARD:
                            BackstackManager.getInstance().navigateToFragment(DevicesFragment.newInstance(false, DevicesFragment.title.ON), true);
                            break;
                        case PARTIAL_DEVICES_CARD:
                            BackstackManager.getInstance().navigateToFragment(DevicesFragment.newInstance(true, DevicesFragment.title.PARTIAL), true);
                            break;
                        case HISTORY_CARD:
                            BackstackManager.getInstance().navigateToFragment(SecurityHistory.newInstance(), true);
                            break;
                        case NOTIFICATIONS_CARD:
                            BackstackManager.getInstance().navigateToFragment(AlarmCallListFragment.newInstance(AlarmCallListFragment.SUBSYSTEM_SECURITY), true);
                            break;
                        default:
                            break;
                    }
                }
            }
        }));*/

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        populateCards();

        if (mStatusController == null) {
            mStatusController = SecurityStatusController.instance();
        }

        mAlarmListener = mStatusController.setAlarmCallback(this);
        mButtonListener = mStatusController.setButtonCallback(this);

        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        mAlarmListener.remove();
        mButtonListener.remove();
    }

    private void populateCards() {
        ArrayList<SimpleDividerCard> cards = new ArrayList<>();
        if (mListView == null) {
            logger.debug("Triggers -> bg view null");
        }
        else {
            mListView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Card order is in order of first added to last
        mslidingTabLayout.setVisibility(View.VISIBLE);
        mListView.setBackgroundColor(Color.TRANSPARENT);

        if (mTopCard == null) {
            mAlarmTriggered = true;
            mTopCard = new AlarmTopCard(getActivity());
            mTopCard.setTag(GlobalSetting.AlertCardTags.TOP_CARD);
            mTopCard.setActiveDevices(1);
        }
        cards.add(mTopCard);

        if (mStatusCard == null) {
            mStatusCard = new AlarmStatusCard(getActivity());
            mStatusCard.setTag(GlobalSetting.AlertCardTags.STATUS_CARD);
            mStatusCard.showDivider();
        }

        cards.add(mStatusCard);

        if (mAllDevicesCard == null) {
            mAllDevicesCard = new AlarmInfoCard(getActivity());
            mAllDevicesCard.setTag(GlobalSetting.AlertCardTags.ALL_DEVICES_CARD);
            mAllDevicesCard.setTitle(getString(R.string.security_all_devices));
            mAllDevicesCard.setImageResource(R.drawable.icon_homesafety_white);
            mAllDevicesCard.showChevron();
            mAllDevicesCard.showDivider();
        }
        cards.add(mAllDevicesCard);

        if (mPartialDevicesCard == null) {
            mPartialDevicesCard = new AlarmInfoCard(getActivity());
            mPartialDevicesCard.setTag(GlobalSetting.AlertCardTags.PARTIAL_DEVICES_CARD);
            mPartialDevicesCard.setTitle(getString(R.string.security_partial_devices));
            mPartialDevicesCard.setImageResource(R.drawable.icon_homesafety_white);
            mPartialDevicesCard.showChevron();
            mPartialDevicesCard.showDivider();
        }
        cards.add(mPartialDevicesCard);


        if (mHistoryCard == null) {
            mHistoryCard = new AlarmInfoCard(getActivity());
            mHistoryCard.setTag(GlobalSetting.AlertCardTags.HISTORY_CARD);
            mHistoryCard.setTitle(getString(R.string.security_alarm_history));
            mHistoryCard.setImageResource(R.drawable.icon_alert);
            mHistoryCard.showChevron();
            mHistoryCard.showDivider();
        }
        cards.add(mHistoryCard);

        if (mNotificationListCard == null) {
            mNotificationListCard = new AlarmInfoCard(getActivity());
            mNotificationListCard.setTag(GlobalSetting.AlertCardTags.NOTIFICATIONS_CARD);
            mNotificationListCard.setTitle(getString(R.string.security_alarm_notification_list));
            mNotificationListCard.setImageResource(R.drawable.notification_icon);
            mNotificationListCard.showChevron();
            mNotificationListCard.showDivider();
        }

        cards.add(mNotificationListCard);
        if(adapter == null) {
            adapter = new SecurityFragmentRecyclerAdapter(cards);
        }
        mListView.setAdapter(adapter);
    }

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.security_alarm_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_security_alarm;
    }

    /*
     * AlarmCallback
     */

    private void updateAdapter() {
        if(mListView.getAdapter() != null) {
            mListView.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void showOff(Date date) {
        // Update Top Card
        bAlerting = false;
        showWalkthrough();
        mTopCard.setAlarmState(AlarmTopCard.AlarmState.OFF);
        mTopCard.setCenterTopText(new SpannableString(getString(R.string.security_alarm_twoline)));
        mTopCard.setCenterBottomText(new SpannableString(getResources().getString(R.string.off_first_capital)));
        mStatusCard.setAlarmState(AlarmStatusCard.AlarmState.OFF);
        mStatusCard.setSinceDate(date);
        mStatusCard.setStatus(getString(R.string.security_alarm_off_since));

        // On Listener
        mStatusCard.setLeftButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusController.arm(SecuritySubsystem.ALARMMODE_ON);
            }
        });

        // Partial Listener
        mStatusCard.setRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusController.arm(SecuritySubsystem.ALARMMODE_PARTIAL);
            }
        });
        updateAdapter();
    }

    @Override
    public void showArming(@NonNull ArmingModel model) {
        // Update Top Card
        bAlerting = false;
        showWalkthrough();
        mTopCard.setAlarmState(AlarmTopCard.AlarmState.ARMING);
        mTopCard.setCenterTopText(new SpannableString(getString(R.string.security_arming_in)));
        mTopCard.setCenterBottomText(StringUtils.getSuperscriptSpan(String.valueOf(model.getCountdownSec()) + " ", "S"));
        mStatusCard.setAlarmState(AlarmStatusCard.AlarmState.ARMING);
        mStatusCard.setSinceDate(null);

        // ArmNow Listener
        mStatusCard.setLeftButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusController.armNow();
            }
        });

        // Off Listener
        mStatusCard.setRightButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusController.disarm();
            }
        });
        updateAdapter();
    }

    @Override
    public void updateArming(int i) {
        // Update Top Card
        bAlerting = false;
        showWalkthrough();
        mTopCard.setCenterBottomText(StringUtils.getSuperscriptSpan(String.valueOf(i) + " ", "S"));
        updateAdapter();
    }

    @Override
    public void showArmed(@NonNull ArmedModel armedModel) {
        // Update Top Card
        bAlerting = false;
        showWalkthrough();
        mTopCard.setCenterTopText(new SpannableString(getString(R.string.security_devices)));
        mTopCard.setCenterBottomText(new SpannableString(String.valueOf(armedModel.getTotal())));

        if (armedModel.getActive() != armedModel.getTotal()) {
            mTopCard.setCenterBottomText(StringUtils.getSuperscriptSpan(
                    String.valueOf(armedModel.getActive()),
                    " / " + String.valueOf(armedModel.getTotal())));
        }

        mTopCard.setActiveDevices(armedModel.getActive());
        mTopCard.setBypassDevices(armedModel.getBypassed());
        mTopCard.setOfflineDevices(armedModel.getOffline());

        mStatusCard.setSinceDate(armedModel.getArmedSince());

        if (armedModel.getMode().equals("PARTIAL")) {


            mTopCard.setAlarmState(AlarmTopCard.AlarmState.PARTIAL);
            mStatusCard.setAlarmState(AlarmStatusCard.AlarmState.PARTIAL);
            mStatusCard.setStatus(getString(R.string.security_partial_since));
        } else {
            mTopCard.setAlarmState(AlarmTopCard.AlarmState.ON);
            mStatusCard.setAlarmState(AlarmStatusCard.AlarmState.ON);
            mStatusCard.setStatus(getString(R.string.security_on_since));
        }

        // Off Listener
        mStatusCard.setLeftButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStatusController.disarm();
            }
        });
        updateAdapter();
    }

    @Override
    public void showAlert(Trigger cause, Date alarmSince, List<Trigger> allAlerts) {
        bAlerting = true;
        if (mListView == null) {
            logger.debug("Triggers -> bg view null");
        }
        else {
            mListView.setBackgroundColor(Color.WHITE);
        }

        walkthroughTriggered=true;

        if(BackstackManager.getInstance().getCurrentFragment() instanceof SecurityWalkthroughFragment) {
            BackstackManager.getInstance().navigateBack();
        }

        // Update Top Card
        if(cause.isPanic()) {
            mListView.setBackgroundColor(Color.WHITE);
            mTopCard.setAlarmState(AlarmTopCard.AlarmState.ALERT);
            mTopCard.setDeviceModel(null);
            String label = getActivity().getString(R.string.security_alarm_panic);
            mTopCard.setCenterTopText(new SpannableString(label));
        }
        else {
            DeviceModel deviceModel = SessionModelManager.instance().getDeviceWithId(cause.getId(), false);
            mTopCard.setAlarmState(AlarmTopCard.AlarmState.ALERT);
            mTopCard.setDeviceModel(deviceModel);
            mTopCard.setCenterTopText(SpannableString.valueOf(cause.getName()));
        }

        mStatusCard.setAlarmState(AlarmStatusCard.AlarmState.ALERT);
        mStatusCard.setStatus(getString(R.string.security_alarm_since));
        mStatusCard.setSinceDate(alarmSince);

        // Cancel Listener
        mStatusCard.setLeftButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlarmTriggered = false;
                mStatusController.disarm();
            }
        });

        // Add cards needed
        SecurityFragmentRecyclerAdapter adapter = (SecurityFragmentRecyclerAdapter) mListView.getAdapter();
        if(adapter != null) {
            adapter.removeAll();
        }

        ArrayList<SimpleDividerCard> cards = new ArrayList<>();
        cards.add(mTopCard);
        cards.add(mStatusCard);
        mslidingTabLayout.setVisibility(View.GONE);

        // Insert Triggered Cards
        SimpleDateFormat sdf = new SimpleDateFormat(SDF_TRIGGERS, Locale.getDefault());
        for (Trigger triggeredCard : allAlerts) {
            if(!allAlerts.get(0).equals(triggeredCard)){
                mAlarmActiveCard.showDivider();
            }
            String activeTime = sdf.format(triggeredCard.getTriggeredSince());
            DeviceModel activeDeviceModel = SessionModelManager.instance().getDeviceWithId(triggeredCard.getId(), false);
            mAlarmActiveCard = new AlarmActiveCard(getActivity());
            mAlarmActiveCard.setDeviceModel(activeDeviceModel);
            mAlarmActiveCard.setTag(GlobalSetting.AlertCardTags.ALARM_ACTIVE);
            if ( activeDeviceModel != null ) mAlarmActiveCard.setTitle(activeDeviceModel.getName());
            else mAlarmActiveCard.setTitle("DEVICE TRIGGERED");
            mAlarmActiveCard.setDescription(triggeredCard.getType().substring(0, 1).toUpperCase() + triggeredCard.getType().substring(1));
            mAlarmActiveCard.setAlertTime(activeTime);
            cards.add(mAlarmActiveCard);
        }
        if(adapter == null) {
            adapter = new SecurityFragmentRecyclerAdapter(cards);
        }
        mListView.setAdapter(adapter);
    }

    @Override
    public void promptUnsecured(@NonNull final PromptUnsecuredModel model) {
        // Show Prompt
//        showUnsecuredDevicesDialog(s);
        final AlertFloatingFragment floatingFragment = AlertFloatingFragment.newInstance(
                getActivity().getString(R.string.security_alarm_alert_dialog_devices_message),
                getActivity().getString(R.string.security_alarm_alert_dialog_message),
                getActivity().getString(R.string.continue_to_arm),
                getActivity().getString(R.string.cancel),
                new AlertFloatingFragment.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        mStatusController.armBypassed(model.getMode());
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return true;
                    }
                });
        BackstackManager.getInstance().navigateToFloatingFragment(floatingFragment, floatingFragment.getClass().getName(), true);
    }

    /*
     * ButtonCallback
     */

    @Override
    public void updateAllDevices(String s) {
        mAllDevicesCard.setDescription(s);
        updateAdapter();
    }

    @Override
    public void updatePartialDevices(String s) {
        mPartialDevicesCard.setDescription(s);
        updateAdapter();
    }

    @Override
    public void updateHistory(Date date) {
        if (date == null) {
            return;
        }

        try {
            String prefix = getString(R.string.last_alarm);
            mHistoryCard.setDescription(String.format("%s%s", prefix, DateUtils.format(date)));
            updateAdapter();
        }
        catch (Exception ex) {
            logger.debug("Could not update last trigger time.", ex);
        }
    }

    @Override
    public void showBasicContact(List<String> s) {
        logger.debug("Show basic contact list: {}", s);
        mBasicContact = true;
        loadPersons(s);
        populateCards();
    }

    @Override
    public void showAllContacts(@NonNull List<String> personIds) {
        logger.debug("Show all contact list: {}", personIds);
        mBasicContact = false;
        List<String> personImages = new ArrayList<>();

        for(String thisPerson : personIds){
            personImages.add(thisPerson);
        }
        loadPersons(personImages);
        populateCards();
    }

    public boolean isAlerting() {
        return bAlerting;
    }

    public void showWalkthrough() {
        if(!walkthroughTriggered){
            if (!PreferenceCache.getInstance().getBoolean(PreferenceUtils.ALARMS_WALKTHROUGH_DONT_SHOW_AGAIN, false)) {
                WalkthroughBaseFragment climate = WalkthroughBaseFragment.newInstance(WalkthroughType.SECURITY);
                BackstackManager.getInstance().navigateToFloatingFragment(climate, climate.getClass().getName(), true);
                walkthroughTriggered=true;
            }
        }
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
                                if(people.size() == 2) {
                                    display+= String.format(" " + getResources().getString(R.string.notification_list_description), people.size()-1);
                                }
                                else {
                                    display+= String.format(" " + getResources().getString(R.string.notification_list_description_plural), people.size()-1);
                                }
                            }
                            mNotificationListCard.setDescription(display);
                        }
                    }
                }));
        }
    }
}
