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



public class InactiveAlarmStatusModel extends AlarmStatusModel {

    private String notAvailableCopy;

    public InactiveAlarmStatusModel(int iconResourceId, String alarmTypeString) {
        super(iconResourceId, alarmTypeString);
    }

    public String getNotAvailableCopy() {
        return notAvailableCopy;
    }

    public void setNotAvailableCopy(String notAvailableCopy) {
        this.notAvailableCopy = notAvailableCopy;
    }

    @Override
    public String toString() {
        return "InactiveAlarmStatusModel{" +
                "notAvailableCopy='" + notAvailableCopy + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InactiveAlarmStatusModel)) return false;
        if (!super.equals(o)) return false;

        InactiveAlarmStatusModel that = (InactiveAlarmStatusModel) o;

        return notAvailableCopy.equals(that.notAvailableCopy);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + notAvailableCopy.hashCode();
        return result;
    }
}
