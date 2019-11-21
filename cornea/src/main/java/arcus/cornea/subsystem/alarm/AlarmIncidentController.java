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
package arcus.cornea.subsystem.alarm;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.provider.AlarmIncidentProvider;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.Place;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.model.AlarmIncidentModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.PlaceModel;

import org.apache.commons.lang3.StringUtils;


public class AlarmIncidentController extends BaseSubsystemController<AlarmIncidentController.Callback> {

    private final static AlarmIncidentController instance = new AlarmIncidentController();

    public interface Callback {
        void onError(Throwable t);
        void onIncidentUpdated(PlaceModel place, AlarmIncidentModel incident, int prealertSecRemaining);
    }

    private AlarmIncidentController() {
        super(AlarmSubsystem.NAMESPACE);
        init();
    }

    public static AlarmIncidentController getInstance() {
        return instance;
    }

    public void requestUpdate(String incidentAddress) {
        if (!StringUtils.isEmpty(incidentAddress)) {

            AlarmIncidentProvider.getInstance().getIncident(incidentAddress).onSuccess(new Listener<AlarmIncidentModel>() {
                @Override
                public void onEvent(AlarmIncidentModel incidentModel) {
                    fetchPlaceThenHistory(incidentModel);
                }
            }).onFailure(failureListener);
        }
    }

    public ClientFuture<HistoryLogEntries> getHistoryForIncident(@NonNull AlarmIncidentModel incident) {
        return HistoryLogEntries.forAlarmIncident(incident, 100, null);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        AlarmSubsystem alarmSubsystem = (AlarmSubsystem) getModel();
        requestUpdate(alarmSubsystem.getCurrentIncident());
    }

    private String getPlaceAddress(String id) {
        return "SERV:" + Place.NAMESPACE + ":" + id;
    }

    private void fetchPlaceThenHistory(final AlarmIncidentModel incident) {
        PlaceModelProvider.getPlace(getPlaceAddress(incident.getPlaceId())).onSuccess(new Listener<PlaceModel>() {
            @Override
            public void onEvent(final PlaceModel place) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Callback c = getCallback();
                        if (c != null) {
                            final int prealertSeconds = AlarmIncidentPrealertController.getPrealertRemainingSeconds(incident);

                            // Notify callback of change to
                            c.onIncidentUpdated(place, incident, prealertSeconds);

                            if (prealertSeconds > 0) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        requestUpdate(incident.getAddress());
                                    }
                                }, 1000);
                            }
                        }
                    }
                });
            }
        }).onFailure(failureListener);
    }

    private Listener<Throwable> failureListener = new Listener<Throwable>() {
        @Override
        public void onEvent(final Throwable throwable) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Callback c = getCallback();
                    if (c != null) {
                        c.onError(throwable);
                    }
                }
            });
        }
    };

}
