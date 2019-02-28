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
package arcus.app.device.pairing.nohub.swannwifi.controller;

import android.os.AsyncTask;

import arcus.cornea.subsystem.lightsnswitches.schedule.LightsNSwitchesScheduleViewController;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.Switch;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;
import arcus.app.common.schedule.model.ScheduleCommandModel;

import java.util.Set;
import java.util.concurrent.CountDownLatch;


public class SwannScheduleController implements ScheduleCommandEditController.Callbacks {

    private CountDownLatch latch;

    public interface Callbacks {
        void onSuccess();
        void onFailure(Throwable cause);
    }

    public class SwannScheduleCommand extends AbstractScheduleCommandModel {
        public SwannScheduleCommand(String switchState, TimeOfDay timeOfDay, Set<DayOfWeek> repeatOnDays) {
            super(LightsNSwitchesScheduleViewController.LNS_GROUP_ID, timeOfDay, repeatOnDays);
            super.getAttributes().put(Switch.ATTR_STATE, switchState);
        }
    }

    private final static SwannScheduleController instance = new SwannScheduleController();
    private Callbacks callbacks;

    private SwannScheduleController () {}

    public static SwannScheduleController getInstance() {
        return instance;
    }

    public void saveSchedule (final Callbacks callbacks, final String deviceAddress, TimeOfDay timeOn, TimeOfDay timeOff, Set<DayOfWeek> days) {
        this.callbacks = callbacks;

        SwannScheduleCommand timeOnCommand = new SwannScheduleCommand(Switch.STATE_ON, timeOn, days);
        final SwannScheduleCommand timeOffCommand = new SwannScheduleCommand(Switch.STATE_OFF, timeOff, days);

        latch = new CountDownLatch(1);

        ScheduleCommandEditController.getInstance().setListener(this);
        ScheduleCommandEditController.getInstance().addCommand(deviceAddress, timeOnCommand);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    latch.await();  // Wait for the addCommand() to complete to prevent platform race conditions
                    ScheduleCommandEditController.getInstance().addCommand(deviceAddress, timeOffCommand);
                } catch (InterruptedException e) {
                    callbacks.onFailure(e);
                }
            }
        });
    }

    @Override
    public void onSchedulerError(Throwable cause) {
        latch.countDown();

        if (callbacks != null) {
            callbacks.onFailure(cause);
        }
    }

    @Override
    public void onTimeOfDayCommandLoaded(ScheduleCommandModel scheduleCommandModel) {
        // Nothing to do
    }

    @Override
    public void onConfirmDeleteAllDays(ScheduleCommandModel scheduleCommandModel) {
        // Nothing to do; no means to delete schedules in this context
    }

    @Override
    public void onConfirmUpdateAllDays(ScheduleCommandModel scheduleCommandModel) {
        // Nothing to do; no schedules should ever exist at this point
    }

    @Override
    public void onSuccess() {
        latch.countDown();

        if (callbacks != null) {
            callbacks.onSuccess();
        }
    }
}
