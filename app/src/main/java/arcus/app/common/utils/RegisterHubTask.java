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
package arcus.app.common.utils;

import androidx.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import com.iris.client.IrisClientFactory;
import com.iris.client.capability.Place;
import com.iris.client.capability.Place.RegisterHubV2Response;
import com.iris.client.event.Listener;
import com.iris.client.model.HubModel;
import com.iris.client.model.PlaceModel;
import arcus.app.ArcusApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * Usage:
 *
    RegisterHubTask r = new RegisterHubTask() {
        @Override
        public void onEvent(final Result<Boolean> aBoolean) {
            if (aBoolean.isError()) {
                switch (aBoolean.getError().getMessage()) {
                    case Errors.Hub.ALREADY_REGISTERED:
                }
            }
            else {
                // if false == exceeded retry limit, if true found hub.
            }
        }
    };
    r.setPollingIntervalTimeoutMS(2500);
    r.registerHub();
 *
 *
 */
// TODO: Change listener to be added instead of forcing impl?
public abstract class RegisterHubTask {
    private static final Logger logger = LoggerFactory.getLogger(RegisterHubTask.class);
    private Timer hubRegistrationPollingTimer;
    private int retryNumber;
    private int pollingIntervalMS = 2000;
    private boolean isRunning = false;
    private boolean isDone = false;
    private boolean isCancelled = false;
    private boolean inFlight = false;
    private PlaceModel place;

    public RegisterHubTask() {
        CorneaClientFactory.getStore(HubModel.class);
    }

    public void setPollingIntervalMS(int pollingIntervalMS) {
        int MAX_POLLING_INTERVAL_MS = 5000;
        if (pollingIntervalMS > 1500 && pollingIntervalMS < MAX_POLLING_INTERVAL_MS) {
            this.pollingIntervalMS = pollingIntervalMS;
        }
        else {
            logger.trace("Using defalut value [{}] for pollingIntervalMS, invalid number passed in [{}]", this.pollingIntervalMS, pollingIntervalMS);
        }
    }

    public void cancel() {
        cancelTimers();
    }

    private void cancelTimers() {
        if (hubRegistrationPollingTimer == null) {
            return;
        }

        hubRegistrationPollingTimer.cancel();
        int timersPurged = hubRegistrationPollingTimer.purge();
        this.isCancelled = true;
        this.isDone = true;
        this.isRunning = false;

        logger.trace("Cancel was called, Timers purged {}", timersPurged);
    }

    public void registerHub() {
        if (isRunning) {
            logger.trace("Not processing register hub request. Currently running.");
            return;
        }
        else if (isDone || isCancelled) {
            // reusing previously created instance that finished. Ensure everything is reset.
            logger.trace("Reusing {}, Clearing & Restting.", getClass().getSimpleName());
            cancel();
            resetState();
        }

        place = ArcusApplication.getRegistrationContext().getPlaceModel();
        if (place == null) {
            onError(new RuntimeException(Errors.Process.NULL_MODEL_FOUND));
            return;
        }

        resetState();
        doRegisterHub();
    }

    private void resetState() {
        inFlight = false;
        isCancelled = false;
        isDone = false;
    }

    private void doRegisterHub() {
        isRunning = true;
        retryNumber = 0;
        hubRegistrationPollingTimer = new Timer("HUB_REG_TIMER");
        RegisterHubTimerTask task = new RegisterHubTimerTask();
        hubRegistrationPollingTimer.schedule(task, 0, pollingIntervalMS);
    }

    public void onEvent(@NonNull Place.RegisterHubV2Response response) {

    }

    public void onError(@NonNull Throwable throwable) {

    }

    private class RegisterHubTimerTask extends TimerTask {

        RegisterHubTimerTask() {
        }

        @Override
        public boolean cancel() {
            return super.cancel();
        }

        @Override
        public void run() {
            if (inFlight) { // Still waiting on a response, don't make another request until we receive a response.
                logger.trace("InFlight - NOT Sending register hub request.");
                return;
            }

            if (isCancelled) {
                logger.trace("isCancelled found, emitting message to indicate.");
                emitMessage(new RuntimeException(Errors.Process.CANCELLED));
                return;
            }

            if (isDone) {
                logger.trace("isDone found, emitting message to indicate.");
                cancel();
                return;
            }

            inFlight = true;
            retryNumber++;
            try {
                // If client closes during this time will cause #221 Fabric
                place.registerHubV2(ArcusApplication.getRegistrationContext().getHubID())
                        .onSuccess(Listeners.runOnUiThread(onSuccess))
                        .onFailure(Listeners.runOnUiThread(onError));
            }
            catch (Exception ex) {
                emitMessage(ex);
            }
        }


        private final Listener<Place.RegisterHubV2Response> onSuccess = new Listener<Place.RegisterHubV2Response>() {
            @Override
            public void onEvent(Place.RegisterHubV2Response response) {
                inFlight = false;
                switch(response.getState()) {
                    case RegisterHubV2Response.STATE_REGISTERED:
                        IrisClientFactory.getModelCache().addOrUpdate(response.getHub());
                    default:
                        //always do this to update the UI
                        RegisterHubTask.this.onEvent(response);
                        break;
                }
            }
        };

        private final Listener<Throwable> onError = new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                emitMessage(throwable);
            }
        };
        
        private void emitMessage(Throwable result) {
            if (isCancelled) {
                logger.debug("Not emitting message, Task was cancelled. Message.", result);
                return;
            }

            RegisterHubTask.this.onError(result);
            cancel();
        }
    }
}
