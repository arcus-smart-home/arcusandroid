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
package arcus.cornea.dto;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Function;
import arcus.cornea.controller.SubscriptionController;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.AlarmIncident;
import com.iris.client.capability.Device;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.capability.Subsystem;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.AlarmIncidentModel;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.model.SubsystemModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HistoryLogEntries {
    private List<HistoryLog> entries;
    private String nextToken;

    public static HistoryLogEntries empty() {
        return new HistoryLogEntries();
    }

    protected HistoryLogEntries() {
        entries = new ArrayList<>();
    }

    protected HistoryLogEntries(@Nullable List<Map<String, Object>> response, @Nullable String nextToken) {
        this();

        if (response != null && !response.isEmpty()) {
            for (Map<String, Object> item : response) {
                entries.add(new HistoryLog(item));
            }
        }
        this.nextToken = nextToken;
    }

    public HistoryLogEntries(@NonNull AlarmIncidentModel.ListHistoryEntriesResponse response) {
        this(response.getResults(), response.getNextToken());
    }

    public HistoryLogEntries(@NonNull Place.ListDashboardEntriesResponse response) {
        this(response.getResults(), response.getNextToken());
    }

    public HistoryLogEntries(@NonNull Place.ListHistoryEntriesResponse response) {
        this(response.getResults(), response.getNextToken());
    }

    public HistoryLogEntries(@NonNull Device.ListHistoryEntriesResponse response) {
        this(response.getResults(), response.getNextToken());
    }

    public HistoryLogEntries(@NonNull Person.ListHistoryEntriesResponse response) {
        this(response.getResults(), response.getNextToken());
    }

    public HistoryLogEntries(@NonNull Subsystem.ListHistoryEntriesResponse response) {
        this(response.getResults(), response.getNextToken());
    }

    @NonNull public List<HistoryLog> getEntries() {
        return Collections.unmodifiableList(getEntriesForSubscriptionLevel());
    }

    @Nullable public String getNextToken() {
        return this.nextToken;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryLogEntries that = (HistoryLogEntries) o;

        if (!entries.equals(that.entries)) {
            return false;
        }
        return !(nextToken != null ? !nextToken.equals(that.nextToken) : that.nextToken != null);

    }

    @Override public int hashCode() {
        int result = entries.hashCode();
        result = 31 * result + (nextToken != null ? nextToken.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "HistoryLogEntries{" +
              "entries=" + entries +
              ", nextToken='" + nextToken + '\'' +
              '}';
    }

    public static ClientFuture<HistoryLogEntries> forAlarmIncident(@NonNull AlarmIncidentModel model, @Nullable Integer limit, @Nullable String token) {
        try {
            return Futures.transform(model.listHistoryEntries(limit, token), new Function<AlarmIncident.ListHistoryEntriesResponse, HistoryLogEntries>() {
                @Override
                public HistoryLogEntries apply(AlarmIncident.ListHistoryEntriesResponse in) {
                    return (in != null) ? new HistoryLogEntries(in) : HistoryLogEntries.empty();
                }
            });
        }
        catch (Exception e) {
            return Futures.failedFuture(e);
        }
    }

    public static ClientFuture<HistoryLogEntries> forSubsytemModel(@NonNull SubsystemModel model, @Nullable Integer limit, @Nullable String token) {
        try {
            return Futures.transform(model.listHistoryEntries(limit, token, true), new Function<Subsystem.ListHistoryEntriesResponse, HistoryLogEntries>() {
                @Override
                public HistoryLogEntries apply(Subsystem.ListHistoryEntriesResponse in) {
                    return (in != null) ? new HistoryLogEntries(in) : HistoryLogEntries.empty();
                }
            });
        }
        catch (Exception e) {
            return Futures.failedFuture(e);
        }
    }

    public static ClientFuture<HistoryLogEntries> forPersonModel(@NonNull PersonModel model, @Nullable Integer limit, @Nullable String token) {
        try {
            return Futures.transform(model.listHistoryEntries(limit, token), new Function<Person.ListHistoryEntriesResponse, HistoryLogEntries>() {
                @Override public HistoryLogEntries apply(@Nullable Person.ListHistoryEntriesResponse in) {
                    return (in != null) ? new HistoryLogEntries(in) : HistoryLogEntries.empty();
                }
            });
        }
        catch (Exception ex) {
            return Futures.failedFuture(ex);
        }
    }

    public static ClientFuture<HistoryLogEntries> forDeviceModel(@NonNull DeviceModel model, @Nullable Integer limit, @Nullable String token) {
        try {
            return Futures.transform(model.listHistoryEntries(limit, token), new Function<Device.ListHistoryEntriesResponse, HistoryLogEntries>() {
                @Override
                public HistoryLogEntries apply(@Nullable Device.ListHistoryEntriesResponse in) {
                    return (in != null) ? new HistoryLogEntries(in) : HistoryLogEntries.empty();
                }
            });
        }
        catch (Exception ex) {
            return Futures.failedFuture(ex);
        }
    }

    public static ClientFuture<HistoryLogEntries> forDashboard(@NonNull PlaceModel model, @Nullable Integer limit, @Nullable String token) {
        try {
            return Futures.transform(model.listDashboardEntries(limit, token), new Function<Place.ListDashboardEntriesResponse, HistoryLogEntries>() {
                @Override public HistoryLogEntries apply(@Nullable Place.ListDashboardEntriesResponse in) {
                    return (in != null) ? new HistoryLogEntries(in) : HistoryLogEntries.empty();
                }
            });
        }
        catch (Exception ex) {
            return Futures.failedFuture(ex);
        }
    }

    public static ClientFuture<HistoryLogEntries> forPlaceModel(@NonNull PlaceModel model, @Nullable Integer limit, @Nullable String token) {
        try {
            return Futures.transform(model.listHistoryEntries(limit, token), new Function<Place.ListHistoryEntriesResponse, HistoryLogEntries>() {
                @Override public HistoryLogEntries apply(@Nullable Place.ListHistoryEntriesResponse in) {
                    return (in != null) ? new HistoryLogEntries(in) : HistoryLogEntries.empty();
                }
            });
        }
        catch (Exception ex) {
            return Futures.failedFuture(ex);
        }
    }

    private List<HistoryLog> getEntriesForSubscriptionLevel () {

        if (SubscriptionController.isPremiumOrPro()) {
            return entries;
        }

        List<HistoryLog> filteredLog = new LinkedList<>();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        for (HistoryLog thisEntry : entries) {
            if (thisEntry.getTimestamp().after(yesterday.getTime())) {
                filteredLog.add(thisEntry);
            }
        }

        return filteredLog;
    }
}
