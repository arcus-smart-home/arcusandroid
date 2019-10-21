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
package arcus.cornea.subsystem.safety.model;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.util.Date;

public class HistoryEvent {

    private static final String[] DURATIONS = { "h", "m", "s"};

    private final String triggeredBy;
    private final String disarmedBy;
    private final String eventTitle;
    private final Date triggeredAt;
    private final Date clearedAt;
    private final String duration;

    public HistoryEvent(String triggeredBy, String disarmedBy, String eventTitle, Date triggeredAt,
                 Date clearedAt) {

        this.triggeredBy = triggeredBy;
        this.disarmedBy = disarmedBy;
        this.eventTitle = eventTitle;
        this.triggeredAt = triggeredAt;
        this.clearedAt = clearedAt;
        this.duration = formatDuration();
    }

    public String getTriggeredBy() { return triggeredBy; }
    public String getDisarmedBy() { return disarmedBy; }
    public String getEventTitle() { return eventTitle; }
    public Date getTriggeredAt() { return triggeredAt; }
    public Date getClearedAt() { return clearedAt; }
    public String getDuration() { return duration; }

    private String formatDuration() {
        Date end = clearedAt == null ? new Date() : clearedAt;
        long durationInMs = end.getTime() - triggeredAt.getTime();
        String formatted = DurationFormatUtils.formatDurationHMS(durationInMs);
        return reformatDuration(formatted);
    }

    private String reformatDuration(String formatted) {
        String[] parts = formatted.split(":");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < parts.length; i++) {
            double val = Double.parseDouble(parts[i]);
            if(val > 0) {
                if(sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append(Math.round(val)).append(DURATIONS[i]);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoryEvent that = (HistoryEvent) o;

        if (triggeredBy != null ? !triggeredBy.equals(that.triggeredBy) : that.triggeredBy != null)
            return false;
        if (disarmedBy != null ? !disarmedBy.equals(that.disarmedBy) : that.disarmedBy != null)
            return false;
        if (eventTitle != null ? !eventTitle.equals(that.eventTitle) : that.eventTitle != null)
            return false;
        if (triggeredAt != null ? !triggeredAt.equals(that.triggeredAt) : that.triggeredAt != null)
            return false;
        if (clearedAt != null ? !clearedAt.equals(that.clearedAt) : that.clearedAt != null)
            return false;
        return !(duration != null ? !duration.equals(that.duration) : that.duration != null);

    }

    @Override
    public int hashCode() {
        int result = triggeredBy != null ? triggeredBy.hashCode() : 0;
        result = 31 * result + (disarmedBy != null ? disarmedBy.hashCode() : 0);
        result = 31 * result + (eventTitle != null ? eventTitle.hashCode() : 0);
        result = 31 * result + (triggeredAt != null ? triggeredAt.hashCode() : 0);
        result = 31 * result + (clearedAt != null ? clearedAt.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SafetyHistoryEvent{" +
                "triggeredBy='" + triggeredBy + '\'' +
                ", disarmedBy='" + disarmedBy + '\'' +
                ", eventTitle='" + eventTitle + '\'' +
                ", triggeredAt=" + triggeredAt +
                ", clearedAt=" + clearedAt +
                ", duration='" + duration + '\'' +
                '}';
    }
}
