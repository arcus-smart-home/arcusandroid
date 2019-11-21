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
package arcus.app.subsystems.alarm.promonitoring.models;

import androidx.annotation.NonNull;

import arcus.app.ArcusApplication;
import arcus.app.R;

import java.text.SimpleDateFormat;
import java.util.Date;



public class HistoryListItemModel implements Comparable<HistoryListItemModel> {

    public enum HistoryListItemStyle {
        SECTION_HEADING,                // Section header
        HISTORY_ITEM,                   // Normal item
        HISTORY_DETAIL_DISCLOSURE,      // Item with chevron ('>') disclosure leading to detail
        NO_ACTIVITY                     // "You have no alarm activity" item
    }

    private HistoryListItemStyle style;
    private String id;
    private String title;
    private String subtitle;
    private Date timestamp;
    private String abstractString;
    private Integer abstractIcon;
    private String incidentAddress;

    public static class Builder {

        private final HistoryListItemModel model = new HistoryListItemModel();

        private Builder(HistoryListItemStyle style) {
            model.setStyle(style);
        }

        public static HistoryListItemModel noActivity() {
            return new Builder(HistoryListItemStyle.NO_ACTIVITY)
                    .withTitle(ArcusApplication.getContext().getString(R.string.alarm_no_activity))
                    .build();
        }

        public static Builder sectionHeader() {
            return new Builder(HistoryListItemStyle.SECTION_HEADING);
        }

        public static Builder historyLogItem() {
            return new Builder(HistoryListItemStyle.HISTORY_ITEM);
        }

        public static Builder historyIncidentItem() {
            return new Builder(HistoryListItemStyle.HISTORY_DETAIL_DISCLOSURE);
        }

        public Builder withId(String id) {
            model.setId(id);
            return this;
        }

        public Builder withTitle(String title) {
            model.setTitle(title);
            return this;
        }

        public Builder withSubtitle(String subtitle) {
            model.setSubtitle(subtitle);
            return this;
        }

        public Builder withTimestamp(@NonNull Date timestamp) {
            model.setTimestamp(timestamp);
            return this;
        }

        public Builder withAbstract(String abstractString) {
            model.setAbstractString(abstractString);
            return this;
        }

        public Builder withAbstractIcon(int abstractIcon) {
            model.setAbstractIcon(abstractIcon);
            return this;
        }

        public Builder withIncidentAddress(String incidentAddress) {
            model.incidentAddress = incidentAddress;
            return this;
        }

        public HistoryListItemModel build() {
            return model;
        }
    }

    public HistoryListItemStyle getStyle() {
        return style;
    }

    public void setStyle(HistoryListItemStyle style) {
        this.style = style;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getTimestampString() {
        return new SimpleDateFormat("h:mm a").format(timestamp);
    }

    public Date getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getAbstractString() {
        return abstractString;
    }

    public void setAbstractString(String abstractString) {
        this.abstractString = abstractString;
    }

    public Integer getAbstractIcon() {
        return abstractIcon;
    }

    public void setAbstractIcon(Integer abstractIcon) {
        this.abstractIcon = abstractIcon;
    }

    public String getIncidentAddress() {
        return incidentAddress;
    }

    public void setIncidentAddress(String incidentAddress) {
        this.incidentAddress = incidentAddress;
    }

    @Override
    public String toString() {
        return "HistoryListItemModel{" +
                "style=" + style +
                ", id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", timestamp=" + timestamp +
                ", abstractString='" + abstractString + '\'' +
                ", abstractIcon=" + abstractIcon +
                ", incidentAddress='" + incidentAddress + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HistoryListItemModel)) return false;

        HistoryListItemModel model = (HistoryListItemModel) o;

        if (style != model.style) return false;
        if (id != null ? !id.equals(model.id) : model.id != null) return false;
        if (title != null ? !title.equals(model.title) : model.title != null) return false;
        if (subtitle != null ? !subtitle.equals(model.subtitle) : model.subtitle != null)
            return false;
        if (timestamp != null ? !timestamp.equals(model.timestamp) : model.timestamp != null)
            return false;
        if (abstractString != null ? !abstractString.equals(model.abstractString) : model.abstractString != null)
            return false;
        if (abstractIcon != null ? !abstractIcon.equals(model.abstractIcon) : model.abstractIcon != null)
            return false;
        return incidentAddress != null ? incidentAddress.equals(model.incidentAddress) : model.incidentAddress == null;

    }

    @Override
    public int hashCode() {
        int result = style != null ? style.hashCode() : 0;
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (subtitle != null ? subtitle.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (abstractString != null ? abstractString.hashCode() : 0);
        result = 31 * result + (abstractIcon != null ? abstractIcon.hashCode() : 0);
        result = 31 * result + (incidentAddress != null ? incidentAddress.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(HistoryListItemModel another) {
        if (another == null || another.timestamp == null) {
            return -1;
        }

        else if (this.timestamp == null) {
            return 1;
        }

        else {
            return this.timestamp.compareTo(another.timestamp);
        }
    }

}
