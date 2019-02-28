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

import android.support.annotation.Nullable;

import arcus.cornea.SessionController;
import arcus.cornea.common.BasePresenter;
import arcus.cornea.common.PresentedView;
import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.alarm.AlarmProviderController;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubConnection;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.capability.SwannBatteryCamera;
import com.iris.client.model.AlarmSubsystemModel;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.PlaceModel;
import com.iris.client.model.SubsystemModel;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.fragment.HubAlarmDisconnectedPopup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;



public class AlarmProviderOfflinePresenter <T extends PresentedView> extends BasePresenter<T> implements HubAlarmDisconnectedPopup.Callback  {

    public Logger logger = LoggerFactory.getLogger(this.getClass());
    private HubModel hubModel;

    private final ModelSource<SubsystemModel> securitySubsystem = SubsystemController.instance().getSubsystemModel(SecuritySubsystem.NAMESPACE);

    private AlarmProviderController alarmProviderController = new AlarmProviderController();
    private AlarmProviderCallbackHandler alarmProviderCallbackHandler = new AlarmProviderCallbackHandler();
    private boolean isAlarmProviderTheHub = false;
    private boolean noHubPlace = false;

    private boolean isSecurityAvailable = false;
    private boolean isAlarmingState = false;
    private boolean isHubAlarmPopupShowing = false;
    private String newSecurityMode, newHubLastChangeTime;


    public void startPresenting(@Nullable T view){
        super.startPresenting(view);

        registerAlarmProviderControllerCallback();
        hubModel = HubModelProvider.instance().getHubModel();
        if (hubModel != null) {
            checkHubConnectionState(hubModel.get(Hub.ATTR_STATE));
            addHubPropertyChangeListener();
        } else {
            noHubPlace = true;
            getSecuritySubsystem();
            securitySubsystem.addModelListener(Listeners.runOnUiThread(
                    event -> {
                        if (!(event instanceof ModelDeletedEvent)){
                            checkAllWifiDevicesOffline();
                        }
                    }
            ));
        }
    }

    private void checkAllWifiDevicesOffline() {
        SecuritySubsystem security = getSecuritySubsystem();
        Set<String> securityDevices = nonNullCollection(security.getSecurityDevices());

        if(!securityDevices.isEmpty()) {
            DeviceModelProvider
                .instance()
                .getModels(securityDevices)
                .load()
                .onSuccess( devices -> {
                    ArrayList<DeviceModel> wifiCameras = new ArrayList<>();
                    ArrayList<DeviceModel> offlineDevices = new ArrayList<>();
                    for(DeviceModel device : devices){
                        DeviceModel camera = null;
                        Collection<String> caps = device.getCaps();
                        // First, check that this security device is a Swann camera
                        if((caps != null && caps.contains(SwannBatteryCamera.NAMESPACE))){
                            wifiCameras.add(device);
                            camera = device;
                        }
                        if(camera != null && DeviceConnection.STATE_OFFLINE.equals(camera.get(DeviceConnection.ATTR_STATE))) {
                            offlineDevices.add(device);
                        }
                    }
                    // If all the wifi cameras are offline, show the popup
                    if(!wifiCameras.isEmpty() && (offlineDevices.size() == wifiCameras.size())){
                        showAlarmProviderHubOfflineErrorPopup();
                    }
                });
        } else {
            closeAlarmProviderHubOfflineErrorPopup();
        }
    }

    private void registerAlarmProviderControllerCallback() {
        String alarmProviderId = AlarmProviderController.class.getCanonicalName();
        addListener(alarmProviderId, alarmProviderController.setCallback(alarmProviderCallbackHandler));
    }

    @Override
    public void closed() {

    }

    private void addHubPropertyChangeListener() {
        if (hubModel == null) {
            logger.debug("Cannot add property change listener to null model.");
            return;
        }
        addListener("hubModelListener", hubModel.addPropertyChangeListener(event -> {
            if (event.getPropertyName().equals(Hub.ATTR_STATE)) {
                checkHubConnectionState(event.getNewValue());
            }
        }));
    }

