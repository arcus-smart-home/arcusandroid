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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.alarm.AlarmProviderController;
import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import arcus.cornea.subsystem.alarm.model.AlarmModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Hub;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;
import arcus.app.R;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.AbstractCardController;
import arcus.app.common.utils.StringUtils;
import arcus.app.dashboard.settings.services.ServiceCard;
import arcus.app.subsystems.alarm.promonitoring.cards.ProMonitoringDashboardCard;
import arcus.app.subsystems.alarm.promonitoring.util.AlarmUtils;
import arcus.app.subsystems.learnmore.cards.LearnMoreCard;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlarmCardPresenter extends AbstractCardController<SimpleDividerCard> implements AlarmSubsystemController.Callback,  AlarmProviderController.Callback {

    public Logger logger = LoggerFactory.getLogger(this.getClass());
    private SimpleDividerCard card;
    private ListenerRegistration hubPropertyChangeRegistry;
    private ListenerRegistration securitySubsystemListener;
    private AlarmProviderController alarmProviderController = new AlarmProviderController();
    private final ModelSource<SubsystemModel> securitySubsystem = SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE);


    private String securityMode;
    private List<String> activeAlerts = new ArrayList<>();
    private Set<String> availableAlerts = new HashSet<>();
    private boolean subsystemAvailable = false;
    private boolean isAlarmProviderHub = false;
    private boolean isInactiveState = false;
    private HubModel hubModel;

    public AlarmCardPresenter(Context context) {
        super(context);

        card = new ProMonitoringDashboardCard(context);
        AlarmSubsystemController.getInstance().setCallback(this);
        AlarmSubsystemController.getInstance().requestUpdate();
        alarmProviderController.setCallback(this);
        alarmProviderController.requestUpdate();

        addHubPropertyChangeListener();
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        AlarmSubsystemController.getInstance().setCallback(this);
        AlarmSubsystemController.getInstance().requestUpdate();

        alarmProviderController.setCallback(this);
        alarmProviderController.requestUpdate();
    }

    @Nullable
    @Override
    public SimpleDividerCard getCard() {
        return card;
    }

    @Override
    public void onSecurityModeChanged(String newMode) {
        this.securityMode = newMode;
        updateCard();
    }

    @Override
    public void onAlertsChanged(List<String> activeAlerts, Set<String> availableAlerts, Set<String> monitoredAlerts) {
        this.activeAlerts = activeAlerts;
        this.availableAlerts = availableAlerts;

        updateCard();
    }

    @Override
    public void onAlarmStateChanged(String newAlarmState) {
        if (newAlarmState.equals(AlarmSubsystem.ALARMSTATE_INACTIVE)) {
            isInactiveState= true;
        }
        else {
            isInactiveState = false;
        }

        updateCard();
    }

    @Override
    public void onIncidentChanged(String incidentAddress) {
        updateCard();
    }

    @Override
    public void onAlarmsChanged(List<AlarmModel> alarmModels) {
        updateCard();
    }

    @Override
    public void onSubsystemAvailableChange(boolean available) {
        subsystemAvailable = available;
        updateCard();
    }

    @Override
    public void onAlarmProviderChanged(boolean isAlarmProviderHub) {
        this.isAlarmProviderHub = isAlarmProviderHub;
        updateCard();
    }

    @Override
    public void onAvailableAlertsChanged(Set<String> newAvailableAlerts) {
        updateCard();
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onActivateComplete() {
        updateCard();
    }

    @Override
    public void removeCallback() {
        super.removeCallback();
        securitySubsystemListener = Listeners.clear(securitySubsystemListener);
    }

    public void updateCard() {
        if (isInactiveState) {
            card = new LearnMoreCard(getContext(), ServiceCard.SECURITY_ALARM);
        }
        else {
            card  = new ProMonitoringDashboardCard(getContext());

            ((ProMonitoringDashboardCard) card).setSubsystemAvailable(subsystemAvailable);
            // One or more alarms are alerting
            if (activeAlerts.size() > 0) {
                ((ProMonitoringDashboardCard) card).setAlarmState(ProMonitoringDashboardCard.AlarmState.ALERT);
                ((ProMonitoringDashboardCard) card).setSummary(AlarmUtils.getAlarmTypeDashboardDisplayString(activeAlerts.get(0)));

                if(activeAlerts.get(0).equals(AlarmSubsystem.ACTIVEALERTS_SECURITY)) {
                    card.setBackgroundColor(R.color.security_color);
                } else if(activeAlerts.get(0).equals(AlarmSubsystem.ACTIVEALERTS_PANIC)) {
                    card.setBackgroundColor(R.color.panic_color);
                } else if(activeAlerts.get(0).equals(AlarmSubsystem.ACTIVEALERTS_CO) || activeAlerts.contains(AlarmSubsystem.ACTIVEALERTS_SMOKE)) {
                    card.setBackgroundColor(R.color.safety_color);
                } else if(activeAlerts.get(0).equals(AlarmSubsystem.ACTIVEALERTS_WATER)) {
                    card.setBackgroundColor(R.color.waterleak_color);
                }
            }

            // User has security alarm; report state of it
            else if (!StringUtils.isEmpty(securityMode) && availableAlerts.contains(AlarmSubsystem.ACTIVEALERTS_SECURITY)) {
                ((ProMonitoringDashboardCard) card).setAlarmState(ProMonitoringDashboardCard.AlarmState.NORMAL);

                if(AlarmSubsystem.SECURITYMODE_INACTIVE.equalsIgnoreCase(securityMode)) {
                    ((ProMonitoringDashboardCard) card).setSummary(getContext().getString(R.string.alarm_status_okay));
                }
                if(AlarmSubsystem.SECURITYMODE_DISARMED.equalsIgnoreCase(securityMode)) {
                    ((ProMonitoringDashboardCard) card).setSummary(getContext().getString(R.string.security_alarm_off));
                }
                if(AlarmSubsystem.SECURITYMODE_ON.equalsIgnoreCase(securityMode)) {
                    ((ProMonitoringDashboardCard) card).setSummary(getContext().getString(R.string.security_alarm_on));
                }
                if(AlarmSubsystem.SECURITYMODE_PARTIAL.equalsIgnoreCase(securityMode)) {
                    ((ProMonitoringDashboardCard) card).setSummary(securityMode = getContext().getString(R.string.security_alarm_partial));
                }
            }

            // User has no active alerts, and no security system
            else {
                ((ProMonitoringDashboardCard) card).setAlarmState(ProMonitoringDashboardCard.AlarmState.NORMAL);
                ((ProMonitoringDashboardCard) card).setSummary(getContext().getString(R.string.alarm_status_okay));
                card.setBackgroundColor(android.R.color.transparent);
            }

            // If no hub, check if using other devices for security (ex: WiFi camera)
            if(hubModel == null){
                securitySubsystemListener = securitySubsystem.addModelListener(Listeners.runOnUiThread( event -> {
                    if(!(event instanceof ModelDeletedEvent)){
                        Set<String> securityDevices = nonNullCollection(getSecuritySubsystem().getSecurityDevices());

                        // If all WiFi cameras are offline, show "Unable to Notify" on the card
                        if(!securityDevices.isEmpty()) {
                            DeviceModelProvider
                                .instance()
                                .getModels(securityDevices)
                                .load()
                                .onSuccess(devices -> {
                                    ArrayList<DeviceModel> wifiCameras = new ArrayList<>();
                                    ArrayList<DeviceModel> offlineDevices = new ArrayList<>();
                                    for(DeviceModel device : devices){
                                        DeviceModel camera = null;
                                        Collection<String> caps = device.getCaps();

                                        // TODO: detect if device is a supported wifi camera, add it to wifiCameras, and assign camera to it

                                        if(camera != null && DeviceConnection.STATE_OFFLINE.equals(camera.get(DeviceConnection.ATTR_STATE))) {
                                            offlineDevices.add(device);
                                        }
                                    }
                                    // If all the wifi cameras are offline, show the popup
                                    if(!wifiCameras.isEmpty() && (offlineDevices.size() == wifiCameras.size())){
                                        ((ProMonitoringDashboardCard) card)
                                            .setSummary(getContext()
                                            .getString(R.string.hub_local_offline_dasboard_card_text));
                                    }
                                });
                        }
                    }
                }));
            }

            // Check Hub Local Alarm states
            if (hubModel != null && Hub.STATE_DOWN.equals(hubModel.get(Hub.ATTR_STATE)) && isAlarmProviderHub && activeAlerts.size() == 0) {
                ((ProMonitoringDashboardCard) card).setSummary(getContext().getString(R.string.hub_local_offline_dasboard_card_text));
            }

            if(!((ProMonitoringDashboardCard) card).isSubsystemAvailable()) {
                ((ProMonitoringDashboardCard) card).setSummary(getContext().getString(R.string.upgrade));
            }
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                setCurrentCard(card);
             }
        });

    }

    private void addHubPropertyChangeListener() {
        hubModel = HubModelProvider.instance().getHubModel();
        Listeners.clear(hubPropertyChangeRegistry);

        if (hubModel != null) {
            hubPropertyChangeRegistry = hubModel.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(@NonNull PropertyChangeEvent event) {
                    if (event.getPropertyName().equals(Hub.ATTR_STATE)) {
                        updateCard();
                    }
                }
            });
        }
    }

    private SecuritySubsystem getSecuritySubsystem(){
        securitySubsystem.load();
        return (SecuritySubsystem) securitySubsystem.get();
    }

    private Set<String> nonNullCollection(Collection<String> collection) {
        if (collection == null) {
            return Collections.emptySet();
        }

        if (collection instanceof Set) {
            return (Set<String>) collection;
        }

        return new HashSet<>(collection);
    }
}
