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
package arcus.app.subsystems.scenes.schedule.controller;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.event.Listener;
import com.iris.client.model.Model;
import com.iris.client.model.SceneModel;
import com.iris.client.model.SchedulerModel;
import com.iris.client.service.SceneService;
import com.iris.client.service.SchedulerService;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class WeeklySchedulerFragmentController extends FragmentController<WeeklySchedulerFragmentController.Callbacks> {

    private final static Logger logger = LoggerFactory.getLogger(WeeklySchedulerFragmentController.class);
    private final static WeeklySchedulerFragmentController instance = new WeeklySchedulerFragmentController();

    public interface Callbacks {
        void onScheduledCommandsLoaded(List<TimeOfDayCommand> scheduledCommands, Set<DayOfWeek> daysWithScheduledEvents);
        void onCorneaError(Throwable cause);
    }

    private WeeklySchedulerFragmentController() {}
    public static WeeklySchedulerFragmentController getInstance () { return instance; }

    public void loadScheduleCommandsForDay (final String sceneAddress, final DayOfWeek dayOfWeek) {
        logger.debug("Loading scheduled commands for day {} on scene {}", dayOfWeek, sceneAddress);

        final String placeId = CorneaUtils.getIdFromAddress(PlaceModelProvider.getCurrentPlace().getAddress());
        CorneaClientFactory.getService(SceneService.class).listScenes(placeId).onSuccess(Listeners.runOnUiThread(new Listener<SceneService.ListScenesResponse>() {
            @Override
            public void onEvent(SceneService.ListScenesResponse listScenesResponse) {
                List<Model> scenes = CorneaClientFactory.getModelCache().addOrUpdate(listScenesResponse.getScenes());

                // Find our scene in the cache
                for (Model thisScene : scenes) {
                    if (sceneAddress.equalsIgnoreCase(thisScene.getAddress())) {
                        loadScheduledCommandsFromSceneModel((SceneModel) thisScene, dayOfWeek);
                        return;
                    }
                }

                // None found; this shouldn't be possible...
                fireOnCorneaError(new IllegalArgumentException("Bug! No scene model found with address " + sceneAddress));
            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void loadScheduledCommandsFromSceneModel(SceneModel sceneModel, final DayOfWeek dayOfWeek) {
        logger.debug("Loading scheduler for scene {}", sceneModel.getAddress());

        final Set<DayOfWeek> daysWithScheduledEvents = new HashSet<>();
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(sceneModel.getAddress()).onSuccess(Listeners.runOnUiThread(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                List<TimeOfDayCommand> commands = new ArrayList<>();
                List<TimeOfDayCommand> sunriseSunset = new ArrayList<>();
                List<TimeOfDayCommand> absolute = new ArrayList<>();

                if (scheduler.getCommands() != null) {
                    logger.debug("Got {} commands associated with scheduler; filtering for day {}", scheduler.getCommands().values().size(), dayOfWeek);

                          for (Map<String, Object> thisCommandData : scheduler.getCommands().values()) {
                              TimeOfDayCommand thisCommand = new TimeOfDayCommand(thisCommandData);
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

                Collections.sort(sunriseSunset, timeOfDayCommandComparator);
                Collections.sort(absolute, timeOfDayCommandComparator);

                commands.addAll(sunriseSunset);
                commands.addAll(absolute);
                fireOnScheduledCommandsLoaded(commands, daysWithScheduledEvents);

            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void fireOnScheduledCommandsLoaded(List<TimeOfDayCommand> scheduledCommands, Set<DayOfWeek> daysWithScheduledEvents) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onScheduledCommandsLoaded(scheduledCommands, daysWithScheduledEvents);
        }
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

    private final Comparator<TimeOfDayCommand> timeOfDayCommandComparator = new Comparator<TimeOfDayCommand>() {
        @Override public int compare(TimeOfDayCommand lhs, TimeOfDayCommand rhs) {
            TimeOfDay lhsTOD = TimeOfDay.fromString(lhs.getTime());
            TimeOfDay rhsTOD = TimeOfDay.fromString(rhs.getTime());

            return lhsTOD.compareTo(rhsTOD);
        }
    };
}
