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

import androidx.annotation.NonNull;

import arcus.cornea.common.BasePresenter;
import arcus.cornea.subsystem.alarm.AlarmProviderController;
import com.iris.client.capability.HubConnection;
import com.iris.client.model.HubModel;
import arcus.app.common.models.SessionModelManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;



public class HubAlarmDisconnectedPresenter extends BasePresenter<HubAlarmDisconnectedContract.HubAlarmDisconnectedView>
        implements HubAlarmDisconnectedContract.HubAlarmDisconnectedPresenter, AlarmProviderController.Callback {

    public Logger logger = LoggerFactory.getLogger(this.getClass());
    private HubModel hubModel;
    private AlarmProviderController alarmProviderController = new AlarmProviderController();



    public void startPresenting(HubAlarmDisconnectedContract.HubAlarmDisconnectedView view){
        super.startPresenting(view);
        registerAlarmProviderControllerCallback();
        alarmProviderController.requestUpdate();
        hubModel = SessionModelManager.instance().getHubModel();
        if (hubModel != null) {
            addHubPropertyChangeListener();
            getPresentedView().setHubLastChangedTime(getHubLastChangedTime());
        }
    }

    private void registerAlarmProviderControllerCallback() {
        String alarmProviderId = AlarmProviderController.class.getCanonicalName();
        addListener(alarmProviderId, alarmProviderController.setCallback(this));
    }

    @Override
    public void onAlarmProviderChanged(boolean isAlarmProviderHub) {
        // Nothing to do.
    }

    @Override
    public void onAlarmStateChanged(String incidentAddress) {
    }

    @Override
    public void onSecurityModeChanged(String newMode){
        getPresentedView().setSecurityModeChanged(newMode);
    }

    @Override
    public void onAvailableAlertsChanged(Set<String> newAvailableAlerts) {
        // Nothing to do.
    }

    @Override
    public void onError(Throwable error) {
        // Nothing to do.
    }



    public void addHubPropertyChangeListener() {

        if (hubModel == null) {
            logger.debug("Cannot add property change listener to null model.");
            return;
        }

        addListener(hubModel.getClass().getCanonicalName(), hubModel.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(@NonNull PropertyChangeEvent event) {
                if (event.getPropertyName().equals(HubConnection.ATTR_LASTCHANGE)) {
                    if (isPresenting()) {
                        getPresentedView().setHubLastChangedTime(getHubLastChangedTime());
                    }
                }
            }
        }));
    }

    private String getHubLastChangedTime() {
        hubModel = SessionModelManager.instance().getHubModel();
        String hubconnLastChangeTime = "";
        if (hubModel != null) {
            StringBuilder formattedHubLastChangeTime = new StringBuilder(new SimpleDateFormat("h:mm a").format(new Date(((Number) hubModel.get(HubConnection.ATTR_LASTCHANGE)).longValue())));
            formattedHubLastChangeTime.append(" on ");
            formattedHubLastChangeTime.append(new StringBuilder(new SimpleDateFormat("EEE MMMM d").format(new Date(((Number) hubModel.get(HubConnection.ATTR_LASTCHANGE)).longValue()))));
            hubconnLastChangeTime = formattedHubLastChangeTime.toString();
        }
        return hubconnLastChangeTime;
    }

}