    private void checkHubConnectionState(Object hubState) {
        if (hubModel == null) {
            return;
        }

        if(Hub.STATE_DOWN.equals(hubState)) {
            getHubLastChangedTime(null);
            showAlarmProviderHubOfflineErrorPopup();
        }
        else if(Hub.STATE_NORMAL.equals(hubState)){
            closeAlarmProviderHubOfflineErrorPopup();
        }
    }

    private void showAlarmProviderHubOfflineErrorPopup() {
        final PlaceModel placeModel = SessionController.instance().getPlace();
        boolean isPopupVisible = BackstackManager.getInstance().isFragmentOnStack(HubAlarmDisconnectedPopup.class);

        if (placeModel == null) {
            return;
        }

        if ((isAlarmProviderTheHub && !isPopupVisible) || (noHubPlace && !isPopupVisible)) {

            HubAlarmDisconnectedPopup popup = HubAlarmDisconnectedPopup.newInstance(newHubLastChangeTime,
                    newSecurityMode, isAlarmingState, SubscriptionController.isProfessional(), isSecurityAvailable);

            popup.setCallback(this);
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);

            isHubAlarmPopupShowing = true;
        }
    }

    private void closeAlarmProviderHubOfflineErrorPopup() {
        if (isHubAlarmPopupShowing) {
            BackstackManager.getInstance().navigateBack();

            isHubAlarmPopupShowing = false;
        }
    }

    private void getHubLastChangedTime(@Nullable DeviceModel deviceModel) {
        if (hubModel != null) {
            StringBuilder formattedHubLastChangeTime = new StringBuilder(new SimpleDateFormat("h:mm a").format(new Date(((Number) hubModel.get(HubConnection.ATTR_LASTCHANGE)).longValue())));
            formattedHubLastChangeTime.append(" on ");
            formattedHubLastChangeTime.append(new StringBuilder(new SimpleDateFormat("EEE MMMM d").format(new Date(((Number) hubModel.get(HubConnection.ATTR_LASTCHANGE)).longValue()))));
            newHubLastChangeTime = formattedHubLastChangeTime.toString();
        }
        if (deviceModel != null) {
            StringBuilder formattedHubLastChangeTime = new StringBuilder(new SimpleDateFormat("h:mm a").format(new Date(((Number) deviceModel.get(DeviceConnection.ATTR_LASTCHANGE)).longValue())));
            formattedHubLastChangeTime.append(" on ");
            formattedHubLastChangeTime.append(new StringBuilder(new SimpleDateFormat("EEE MMMM d").format(new Date(((Number) deviceModel.get(DeviceConnection.ATTR_LASTCHANGE)).longValue()))));
            newHubLastChangeTime = formattedHubLastChangeTime.toString();
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

    private class AlarmProviderCallbackHandler implements AlarmProviderController.Callback {

        @Override
        public void onAlarmProviderChanged(boolean isNewAlarmProviderHub) {
            isAlarmProviderTheHub = isNewAlarmProviderHub;
        }

        @Override
        public void onAlarmStateChanged(String newAlarmState) {
            if (newAlarmState.equals(AlarmSubsystem.ALARMSTATE_PREALERT) || newAlarmState.equals(AlarmSubsystem.ALARMSTATE_ALERTING)) {
                isAlarmingState = true;
            }
            else {
                isAlarmingState = false;
            }
        }

        @Override
        public void onSecurityModeChanged(String newMode) {
            newSecurityMode = newMode;
        }

        @Override
        public void onAvailableAlertsChanged(Set<String> newAvailableAlerts) {
            HashSet availableAlerts = new HashSet(newAvailableAlerts);
            if (availableAlerts.contains(AlarmSubsystemModel.AVAILABLEALERTS_SECURITY)) {
                isSecurityAvailable = true;
            }
            else {
                isSecurityAvailable = false;
            }
        }

        @Override
        public void onError(Throwable error) {

        }
    }

}
