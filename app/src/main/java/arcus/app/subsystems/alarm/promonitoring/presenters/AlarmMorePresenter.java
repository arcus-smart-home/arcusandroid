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

import com.google.common.collect.Sets;
import arcus.cornea.common.BasePresenter;
import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import arcus.cornea.subsystem.alarm.model.AlarmModel;
import arcus.cornea.subsystem.safety.SettingsController;
import arcus.cornea.subsystem.safety.model.Settings;
import com.iris.client.capability.AlarmSubsystem;

import java.util.List;
import java.util.Set;



public class AlarmMorePresenter extends BasePresenter<AlarmMoreContract.AlarmMoreView> implements AlarmMoreContract.AlarmMorePresenter, SettingsController.Callback, AlarmSubsystemController.Callback {

    private Settings waterValveSettings;
    private Set<String> availableAlerts = Sets.newHashSet();

    @Override
    public void requestUpdate() {
        registerSafetySubsystemListener();
        registerAlarmSubsystemListener();

        AlarmSubsystemController.getInstance().requestUpdate();
    }

    @Override
    public void setWaterShutOffValue(boolean shutOffWaterWhenLeakDetected) {
        if (waterValveSettings == null) {
            getPresentedView().onError(new IllegalStateException("Attempt to update water valve state before existing state has been retrieved."));
        }

        SettingsController.instance().setSettings(Settings.builder().from(waterValveSettings).withWaterShutoffEnabled(shutOffWaterWhenLeakDetected).build());
    }

    @Override
    public void setRecordOnAlarmValue(boolean recordOnAlarm) {
        AlarmSubsystemController.getInstance().setRecordOnSecurity(recordOnAlarm);
    }

    @Override
    public void setShutFansOffOnCOValue(boolean shutFanOffOnCOValue) {
        AlarmSubsystemController.getInstance().setShutOffFanOnCO(shutFanOffOnCOValue);
    }

    @Override
    public void setShutFansOffOnSmokeValue(boolean shutFanOffOnSmokeValue) {
        AlarmSubsystemController.getInstance().setShutOffFanOnSmoke(shutFanOffOnSmokeValue);
    }

    private void registerAlarmSubsystemListener() {
        String alarmSubsystemListenerId = AlarmSubsystemController.class.getCanonicalName();
        addListener(alarmSubsystemListenerId, AlarmSubsystemController.getInstance().setCallback(this));
    }

    private void registerSafetySubsystemListener() {
        String safetySubsystemListenerId = SettingsController.class.getCanonicalName();
        addListener(safetySubsystemListenerId, SettingsController.instance().setCallback(this));
    }

    private AlarmMoreContract.AlarmMoreModel buildModel() {

        AlarmMoreContract.AlarmMoreModel model = new AlarmMoreContract.AlarmMoreModel();
        model.isCoAvailable = availableAlerts.contains(AlarmSubsystem.ACTIVEALERTS_CO);
        model.isSecurityAvailable = availableAlerts.contains(AlarmSubsystem.ACTIVEALERTS_SECURITY);
        model.isSmokeAvailable = availableAlerts.contains(AlarmSubsystem.ACTIVEALERTS_SMOKE);
        model.isWaterAvailable = availableAlerts.contains(AlarmSubsystem.ACTIVEALERTS_WATER) && waterValveSettings.isWaterShutoffAvailable();
        model.waterShutoffEnabled = waterValveSettings != null && waterValveSettings.isWaterShutoffEnabled();

        model.isRecordSupported = AlarmSubsystemController.getInstance().getRecordingSupported();
        model.recordOnSecurity = AlarmSubsystemController.getInstance().getRecordOnSecurity();

        model.fanShutOffSupported = AlarmSubsystemController.getInstance().getShutOffFansSupported();
        model.shutOffFansOnSmoke = AlarmSubsystemController.getInstance().getShutOffFanOnSmoke();
        model.shutOffFansOnCO = AlarmSubsystemController.getInstance().getShutOffFanOnCO();
        return model;
    }

    @Override
    public void showSettings(Settings settings) {
        this.waterValveSettings = settings;
        AlarmMoreContract.AlarmMoreModel model = buildModel();
        if(model.hasDevices()) {
            getPresentedView().updateView(buildModel());
        } else {
            getPresentedView().presentNoDevicesAvailable();
        }
    }

    @Override
    public void showUpdateError(Throwable t, Settings currentSettings) {
        getPresentedView().onError(t);
    }

    @Override
    public void onAlarmStateChanged(String newAlarmState) {
        // Nothing to do
    }

    @Override
    public void onSecurityModeChanged(String newMode) {
        // Nothing to do
    }

    @Override
    public void onAlertsChanged(List<String> activeAlerts, Set<String> availableAlerts, Set<String> monitoredAlerts) {
        this.availableAlerts = availableAlerts;
        AlarmMoreContract.AlarmMoreModel model = buildModel();
        if(model.hasDevices()) {
            getPresentedView().updateView(buildModel());
        } else {
            getPresentedView().presentNoDevicesAvailable();
        }
    }

    @Override
    public void onIncidentChanged(String incidentAddress) {
        // Nothing to do
    }

    @Override
    public void onAlarmsChanged(List<AlarmModel> alarmModels) {
        // Nothing to do
    }

    @Override
    public void onSubsystemAvailableChange(boolean available) {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onActivateComplete() {

    }
}
