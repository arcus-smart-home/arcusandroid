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

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.text.Html;

import arcus.cornea.SessionController;
import arcus.cornea.common.BasePresenter;
import arcus.cornea.controller.AlarmIncidentHistoryController;
import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.subsystem.alarm.AlarmIncidentController;
import arcus.cornea.subsystem.alarm.AlarmProviderController;
import com.iris.client.bean.HistoryLog;
import com.iris.client.bean.TrackerEvent;
import com.iris.client.capability.AlarmIncident;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubConnection;
import com.iris.client.event.Listener;
import com.iris.client.model.AlarmIncidentModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.PlaceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmTrackerModel;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmTrackerStateModel;
import arcus.app.subsystems.alarm.promonitoring.models.HistoryListItemModel;
import arcus.app.subsystems.alarm.promonitoring.util.AlarmUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AlarmIncidentPresenter extends BasePresenter<AlarmIncidentContract.AlarmIncidentView>
        implements AlarmIncidentContract.AlarmIncidentPresenter, AlarmIncidentController.Callback,
        AlarmProviderController.Callback, AlarmIncidentHistoryController.IncidentHistoryAddedListener {

    private final static Logger logger = LoggerFactory.getLogger(AlarmIncidentPresenter.class);
    private static String lastPresentedTrackerState;

    private HubModel hubModel;
    private AlarmProviderController alarmProviderController = new AlarmProviderController();

    private AlarmIncidentModel incident;
    private PlaceModel place;
    private List<HistoryLog> history = new ArrayList<>();
    private int prealertSecRemaining;
    private boolean isAlarmProviderTheHub = false;
    boolean isHubDisconnected = false;




    @Override
    public void requestUpdate(final String incidentAddress) {
        logger.debug("Request to present incident {}", incidentAddress);
        addListener(AlarmIncidentController.class.getCanonicalName(), AlarmIncidentController.getInstance().setCallback(this));
        AlarmIncidentController.getInstance().requestUpdate(incidentAddress);
    }

    @Override
    public void requestConfirm() {
        logger.debug("Request to confirm incident.");
        if (incident != null) {
            incident.verify().onSuccess(new Listener<AlarmIncident.VerifyResponse>() {
                @Override
                public void onEvent(AlarmIncident.VerifyResponse event) {
                    requestUpdate(incident.getAddress());
                }
            }).onFailure(failureListener);
        } else {
            logger.error("Bug! Cannot confirm a null alarm incident.");
        }
    }

    @Override
    public void requestCancel() {
        logger.debug("Request to cancel incident.");
        if (incident != null) {
            incident.cancel().onSuccess(new Listener<AlarmIncident.CancelResponse>() {
                @Override
                public void onEvent(AlarmIncident.CancelResponse event) {
                    logger.debug("Cancel request succeeded. isCleared? {}, title: {}, message: {}", event.getCleared(), event.getWarningTitle(), event.getWarningMessage());
                    if (!event.getCleared() && !StringUtils.isEmpty(event.getWarningTitle()) && !StringUtils.isEmpty(event.getWarningMessage())) {
                        presentCancel(event.getWarningTitle(), Html.fromHtml(event.getWarningMessage().replaceAll("%s", GlobalSetting.PRO_MONITORING_STATION_NUMBER)));
                    }
                }
            }).onFailure(failureListener);
        } else {
            logger.error("Bug! Cannot cancel a null alarm incident.");
        }
    }

    public static String getLastPresentedAlarmTrackerState() {
        return lastPresentedTrackerState == null ? "UNKNOWN" : lastPresentedTrackerState;
    }

    private Listener<Throwable> failureListener = new Listener<Throwable>() {
        @Override
        public void onEvent(final Throwable throwable) {
            String exceptionMessage = ArcusApplication.getArcusApplication().getString(R.string.hub_local_offline_incident_exception_message);
            if (exceptionMessage.equals(throwable.getMessage())) {
                String warningDescription;
                String warningTitle = ArcusApplication.getArcusApplication().getString(R.string.hub_local_offline_incident_popup_title);
                String normalDescription = ArcusApplication.getArcusApplication().getString(R.string.hub_local_offline_incident_desc_text);
                String promonDescrition = ArcusApplication.getArcusApplication().getString(R.string.hub_local_offline_incident_promon_text);

                PlaceModel placeModel = SessionController.instance().getPlace();
                if (SubscriptionController.isProfessional()) {

                    warningDescription = normalDescription + promonDescrition;
                }
                else {
                    warningDescription = normalDescription;
                }

                presentCancel(warningTitle, Html.fromHtml(warningDescription.replaceAll("%s", GlobalSetting.PRO_MONITORING_STATION_NUMBER)));
            }
            else {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        logger.debug("Got error: {}", throwable);
                        if (isPresenting()) {
                            getPresentedView().showError(throwable);
                        }
                    }
                });
            }
        }
    };

    private void presentCancel(final String reasonTitle, final CharSequence reasonCopy) {
        logger.debug("Presenting cancel with title {} and message {}.", reasonTitle, reasonCopy);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isPresenting()) {
                    getPresentedView().showCancel(reasonTitle, reasonCopy);
                }
            }
        });
    }

    private void present(PlaceModel place, AlarmIncidentModel alarmIncidentModel, List<HistoryLog> historyLogList, int prealertRemainingSeconds) {
        final AlarmTrackerModel trackerModel = new AlarmTrackerModel();
        trackerModel.setProMonitored(alarmIncidentModel.getMonitored());
        trackerModel.setIncidentLayoutTint(getIncidentBackgroundTintForAlert(alarmIncidentModel.getAlert()));

        trackerModel.setComplete(
                AlarmIncidentModel.ALERTSTATE_COMPLETE.equals(alarmIncidentModel.getAlertState()) ||
                AlarmIncidentModel.ALERTSTATE_CANCELLING.equals(alarmIncidentModel.getAlertState())
        );

        trackerModel.setCancelable(
                !AlarmIncident.ALERTSTATE_CANCELLING.equals(alarmIncidentModel.getAlertState()) &&
                !AlarmIncident.ALERTSTATE_COMPLETE.equals(alarmIncidentModel.getAlertState())
        );

        trackerModel.setConfirmable(
                alarmIncidentModel.getMonitored() &&
                //panic is immediately dispatched and not confirmable
                !AlarmIncidentModel.ALERT_PANIC.equals(alarmIncidentModel.getAlert()) &&
                !alarmIncidentModel.getConfirmed()
        );

        trackerModel.setAlarmTypeTitle(AlarmUtils.getAlarmTypeDashboardDisplayString(alarmIncidentModel.getAlert()));

        logger.debug("Presenting alarm incident for place {}, with {} history items. Alert state: {}, is cancelable: {}, is confirmable: {}", place.getId(), historyLogList.size(), alarmIncidentModel.getAlertState(), trackerModel.isCancelable(), trackerModel.isConfirmable());

        for (HistoryLog thisEntry : historyLogList) {
            trackerModel.getHistoryListItems().add(buildHistoryListItem(thisEntry));
        }

        for (Map<String, Object> thisTrackerData : alarmIncidentModel.getTracker()) {
            TrackerEvent thisTrackerEvent = new TrackerEvent(thisTrackerData);
            trackerModel.getTrackerStates().add(buildTrackerStateModel(place, alarmIncidentModel, thisTrackerEvent, prealertRemainingSeconds));
        }

        if (trackerModel.getTrackerStates().size() > 0) {
            lastPresentedTrackerState = trackerModel.getTrackerStates().get(0).getIncidentStateName();
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isPresenting()) {
                    getPresentedView().updateView(trackerModel);
                }
            }
        });
    }

    private HistoryListItemModel buildHistoryListItem(HistoryLog logEntry) {
        HistoryListItemModel listItem = new HistoryListItemModel();

        // TODO: Insert abstract icon based on message key
        listItem.setTitle(logEntry.getSubjectName().toUpperCase());
        listItem.setSubtitle(logEntry.getLongMessage());
        listItem.setTimestamp(logEntry.getTimestamp());
        listItem.setStyle(HistoryListItemModel.HistoryListItemStyle.HISTORY_ITEM);

        return listItem;
    }

    private AlarmTrackerStateModel buildTrackerStateModel(PlaceModel place, AlarmIncidentModel alarmIncidentModel, TrackerEvent trackerEvent, int prealertRemainingSeconds) {
        AlarmTrackerStateModel stateModel = new AlarmTrackerStateModel();

        stateModel.setPro(alarmIncidentModel.getMonitored());

        stateModel.setPlaceName(place.getName());
        stateModel.setIncidentStateName(trackerEvent.getMessage());
        stateModel.setTintColor(AlarmUtils.getTintForAlert(alarmIncidentModel.getAlert()));
        stateModel.setSelectedPizzaIconResId(getSelectedIconForTrackerStateKey(trackerEvent.getState()));
        stateModel.setUnselectedPizzaIconResId(getSelectedIconForTrackerStateKey(trackerEvent.getState()));
        stateModel.setCountdown(prealertRemainingSeconds > 0 ? prealertRemainingSeconds : null);
        stateModel.setButtonColor(getButtonColorForAlert(alarmIncidentModel.getAlert()));

        return stateModel;
    }


    private int getSelectedIconForTrackerStateKey(String trackerKey) {
        switch (trackerKey) {
            case TrackerEvent.STATE_ALERT:
                return R.drawable.promon_alarm;
            case TrackerEvent.STATE_CANCELLED:
                return R.drawable.promon_cancel_alarm;
            case TrackerEvent.STATE_DISPATCH_CANCELLED:
                return R.drawable.promon_cancel_badge;
            case TrackerEvent.STATE_DISPATCH_FAILED:
                return R.drawable.promon_response_waiting;
            case TrackerEvent.STATE_DISPATCH_REFUSED:
                return R.drawable.promon_cancel_badge;
            case TrackerEvent.STATE_DISPATCHING:
                return R.drawable.promon_headset;
            case TrackerEvent.STATE_PREALERT:
                return R.drawable.promon_prealert;
            case TrackerEvent.STATE_DISPATCHED:
                return R.drawable.promon_badge;

            default:
                throw new IllegalArgumentException("Bug! No icon for tracker state key: " + trackerKey);
        }
    }

    private Version1ButtonColor getButtonColorForAlert(String alertType) {
        switch (alertType.toUpperCase()) {
            case AlarmIncidentModel.ALERT_SMOKE:
            case AlarmIncidentModel.ALERT_CO:
                return Version1ButtonColor.SAFETY;
            case AlarmIncidentModel.ALERT_PANIC:
                return Version1ButtonColor.PANIC;
            case AlarmIncidentModel.ALERT_SECURITY:
                return Version1ButtonColor.SECURITY;
            case AlarmIncidentModel.ALERT_WATER:
                return Version1ButtonColor.WATER;

            default:
                throw new IllegalArgumentException("Bug! Unsupported alert type: " + alertType);
        }
    }

    private int getIncidentBackgroundTintForAlert(String alertType) {
        switch (alertType.toUpperCase()) {
            case AlarmIncidentModel.ALERT_SECURITY:
                return ArcusApplication.getContext().getResources().getColor(R.color.security_background_color);
            case AlarmIncidentModel.ALERT_SMOKE:
            case AlarmIncidentModel.ALERT_CO:
                return ArcusApplication.getContext().getResources().getColor(R.color.safety_background_color);
            case AlarmIncidentModel.ALERT_PANIC:
                return ArcusApplication.getContext().getResources().getColor(R.color.panic_background_color);
            case AlarmIncidentModel.ALERT_WATER:
                return ArcusApplication.getContext().getResources().getColor(R.color.waterleak_background_color);

            default:
                throw new IllegalArgumentException("Bug! Unsupported alert type: " + alertType);
        }
    }



    @Override
    public void onIncidentUpdated(PlaceModel place, final AlarmIncidentModel incident, int prealertSecRemaining) {
        logger.debug("Incident update by subsystem. Alert state: {}, Monitoring state: {} ", incident.getAlertState(), incident.getMonitoringState());

        this.incident = incident;
        this.place = place;
        this.prealertSecRemaining = prealertSecRemaining;

        if (this.history.size() == 0) {
            logger.debug("Initial incident update; fetching history entries.");
            updateHistoryEntries();
        }

        addListener(AlarmIncidentHistoryController.class.getCanonicalName(), AlarmIncidentHistoryController.addIncidentHistoryAddedListener(incident.getAddress(), this));
        present(place, incident, history, prealertSecRemaining);
    }

    @Override
    public void onHistoryAdded() {
        logger.debug("Incident history appended; fetching new entries.");
        updateHistoryEntries();
    }

    private void updateHistoryEntries() {
        AlarmIncidentController.getInstance().getHistoryForIncident(incident).onSuccess(new Listener<HistoryLogEntries>() {
            @Override
            public void onEvent(HistoryLogEntries event) {
                logger.debug("Updated incident history. {} entries available.", event.getEntries().size());

                AlarmIncidentPresenter.this.history = event.getEntries();
                present(place, incident, history, prealertSecRemaining);
            }
        });
    }

    // Beginning of Hub Offline Banner Code.
    @Override
    public void onAlarmProviderChanged(boolean isNewAlarmProviderHub) {
        isAlarmProviderTheHub = isNewAlarmProviderHub;
    }

    @Override
    public void onAlarmStateChanged(String incidentAddress) {

    }

    @Override
    public void onSecurityModeChanged(String newMode) {

    }

    @Override
    public void onAvailableAlertsChanged(Set<String> newAvailableAlerts) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void startPresenting(AlarmIncidentContract.AlarmIncidentView view){
        super.startPresenting(view);
        alarmProviderController.setCallback(this);
        alarmProviderController.requestUpdate();

        hubModel = SessionModelManager.instance().getHubModel();
        if (hubModel != null) {
            addHubPropertyChangeListener();
            checkHubConnectionState(hubModel.get(Hub.ATTR_STATE));
        }
    }

    public void addHubPropertyChangeListener() {
        if (hubModel == null) {
            logger.debug("Cannot add property change listener to null model.");
            return;
        }

        addListener(hubModel.getClass().getCanonicalName(), hubModel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(@NonNull PropertyChangeEvent event) {
                if (event.getPropertyName().equals(Hub.ATTR_STATE)) {
                    checkHubConnectionState(event.getNewValue());
                }
                if (event.getPropertyName().equals(HubConnection.ATTR_LASTCHANGE)) {
                    hubModel = SessionModelManager.instance().getHubModel();
                    String hubLastChangeTime = StringUtils.getTimestampString(new Date(((Number) hubModel.get(HubConnection.ATTR_LASTCHANGE)).longValue()));

                    if (isPresenting()) {
                        getPresentedView().updateHubDisconnectedBanner(hubLastChangeTime);
                    }
                }
            }
        }));
    }

    private void checkHubConnectionState(Object hubState) {

        if (hubModel == null) {
            return;
        }

        if (isAlarmProviderTheHub) {
            if (Hub.STATE_DOWN.equals(hubState)) {
                isHubDisconnected = true;
            } else if (Hub.STATE_NORMAL.equals(hubState)) {
                isHubDisconnected = false;
            }

            String hubLastChangeTime = StringUtils.getTimestampString(new Date(((Number) hubModel.get(HubConnection.ATTR_LASTCHANGE)).longValue()));
            if (isPresenting()) {
                getPresentedView().showHubDisconnectedBanner(isHubDisconnected, hubLastChangeTime);
            }
        }
    }

}
