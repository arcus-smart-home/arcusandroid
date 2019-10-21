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
package arcus.cornea.account;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import com.iris.client.EmptyEvent;
import com.iris.client.ErrorEvent;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.PersonModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;

import static com.iris.client.capability.Person.AcceptPolicyRequest.*;

public class TermsAndConditionsControllerTest extends MockClientTestCase {
    TermsAndConditionsController conditionsController;
    @Mock TermsAndConditionsController.Callback callback;

    @Before public void setup() {
        MockitoAnnotations.initMocks(this);
        conditionsController = Mockito.spy(new TermsAndConditionsController((PersonModel) Fixtures.loadModel("people/personOne.json")));
    }

    @Test public void testSetCallback() throws Exception {
        ListenerRegistration registration = conditionsController.setCallback(callback);
        assertNotNull(registration);
        assertTrue(registration.isRegistered());
    }

    @Test public void testAcceptTermsAndPrivacySuccess() throws Exception {
        Mockito.doAnswer(new PoliciesNeededAnswer()).when(conditionsController).policiesNeeded();
        expectRequestOfType(NAME)
              .andRespondWithMessage(EmptyEvent.NAME);

        conditionsController.setCallback(callback);
        conditionsController.acceptTermsAndConditions();

        Mockito.verify(callback, Mockito.never()).onFailure(Matchers.any(Exception.class));
        Mockito.verify(callback, Mockito.timeout(2_000).times(1)).onSuccess();
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testAcceptTermsSuccess() throws Exception {
        Mockito.doAnswer(new TermsNeededAnswer()).when(conditionsController).policiesNeeded();
        expectRequest(
              "SERV:person:69852cea-2dd7-40d4-851d-026df9db4fdb", NAME,
              ImmutableMap.<String, Object>of(ATTR_TYPE, TYPE_TERMS)
        ).andRespondWithMessage(EmptyEvent.NAME);

        conditionsController.setCallback(callback);
        conditionsController.acceptTermsAndConditions();

        Mockito.verify(callback, Mockito.never()).onFailure(Matchers.any(Exception.class));
        Mockito.verify(callback, Mockito.timeout(2_000).times(1)).onSuccess();
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testAcceptPrivacySuccess() throws Exception {
        Mockito.doAnswer(new PrivacyNeededAnswer()).when(conditionsController).policiesNeeded();
        expectRequest(
              "SERV:person:69852cea-2dd7-40d4-851d-026df9db4fdb", NAME,
              ImmutableMap.<String, Object>of(ATTR_TYPE, TYPE_PRIVACY)
        ).andRespondWithMessage(EmptyEvent.NAME);

        conditionsController.setCallback(callback);
        conditionsController.acceptTermsAndConditions();

        Mockito.verify(callback, Mockito.never()).onFailure(Matchers.any(Exception.class));
        Mockito.verify(callback, Mockito.timeout(2_000).times(1)).onSuccess();
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testAcceptTermsAndConditionsFailure() throws Exception {
        Mockito.doAnswer(new PoliciesNeededAnswer()).when(conditionsController).policiesNeeded();
        expectRequestOfType(NAME)
              .andRespondWithError(new ErrorEvent("Failed."));

        conditionsController.setCallback(callback);
        conditionsController.acceptTermsAndConditions();

        Mockito.verify(callback, Mockito.never()).onSuccess();
        Mockito.verify(callback, Mockito.times(1)).onFailure(Matchers.any(Exception.class));
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testGetCallback() throws Exception {
        TermsAndConditionsController.Callback noOpCallback = conditionsController.getNoOpCallback();
        TermsAndConditionsController.Callback currentCallback = conditionsController.getCallback();
        assertNotNull(currentCallback);
        assertEquals(noOpCallback, currentCallback);

        conditionsController.setCallback(callback);

        currentCallback = conditionsController.getCallback();
        assertNotEquals(noOpCallback, currentCallback);
    }

    class PoliciesNeededAnswer implements Answer<Integer> {
        @Override public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
            return TermsAndConditionsController.PRIVACY | TermsAndConditionsController.TERMS;
        }
    }

    class PrivacyNeededAnswer implements Answer<Integer> {
        @Override public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
            return TermsAndConditionsController.PRIVACY;
        }
    }

    class TermsNeededAnswer implements Answer<Integer> {
        @Override public Integer answer(InvocationOnMock invocationOnMock) throws Throwable {
            return TermsAndConditionsController.TERMS;
        }
    }
}