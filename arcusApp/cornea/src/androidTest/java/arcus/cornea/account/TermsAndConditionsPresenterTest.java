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

import arcus.cornea.mock.MockClientTestCase;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class TermsAndConditionsPresenterTest extends MockClientTestCase {
    @Mock TermsAndConditionsContract.View viewMock;
    @Spy TermsAndConditionsController termsController;
    TermsAndConditionsPresenter presenter;

    @Before public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter = new TermsAndConditionsPresenter(viewMock, termsController);
    }

    @Test public void testClearReferences() throws Exception {
        assertFalse(presenter.referencesCleared());

        presenter.clearReferences();

        assertTrue(presenter.referencesCleared());
    }

    @Test public void testReCheckIfNeededReturnsFalse() throws Exception {
        Mockito.when(termsController.stillRequiresUpdate()).thenAnswer(new NoTermsNeededAnswer());

        presenter.recheckNeedToAccept();
        Mockito.verify(viewMock, Mockito.times(1)).onAcceptSuccess();
        Mockito.verifyNoMoreInteractions(viewMock);
    }

    @Test public void testReCheckIfNeededReturnsTrue() throws Exception {
        Mockito.when(termsController.stillRequiresUpdate()).thenAnswer(new TermsNeededAnswer());

        presenter.recheckNeedToAccept();
        Mockito.verify(viewMock, Mockito.times(1)).acceptRequired();
        Mockito.verifyNoMoreInteractions(viewMock);
    }

    @Test public void testTermsAcceptSuccess() throws Exception {
        Mockito.doAnswer(new AcceptTermsSuccessAnswer()).when(termsController).acceptTermsAndConditions();

        presenter.acceptTermsAndConditions();
        Mockito.verify(viewMock, Mockito.times(1)).onAcceptSuccess();
        Mockito.verifyNoMoreInteractions(viewMock);
    }

    @Test public void testTermsAcceptFailure() throws Exception {
        Mockito.doAnswer(new AcceptTermsFailureAnswer()).when(termsController).acceptTermsAndConditions();

        presenter.acceptTermsAndConditions();
        Mockito.verify(viewMock, Mockito.times(1)).onError(Matchers.any(Exception.class));
        Mockito.verifyNoMoreInteractions(viewMock);
    }

    class AcceptTermsSuccessAnswer implements Answer<Void> {
        @Override public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            presenter.onSuccess();
            return null;
        }
    }

    class AcceptTermsFailureAnswer implements Answer<Void> {
        @Override public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            presenter.onFailure(new RuntimeException("Failed."));
            return null;
        }
    }

    class NoTermsNeededAnswer implements Answer<Boolean> {
        @Override public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
            return false;
        }
    }

    class TermsNeededAnswer implements Answer<Boolean> {
        @Override public Boolean answer(InvocationOnMock invocationOnMock) throws Throwable {
            return true;
        }
    }
}