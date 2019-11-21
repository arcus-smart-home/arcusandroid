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

import com.google.common.collect.ComparisonChain;
import com.iris.client.bean.HistoryLog;



public class ProMonitoringHistoryItem implements Comparable<ProMonitoringHistoryItem> {
    HistoryLog log;
    public String subText;
    public boolean isInfoItem, isHeader;

    @SuppressWarnings("ConstantConditions") public ProMonitoringHistoryItem() {
    }

    public static ProMonitoringHistoryItem headerModelType() {
        ProMonitoringHistoryItem m = new ProMonitoringHistoryItem();
        m.isHeader = true;

        return m;
    }

    public static ProMonitoringHistoryItem infoModelType() {
        ProMonitoringHistoryItem m = new ProMonitoringHistoryItem();
        m.isInfoItem = true;

        return m;
    }

    public int getViewType() {
        if (isHeader) {
            return 1;
        }

        return isInfoItem ? 2 : 3;
    }

    public HistoryLog getLog() {
        return log;
    }

    public void setLog(HistoryLog log) {
        this.log = log;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public void setHeader(boolean header) {
        isHeader = header;
    }

    public boolean isInfoItem() {
        return isInfoItem;
    }

    public void setInfoItem(boolean infoItem) {
        isInfoItem = infoItem;
    }

    @Override public int compareTo(@NonNull ProMonitoringHistoryItem another) {
        return ComparisonChain
                .start() // Sort by view type fist, then by text content
                .compare(getViewType(), another.getViewType())
                .compare(String.valueOf(log.getTimestamp()), String.valueOf(another.log.getTimestamp()))
                .compare(String.valueOf(subText), String.valueOf(another.subText))
                .result();
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProMonitoringHistoryItem that = (ProMonitoringHistoryItem) o;
        if(log.getTimestamp() != that.log.getTimestamp()) {
            return false;
        }
        return (isHeader == that.isHeader);

    }

    @Override public int hashCode() {
        int result = log.getTimestamp() != null ? log.getTimestamp().hashCode() : 0;
        result = 31 * result + (isHeader ? 1 : 0);
        return result;
    }

}
