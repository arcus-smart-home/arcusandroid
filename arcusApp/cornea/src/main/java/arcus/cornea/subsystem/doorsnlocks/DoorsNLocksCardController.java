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
package arcus.cornea.subsystem.doorsnlocks;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.doorsnlocks.model.StateSummary;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.DoorsNLocksSubsystem;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.Set;

public final class DoorsNLocksCardController extends BaseSubsystemController<DoorsNLocksCardController.Callback> {

    public interface Callback {
        /**
         * Invoked when the place has no applicable devices (door locks, garage doors or contact sensors
         * flagged as being on a door).
         */
        void showLearnMore();

        /**
         * Invoked when the place has devices required for the subsystem, but they are not all in a "safe"
         * state (closed and locked in this case).
         *
         * @param summary
         */
        void showSummary(StateSummary summary);

        /**
         * Shown when the place has devices required for the subsystem and they are all in a "safe"
         * state.  The string content will be based on the devices available within the home.
         *
         * @param summary
         */
        void showTextualSummary(String summary);

        void updateLockOfflineState(boolean bOfflineLocks);
        void updateGarageOfflineState(boolean bOfflineGarage);
        void updateDoorOfflineState(boolean bOfflineDoor);
    }

    private static final Set<String> UPDATE_ON = ImmutableSet.of(
            DoorsNLocksSubsystem.ATTR_CONTACTSENSORDEVICES,
            DoorsNLocksSubsystem.ATTR_LOCKDEVICES,
            DoorsNLocksSubsystem.ATTR_MOTORIZEDDOORDEVICES,
            DoorsNLocksSubsystem.ATTR_OFFLINECONTACTSENSORS,
            DoorsNLocksSubsystem.ATTR_OFFLINELOCKS,
            DoorsNLocksSubsystem.ATTR_OFFLINEMOTORIZEDDOORS,
            DoorsNLocksSubsystem.ATTR_OPENCONTACTSENSORS,
            DoorsNLocksSubsystem.ATTR_OPENMOTORIZEDDOORS,
            DoorsNLocksSubsystem.ATTR_UNLOCKEDLOCKS,
            DoorsNLocksSubsystem.ATTR_PETDOORDEVICES,
            DoorsNLocksSubsystem.ATTR_UNLOCKEDPETDOORS,
            DoorsNLocksSubsystem.ATTR_AUTOPETDOORS,
            DoorsNLocksSubsystem.ATTR_OFFLINEPETDOORS
    );

    private static final DoorsNLocksCardController instance;

    static {
        instance = new DoorsNLocksCardController();
        instance.init();
    }

    public static DoorsNLocksCardController instance() {
        return instance;
    }

    DoorsNLocksCardController() {
        this(SubsystemController.instance().getSubsystemModel(DoorsNLocksSubsystem.NAMESPACE));
    }

    DoorsNLocksCardController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        Set<String> intersection = Sets.intersection(changes, UPDATE_ON);
        if(intersection.isEmpty()) {
            return;
        }
        updateView();
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            return;
        }

        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();

        if(!dnl.getAvailable()) {
            callback.showLearnMore();
        } /*else if(shouldShowTextualSummary(dnl)) {
            callback.showTextualSummary(generateTextualSummary(dnl));
        }*/ else {
            callback.showSummary(getSummary(dnl));
        }
        callback.updateLockOfflineState(!dnl.getOfflineLocks().isEmpty());
        callback.updateGarageOfflineState(!dnl.getOfflineMotorizedDoors().isEmpty());
        callback.updateDoorOfflineState(!dnl.getOfflineContactSensors().isEmpty());
    }

    private StateSummary getSummary(DoorsNLocksSubsystem dnl) {
        StateSummary summary = new StateSummary();
        summary.setGarageOpenCount(dnl.getOpenMotorizedDoors().size());
        summary.setDoorOpenCount(dnl.getOpenContactSensors().size());
        //unlocked doors should include petdoors that are in auto mode or unlocked.
        summary.setLockUnlockedCount(dnl.getUnlockedLocks().size() + dnl.getAutoPetDoors().size() + dnl.getUnlockedPetDoors().size());
        summary.setPetUnlockedCount(generatePetSummary(dnl));

        summary.setLockCount(generateLockCount(dnl));
        summary.setGarageCount(generateGarageCount(dnl));
        summary.setDoorCount(generateDoorCount(dnl));
        summary.setPetCount(generatePetCount(dnl));
        return summary;
    }

    private int generatePetSummary (DoorsNLocksSubsystem dnl) {
        int differentStates = 0;

        int doorCount = dnl.getPetDoorDevices().size();
        int lockedDoorCount = doorCount - dnl.getOfflinePetDoors().size() - dnl.getAutoPetDoors().size() - dnl.getUnlockedPetDoors().size();

        if (lockedDoorCount > 0) differentStates++;
        if (dnl.getOfflinePetDoors().size() > 0) differentStates++;
        if (dnl.getAutoPetDoors().size() > 0) differentStates++;
        if (dnl.getUnlockedPetDoors().size() > 0) differentStates++;

        // Special case: User has multiple doors and they're in different states. Just show number of doors
        if (differentStates > 1) {
            return doorCount;
        }

        if (dnl.getOfflinePetDoors().size() > 0) {
            return doorCount;
        }

        if (dnl.getAutoPetDoors().size() > 0) {
            return doorCount;
        }

        if (dnl.getUnlockedPetDoors().size() > 0) {
            return doorCount;
        }

        return lockedDoorCount;
    }

    private String generateSummary(Set<String> open, String openTxt) {
        if(!open.isEmpty()) {
            return open.size() + " " + openTxt;
        }
        else {
            return "0 " + openTxt;
        }
    }

    private int generateLockCount(DoorsNLocksSubsystem dnl) {
        return dnl.getLockDevices().size() + dnl.getOfflineLocks().size() + dnl.getUnlockedLocks().size();
    }

    private int generateGarageCount(DoorsNLocksSubsystem dnl) {
        return dnl.getMotorizedDoorDevices().size() + dnl.getOfflineMotorizedDoors().size() + dnl.getOpenMotorizedDoors().size();
    }

    private int generateDoorCount(DoorsNLocksSubsystem dnl) {
        return dnl.getContactSensorDevices().size() + dnl.getOfflineContactSensors().size() + dnl.getOpenContactSensors().size();
    }

    private int generatePetCount(DoorsNLocksSubsystem dnl) {
        return dnl.getPetDoorDevices().size() + dnl.getOfflinePetDoors().size() + dnl.getAutoPetDoors().size() + dnl.getUnlockedPetDoors().size();
    }

    private String generateTextualSummary(DoorsNLocksSubsystem dnl) {
        if(dnl.getLockDevices().isEmpty()) {
            return "All doors closed.";
        }
        return "All doors locked and closed.";
    }

    private boolean shouldShowTextualSummary(DoorsNLocksSubsystem dnl) {
        return dnl.getUnlockedLocks().isEmpty() &&
               dnl.getOpenContactSensors().isEmpty() &&
               dnl.getOpenMotorizedDoors().isEmpty() &&
               dnl.getOfflineLocks().isEmpty() &&
               dnl.getOfflineContactSensors().isEmpty() &&
               dnl.getOfflineMotorizedDoors().isEmpty() &&
               dnl.getAutoPetDoors().isEmpty() &&
               dnl.getOfflinePetDoors().isEmpty() &&
               dnl.getUnlockedPetDoors().isEmpty();
    }
}
