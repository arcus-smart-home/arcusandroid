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

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import arcus.cornea.subsystem.alarm.model.AlertDeviceStateModel;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmStatusModel;

import java.util.List;

import arcus.app.subsystems.alarm.promonitoring.ProMonitoringIncidentFragment;


public interface AlarmStatusContract {

    class AlarmStatusPresenterModel {
        private final Uri adTarget;
        private final List<AlarmStatusModel> alarmModels;

        public AlarmStatusPresenterModel(Uri adTarget, List<AlarmStatusModel> alarmModels) {
            this.adTarget = adTarget;
            this.alarmModels = alarmModels;
        }

        public Uri getAdTarget() {
            return adTarget;
        }

        public List<AlarmStatusModel> getAlarmModels() {
            return alarmModels;
        }
    }

    interface AlarmStatusView extends PresentedView<AlarmStatusPresenterModel> {

        /**
         * Invoked when a user is attempting to arm their security alarm, but one or more
         * participating devices are already triggered (i.e., window is open).
         *
         * @param unsecuredDeviceNames Unsecured device names
         * @param isPartialMode
         */
        void onPromptUnsecured(List<AlertDeviceStateModel> unsecuredDeviceNames, boolean isPartialMode);

        /**
         * Invoked when a user attempts to arm the security alarm but does not have enough devices
         * available to participate, either because all the user's devices are triggered, or
         * because the number of available motion sensors is below the user-specified threshold.
         */
        void onPromptNotEnoughSecurityDevices();

        /**
         * Invoked when a user tries to change the state of their security alarm after an alarm
         * has been sent to the monitoring station for dispatch, and the Platform returns a
         * "security.hubDisarming" error code. User must wait for dispatcher to clear the alarm
         * before re-arming.
         */
        void onPromptWaitingForMonitoringStation();

        /**
         * Invoked when a user tries to change the state of their security alarm, and the Platform
         * returns an "UnknownDevice" error code.  This will happen when the Hub is the Alarm
         * Provider and the Hub is disconnect, and the Platform has not yet detected the Hub is disconnected.
         */
        void onPromptRequestTimedOut();

        /**
         * Invoked when a user tries to change the state of their security alarm, and the Platform
         * returns a "security.hubDisarming" error code.  This will happen when the Platform is still disarming
         * the Security Alarms, and the user clicks on the ON or PARTIAL button to rearm the security system.
         */
        void onPromptHubDisarming();
    }

    interface AlarmStatusPresenter extends Presenter<AlarmStatusView> {

        /**
         * Requests the presenter to fetch data from the platform and refresh the UI via the
         * {@link AlarmStatusView#updateView(Object)} method.
         */
        void requestUpdate();

        /**
         * Transition to the {@link ProMonitoringIncidentFragment}
         * fragment, displaying the current incident.
         */
        void presentCurrentIncident();

        /**
         * Attempts to arm the security alarm; may result in the {@link AlarmStatusView#onPromptUnsecured(List, boolean)}
         * callback firing if one or more devices are bypassed.
         *
         * @param bypassed When true, the alarm is armed even if there are triggered devices. When false,
         *                 {@link AlarmStatusView#onPromptUnsecured(List, boolean)} will be
         *                 invoked if there are triggered devices. Typically, true will be passed in
         *                 this parameter only after {@link AlarmStatusView#onPromptUnsecured(List, boolean)}
         *                 has been invoked, the user has been made aware of the triggered devices,
         *                 and confirmed that the alarm should be armed.
         */
        void armSecurityAlarm(boolean bypassed);

        /**
         * Attempts to arm-partial the security alarm; may result in the {@link AlarmStatusView#onPromptUnsecured(List, boolean)}
         * callback firing if one or more devices are bypassed.
         *
         * @param bypassed When true, the alarm is armed even if there are triggered devices. When false,
         *                 {@link AlarmStatusView#onPromptUnsecured(List, boolean)} will be
         *                 invoked if there are triggered devices. Typically, true will be passed in
         *                 this parameter only after {@link AlarmStatusView#onPromptUnsecured(List, boolean)}
         *                 has been invoked, the user has been made aware of the triggered devices,
         *                 and confirmed that the alarm should be armed.
         */
        void armPartialSecurityAlarm(boolean bypassed);

        /**
         * Disarms the security alarm.
         */
        void disarmSecurityAlarm();

        /**
         * Dismisses an alerting alarm.
         */
        void dismissAlarm();//could very depending on type?
    }
}
