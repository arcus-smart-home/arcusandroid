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
package arcus.cornea.subsystem.model;

import androidx.annotation.NonNull;

import arcus.cornea.utils.DateUtils;

import java.util.Date;

public class CareHistoryModel implements Comparable<CareHistoryModel> {
    private String date;
    private String shortDate;
    private String title;
    private String subTitle;
    private String address;
    private Long timestamp;
    private boolean isHeaderRow;
    private int calendarDayOfYear;

    public CareHistoryModel() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isHeaderRow() {
        return isHeaderRow;
    }

    public void setIsHeaderRow(boolean isHeaderRow) {
        this.isHeaderRow = isHeaderRow;
    }

    public int getCalendarDayOfYear() {
        return calendarDayOfYear;
    }

    public void setCalendarDayOfYear(int calendarDayOfYear) {
        this.calendarDayOfYear = calendarDayOfYear;
    }

    public String getShortDate() {
        return shortDate;
    }

    public void setShortDate(String shortDate) {
        this.shortDate = shortDate;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override public int compareTo(@NonNull CareHistoryModel another) {
        return another.timestamp.compareTo(this.timestamp);
    }

    public boolean isToday() {
        return DateUtils.Recency.TODAY.equals(DateUtils.getRecency(new Date(this.timestamp)));
    }

    public CareHistoryModel headerCopy() {
        CareHistoryModel copy = new CareHistoryModel();
        copy.title = isToday() ? "Today" : this.shortDate;
        copy.isHeaderRow = true;
        copy.timestamp = this.timestamp;
        return copy;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CareHistoryModel model = (CareHistoryModel) o;

        if (isHeaderRow != model.isHeaderRow) {
            return false;
        }
        if (calendarDayOfYear != model.calendarDayOfYear) {
            return false;
        }
        if (date != null ? !date.equals(model.date) : model.date != null) {
            return false;
        }
        if (shortDate != null ? !shortDate.equals(model.shortDate) : model.shortDate != null) {
            return false;
        }
        if (title != null ? !title.equals(model.title) : model.title != null) {
            return false;
        }
        if (subTitle != null ? !subTitle.equals(model.subTitle) : model.subTitle != null) {
            return false;
        }
        if (address != null ? !address.equals(model.address) : model.address != null) {
            return false;
        }
        return !(timestamp != null ? !timestamp.equals(model.timestamp) : model.timestamp != null);

    }

    @Override public int hashCode() {
        int result = date != null ? date.hashCode() : 0;
        result = 31 * result + (shortDate != null ? shortDate.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (subTitle != null ? subTitle.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (isHeaderRow ? 1 : 0);
        result = 31 * result + calendarDayOfYear;
        return result;
    }

    @Override public String toString() {
        return "CareHistoryModel{" +
              "date='" + date + '\'' +
              ", shortDate='" + shortDate + '\'' +
              ", title='" + title + '\'' +
              ", subTitle='" + subTitle + '\'' +
              ", address='" + address + '\'' +
              ", timestamp=" + timestamp +
              ", isHeaderRow=" + isHeaderRow +
              ", calendarDayOfYear=" + calendarDayOfYear +
              '}';
    }
}
