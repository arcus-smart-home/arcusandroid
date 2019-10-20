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

public class SensorSummary {
    private final String smokeStatus;
    private final String coStatus;
    private final String waterLeakStatus;

    public SensorSummary(String smokeStatus, String coStatus, String waterLeakStatus) {
        this.smokeStatus = smokeStatus;
        this.coStatus = coStatus;
        this.waterLeakStatus = waterLeakStatus;
    }

    /**
     * @return
     *      the current status of any smoke sensors within the subsystem
     */
    public String getSmokeStatus() { return smokeStatus; }

    /**
     * @return
     *      the current status of any CO sensors within the subsystem
     */
    public String getCoStatus() { return coStatus; }

    /**
     * @return
     *      the current status of any water leak sensors within the subsystem
     */
    public String getWaterLeakStatus() { return waterLeakStatus; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SensorSummary that = (SensorSummary) o;

        if (smokeStatus != null ? !smokeStatus.equals(that.smokeStatus) : that.smokeStatus != null)
            return false;
        if (coStatus != null ? !coStatus.equals(that.coStatus) : that.coStatus != null)
            return false;
        return !(waterLeakStatus != null ? !waterLeakStatus.equals(that.waterLeakStatus) : that.waterLeakStatus != null);

    }

    @Override
    public int hashCode() {
        int result = smokeStatus != null ? smokeStatus.hashCode() : 0;
        result = 31 * result + (coStatus != null ? coStatus.hashCode() : 0);
        result = 31 * result + (waterLeakStatus != null ? waterLeakStatus.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SensorSummary{" +
                "smokeStatus='" + smokeStatus + '\'' +
                ", coStatus='" + coStatus + '\'' +
                ", waterLeakStatus='" + waterLeakStatus + '\'' +
                '}';
    }
}
