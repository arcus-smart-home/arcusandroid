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
package arcus.cornea.subsystem.doorsnlocks.model;

public class StateSummary {

    private int lockUnlockedCount;
    private int garageOpenCount;
    private int doorOpenCount;
    private int petUnlockedCount;

    private int lockCount;
    private int garageCount;
    private int doorCount;
    private int petCount;

    public int getLockUnlockedCount() {
        return lockUnlockedCount;
    }

    public void setLockUnlockedCount(int lockUnlockedCount) {
        this.lockUnlockedCount = lockUnlockedCount;
    }

    public int getGarageOpenCount() {
        return garageOpenCount;
    }

    public void setGarageOpenCount(int garageOpenCount) {
        this.garageOpenCount = garageOpenCount;
    }

    public int getDoorOpenCount() {
        return doorOpenCount;
    }

    public void setDoorOpenCount(int doorOpenCount) {
        this.doorOpenCount = doorOpenCount;
    }

    public int getPetUnlockedCount() {
        return petUnlockedCount;
    }

    public void setPetUnlockedCount(int petUnlockedCount) {
        this.petUnlockedCount = petUnlockedCount;
    }

    public int getLockCount() {
        return lockCount;
    }

    public void setLockCount(int lockCount) {
        this.lockCount = lockCount;
    }

    public int getGarageCount() {
        return garageCount;
    }

    public void setGarageCount(int garageCount) {
        this.garageCount = garageCount;
    }

    public int getDoorCount() {
        return doorCount;
    }

    public void setDoorCount(int doorCount) {
        this.doorCount = doorCount;
    }

    public int getPetCount() {
        return petCount;
    }

    public void setPetCount(int petCount) {
        this.petCount = petCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StateSummary that = (StateSummary) o;

        if (lockUnlockedCount != that.lockUnlockedCount)
            return false;
        if (garageOpenCount != that.garageOpenCount)
            return false;
        if (lockCount != that.lockCount)
            return false;
        if (garageCount != that.garageCount)
            return false;
        if (doorCount != that.doorCount)
            return false;
        if (petCount != that.petCount)
            return false;

        return !(doorOpenCount != that.doorOpenCount);

    }

    @Override
    public int hashCode() {
        int result = lockUnlockedCount;
        result = 31 * result + garageOpenCount;
        result = 31 * result + doorOpenCount;
        result = 31 * result + lockCount;
        result = 31 * result + garageCount;
        result = 31 * result + doorCount;
        result = 31 * result + petCount;

        return result;
    }

    @Override
    public String toString() {
        return "DeviceTypeSummary{" +
                "lockUnlockedCount='" + Integer.toString(lockUnlockedCount) + '\'' +
                ", garageOpenCount='" + Integer.toString(garageOpenCount)+ '\'' +
                ", doorOpenCount='" + Integer.toString(doorOpenCount)+ '\'' +
                ", lockCount=" + Integer.toString(lockCount) +
                ", garageCount=" + Integer.toString(garageCount) +
                ", doorCount=" + Integer.toString(doorCount) +
                ", petCount=" + Integer.toString(petCount) +
                '}';
    }
}
