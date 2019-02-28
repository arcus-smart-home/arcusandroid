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
package arcus.app.seasonal.christmas.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class ChristmasModel implements Serializable {
    private static final long serialVersionUID = -29238982928391L;

    private Boolean isSetupComplete = Boolean.FALSE;
    private Boolean hasImageSaved = Boolean.FALSE;
    private String landingSpot;
    private HashSet<String> contactSensors;
    private HashSet<String> motionSensors;

    public String getLandingSpot(String defValue) {
        if (landingSpot == null) {
            landingSpot = defValue;
        }

        return landingSpot;
    }

    public void setLandingSpot(String landingSpot) {
        this.landingSpot = landingSpot;
    }

    public HashSet<String> getContactSensors() {
        if (contactSensors == null) {
            contactSensors = new HashSet<>();
        }

        return contactSensors;
    }

    public void setContactSensors(Set<String> contactSensors) {
        this.contactSensors = new HashSet<>(contactSensors);
    }

    public HashSet<String> getMotionSensors() {
        if (motionSensors == null) {
            motionSensors = new HashSet<>();
        }

        return motionSensors;
    }

    public void setMotionSensors(Set<String> motionSensors) {
        this.motionSensors = new HashSet<>(motionSensors);
    }

    public void setupIsComplete() {
        this.isSetupComplete = Boolean.TRUE;
    }

    public boolean isSetupComplete() {
        return this.isSetupComplete;
    }

    public void setHasImageSaved(boolean hasImageSaved) {
        this.hasImageSaved = hasImageSaved;
    }

    public boolean hasImageSaved() {
        return hasImageSaved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChristmasModel)) return false;

        ChristmasModel that = (ChristmasModel) o;

        if (isSetupComplete != null ? !isSetupComplete.equals(that.isSetupComplete) : that.isSetupComplete != null)
            return false;
        if (hasImageSaved != null ? !hasImageSaved.equals(that.hasImageSaved) : that.hasImageSaved != null)
            return false;
        if (landingSpot != null ? !landingSpot.equals(that.landingSpot) : that.landingSpot != null)
            return false;
        if (contactSensors != null ? !contactSensors.equals(that.contactSensors) : that.contactSensors != null)
            return false;
        return motionSensors != null ? motionSensors.equals(that.motionSensors) : that.motionSensors == null;
    }

    @Override
    public int hashCode() {
        int result = isSetupComplete != null ? isSetupComplete.hashCode() : 0;
        result = 31 * result + (hasImageSaved != null ? hasImageSaved.hashCode() : 0);
        result = 31 * result + (landingSpot != null ? landingSpot.hashCode() : 0);
        result = 31 * result + (contactSensors != null ? contactSensors.hashCode() : 0);
        result = 31 * result + (motionSensors != null ? motionSensors.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ChristmasModel{" +
                "isSetupComplete=" + isSetupComplete +
                ", hasImageSaved=" + hasImageSaved +
                ", landingSpot='" + landingSpot + '\'' +
                ", contactSensors=" + contactSensors +
                ", motionSensors=" + motionSensors +
                '}';
    }
}
