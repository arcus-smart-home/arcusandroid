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
package arcus.cornea.subsystem.lightsnswitches;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesSummary;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.LightsNSwitchesSubsystem;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.Set;

public class LightsNSwitchesDashCardController extends BaseSubsystemController<LightsNSwitchesDashCardController.Callback> {
    private static final String LIGHT = "light";
    private static final String DIMMER = "dimmer";
    private static final String SWITCH = "switch";

    public interface Callback {
        /**
         * Invoked when the place has no applicable devices for this card.
         */
        void showLearnMore();

        /**
         * Invoked when the place has devices required for the subsystem, and are some are on, some are off.
         *
         * @param summary
         */
        void showSummary(LightsNSwitchesSummary summary);
    }

    private static final LightsNSwitchesDashCardController instance;

    private static final Set<String> UPDATE_ON = ImmutableSet.of(
          LightsNSwitchesSubsystem.ATTR_ONDEVICECOUNTS,
          LightsNSwitchesSubsystem.ATTR_SWITCHDEVICES
    );

    static {
        instance = new LightsNSwitchesDashCardController(
              SubsystemController.instance().getSubsystemModel(LightsNSwitchesSubsystem.NAMESPACE)
        );
        instance.init();
    }

    public static LightsNSwitchesDashCardController instance() {
        return instance;
    }

    LightsNSwitchesDashCardController(ModelSource<SubsystemModel> source) {
        super(source);
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

        LightsNSwitchesSubsystem subsystem = (LightsNSwitchesSubsystem) getModel();
        if (subsystem.getSwitchDevices() == null || subsystem.getSwitchDevices().size() == 0) {
            callback.showLearnMore();
        }
        else {
            Integer lightsOn = subsystem.getOnDeviceCounts().get(LIGHT);
            Integer dimmerOn = subsystem.getOnDeviceCounts().get(DIMMER);
            Integer switchOn = subsystem.getOnDeviceCounts().get(SWITCH);

            LightsNSwitchesSummary summary = new LightsNSwitchesSummary();
            summary.setLightsOn(lightsOn == null ? 0 : lightsOn);
            summary.setDimmersOn(dimmerOn == null ? 0 : dimmerOn);
            summary.setSwitchesOn(switchOn == null ? 0 : switchOn);

            callback.showSummary(summary);
        }
    }
}
