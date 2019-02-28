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
package arcus.app.common.schedule.controller;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.RuleModel;
import com.iris.client.model.SchedulerModel;
import com.iris.client.service.SchedulerService;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.schedule.model.ScheduleCommandModelFactory;
import arcus.app.common.schedule.model.TimeOfDayCommandSortable;
import arcus.app.common.utils.CorneaUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller intended to list scheduled commands that occur on a specified day of the week for a
 * given addressable device.
 */
public class ScheduleCommandListController extends FragmentController<ScheduleCommandListController.Callbacks> {
    private final static Logger logger = LoggerFactory.getLogger(ScheduleCommandListController.class);

    public interface Callbacks {
        void onScheduledCommandsLoaded(DayOfWeek selectedDay, List<ScheduleCommandModel> scheduledEvents, Set<DayOfWeek> daysWithScheduledEvents);
        void onCorneaError (Throwable cause);
    }


    private static ScheduleCommandListController instance = new ScheduleCommandListController();
    private ScheduleCommandListController() {}

    public static ScheduleCommandListController getInstance () {
        return instance;
    }

    public void loadScheduledCommandsForDayOfWeek(final String modelAddress, final DayOfWeek dayOfWeek, final ScheduleCommandModelFactory modelFactory) {
        logger.debug("Loading scheduled commands occurring on {} for model {}", dayOfWeek, modelAddress);

        // If address is a device...
        if (CorneaUtils.isDeviceAddress(modelAddress)) {
           DeviceModelProvider.instance().getModel(modelAddress).load().onSuccess(new Listener<DeviceModel>() {
               @Override
               public void onEvent(DeviceModel deviceModel) {
                   loadScheduledEventsForDeviceModelOnDayOfWeek(deviceModel, dayOfWeek, modelFactory);
               }
           }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
               @Override
               public void onEvent(Throwable throwable) {
                   fireOnCorneaError(throwable);
               }
           }));
        }

        // Otherwise, assumption is that it's a rule.
        else {
           RuleModelProvider.instance().getModel(modelAddress).reload().onSuccess(new Listener<RuleModel>() {
               @Override
               public void onEvent(RuleModel ruleModel) {
                   loadScheduledEventsForDeviceModelOnDayOfWeek(ruleModel, dayOfWeek, modelFactory);
               }
           }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
               @Override
               public void onEvent(Throwable throwable) {
                   fireOnCorneaError(throwable);
               }
           }));
       }
    }

    private void loadScheduledEventsForDeviceModelOnDayOfWeek (Model corneaModel, final DayOfWeek dayOfWeek, final ScheduleCommandModelFactory modelFactory) {
        final Set<DayOfWeek> daysWithScheduledEvents = new HashSet<>();

        logger.debug("Loading scheduler for model {}", corneaModel.getAddress());
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(corneaModel.getAddress()).onSuccess(Listeners
              .runOnUiThread(new Listener<SchedulerService.GetSchedulerResponse>() {
                  @Override
                  public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                      // Create a scheduler model object from the raw map data
                      SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                      List<TimeOfDayCommandSortable> commands = new ArrayList<>();
                      List<TimeOfDayCommandSortable> sunriseSunset = new ArrayList<>();
                      List<TimeOfDayCommandSortable> absolute = new ArrayList<>();

                      if (scheduler.getCommands() != null) {
                          logger.debug("Got {} commands associated with scheduler; filtering for day {}", scheduler.getCommands()
                                .values()
                                .size(), dayOfWeek);

                          // Create TimeOfDay objects from each of the raw command maps
                          for (Map<String, Object> thisCommandData : scheduler.getCommands().values()) {
                              TimeOfDayCommandSortable thisCommand = new TimeOfDayCommandSortable(thisCommandData);

                              for (String day : thisCommand.getDays()) {
                                  daysWithScheduledEvents.add(DayOfWeek.from(day));

                                  if (String.valueOf(day).toUpperCase().equals(dayOfWeek.toString().substring(0, 3))) {
                                      if (TimeOfDayCommand.MODE_ABSOLUTE.equals(thisCommand.getMode())) {
                                          absolute.add(thisCommand);
                                      }
                                      else {
                                          sunriseSunset.add(thisCommand);
                                      }
                                  }
                              }
                          }
                      }

                      Collections.sort(sunriseSunset);
                      Collections.sort(absolute);
                      commands.addAll(sunriseSunset);
                      commands.addAll(absolute);

                      fireOnScheduledEventsForDay(dayOfWeek, AbstractScheduleCommandModel.fromTimeOfDayCommands(commands, modelFactory), daysWithScheduledEvents);
                  }
              })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks callbacks = getListener();
        if (callbacks != null) {
            callbacks.onCorneaError(cause);
        }
    }

    private void fireOnScheduledEventsForDay(DayOfWeek selectedDay, List<ScheduleCommandModel> scheduledEvents, Set<DayOfWeek> daysWithScheduledEvents) {
        Callbacks callbacks = getListener();
        if (callbacks != null) {
            callbacks.onScheduledCommandsLoaded(selectedDay, scheduledEvents, daysWithScheduledEvents);
        }
    }
}
