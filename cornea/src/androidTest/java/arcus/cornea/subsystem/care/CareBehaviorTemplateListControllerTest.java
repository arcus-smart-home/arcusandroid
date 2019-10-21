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
package arcus.cornea.subsystem.care;

import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.subsystem.care.model.BehaviorTemplate;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.model.SubsystemModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.*;

public class CareBehaviorTemplateListControllerTest extends MockClientTestCase {
    CareBehaviorTemplateListController controller;
    SettableModelSource<SubsystemModel> source;
    CareBehaviorTemplateListController.Callback callback;

    @Captor ArgumentCaptor<List<BehaviorTemplate>> satisfiableArgCap;
    @Captor ArgumentCaptor<List<BehaviorTemplate>> nonSatisfiableArgCap;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        source = new SettableModelSource<>();
        controller = new CareBehaviorTemplateListController(source);
        callback = Mockito.mock(CareBehaviorTemplateListController.Callback.class);
        controller.setCallback(callback);

        expectRequestOfType(CareSubsystem.ListBehaviorsRequest.NAME)
              .andRespondFromPath("subsystems/care/listBehaviorsResponse.json");
        expectRequestOfType(CareSubsystem.ListBehaviorTemplatesRequest.NAME)
              .andRespondFromPath("subsystems/care/listBehaviorTemplatesResponse.json");
    }

    @Test public void showTemplates() throws Exception {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/care/subsystemAlarmReadyON.json"));
        controller.listBehaviorTemplates();

        Mockito.verify(callback, Mockito.times(1)).showTemplates(satisfiableArgCap.capture(), nonSatisfiableArgCap.capture());
        List<BehaviorTemplate> satisfiable = satisfiableArgCap.getValue();
        List<BehaviorTemplate> nonSatisfiable = nonSatisfiableArgCap.getValue();

        assertNotNull(satisfiable);
        assertNotNull(nonSatisfiable);

        assertFalse(satisfiable.isEmpty());
        assertTrue(nonSatisfiable.isEmpty());

        Mockito.verifyNoMoreInteractions(callback);
    }
}