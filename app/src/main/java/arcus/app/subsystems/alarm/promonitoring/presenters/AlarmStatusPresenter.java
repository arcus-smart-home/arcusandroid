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
package arcus.app.subsystems.alarm.promonitoring.presenters;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.SessionController;
import arcus.cornea.common.BasePresenter;
import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.subsystem.alarm.AlarmExitController;
import arcus.cornea.subsystem.alarm.AlarmIncidentPrealertController;
import arcus.cornea.subsystem.alarm.AlarmSecuritySubsystemController;
import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import arcus.cornea.subsystem.alarm.model.AlarmModel;
import arcus.cornea.subsystem.alarm.model.AlertDeviceStateModel;
import arcus.cornea.utils.DateUtils;
import com.iris.client.capability.Alarm;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.PlaceModel;
import com.iris.client.session.SessionInfo;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.subsystems.alarm.promonitoring.ProMonitoringIncidentFragment;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.AlertingAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.InactiveAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.PanicAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.SafetyAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.models.SecurityAlarmStatusModel;
import arcus.app.subsystems.alarm.promonitoring.util.AlarmUtils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;



public class AlarmStatusPresenter extends BasePresenter<AlarmStatusContract.AlarmStatusView> implements AlarmStatusContract.AlarmStatusPresenter,
        AlarmSubsystemController.Callback, AlarmSecuritySubsystemController.Callback, AlarmIncidentPrealertController.Callback, AlarmExitController.Callback {

    private final static Logger logger = LoggerFactory.getLogger(AlarmStatusPresenter.class);
    private final static String UNKNOWN_DEVICE_ERROR_CODE = "UnknownDevice";
    private final static String INVALID_STATE_ERROR_CODE = "security.invalidState";
    private final static String HUB_DISARMING_ERROR_CODE = "security.hubDisarming";

    private int securityAlarmArmingSecondsRemaining;
    private int prealertSecondsRemaining;
    private List<AlarmModel> alarmModels;

    private Comparator<AlarmStatusModel> alarmListOrder = new Comparator<AlarmStatusModel>() {
        @Override
        public int compare(AlarmStatusModel lhs, AlarmStatusModel rhs) {
            return getAlarmOrder(lhs.getAlarmTypeString()).compareTo(getAlarmOrder(rhs.getAlarmTypeString()));
        }

        private Integer getAlarmOrder(String alarmTypeString) {
            if (alarmTypeString.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_SMOKE)))    return 0;
            if (alarmTypeString.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_CO)))       return 1;
            if (alarmTypeString.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_WATER)))    return 2;
            if (alarmTypeString.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_PANIC)))    return 3;
            if (alarmTypeString.equalsIgnoreCase(AlarmUtils.getAlarmTypeStatusDisplayString(AlarmSubsystem.ACTIVEALERTS_SECURITY))) return 4;

            throw new IllegalArgumentException("Bug! Unimplemented alarm type: " + alarmTypeString);
        }
    };

    @Override
    public void requestUpdate() {
        if (isPresenting()) {
            getPresentedView().onPending(null);

            registerAlarmSubsystemControllerCallback();
            registerSecurityStatusControllerCallback();
            registerAlarmIncidentPrealertCallback();
            registerAlarmExitCallback();

            AlarmSubsystemController.getInstance().requestUpdate();
            AlarmIncidentPrealertController.getInstance().startPrealertCountdown();
        }
    }

    @Override
    public void presentCurrentIncident() {
        String currentIncident = AlarmSubsystemController.getInstance().getCurrentIncident();
        if (currentIncident != null) {
            BackstackManager.getInstance().navigateToFragment(ProMonitoringIncidentFragment.newInstance(currentIncident), true);
        } else {
            logger.error("No current incident to present.");
        }
    }

    @Override
    public void armSecurityAlarm(boolean bypassed) {
        getPresentedView().onPending(null);
        registerSecurityStatusControllerCallback();
        AlarmSecuritySubsystemController.getInstance().armOn(bypassed);
    }

    @Override
    public void armPartialSecurityAlarm(boolean bypassed) {
        if(!isPresenting()) {
            registerSecurityStatusControllerCallback();
            AlarmSecuritySubsystemController.getInstance().armPartial(bypassed);
            return;
        }

        getPresentedView().onPending(null);
        registerSecurityStatusControllerCallback();
        AlarmSecuritySubsystemController.getInstance().armPartial(bypassed);
    }

    @Override
    public void disarmSecurityAlarm() {
        if(!isPresenting()) {
            registerSecurityStatusControllerCallback();
            AlarmSecuritySubsystemController.getInstance().disarm();
            return;
        }

        // Stop the controller from continuing the countdown
        AlarmExitController.getInstance().cancelPrealertCountdown();
        getPresentedView().onPending(null);
        registerSecurityStatusControllerCallback();
        AlarmSecuritySubsystemController.getInstance().disarm();
    }

    @Override
    public void dismissAlarm() {
        // TODO: What to do?
    }

    private void registerSecurityStatusControllerCallback() {
        String securityListenerId = AlarmSecuritySubsystemController.class.getCanonicalName();
        addListener(securityListenerId, AlarmSecuritySubsystemController.getInstance().setCallback(this));
    }

    private void registerAlarmSubsystemControllerCallback() {
        String alarmListenerId = AlarmSubsystemController.class.getCanonicalName();
        addListener(alarmListenerId, AlarmSubsystemController.getInstance().setCallback(this));
    }

    private void registerAlarmIncidentPrealertCallback() {
        String alarmPrealertId = AlarmIncidentPrealertController.class.getCanonicalName();
        addListener(alarmPrealertId, AlarmIncidentPrealertController.getInstance().setCallback(this));
    }

    private void registerAlarmExitCallback() {
        String alarmExitId = AlarmExitController.class.getCanonicalName();
        addListener(alarmExitId, AlarmExitController.getInstance().setCallback(this));
    }

    private List<AlarmStatusModel> buildAlarmStatusModels(List<AlarmModel> alarmModels) {

        ArrayList<AlarmStatusModel> alarmStatusModels = new ArrayList<>();

        for (AlarmModel thisModel : alarmModels) {
            AlarmStatusModel alarmStatusModel = buildAlarmStatusModel(thisModel, alarmModels);

            if (alarmStatusModel != null) {
                alarmStatusModels.add(alarmStatusModel);
            }
        }

        return alarmStatusModels;
    }

    @Nullable
    private AlarmStatusModel buildAlarmStatusModel(AlarmModel alarmModel, List<AlarmModel> otherAlarms) {

        boolean isSubAlarmClearing = AlarmSubsystemController.getInstance().isAlarmStateClearing();

        // No card rendered for this alarm
        if (!isAlarmVisible(alarmModel, otherAlarms)) {
            return null;
        }

        // Alarm is in alerting state and the subalarm Alert State is not CLEARING
        else if (com.iris.client.model.AlarmModel.ALERTSTATE_ALERT.equals(alarmModel.getAlertState())
                && !isSubAlarmClearing) {
            return buildAlertingAlarmStatusModel(alarmModel);
        }

        // Alarm is in inactive state (i.e., no devices available to participate)
        else if (com.iris.client.model.AlarmModel.ALERTSTATE_INACTIVE.equals(alarmModel.getAlertState())) {
            return buildInactiveAlarmStatusModel(alarmModel);
        }

        // Alarm is in active (normal) state; show status
        else {
            switch (alarmModel.getType()) {
                case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                case AlarmSubsystem.ACTIVEALERTS_CO:
                case AlarmSubsystem.ACTIVEALERTS_WATER:
                    return populateSafetyAlarmStatusModel(new SafetyAlarmStatusModel(AlarmUtils.getIconResIdForAlarmType(alarmModel), AlarmUtils.getAlarmTypeStatusDisplayString(alarmModel)), alarmModel);

                case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                    return populateSecurityAlarmStatusModel(new SecurityAlarmStatusModel(AlarmUtils.getIconResIdForAlarmType(alarmModel), AlarmUtils.getAlarmTypeStatusDisplayString(alarmModel)), alarmModel);

                case AlarmSubsystem.ACTIVEALERTS_PANIC:
                    return populatePanicAlarmStatusModel(new PanicAlarmStatusModel(AlarmUtils.getIconResIdForAlarmType(alarmModel), AlarmUtils.getAlarmTypeStatusDisplayString(alarmModel)), alarmModel);

                default:
                    return null;
            }
        }
    }

    private boolean isAlarmVisible(AlarmModel thisAlarm, List<AlarmModel> allAlarms) {

        switch (thisAlarm.getType()) {
            case AlarmSubsystem.ACTIVEALERTS_SMOKE:
            case AlarmSubsystem.ACTIVEALERTS_CO:
            case AlarmSubsystem.ACTIVEALERTS_WATER:
                return true;

            // Show security card when:
            //   - Panic alarm is not alerting, AND
            //   - User has security devices AND user has no devices participating in panic
            case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                return !isPanicVisible(allAlarms);

            // Show panic card when:
            //   - Panic alarm is alerting, OR
            //   - User has devices participating in panic AND has no devices participating in security
            case AlarmSubsystem.ACTIVEALERTS_PANIC:
                return isPanicVisible(allAlarms);

            default:
                return false;
        }
    }

    private boolean isPanicVisible(List<AlarmModel> allAlarms) {
        return (isAlarmInState(AlarmSubsystem.ACTIVEALERTS_PANIC, allAlarms, Alarm.ALERTSTATE_ALERT) && !isAlarmInState(AlarmSubsystem.ACTIVEALERTS_SECURITY, allAlarms, Alarm.ALERTSTATE_ALERT))
                || (isAlarmInState(AlarmSubsystem.ACTIVEALERTS_PANIC, allAlarms, Alarm.ALERTSTATE_READY) && isAlarmInState(AlarmSubsystem.ACTIVEALERTS_SECURITY, allAlarms, Alarm.ALERTSTATE_INACTIVE));
    }

    private boolean isAlarmInState(String alarmType, List<AlarmModel> alarmModels, String alertState) {
        for (AlarmModel thisModel : alarmModels) {
            if (thisModel.getType().equalsIgnoreCase(alarmType)) {
                return alertState.equalsIgnoreCase(thisModel.getAlertState());
            }
        }

        return false;
    }

    private AlarmStatusModel buildAlertingAlarmStatusModel(AlarmModel alarmModel) {
        AlertingAlarmStatusModel model = new AlertingAlarmStatusModel(AlarmUtils.getIconResIdForAlarmType(alarmModel), AlarmUtils.getAlarmTypeStatusDisplayString(alarmModel));
        model.setAlertingStatusString(ArcusApplication.getContext().getString(R.string.alarming));
        model.setProMonitored(alarmModel.isMonitored());
        return model;
    }

    private AlarmStatusModel buildInactiveAlarmStatusModel(AlarmModel alarmModel) {
        InactiveAlarmStatusModel model = new InactiveAlarmStatusModel(AlarmUtils.getIconResIdForAlarmType(alarmModel), AlarmUtils.getAlarmTypeStatusDisplayString(alarmModel));
        model.setNotAvailableCopy(getInactiveAlarmDisplayString(alarmModel));
        model.setProMonitored(alarmModel.isMonitored());
        return model;
    }

    private <T extends PanicAlarmStatusModel> T populatePanicAlarmStatusModel(T panicModel, AlarmModel alarmModel) {
        panicModel.setProMonitored(alarmModel.isMonitored());
        return panicModel;
    }

    private <T extends SafetyAlarmStatusModel> T populateSafetyAlarmStatusModel(T safetyModel, AlarmModel alarmModel) {

        safetyModel.setParticipatingDevicesCount(alarmModel.getActiveDevices().size()+ alarmModel.getTriggeredDevices().size() + alarmModel.getOfflineDevices().size());
        safetyModel.setTotalDevicesCount(alarmModel.getDevices().size());
        safetyModel.setBypassedDevicesCount(alarmModel.getExcludedDevices().size() - alarmModel.getOfflineDevices().size());
        safetyModel.setOfflineDevicesCount(alarmModel.getOfflineDevices().size());
        safetyModel.setTiggeredDevicesCount(alarmModel.getTriggeredDevices().size());
        safetyModel.setSubtext(getAlarmStatusSubtext(alarmModel));
        safetyModel.setProMonitored(alarmModel.isMonitored());
        safetyModel.setActiveDevicesCount(alarmModel.getActiveDevices().size());

        return safetyModel;
    }

    private <T extends SecurityAlarmStatusModel> T populateSecurityAlarmStatusModel(T securityModel, AlarmModel alarmModel) {

        securityModel = populateSafetyAlarmStatusModel(securityModel, alarmModel);
        securityModel.setAlarmState(getSecurityAlarmState(alarmModel));
        securityModel.setArmingSecondsRemaining(this.securityAlarmArmingSecondsRemaining);
        securityModel.setPrealertSecondsRemaining(isPrealerting(alarmModel) ? this.prealertSecondsRemaining : 0);

        return securityModel;
    }

    private SecurityAlarmStatusModel.SecurityAlarmArmingState getSecurityAlarmState(AlarmModel alarmModel) {

        switch (alarmModel.getAlertState()) {
            case com.iris.client.model.AlarmModel.ALERTSTATE_ARMING:
                return SecurityAlarmStatusModel.SecurityAlarmArmingState.ARMING;

            case com.iris.client.model.AlarmModel.ALERTSTATE_READY:
            case com.iris.client.model.AlarmModel.ALERTSTATE_ALERT:
            case com.iris.client.model.AlarmModel.ALERTSTATE_PREALERT:
                return SecurityAlarmStatusModel.SecurityAlarmArmingState.ON;

            case com.iris.client.model.AlarmModel.ALERTSTATE_DISARMED:
            case com.iris.client.model.AlarmModel.ALERTSTATE_CLEARING:
                return SecurityAlarmStatusModel.SecurityAlarmArmingState.DISARMED;

            default:
                return SecurityAlarmStatusModel.SecurityAlarmArmingState.INACTIVE;
        }
    }

    @NonNull
    private String getInactiveAlarmDisplayString(AlarmModel alarmModel) {

        switch (alarmModel.getType()) {
            case AlarmSubsystem.ACTIVEALERTS_SMOKE:
                return ArcusApplication.getContext().getString(R.string.smoke_alarm_devices_needed_copy);
            case AlarmSubsystem.ACTIVEALERTS_CO:
                return ArcusApplication.getContext().getString(R.string.co_alarm_devices_needed_copy);
            case AlarmSubsystem.ACTIVEALERTS_WATER:
                return ArcusApplication.getContext().getString(R.string.waterleak_alarm_devices_needed_copy);
            case AlarmSubsystem.ACTIVEALERTS_SECURITY:
                return ArcusApplication.getContext().getString(R.string.security_alarm_devices_needed_copy);

            default:
                throw new IllegalArgumentException("Bug! Alarm type should never be inactive: " + alarmModel.getType());
        }
    }

    @Nullable
    private String getAlarmStatusSubtext(AlarmModel alarmModel) {

        StringBuilder builder = new StringBuilder();

        // In pre-alert countdown state
        if (com.iris.client.model.AlarmModel.ALERTSTATE_PREALERT.equals(alarmModel.getAlertState())) {
            return ArcusApplication.getContext().getString(R.string.incident_grace_countdown);
        }

        // In arming state
        else if (com.iris.client.model.AlarmModel.ALERTSTATE_ARMING.equals(alarmModel.getAlertState())) {
            return ArcusApplication.getContext().getString(R.string.alertstate_arming);
        }

        // Some device are inactive; display status text
        else if (alarmModel.getDevices().size() != alarmModel.getActiveDevices().size()) {

            // "X offline, y bypassed"
            if (alarmModel.getExcludedDevices().size() - alarmModel.getOfflineDevices().size() > 0 && alarmModel.getOfflineDevices().size() > 0) {
                builder.append(ArcusApplication.getContext().getString(R.string.x_offline_y_bypassed, alarmModel.getOfflineDevices().size(), alarmModel.getExcludedDevices().size() - alarmModel.getOfflineDevices().size())).append("\n");
            }

            // "X devices bypassed"
            else if (alarmModel.getExcludedDevices().size() - alarmModel.getOfflineDevices().size() > 0) {
                builder.append(ArcusApplication.getContext().getString(R.string.x_bypassed, alarmModel.getExcludedDevices().size() - alarmModel.getOfflineDevices().size())).append("\n");
            }

            // "X devices offline"
            else if (alarmModel.getOfflineDevices().size() > 0) {
                builder.append(ArcusApplication.getContext().getString(R.string.x_offline, alarmModel.getOfflineDevices().size())).append("\n");
            }

            // "X detecting Y (one or more devices are triggered)"
            if (alarmModel.getTriggeredDevices().size() - alarmModel.getExcludedDevices().size() > 0) {
                builder.append(ArcusApplication.getContext().getString(R.string.x_detecting_y, alarmModel.getTriggeredDevices().size() - alarmModel.getExcludedDevices().size(), AlarmUtils.getAlarmTypeStatusDisplayString(alarmModel))).append("\n");
            }
        }

        if (AlarmSubsystem.ACTIVEALERTS_SECURITY.equals(alarmModel.getType())) {
            builder.append(getSecuritySinceString(alarmModel));
        }

        return builder.toString().trim();
    }

    private String getSecuritySinceString(AlarmModel alarmModel) {

        SecurityAlarmStatusModel.SecurityAlarmArmingState state = getSecurityAlarmState(alarmModel);

        if (state == SecurityAlarmStatusModel.SecurityAlarmArmingState.DISARMED && AlarmSubsystemController.getInstance().getSecurityLastDisrmedTime() != null) {
            return ArcusApplication.getContext().getString(R.string.alarm_off_since, lastChangedString(AlarmSubsystemController.getInstance().getSecurityLastDisrmedTime()));
        } else if (state == SecurityAlarmStatusModel.SecurityAlarmArmingState.ON && AlarmSubsystemController.getInstance().getSecurityLastArmedTime() != null) {
            if (AlarmSubsystem.SECURITYMODE_PARTIAL.equals(AlarmSubsystemController.getInstance().getSecurityMode())) {
                return ArcusApplication.getContext().getString(R.string.alarm_partial_since, lastChangedString(AlarmSubsystemController.getInstance().getSecurityLastArmedTime()));
            } else {
                return ArcusApplication.getContext().getString(R.string.alarm_on_since,lastChangedString(AlarmSubsystemController.getInstance().getSecurityLastArmedTime()));
            }
        } else {
            return "";
        }
    }

    private boolean isPrealerting(AlarmModel alarmModel) {
        return com.iris.client.model.AlarmModel.ALERTSTATE_PREALERT.equals(alarmModel.getAlertState());
    }

    @Nullable
    private Uri getAdTarget() {

        SessionInfo.PlaceDescriptor placeDescriptor = SessionController.instance().getPlaceDescriptorForActivePlace();
        String adTarget = SessionController.instance().getSessionInfo().getPromonAdUrl();
        boolean showAd = placeDescriptor != null && Boolean.valueOf(placeDescriptor.getPromonAdEnabled()) && !StringUtils.isEmpty(adTarget);

        return showAd ? Uri.parse(adTarget) : null;
    }

    @Override
    public void onAlarmStateChanged(String newAlarmState) {
        // Nothing to do; change in overall alarm state will trigger onAlarmChanged
    }

    @Override
    public void onSecurityModeChanged(String newMode) {
        if (alarmModels != null) {
            presentAlarmStatusModels(buildAlarmStatusModels(alarmModels));
        }
    }

    @Override
    public void onAlertsChanged(List<String> activeAlerts, Set<String> availableAlerts, Set<String> monitoredAlerts) {
        // Nothing to do; change in alert status will be reflected in onAlarmsChanged
    }

    @Override
    public void onIncidentChanged(String incidentAddress) {
        // Nothing to do
    }

    @Override
    public void onAlarmsChanged(final List<AlarmModel> alarmModels) {
        this.alarmModels = alarmModels;
        presentAlarmStatusModels(buildAlarmStatusModels(alarmModels));
    }

    @Override
    public void onSubsystemAvailableChange(boolean available) {

    }

    @Override
    public void onAlarmSecurityError(Throwable t) {
        // Client times out or Platform returns Error Code of "Unkown Device", show Time Out popup
        if (t instanceof CancellationException || (t instanceof ErrorResponseException && ((ErrorResponseException) t).getCode().equalsIgnoreCase(UNKNOWN_DEVICE_ERROR_CODE))) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    getPresentedView().onPromptRequestTimedOut();
                }
            });
        }
        else if (t instanceof ErrorResponseException && ((ErrorResponseException) t).getCode().equalsIgnoreCase(INVALID_STATE_ERROR_CODE)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    final PlaceModel placeModel = SessionController.instance().getPlace();
                    if (SubscriptionController.isProfessional()) {
                        getPresentedView().onPromptWaitingForMonitoringStation();
                    }
                    else {
                        getPresentedView().onPromptHubDisarming();
                    }
                }
            });
        }
        else if (t instanceof ErrorResponseException && ((ErrorResponseException) t).getCode().equalsIgnoreCase(HUB_DISARMING_ERROR_CODE)) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    getPresentedView().onPromptHubDisarming();
                }
            });
        }
        else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    getPresentedView().onPromptNotEnoughSecurityDevices();
                }
            });
        }
    }

    @Override
    public void onArmedSuccessfully(int remainingSeconds) {
        //this.securityAlarmArmingSecondsRemaining = remainingSeconds;
        requestUpdate();
    }

    @Override
    public void onRequiresBypass(final List<AlertDeviceStateModel> deviceNames, final boolean isPartialMode) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Collections.sort(deviceNames, AlertDeviceStateModel.sortAlphaOrder);
                getPresentedView().onPromptUnsecured(deviceNames, isPartialMode);
            }
        });
    }

    @Override
    public void onDisarmedSuccessfully() {
        requestUpdate();
    }

    private void presentAlarmStatusModels(final List<AlarmStatusModel> statusModels) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Collections.sort(statusModels, alarmListOrder);
                if (isPresenting()) {
                    getPresentedView().updateView(new AlarmStatusContract.AlarmStatusPresenterModel(getAdTarget(), statusModels));
                }
            }
        });
    }

    @Override
    public void onPrealertTimeChanged(int secondsRemaining) {
        this.prealertSecondsRemaining = secondsRemaining;
        presentAlarmStatusModels(buildAlarmStatusModels(alarmModels));
    }

    @Override
    public void onExitTimeChanged(int secondsRemaining) {
        this.securityAlarmArmingSecondsRemaining = secondsRemaining;
        presentAlarmStatusModels(buildAlarmStatusModels(alarmModels));
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onActivateComplete() {

    }

    private String lastChangedString(Date date) {
        if(isToday(date)) {
            return new SimpleDateFormat("\nh:mm a").format(date);
        } else if(isYesterday(date)){
            return "Yesterday" + new SimpleDateFormat("\nh:mm a").format(date);
        } else {
            return new SimpleDateFormat("MMM d\nh:mm a").format(date);
        }
    }

    private boolean isToday(Date date) { return DateUtils.Recency.TODAY.equals(DateUtils.getRecency(date)); }

    private boolean isYesterday(Date date) { return DateUtils.Recency.YESTERDAY.equals(DateUtils.getRecency(date)); }
}
