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

import androidx.annotation.Nullable;

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import arcus.app.subsystems.alarm.promonitoring.models.AlarmTrackerModel;

public class AlarmIncidentContract {

    public interface AlarmIncidentView extends PresentedView<AlarmTrackerModel> {
        /**
         * Fires to indicate the view should display a cancel dialog containing the provided copy.
         * @param reasonCopy The copy to be displayed in the dialog.
         */
        void showCancel(String title, CharSequence reasonCopy);

        /**showCancel
         * An error occurred fetching incident data; show an appropriate message.
         * @param t The error
         */
        void showError(Throwable t);

        /**showHubDisconnectedBanner
         * Show Banner whe the Hub is the AlarmProvider and it is disconnected from the Internet.
         * @param isHubOffline boolean to show or hide the banner.
         * @param hubOfflineTime Time that the Hub went offline.
         */
        void showHubDisconnectedBanner( boolean isHubOffline, @Nullable String hubOfflineTime);

        /**updateHubDisconnectedBanner
         * Updates the text on the Hub Disconnected banner when the hubconn:lastChange time changes.
         * @param hubOfflineTime Time that the Hub went offline.
         */
        void updateHubDisconnectedBanner(String hubOfflineTime);

    }

    interface AlarmIncidentPresenter extends Presenter<AlarmIncidentContract.AlarmIncidentView> {
        /**
         * Requests the presenter to refresh the view via the
         * {@link AlarmIncidentView#updateView(Object)} callback method.
         */
        void requestUpdate(String incidentAddress);

        /**
         * Indicates that the user has pressed the "Confirm" button.
         */
        void requestConfirm();

        /**
         * Indicates that the user has pressed the "Cancel" button.
         */
        void requestCancel();
    }

}
