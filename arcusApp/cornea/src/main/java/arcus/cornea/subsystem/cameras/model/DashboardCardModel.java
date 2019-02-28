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
package arcus.cornea.subsystem.cameras.model;

import java.util.Date;
import java.util.List;

public class DashboardCardModel {
    private List<DashboardCameraModel> cameras;
    private Date lastRecording;

    public List<DashboardCameraModel> getCameras() {
        return cameras;
    }

    public void setCameras(List<DashboardCameraModel> cameras) {
        this.cameras = cameras;
    }

    public Date getLastRecording() {
        return lastRecording;
    }

    public void setLastRecording(Date lastRecording) {
        this.lastRecording = lastRecording;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardCardModel that = (DashboardCardModel) o;

        if (cameras != null ? !cameras.equals(that.cameras) : that.cameras != null) return false;
        return !(lastRecording != null ? !lastRecording.equals(that.lastRecording) : that.lastRecording != null);

    }

    @Override
    public int hashCode() {
        int result = cameras != null ? cameras.hashCode() : 0;
        result = 31 * result + (lastRecording != null ? lastRecording.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "DashboardCardModel{" +
                "cameras=" + cameras +
                ", lastRecording=" + lastRecording +
                '}';
    }
}
