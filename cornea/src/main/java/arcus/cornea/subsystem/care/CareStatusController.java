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
package arcus.cornea.subsystem.care;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import arcus.cornea.provider.CareBehaviorsProvider;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.subsystem.care.model.AlarmMode;
import arcus.cornea.subsystem.care.model.AlertTrigger;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.CareStatus;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.bean.CallTreeEntry;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.capability.Rule;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.RuleModel;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CareStatusController extends BaseCareController<CareStatusController.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(CareStatusController.class);
    private static final String BY_BEH_FMT = "by %s";
    private static final String BY_PANIC = "by Panic.";
    private static final String ALARM_TRIGGERED = "ALARM TRIGGERED";
    private static final String RULE_TRIGGERED = "RULE TRIGGERED";
    private static final String BEHAVIOR_TRIGGERED = "BEHAVIOR TRIGGERED";
    private static final String UNKNOWN_TRIGGERED = "TRIGGERED";

    public interface Callback {
        void showError(Throwable cause);
        void showSummary(CareStatus careStatus);
        void showAlerting(CareStatus careStatus);
    }

    private final Set<String> UPDATE_ON_CHANGE = ImmutableSet.of(
          CareSubsystem.ATTR_BEHAVIORS,
          CareSubsystem.ATTR_ACTIVEBEHAVIORS,
          CareSubsystem.ATTR_CALLTREE,
          CareSubsystem.ATTR_ALARMMODE,
          CareSubsystem.ATTR_ALARMSTATE,
          CareSubsystem.ATTR_CALLTREEENABLED
    );
    private final Listener<List<Map<String, Object>>> reloadListener =
          new Listener<List<Map<String, Object>>>() {
              @Override
              public void onEvent(List<Map<String, Object>> maps) {
                  updateView();
              }
          };
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(
          new Listener<Throwable>() {
              @Override
              public void onEvent(Throwable throwable) {
                  onError(throwable);
              }
          }
    );
    private final Listener<CareSubsystem.ClearResponse> clearListener =
          new Listener<CareSubsystem.ClearResponse>() {
              @Override
              public void onEvent(CareSubsystem.ClearResponse clearResponse) {
                  updateView();
              }
          };
    private Listener updateViewListener = Listeners.runOnUiThread(new Listener() {
        @Override public void onEvent(Object o) {
            updateView();
        }
    });

    private static final CareStatusController INSTANCE;
    static {
        INSTANCE = new CareStatusController(CareSubsystem.NAMESPACE);
        INSTANCE.init();
    }

    public static CareStatusController instance() {
        return INSTANCE;
    }

    CareStatusController(String namespace) {
        super(namespace);
    }

    CareStatusController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public void setAlarmOn(boolean isOn) {
        CareSubsystem careSubsystem = getCareSubsystemModel();
        if (careSubsystem == null) {
            onError(new RuntimeException("Unable to get subsystem. Cannot Turn Alarm On/Off."));
            return;
        }

        String alarmMode = isOn ? CareSubsystem.ALARMMODE_ON : CareSubsystem.ALARMMODE_VISIT;
        careSubsystem.setAlarmMode(alarmMode);
        ((SubsystemModel)careSubsystem).commit().onFailure(errorListener);
    }

    public void disarm() {
        CareSubsystem careSubsystem = getCareSubsystemModel();
        if (careSubsystem == null) {
            onError(new RuntimeException("Unable to get subsystem. Cannot cancel."));
            return;
        }

        careSubsystem.clear()
              .onFailure(errorListener)
              .onSuccess(clearListener);
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);

        Set<String> currentChanges = event.getChangedAttributes().keySet();
        Set<String> intersection   = Sets.intersection(currentChanges, UPDATE_ON_CHANGE);

        if (!intersection.isEmpty()) {
            updateView();
        }
    }

    protected void showAlerting(final CareStatus careStatus) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Callback callback = getCallback();
                if (callback == null) {
                    return;
                }

                callback.showAlerting(careStatus);
            }
        });
    }

    protected void showSummary(final CareStatus careStatus) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Callback callback = getCallback();
                if (callback == null) {
                    return;
                }

                callback.showSummary(careStatus);
            }
        });
    }

    protected void onError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        callback.showError(throwable);
    }

    @Override public ListenerRegistration setCallback(Callback callback) {
        reloadBehaviors().onSuccess(reloadListener);
        return super.setCallback(callback);
    }

    @Override protected boolean isLoaded() {
        return super.isLoaded() && RuleModelProvider.instance().isLoaded() && DeviceModelProvider.instance().isLoaded() && behaviorsLoaded();
    }

    @SuppressWarnings("unchecked") @Override protected void updateView(Callback callback) {
        if (!isLoaded()) {
            if (!RuleModelProvider.instance().isLoaded()) {
                RuleModelProvider.instance()
                      .load()
                      .onSuccess(updateViewListener)
                      .onFailure(errorListener);
            }

            if (!DeviceModelProvider.instance().isLoaded()) {
                DeviceModelProvider.instance()
                      .load()
                      .onSuccess(updateViewListener)
                      .onFailure(errorListener);
            }

            if (!behaviorsLoaded()) {
                CareBehaviorsProvider.instance().load().onSuccess(updateViewListener);
            }

            boolean subsystem = super.isLoaded();
            boolean rules = RuleModelProvider.instance().isLoaded();
            boolean devices = DeviceModelProvider.instance().isLoaded();
            logger.error("Not updating view not loaded: Sub: {} Rules: {} Devices: {} Behaviors: {}", subsystem, rules, devices, behaviorsLoaded());
            return;
        }

        CareSubsystem careSubsystem = getCareSubsystemModel();
        if (careSubsystem == null) {
            logger.error("Not updating view since CareSubsystem is not loaded.");
            return;
        }

        if (CareSubsystem.ALARMSTATE_ALERT.equals(careSubsystem.getAlarmState())) {
            showAlerting(getAlertStatus(careSubsystem));
        }
        else {
            showSummary(getCareSummary(careSubsystem));
        }
    }

    protected CareStatus getAlertStatus(CareSubsystem careSubsystem) {
        CareStatus careStatus = new CareStatus();

        String alertCause = String.valueOf(careSubsystem.getLastAlertCause());
        List<AlertTrigger> alertTriggers = new ArrayList<>(5);

        Map<String, Date> allTriggers = careSubsystem.getLastAlertTriggers();
        if (allTriggers != null && !allTriggers.isEmpty()) {
            for (Map.Entry<String, Date> item : allTriggers.entrySet()) {
                String itemAddress = String.valueOf(item.getKey());
                Map<String, Object> behavior = CareBehaviorsProvider.instance().getById(item.getKey());
                if (behavior != null) {
                    alertTriggers.add(getBehaviorTrigger(alertCause, item.getKey(), item.getValue()));
                }
                else if (itemAddress.startsWith(Addresses.toServiceAddress(Rule.NAMESPACE))) {
                    addRuleTrigger(alertCause, item.getKey(), item.getValue(), alertTriggers);
                }
                else {
                    alertTriggers.add(getOtherTriggerCause(alertCause, item.getKey(), item.getValue()));
                }
            }

            // If the last one (first thing hit) was a "rule" then it should have 2 entries, unless we couldn't find info about it
            // So if PANIC (rule) AND > 1 get the 1'st one which contains the rule name we actually want to use.
            boolean isPanic = AlertTrigger.TriggerType.PANIC.equals(alertTriggers.get(0).getTriggerType()) && alertTriggers.size() > 1;
            AlertTrigger cause = new AlertTrigger(alertTriggers.get(isPanic ? 1 : 0));
            cause.setTriggerDescription(cause.getTriggerDescription().toUpperCase().replaceFirst("BY ", ""));
            careStatus.setAlertTriggeredBy(cause);
        }
        else {
            careStatus.setAlertTriggeredBy(getOtherTriggerCause(alertCause, alertCause, careSubsystem.getLastAlertTime()));
        }

        careStatus.setAllAlertTriggers(alertTriggers);

        return careStatus;
    }

    protected AlertTrigger getBehaviorTrigger(
          @NonNull String causedByTrigger,
          String currentTrigger,
          Date time
    ) {
        Map<String, Object> behavior = CareBehaviorsProvider.instance().getById(currentTrigger);
        if (behavior == null) {
            return getOtherTriggerCause(causedByTrigger, currentTrigger, time);
        }

        AlertTrigger trigger = new AlertTrigger();
        trigger.setTriggerTitle(causedByTrigger.equals(currentTrigger) ? ALARM_TRIGGERED : BEHAVIOR_TRIGGERED);
        trigger.setTriggerType(AlertTrigger.TriggerType.BEHAVIOR);
        trigger.setTriggerDescription(String.format(BY_BEH_FMT, CareBehaviorModel.fromMap(behavior, "").getName()));
        trigger.setTriggerID(currentTrigger);
        trigger.setTriggerTime(time);
        return trigger;
    }

    protected void addRuleTrigger(
          @NonNull String causedByTrigger,
          String currentTrigger,
          Date time,
          @NonNull List<AlertTrigger> alertTriggers // Since rules add 2 entries...
    ) {
        RuleModel rule = null;
        List<RuleModel> models = Lists.newArrayList(RuleModelProvider.instance().getStore().values());
        for (RuleModel model : models) {
            if (model.getAddress().equals(currentTrigger)) {
                rule = model;
                break;
            }
        }

        if (rule == null) {
            alertTriggers.add(getOtherTriggerCause(causedByTrigger, currentTrigger, time));
        }
        else {
            List<DeviceModel> deviceModels = Lists.newArrayList(DeviceModelProvider.instance().getStore().values());
            Map<String, Object> ruleContext = rule.getContext();
            if (ruleContext == null) {
                ruleContext = Collections.emptyMap();
            }

            Collection<Object> values = ruleContext.values();
            String deviceName = "";
            String deviceAddress = "";
            for (Object value : values) {
                if (!(value instanceof String)) {
                    continue;
                }

                String v = (String) value;
                for (DeviceModel dm : deviceModels) {
                    if (dm.getAddress().equals(v)) {
                        deviceName = dm.getName();
                        deviceAddress = dm.getAddress();
                        break;
                    }
                }
            }

            AlertTrigger trigger = new AlertTrigger();
            trigger.setTriggerTitle(causedByTrigger.equals(currentTrigger) ? ALARM_TRIGGERED : RULE_TRIGGERED);
            trigger.setTriggerType(AlertTrigger.TriggerType.PANIC);
            trigger.setTriggerDescription(String.format(BY_BEH_FMT, String.valueOf(rule.getName())));
            trigger.setTriggerID(currentTrigger);
            trigger.setTriggerTime(time);

            AlertTrigger trigger2 = new AlertTrigger();
            trigger2.setTriggerTitle(RULE_TRIGGERED);
            trigger2.setTriggerType(AlertTrigger.TriggerType.PANIC);
            trigger2.setTriggerDescription(String.format(BY_BEH_FMT, deviceName));
            if (TextUtils.isEmpty(deviceAddress)) {
                trigger2.setTriggerID(currentTrigger);
            }
            else {
                trigger2.setTriggerID(deviceAddress);
            }

            trigger2.setTriggerTime(time);

            alertTriggers.add(trigger2);
            alertTriggers.add(trigger); // So it's descending
        }
    }

    protected AlertTrigger getOtherTriggerCause(
          @NonNull String causedByTrigger,
          String currentTrigger,
          Date time
    ) {
        AlertTrigger trigger = new AlertTrigger();

        trigger.setTriggerTitle(causedByTrigger.equals(currentTrigger) ? ALARM_TRIGGERED : UNKNOWN_TRIGGERED);
        trigger.setTriggerType(AlertTrigger.TriggerType.PANIC);
        trigger.setTriggerDescription(BY_PANIC);
        trigger.setTriggerID(currentTrigger);
        trigger.setTriggerTime(time);

        return trigger;
    }

    protected CareStatus getCareSummary(CareSubsystem careSubsystem) {
        CareStatus careStatus = new CareStatus();

        careStatus.setAlarmMode(CareSubsystem.ALARMMODE_ON.equals(careSubsystem.getAlarmMode()) ? AlarmMode.ON : AlarmMode.VISIT);
        careStatus.setTotalBehaviors(set(careSubsystem.getBehaviors()).size());
        careStatus.setActiveBehaviors(set(careSubsystem.getActiveBehaviors()).size());
        careStatus.setNotificationList(getContacts(careSubsystem));
        Date lastAlert = careSubsystem.getLastAlertTime();
        careStatus.setLastAlertString(lastAlert == null ? "" : getLastAlertTime(lastAlert));

        return careStatus;
    }

    protected String getLastAlertTime(Date lastAlert) {
        String prefix = "Last Alarm: ";
        return String.format("%s%s", prefix, DateUtils.format(lastAlert));
    }

    protected boolean isToday(Date date) {
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR);

        calendar.setTime(date);
        return today == (calendar.get(Calendar.DAY_OF_YEAR) + calendar.get(Calendar.YEAR));
    }

    protected List<String> getContacts(CareSubsystem careSubsystem) {
        if(Boolean.TRUE.equals(careSubsystem.getCallTreeEnabled())) {
            List<String> icons = new ArrayList<>();
            List<Map<String, Object>> callTree = careSubsystem.getCallTree();

            if(callTree != null) {
                for(Map<String, Object> callTreeEntry: careSubsystem.getCallTree()) {
                    Object enabled = callTreeEntry.get(CallTreeEntry.ATTR_ENABLED);
                    if(Boolean.TRUE.equals(enabled)) {
                        icons.add(callTreeEntry.get(CallTreeEntry.ATTR_PERSON).toString());
                    }
                }
            }

            return icons;
        }

        return Collections.emptyList();
    }
}
