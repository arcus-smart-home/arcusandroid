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

import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.capability.Schedule;
import com.iris.client.capability.WeeklySchedule;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.model.SchedulerModel;
import com.iris.client.service.SchedulerService;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.schedule.model.ScheduleCommandModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controller intended to create or edit weekly schedule commands (represented in Cornea as
 * {@link TimeOfDayCommand}).
 *
 * Provides methods for adding a new schedule command ({@link #addCommand(String, ScheduleCommandModel)}),
 * removing existing scheduled commands ({@link #deleteCommand(String, ScheduleCommandModel)}), and
 * updating existing scheduled commands ({@link #updateCommand(String, ScheduleCommandModel)}).
 */
public class ScheduleCommandEditController extends FragmentController<ScheduleCommandEditController.Callbacks> {

    private final static ScheduleCommandEditController instance = new ScheduleCommandEditController();
    private final static Logger logger = LoggerFactory.getLogger(ScheduleCommandEditController.class);

    public interface Callbacks {
        /**
         * Invoked to indicate that a Cornea communications error has occurred. Typically results
         * in an error dialog being displayed.
         *
         * @param cause The throwable provided by Cornea as the cause of the error.
         */
        void onSchedulerError(Throwable cause);

        /**
         * Invoked to indicate the schedule command has been loaded as a result of calling
         * {@link #loadCommand(String, String, ScheduleCommandModel)} .
         *
         * @param scheduleCommandModel The loaded schedule command.
         */
        void onTimeOfDayCommandLoaded(ScheduleCommandModel scheduleCommandModel);

        /**
         * Invoked in response to {@link #deleteCommand(String, ScheduleCommandModel)} to indicate that the
         * requested event occurs on multiple days and that the user should be prompted as to
         * whether to delete all days, or only a single day.
         *
         * After gathering user input, the implementer must invoke either
         * {@link #deleteCommandSingleDay(String, ScheduleCommandModel, DayOfWeek)}
         * or {@link #deleteCommandAllDays(String, ScheduleCommandModel)} to initiate the delete. Failing
         * to call one of these methods will result on the command remaining active on the platform.
         *
         * @param scheduleCommandModel The scheduler command that was passed to {@link #deleteCommand(String, ScheduleCommandModel)}
         */
        void onConfirmDeleteAllDays (ScheduleCommandModel scheduleCommandModel);

        /**
         * Invoked in response to {@link #updateCommand(String, ScheduleCommandModel)} to indicate that the
         * requested event occurs on multiple days and that the user should be prompted as to
         * whether to update all days, or only a single day.
         *
         * After gathering user input, the implemented must invoke either
         * {@link #updateCommandAllDays(String, ScheduleCommandModel)} or
         * {@link #updateCommandSingleDay(String, ScheduleCommandModel, DayOfWeek)} to initiate the update.
         * Failing to call one of these methods will result in the command remaining in its original
         * state on the platform.
         *
         * @param scheduleCommandModel The scheduler command that was passed to {@link #updateCommand(String, ScheduleCommandModel)}
         */
        void onConfirmUpdateAllDays (ScheduleCommandModel scheduleCommandModel);

        /**
         * Invoked to indicate the requested command (i.e., add, update or delete) succeeded. Typically,
         * the implementer will transition back to an instance of {@link arcus.app.common.schedule.AbstractWeeklySchedulerFragment}.
         */
        void onSuccess ();
    }

    private ScheduleCommandEditController() {}
    public static ScheduleCommandEditController getInstance() {
        return instance;
    }

    /**
     * Loads the scheduler command matching the provided ID from the scheduler associated with the
     * given device address. Invokes {@link Callbacks#onTimeOfDayCommandLoaded(ScheduleCommandModel)} when
     * successful or {@link Callbacks#onSchedulerError(Throwable)} if unsuccessful.
     *  @param deviceAddress The address of the device whose scheduler is being edited.
     * @param scheduleCommandId The ID of the scheduler command
     * @param intoModel An instance of ScheduleCommand that the found command data should be loaded into.
     */
    public void loadCommand(final String deviceAddress, final String scheduleCommandId, final ScheduleCommandModel intoModel) {
        logger.debug("Loading scheduled event id={} for {}", scheduleCommandId, deviceAddress);

        findCommand(deviceAddress, scheduleCommandId, new ScheduleCommandLoadedListener() {
            @Override
            public void onCommandLoaded(TimeOfDayCommand command, String schedulerAddress) {
                intoScheduleCommand(command, intoModel);
                fireOnTimeOfDayCommandLoaded(intoModel);
            }
        });
    }

    /**
     * Deletes the provided scheduler command from the scheduler associated with the provided
     * device address. If the given command occurs on multiple days, then {@link Callbacks#onConfirmDeleteAllDays(ScheduleCommandModel)}
     * will be invoked to prompt for single day/everyday delete. Otherwise, invokes {@link Callbacks#onSuccess()} or
     * {@link Callbacks#onSchedulerError(Throwable)}.
     * @param deviceAddress The address of the device whose scheduler is being edited.
     * @param scheduleCommandModel The scheduler command to be deleted.
     */
    public void deleteCommand(final String deviceAddress, final ScheduleCommandModel scheduleCommandModel) {
        findCommand(deviceAddress, scheduleCommandModel.getId(), new ScheduleCommandLoadedListener() {
            @Override
            public void onCommandLoaded(TimeOfDayCommand savedCommand, String schedulerAddress) {
                if (savedCommand != null) {
                    // Does this event repeat?
                    if (savedCommand.getDays().size() > 1) {
                        fireOnConfirmDeleteAllDays(scheduleCommandModel);
                    }

                    // Event occurs on only one day; just delete it.
                    else {
                        delete(schedulerAddress, scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
                    }
                }
            }
        });
    }

    /**
     * Deletes the given scheduler command (on all days on which it repeats) from the scheduler
     * associated with the provided device address. Invokes either {@link Callbacks#onSuccess()} or
     * {@link Callbacks#onSchedulerError(Throwable)} on completion.
     * @param deviceAddress The address of the device whose scheduler is being edited.
     * @param scheduleCommandModel The scheduler command to delete
     */
    public void deleteCommandAllDays(final String deviceAddress, final ScheduleCommandModel scheduleCommandModel) {
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(deviceAddress).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                delete(scheduler.getAddress(), scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
            }
        }).onFailure(fireOnCorneaErrorListener);
    }

    /**
     * Deletes the given scheduler command from only the day provided, leaving the command active
     * on any other days specified by the command. Invokes either {@link Callbacks#onSuccess()} or
     * {@link Callbacks#onSchedulerError(Throwable)} on completion.
     * @param deviceAddress The address of the device whose scheduler is being edited.
     * @param scheduleCommandModel The scheduler command to delete.
     * @param deleteEventOnDay The day on which the command should no longer take place. The method
     */
    public void deleteCommandSingleDay(final String deviceAddress, final ScheduleCommandModel scheduleCommandModel, final DayOfWeek deleteEventOnDay) {
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(deviceAddress).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                final SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());

                // Delete the current event as it exists...
                delete(scheduler.getAddress(), scheduleCommandModel).onSuccess(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {

                        // Then remove the requested day from the repeat set...
                        scheduleCommandModel.getDays().remove(deleteEventOnDay.toString().substring(0, 3));
                        //for some delete commands, we need to wrap the day with a DayOfWeek enum object
                        scheduleCommandModel.getDays().remove(DayOfWeek.from(deleteEventOnDay.toString().substring(0,3)));

                        // ... and recreate the event
                        add(scheduler.getAddress(), scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
                    }
                });
            }
        }).onFailure(fireOnCorneaErrorListener);
    }

    /**
     * Adds the given scheduler command to the scheduler associated with the device identified by
     * the provided device address. Invokes either {@link Callbacks#onSuccess()} or
     * {@link Callbacks#onSchedulerError(Throwable)} on completion.
     * @param deviceAddress The address of the device whose scheduler is being edited.
     * @param scheduleCommandModel The command that should be added to the scheduler
     */
    public void addCommand(final String deviceAddress, final ScheduleCommandModel scheduleCommandModel) {
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(deviceAddress).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                add(scheduler.getAddress(), scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
            }
        }).onFailure(fireOnCorneaErrorListener);
    }

    /**
     * Updates the given scheduler command (pushing any changes made to the object to the platform)
     * on the scheduler associated with the provided device address.
     *
     * If the given scheduler command repeats on multiple days, then {@link Callbacks#onConfirmUpdateAllDays(ScheduleCommandModel)}
     * will be invoked to prompt the user to select single day/every day. Otherwise, either {@link Callbacks#onSuccess()} or
     * {@link Callbacks#onSchedulerError(Throwable)} will be invoked on completion.
     *
     * WARNING: This command cannot update the command message type value. If you need to update the message type
     * as part of the update, use {@link #updateCommandMessageType(String, ScheduleCommandModel)} instead.
     *
     * @param deviceAddress The address of the device whose scheduler is being updated.
     * @param scheduleCommandModel The updated scheduler command.
     */
    public void updateCommand(final String deviceAddress, final ScheduleCommandModel scheduleCommandModel) {
        findCommand(deviceAddress, scheduleCommandModel.getId(), new ScheduleCommandLoadedListener() {
            @Override
            public void onCommandLoaded(TimeOfDayCommand savedCommand, String schedulerAddress) {
                if (savedCommand == null) {
                    logger.warn("No command to update found matching id={}, adding command instead.", scheduleCommandModel.getId());
                    add(schedulerAddress, scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
                } else {
                    // Does this event repeat?
                    if (savedCommand.getDays().size() > 1) {
                        fireOnConfirmUpdateAllDays(scheduleCommandModel);
                    } else {
                        update(schedulerAddress, scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
                    }
                }
            }
        });
    }

    /**
     * "Updates" the given scheduler command when a change to the command's message type is required. Currently, this is only
     * necessary when dealing with rule enable/disable commands that are differentiated not via attributes, but through the
     * message type. Note that if you do not need to update the command message type (because, for example, you're not
     * scheduling rules) then you should use {@link #updateCommand(String, ScheduleCommandModel)} instead.
     *
     * Does not really "update" the command but rather deletes the existing one and creates a new on in its place. The
     * platform does not currently support in-place updating of the command message type.
     *
     * @param modelAddress The address of the device whose scheduler is being updated.
     * @param scheduleCommandModel The updated scheduler command.
     */
    public void updateCommandMessageType(final String modelAddress, final ScheduleCommandModel scheduleCommandModel) {
        findCommand(modelAddress, scheduleCommandModel.getId(), new ScheduleCommandLoadedListener() {
            @Override
            public void onCommandLoaded(TimeOfDayCommand savedCommand, final String schedulerAddress) {
                if (savedCommand == null) {
                    logger.warn("No command to update message type found matching id={}, adding command instead.", scheduleCommandModel.getId());
                    add(schedulerAddress, scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
                } else {
                    if (savedCommand.getDays().size() > 1) {
                        fireOnConfirmUpdateAllDays(scheduleCommandModel);
                    } else {
                        updateCommandMessageTypeAllDays(modelAddress, scheduleCommandModel);
                    }
                }
            }
        });
    }

    /**
     * Updates the given scheduler command with a change to its command message type only for the day specified; all other
     * days' events remain unmodified.
     *
     * This method implementation delegates directly to {@link #updateCommandSingleDay(String, ScheduleCommandModel, DayOfWeek)}
     * because its implementation is compatible with message type changes, however, clients should invoke this method
     * when appropriate for consistency and maintainability.
     *
     * @param deviceAddress The address of the device whose scheduler should be updated
     * @param scheduleCommandModel The updated scheduler command
     * @param updateEventDay The (only) day that should be affected by this update. If the given
     */
    public void updateCommandMessageTypeSingleDay (final String deviceAddress, final ScheduleCommandModel scheduleCommandModel, final DayOfWeek updateEventDay) {
        updateCommandSingleDay(deviceAddress, scheduleCommandModel, updateEventDay);
    }


    /**
     * Updates the given scheduler command with a change to its command message type for all days on which it repeats.
     * See {@link #updateCommandMessageType(String, ScheduleCommandModel)} for information about how this varies from
     * {@link #updateCommandSingleDay(String, ScheduleCommandModel, DayOfWeek)}
     *
     * @param modelAddress
     * @param scheduleCommandModel
     */
    public void updateCommandMessageTypeAllDays (final String modelAddress, final ScheduleCommandModel scheduleCommandModel) {
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(modelAddress).onFailure(fireOnCorneaErrorListener).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                final SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                delete(scheduler.getAddress(), scheduleCommandModel).onFailure(fireOnCorneaErrorListener).onSuccess(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {
                        add(scheduler.getAddress(), scheduleCommandModel).onFailure(fireOnCorneaErrorListener).onSuccess(fireOnSuccessListener);
                    }
                });
            }
        });
    }

    /**
     * Updates the given scheduler command on days on which it repeats. Invokes either {@link Callbacks#onSuccess()} or
     * {@link Callbacks#onSchedulerError(Throwable)} on completion.
     * @param deviceAddress The address of the device whose scheduler is being updated.
     * @param scheduleCommandModel The updated scheduler command.
     */
    public void updateCommandAllDays(final String deviceAddress, final ScheduleCommandModel scheduleCommandModel) {
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(deviceAddress).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                update(scheduler.getAddress(), scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
            }
        }).onFailure(fireOnCorneaErrorListener);
    }

    /**
     * Updates the given scheduler command on a single day. Invokes either {@link Callbacks#onSuccess()} or
     * {@link Callbacks#onSchedulerError(Throwable)} on completion.
     * @param deviceAddress The address of the device whose scheduler should be updated
     * @param scheduleCommandModel The updated scheduler command
     * @param updateEventDay The (only) day that should be affected by this update. If the given
     */
    public void updateCommandSingleDay(final String deviceAddress, final ScheduleCommandModel scheduleCommandModel, final DayOfWeek updateEventDay) {
        findCommand(deviceAddress, scheduleCommandModel.getId(), new ScheduleCommandLoadedListener() {
            @Override
            public void onCommandLoaded(final TimeOfDayCommand savedCommand, final String schedulerAddress) {
                if (savedCommand == null) {
                    logger.error("No command to update on single day found matching id={}, adding command instead.", scheduleCommandModel.getId());
                    add(schedulerAddress, scheduleCommandModel).onSuccess(fireOnSuccessListener).onFailure(fireOnCorneaErrorListener);
                } else {
                    // Delete the current event
                    delete(schedulerAddress, scheduleCommandModel).onFailure(fireOnCorneaErrorListener)
                          .onSuccess(new Listener<ClientEvent>() {
                              @Override
                              public void onEvent(ClientEvent clientEvent) {
                                  // Replace the event on the remaining days...
                                  savedCommand.getDays().remove(updateEventDay.toString().substring(0, 3));

                                  add(schedulerAddress,
                                        scheduleCommandModel.getSchedulerGroupId(),
                                        savedCommand.getMessageType(),
                                        savedCommand.getDays(),
                                        SunriseSunset.fromString(savedCommand.getMode()),
                                        savedCommand.getTime(),
                                        savedCommand.getOffsetMinutes(),
                                        savedCommand.getAttributes()
                                  ).onSuccess(new Listener<ClientEvent>() {
                                      @Override
                                      public void onEvent(ClientEvent clientEvent) {
                                          // Then add a new (updated) event for this day only
                                          scheduleCommandModel.setDays(EnumSet.of(updateEventDay));
                                          add(schedulerAddress, scheduleCommandModel).onFailure(fireOnCorneaErrorListener)
                                                .onSuccess(fireOnSuccessListener);
                                      }
                                  }).onFailure(fireOnCorneaErrorListener);
                              }
                          });

                }
            }
        });
    }

    private void findCommand(final String deviceAddress, final String commandId, final ScheduleCommandLoadedListener listener) {
        if (foundUsingCache(deviceAddress, commandId, listener)) {
            return;
        }

        CorneaClientFactory.getService(SchedulerService.class).getScheduler(deviceAddress).onSuccess(Listeners.runOnUiThread(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                logger.debug("Loaded {} scheduled events for {}; looking for command {}", scheduler.getCommands().size(), deviceAddress, commandId);

                for (Map<String, Object> thisCommandData : scheduler.getCommands().values()) {
                    TimeOfDayCommand thisCommand = new TimeOfDayCommand(thisCommandData);
                    if (thisCommand.getId().equalsIgnoreCase(commandId)) {
                        listener.onCommandLoaded(thisCommand, scheduler.getAddress());
                        return;
                    }
                }

                logger.error("No scheduled command id {} found on scheduler.", commandId);
                listener.onCommandLoaded(null, null);

            }
        })).onFailure(fireOnCorneaErrorListener);
    }

    private ClientFuture<ClientEvent> delete(String schedulerAddress, ScheduleCommandModel scheduleCommandModel) {
        ClientRequest request = new ClientRequest();
        request.setAddress(schedulerAddress);
        request.setCommand(Schedule.DeleteCommandRequest.NAME + ":" + scheduleCommandModel.getSchedulerGroupId());
        request.setAttribute(Schedule.DeleteCommandRequest.ATTR_COMMANDID, scheduleCommandModel.getId());
        return CorneaClientFactory.getClient().request(request);
    }

    private ClientFuture<ClientEvent> update(String schedulerAddress, ScheduleCommandModel scheduleCommandModel) {
        ClientRequest request = new ClientRequest();
        request.setAddress(schedulerAddress);
        request.setCommand(WeeklySchedule.UpdateWeeklyCommandRequest.NAME + ":" + scheduleCommandModel.getSchedulerGroupId());
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_DAYS, serializeDays(scheduleCommandModel.getDays()));
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_MODE, scheduleCommandModel.getTime().getSunriseSunset().name());

        if (SunriseSunset.ABSOLUTE.equals(scheduleCommandModel.getTime().getSunriseSunset())) {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_TIME, scheduleCommandModel.getTime().toString());
        }
        else {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_OFFSETMINUTES, scheduleCommandModel.getTime().getOffset());
        }

        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_ATTRIBUTES, scheduleCommandModel.getAttributes());
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_COMMANDID, scheduleCommandModel.getId());
        return CorneaClientFactory.getClient().request(request);
    }

    private ClientFuture<ClientEvent> add(String schedulerAddress, ScheduleCommandModel scheduleCommandModel) {
        return add(
              schedulerAddress,
              scheduleCommandModel.getSchedulerGroupId(),
              scheduleCommandModel.getCommandMessageType(),
              serializeDays(scheduleCommandModel.getDays()),
              scheduleCommandModel.getTime().getSunriseSunset(),
              scheduleCommandModel.getTime().toString(),
              scheduleCommandModel.getTime().getOffset(),
              scheduleCommandModel.getAttributes()
        );
    }

    private ClientFuture<ClientEvent> add(
          String schedulerAddress,
          String groupId,
          String messageType,
          Set<String> days,
          SunriseSunset mode,
          String time,
          Integer offset,
          Map<String,Object> attributes
    ) {
        ClientRequest request = new ClientRequest();
        request.setAddress(schedulerAddress);
        request.setCommand(WeeklySchedule.ScheduleWeeklyCommandRequest.NAME + ":" + groupId);
        request.setAttribute(WeeklySchedule.ScheduleWeeklyCommandRequest.ATTR_MESSAGETYPE, messageType);
        request.setAttribute(WeeklySchedule.ScheduleWeeklyCommandRequest.ATTR_DAYS, days);
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_MODE, mode.name());

        if (SunriseSunset.ABSOLUTE.equals(mode)) {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_TIME, time);
        }
        else {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_OFFSETMINUTES, offset);
        }

        request.setAttribute(WeeklySchedule.ScheduleWeeklyCommandRequest.ATTR_ATTRIBUTES, attributes);
        return CorneaClientFactory.getClient().request(request);
    }

    private void intoScheduleCommand(TimeOfDayCommand timeOfDayCommand, ScheduleCommandModel scheduleCommandModel) {
        scheduleCommandModel.setCommandMessageType(timeOfDayCommand.getMessageType());
        scheduleCommandModel.setDays(deserializeDays(timeOfDayCommand.getDays()));
        scheduleCommandModel.setTime(TimeOfDay.fromStringWithMode(timeOfDayCommand.getTime(), SunriseSunset.fromString(timeOfDayCommand.getMode()), timeOfDayCommand.getOffsetMinutes()));
        scheduleCommandModel.setId(timeOfDayCommand.getId());
        scheduleCommandModel.setAttributes(timeOfDayCommand.getAttributes());
    }

    private EnumSet<DayOfWeek> deserializeDays (Set<String> days) {
        Set<DayOfWeek> daysOfWeek = new HashSet<>();
        for (String thisDay : days) {
            daysOfWeek.add(DayOfWeek.from(thisDay));
        }

        return EnumSet.copyOf(daysOfWeek);
    }

    private Set<String> serializeDays(Set<DayOfWeek> daysOfWeek) {
        Set<String> days = new HashSet<>();
        for(DayOfWeek dow : daysOfWeek) {
            days.add(dow.name().substring(0, 3));
        }
        return days;
    }

    private Listener<ClientEvent> fireOnSuccessListener = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent o) {
            Callbacks callbacks = getListener();
            if (callbacks != null) {
                callbacks.onSuccess();
            }
        }
    });

    private Listener<Throwable> fireOnCorneaErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable cause) {
            Callbacks callbacks = getListener();
            if (callbacks != null) {
                callbacks.onSchedulerError(cause);
            }
        }
    });

    private void fireOnConfirmUpdateAllDays (ScheduleCommandModel scheduleCommandModel) {
        Callbacks callbacks = getListener();
        if (callbacks != null) {
            callbacks.onConfirmUpdateAllDays(scheduleCommandModel);
        }
    }

    private void fireOnConfirmDeleteAllDays (ScheduleCommandModel scheduleCommandModel) {
        Callbacks callbacks = getListener();
        if (callbacks != null) {
            callbacks.onConfirmDeleteAllDays(scheduleCommandModel);
        }
    }

    private void fireOnTimeOfDayCommandLoaded(ScheduleCommandModel scheduleCommandModel) {
        Callbacks  callbacks = getListener();
        if (callbacks != null) {
            callbacks.onTimeOfDayCommandLoaded(scheduleCommandModel);
        }
    }

    private interface ScheduleCommandLoadedListener {
        void onCommandLoaded(TimeOfDayCommand command, String schedulerAddress);
    }



    protected @Nullable SchedulerModel getModelFor(String deviceAddress) {
        for (SchedulerModel model : CorneaClientFactory.getStore(SchedulerModel.class).values()) {
            if (String.valueOf(deviceAddress).equals(model.getTarget())) {
                return model;
            }
        }

        return null;
    }

    protected boolean foundUsingCache(String deviceAddress, String commandId, ScheduleCommandLoadedListener listener) {
        SchedulerModel model = getModelFor(deviceAddress);
        if (model != null) {
            for (Map<String, Object> thisCommandData : model.getCommands().values()) {
                TimeOfDayCommand thisCommand = new TimeOfDayCommand(thisCommandData);
                if (thisCommand.getId().equalsIgnoreCase(commandId)) {
                    listener.onCommandLoaded(thisCommand, model.getAddress());
                    return true;
                }
            }
        }

        return false;
    }
}
