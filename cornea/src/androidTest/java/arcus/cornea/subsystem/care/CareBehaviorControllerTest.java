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

import arcus.cornea.mock.MockClientTestCase;

public class CareBehaviorControllerTest extends MockClientTestCase {
//    CareBehaviorController controller;
//    SettableModelSource<SubsystemModel> source;
//
//    @Captor ArgumentCaptor<List<CareBehaviorTemplate>> behaviorTemplateArgumentCaptor;
//    @Captor ArgumentCaptor<Throwable> behaviorTemplateErrorCaptor;
//
//    @Before
//    public void setUp() {
//        initClient();
//        MockitoAnnotations.initMocks(this);
//
//        source = new SettableModelSource<>();
//        controller = new CareBehaviorController(source, client());
//    }
//
//    public void parseTemplates() throws Exception {
//        source.set((SubsystemModel) Fixtures.loadModel("subsystems/care/subsystemAlarmReadyON.json"));
//        CareBehaviorController.Callback callback = Mockito.mock(CareBehaviorController.Callback.class);
//        controller.setCallback(callback);
//
//        expectBehaviorTemplateListing();
//        controller.listBehaviorTemplates();
//
//        Mockito.verify(callback, Mockito.times(1)).showTemplates(behaviorTemplateArgumentCaptor.capture());
//        List<CareBehaviorTemplate> careBehaviorTemplates = behaviorTemplateArgumentCaptor.getValue();
//
//        assertNotNull(careBehaviorTemplates);
//        assertFalse(careBehaviorTemplates.isEmpty());
//        assertEquals(8, careBehaviorTemplates.size());
//
//        Mockito.verifyNoMoreInteractions(callback);
//    }
//
//    public void parseTemplatesForError() throws Exception {
//        source.set((SubsystemModel) Fixtures.loadModel("subsystems/care/subsystemAlarmReadyON.json"));
//        CareBehaviorController.Callback callback = Mockito.mock(CareBehaviorController.Callback.class);
//        controller.setCallback(callback);
//
//        expectBehaviorTemplateListingError();
//        controller.listBehaviorTemplates();
//
//        Mockito.verify(callback, Mockito.times(1)).onError(behaviorTemplateErrorCaptor.capture());
//        Throwable cause = behaviorTemplateErrorCaptor.getValue();
//        assertTrue(cause instanceof ErrorResponseException);
//
//        ErrorResponseException errorResponseException = (ErrorResponseException) cause;
//        assertEquals("behaviors.not.found", errorResponseException.getCode());
//        assertEquals("No available behaviors were able to be found", errorResponseException.getErrorMessage());
//
//        Mockito.verifyNoMoreInteractions(callback);
//    }
//
//    private void expectBehaviorTemplateListingError() throws Exception {
//        client()
//              .expectRequestOfType(CareSubsystem.ListBehaviorTemplatesRequest.NAME)
//              .andRespondWithError("behaviors.not.found", "No available behaviors were able to be found");
//    }
//
//    private void expectBehaviorTemplateListing() throws Exception {
//        client()
//              .expectRequestOfType(CareSubsystem.ListBehaviorTemplatesRequest.NAME)
//              .andRespondFromPath("subsystems/care/listBehaviorTemplatesResponse.json");
//    }
}