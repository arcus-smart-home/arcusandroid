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

import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesSummary;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.model.SubsystemModel;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class LightsNSwitchesDashCardControllerTest extends MockClientTestCase {
    LightsNSwitchesDashCardController controller;
    SettableModelSource<SubsystemModel> source;

    @Captor
    ArgumentCaptor<LightsNSwitchesSummary> summaryArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        source = new SettableModelSource<>();
        controller = new LightsNSwitchesDashCardController(source);
    }

    @Test
    public void loadDashboardCard() throws Exception {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/lightsnswitches/subsystem.json"));
        LightsNSwitchesDashCardController.Callback callback = Mockito.mock(LightsNSwitchesDashCardController.Callback.class);
        controller.setCallback(callback);

        Mockito.verify(callback, Mockito.times(1)).showSummary(summaryArgumentCaptor.capture());
        assertEquals(2, summaryArgumentCaptor.getValue().getLightsOn());
        assertEquals(2, summaryArgumentCaptor.getValue().getSwitchesOn());
        assertEquals(0, summaryArgumentCaptor.getValue().getDimmersOn());
    }

    @Test
    public void loadDashboardLearnMoreCard() throws Exception {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/lightsnswitches/subsystem_no_devs.json"));
        LightsNSwitchesDashCardController.Callback callback = Mockito.mock(LightsNSwitchesDashCardController.Callback.class);
        controller.setCallback(callback);

        Mockito.verify(callback, Mockito.times(0)).showSummary(Mockito.any(LightsNSwitchesSummary.class));
        Mockito.verify(callback, Mockito.times(1)).showLearnMore();
    }
}