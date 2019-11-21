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
package arcus.app.device.details.presenters;

import androidx.annotation.NonNull;

import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import com.iris.client.model.DeviceModel;
import arcus.app.device.details.model.ThermostatDisplayModel;
import arcus.app.device.details.model.ThermostatOperatingMode;

import java.util.List;



public class ThermostatPresenterContract {

    public interface ThermostatPresenter extends Presenter<ThermostatControlView> {
        /**
         * Requests that the presenter refresh the view.
         */
        void updateView();

        /**
         * When in auto mode, toggles the setpoint that is modified by calls to
         * {@link #decrementSetpoint()} or {@link #incrementSetpoint()}.
         */
        void toggleActiveSetpoint();

        /**
         * Increments the active setpoint by one degree F. See {@link #toggleActiveSetpoint()}.
         */
        void incrementSetpoint();

        /**
         * Decrements the active setpoint by one degree F. See {@link #toggleActiveSetpoint()}.
         */
        void decrementSetpoint();

        /**
         * Call to indicate that the user wishes to change HVAC operating modes. Results in an
         * immediate callback to {@link ThermostatControlView#onDisplayModeSelection(List)}.
         */
        void selectMode();

        /**
         * Sets the current HVAC operating mode.
         * @param mode The selected mode
         */
        void setOperatingMode(ThermostatOperatingMode mode);

        /**
         * Modifies the setpoint associated with the current operating mode (i.e., if in COOL, a
         * call to this method changes the cool setpoint). Note that when in AUTO mode, view should
         * invoke {@link #setSetpointRange(int, int)} instead.
         * @param setpointF The new setpoint value, in F.
         */
        void setSetpoint(int setpointF);

        /**
         * Modifies both the cool and heat setpoints. This method is intended to be used in AUTO
         * mode; it is recommended that view uses {@link #setSetpoint(int)} in HEAT or COOL modes.
         * @param heatSetpointF
         * @param coolSetpointF
         */
        void setSetpointRange(int heatSetpointF, int coolSetpointF);

        /**
         * Notifies the presenter that the user is actively in the process of making a change and
         * should adjust its commit / throttle behavior accordingly.
         */
        void notifyAdjustmentInProgress();
    }

    public interface ThermostatControlView extends PresentedView<ThermostatDisplayModel> {

        /**
         * Invoked when the use wishes to switch HVAC operating modes. View is responsible for
         * rendering a selection list or popup with the provided choices.
         *
         * @param availableModes A list of available modes that the user can select from.
         * @param useNestTerminology When true, view should use nest terminology for modes, namely
         *                           translating "Auto" to "Heat*Cool"
         */
        void onDisplayModeSelection(List<ThermostatOperatingMode> availableModes, boolean useNestTerminology);

        /**
         * Invoked when the view should display a "NEXT EVENT" string.
         *
         * @param eventText The next event string. When null or empty, no schedule is active.
         */
        void onShowScheduleEvent(String eventText);

        /**
         * Invoked to indicate that the view should update the state of its manufacturer icon
         * footer.
         *
         * @param inAlertState When true, footer should be rendered in alert state (pink footer);
         *                     false, in normal state.
         */
        void onUpdateFooterState(boolean inAlertState);

        /**
         * Invoked to indicate that that the view should display a "Just a moment..." wait state.
         *
         * @param isVisible When true, loading indicator is shown; hidden otherwise.
         * @param isControlDisabled When true, all controls on the page should be disabled (grey'd out)
         */
        void setWaitingIndicatorVisible(boolean isVisible, boolean isControlDisabled);

        /**
         * Invoked by the presenter to get the DeviceModel of the thermostat being controlled.
         *
         * @return The thermostat's DeviceModel. Cannot be null.
         */
        @NonNull DeviceModel getThermostatDeviceModel();
    }

}
